package net.lordofthecraft.arche.save;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Queue;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.SQL.SQLUtils;
import net.lordofthecraft.arche.interfaces.IConsumer;
import net.lordofthecraft.arche.save.rows.ArcheRow;
import net.lordofthecraft.arche.save.rows.MultiStatementRow;
import net.lordofthecraft.arche.save.rows.StatementRow;

public class Consumer extends TimerTask implements IConsumer {
    private final Queue<ArcheRow> queue = new LinkedBlockingQueue<>();
    private final SQLHandler handler;
    private final Lock lock = new ReentrantLock();
    private final ArcheCore pl;
    private final int timePerRun;
    private final int forceToProcess;
    private final int warningSize;
    private final boolean debugConsumer;
    private boolean bypassForce = false;

    public Consumer(SQLHandler handler, ArcheCore pl, int timePerRun, int forceToProcess, int warningSize, boolean debugConsumer) {
        this.handler = handler;
        this.pl = pl;
        this.timePerRun = timePerRun;
        this.forceToProcess = forceToProcess;
        this.warningSize = warningSize;
        this.debugConsumer = debugConsumer;
    }

    public void bypassForce() {
        bypassForce = true;
    }

    @Override
    public void queueRow(ArcheRow row) {
        if (!queue.contains(row)) {
            queue.add(row);
        }
    }

    @Override
    public int getQueueSize() {
        return queue.size();
    }

    @Override
    public synchronized void run() {
        if (debugConsumer) {
            pl.getLogger().info("[Consumer] Beginning save process...");
        }
        if (queue.isEmpty()) {
            if (debugConsumer) pl.getLogger().info("[Consumer] The consumer has no queue, not running.");
            return;
        }
        if (!lock.tryLock()) {
            if (debugConsumer)
                pl.getLogger().info("[Consumer] The consumer is still locked and we attempted to run, we are not running.");
            return;
        } else {
            if (debugConsumer) {
                pl.getLogger().info("[Consumer] We have locked the consumer and are beginning now.");
            }
        }
        final Connection conn = handler.getConnection();
        Statement state = null;
        final long starttime = System.currentTimeMillis();
        if (queue.size() >= warningSize) {
            pl.getLogger().warning("[Consumer] Warning! The Consumer Queue is overloaded! The size of the queue is " + queue.size() + " which is " + (queue.size() - warningSize) + " over our set threshold of " + warningSize + "! We're still running, but this should be looked into!");
        }
        int count = 0;
        try {
            if (conn == null) {
                pl.getLogger().severe("[Consumer] Consumer failed to start, we could not Connect to the Database.");
                return;
            }
            conn.setAutoCommit(false);
            state = conn.createStatement();        
            PreparedStatement[] pending = null;
            
            while (!queue.isEmpty() && (System.currentTimeMillis() - starttime < timePerRun || (count < (pending == null? forceToProcess : forceToProcess*1.5) && !bypassForce))) {
                ArcheRow row = queue.poll();
                if (row == null) continue;
                if (debugConsumer) pl.getLogger().info("[Consumer] Beginning process for " + row.toString());
                long taskstart = System.currentTimeMillis();         
          
                try {
                	if(row instanceof StatementRow) {
                		StatementRow sRow = (StatementRow) row;
                		boolean inBatch = pending != null;

                		if(!inBatch) pending = sRow.prepare(conn);
                		sRow.setValues(pending);

                		ArcheRow other;
                		if(!sRow.isUnique() && (other = queue.peek()) != null && other.getClass() == sRow.getClass() 
                				&& !((StatementRow) other).isUnique()) { 
                			//At least one more of this Row type is behind in queue
                			for(PreparedStatement s : pending) s.addBatch();
                		} else { //None of this Row behind in queue
                			for(PreparedStatement s : pending) {
                				if(inBatch) {s.addBatch(); s.executeBatch();} 
                				else {s.execute();}
                			}
                			pending = null;
                		}
                	} else {
                		for (final String toinsert : row.getInserts()) {
                				state.execute(toinsert);
                		}
                	}
                } catch(SQLException e) {
    				pl.getLogger().log(Level.SEVERE, "[Consumer] SQL exception on "+row.getClass().getSimpleName()+": ", e);
    				Arrays.stream(pending).forEach(ps-> {pl.getLogger().severe("Lost Statement: "+ps.toString()); SQLUtils.close(ps);});
    				pending = null;
    				continue;
                } finally {
                	if (conn.isClosed()) {
                        pl.getLogger().severe("[Consumer] Connection found to be closed after handling a " + row.getClass().getSimpleName());
                        pl.getLogger().severe("[Consumer] Cannot recover. Will abort the saving process.");
                        break;
                    } else if (conn.isReadOnly()) {
                        pl.getLogger().warning("[Consumer] Connection found to be readOnly after handling a " + row.getClass().getSimpleName() );
                        conn.setReadOnly(false);
                    } else if (conn.getAutoCommit()) {
                        pl.getLogger().warning("[Consumer] Connection auto commit: " + row.getClass().getSimpleName());
                        conn.setAutoCommit(false);
                    }
                }
                
                count++;
                if (debugConsumer) pl.getLogger().info("[Consumer] Process took " + (System.currentTimeMillis() - taskstart) + "ms for " + row.toString());
            }
            conn.commit();
        } catch (final SQLException ex) {
            pl.getLogger().log(Level.SEVERE, "[Consumer] We failed to complete Consumer SQL Processes.", ex);
        } finally {
        	StatementRow.close();
        	SQLUtils.close(state);
        	SQLUtils.close(conn);
        	
            lock.unlock();

            long time = System.currentTimeMillis() - starttime;
            pl.getLogger().info("[Consumer] Finished saving in " + time + "ms.");
            if (debugConsumer && count > 0) {
                float perRowTime = count / time;
                pl.getLogger().log(Level.INFO, "[Consumer] Total rows processed: " + count + ". Row/time: " + String.format("%.4f", perRowTime));
                pl.getLogger().log(Level.INFO, "[Consumer] There are " + queue.size() + " rows left in queue");
            } else if (count == 0) {
                pl.getLogger().warning("[Consumer] Ran with 0 tasks, this shouldn't happen?");
            }
        }

    }

    public void writeToFile() throws FileNotFoundException {
        final long time = System.currentTimeMillis();

        int counter = 0;
        new File( String.format("plugins%cArcheCore%<cimport%<c", File.separatorChar) ).mkdirs();
        PrintWriter writer = new PrintWriter(new File( String.format("plugins%cArcheCore%<cimport%<cqueue-%d-0.sql",File.separatorChar,time)) );
        while (!queue.isEmpty()) {
            final ArcheRow r = queue.poll();
            if (r == null) {
                continue;
            }
            for (final String insert : r.getInserts()) {
                writer.println(insert);
            }
            counter++;
            if (counter % 1000 == 0) {
                writer.close();
                writer = new PrintWriter(new File(String.format("plugins%cArcheCore%<cimport%<cqueue-%d-%d.sql",File.separatorChar,time,counter/1000)));
            }
        }
        writer.close();
    }
}
