package net.lordofthecraft.arche.save;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.interfaces.IConsumer;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.archerows.ArchePreparedStatementRow;
import net.lordofthecraft.arche.save.archerows.ArcheRow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Queue;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

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

            process:
            while (!queue.isEmpty() && (System.currentTimeMillis() - starttime < timePerRun || (count < forceToProcess && !bypassForce))) {
                ArcheRow row = queue.poll();
                if (row == null) {
                    continue;
                }
                long taskstart = System.currentTimeMillis();
                if (debugConsumer) {
                    pl.getLogger().info("[Consumer] Beginning process for " + row.toString());
                }
                if (row instanceof ArchePreparedStatementRow) {
                    ArchePreparedStatementRow apsr = (ArchePreparedStatementRow) row;
                    if (row instanceof ArcheMergeableRow) {
                        int batchCount = count;
                        if (count > forceToProcess) {
                            batchCount = forceToProcess / 2;
                        }
                        while (!queue.isEmpty()) {
                            ArcheMergeableRow amRow = (ArcheMergeableRow) row;
                            ArcheRow peeked = queue.peek();
                            if (peeked == null) {
                                break;
                            }
                            if (!(peeked instanceof ArcheMergeableRow)) {
                                break;
                            }
                            ArcheMergeableRow mergeRow = (ArcheMergeableRow) peeked;
                            if (amRow.canMerge(mergeRow)) {
                                apsr = amRow.merge((ArcheMergeableRow) queue.poll());
                                count++;
                                batchCount++;
                                if (batchCount > forceToProcess) {
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                    }
                    if (apsr != null) {
                        apsr.setConnection(conn);
                        try {
                            apsr.executeStatements();
                        } catch (final SQLException ex) {
                            pl.getLogger().log(Level.SEVERE, "[Consumer] SQL Exception in Consumer: ", ex);
                            break;
                        }
                    } else {
                        pl.getLogger().warning("Error! Looks like some rows failed to merge, our row was null! This usually happens if ArcheMergeableRow#merge returns a null!");
                    }
                    //If you are reading through this code to see how this works
                    //Understand that if you close or edit a connection in your row it will explode everything violently.
                    //If this happens, it wont be funny
                    //And you'll be the one getting fucking stabbed.
                    //Thanks.
                    if (conn.isClosed()) {
                        //I hate you.
                        pl.getLogger().severe("[Consumer] So we have an acid-dunk worthy individual who closed the connection we gave them in their ArcheRow, despite specific documentation telling them not to. Offender: " + apsr.getClass().getSimpleName());

                        pl.getLogger().warning("[Consumer] So we're going to terminate this consumer run. This data is visiting ol' yeller but cant really do anything about it");
                        pl.getLogger().info("[Consumer] (Whoever did this needs to go find a bridge)");
                        break;
                    } else if (conn.isReadOnly()) {
                        pl.getLogger().severe("[Consumer] So some not max level retard but definitely over 9000 level retard set the connection to read only, either maliciously or as a meme. Either way, rapid termination of life should occur. Offender: " + apsr.getClass().getSimpleName());
                        conn.setReadOnly(false);
                    } else if (conn.getAutoCommit()) {
                        pl.getLogger().severe("[Consumer] This might be a genuine mistake but probably not, some idiot set the connection for Consumer to auto commit which will nuke performance into kingdom come. We're fixing this but the idiotic code is here: " + apsr.getClass().getSimpleName());
                        conn.setAutoCommit(false);
                    }
                } else {
                    for (final String toinsert : row.getInserts()) {
                        try {
                            state.execute(toinsert);
                        } catch (final SQLException ex) {
                            pl.getLogger().log(Level.SEVERE, "[Consumer] SQL exception on " + toinsert + ": ", ex);
                            break process;
                        }
                    }
                }
                if (debugConsumer) {
                    pl.getLogger().info("[Consumer] Process took " + (System.currentTimeMillis() - taskstart) + "ms for " + row.toString());
                }
                count++;
            }
            conn.commit();
        } catch (final SQLException ex) {
            pl.getLogger().log(Level.SEVERE, "[Consumer] We failed to complete Consumer SQL Processes.", ex);
        } finally {
            try {
                if (state != null) {
                    state.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                pl.getLogger().log(Level.SEVERE, "[Consumer] Failed to finish our Consumer out, either statement or connection failed to close.", e);
            }
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
        new File("plugins/ArcheCore/import/").mkdirs();
        PrintWriter writer = new PrintWriter(new File("plugins/ArcheCore/import/queue-" + time + "-0.sql"));
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
                writer = new PrintWriter(new File("plugins/ArcheCore/import/queue-" + time + "-" + counter / 1000 + ".sql"));
            }
        }
        writer.close();
    }
}
