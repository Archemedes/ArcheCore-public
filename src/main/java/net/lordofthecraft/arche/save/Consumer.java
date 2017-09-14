package net.lordofthecraft.arche.save;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.SQL.WhySQLHandler;
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

    public Consumer(SQLHandler handler, ArcheCore pl, int timePerRun, int forceToProcess, int warningSize) {
        this.handler = handler;
        this.pl = pl;
        this.timePerRun = timePerRun;
        this.forceToProcess = forceToProcess;
        this.warningSize = warningSize;
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
        pl.getLogger().info("[ArcheCore Consumer] Beginning save process...");
        if (!lock.tryLock()) {
            if (pl.debugMode())
                pl.getLogger().warning("[ArcheCore Consumer] The consumer is still locked and we attempted to run, we are not running.");
            return;
        }
        if (!queue.isEmpty()) {
            if (pl.debugMode()) pl.getLogger().info("[ArcheCore Consumer] The consumer has no queue, not running.");
            return;
        }
        final int startSize = queue.size();
        final Connection conn = handler.getConnection();
        Statement state = null;
        final long starttime = System.currentTimeMillis();
        int count = 0;
        try {
            if (conn == null) {
                pl.getLogger().severe("[ArcheCore Consumer] ArcheCore Consumer failed to start, we could not Connect to the Database.");
                return;
            }
            conn.setAutoCommit(false);
            state = conn.createStatement();

            process:
            while (!queue.isEmpty() && (System.currentTimeMillis() - starttime < timePerRun || count < forceToProcess)) {
                ArcheRow row = queue.poll();
                if (row == null) {
                    continue;
                }
                long taskstart = System.currentTimeMillis();
                if (pl.debugMode()) {
                    pl.getLogger().info("[ArcheCore Consumer] Beginning process for " + row.toString());
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
                    apsr.setConnection(conn);
                    try {
                        apsr.executeStatements();
                    } catch (final SQLException ex) {
                        pl.getLogger().log(Level.SEVERE, "[ArcheCore Consumer] SQL Exception in Consumer: ", ex);
                        break;
                    }
                } else {
                    for (final String toinsert : row.getInserts()) {
                        try {
                            state.execute(toinsert);
                        } catch (final SQLException ex) {
                            pl.getLogger().log(Level.SEVERE, "[ArcheCore Consumer] SQL exception on " + toinsert + ": ", ex);
                            break process;
                        }
                    }
                }
                if (pl.debugMode()) {
                    pl.getLogger().info("[ArcheCore Consumer] Process took " + (System.currentTimeMillis() - taskstart) + "ms for " + row.toString());
                }
                count++;
            }
            conn.commit();
        } catch (final SQLException ex) {
            pl.getLogger().log(Level.SEVERE, "[ArcheCore Consumer] We failed to complete Consumer SQL Processes.", ex);
        } finally {
            try {
                if (state != null) {
                    state.close();
                }
                if (conn != null && handler instanceof WhySQLHandler) {
                    conn.close();
                }
            } catch (SQLException e) {
                pl.getLogger().log(Level.SEVERE, "[ArcheCore Consumer] Failed to finish our Consumer out, either statement or connection failed to close.", e);
            }
            lock.unlock();

            pl.getLogger().info("[ArcheCore Consumer] Finished saving!");
            if (pl.debugMode()) {
                long time = System.currentTimeMillis() - starttime;
                float perRowTime = count / time;
                pl.getLogger().log(Level.INFO, "[ArcheCore Consumer] Finished consumer run in " + time + " ms.");
                pl.getLogger().log(Level.INFO, "[ArcheCore Consumer] Total rows processed: " + count + ". Row/time: " + String.format("%.4f", perRowTime));
                pl.getLogger().log(Level.INFO, "[ArcheCore Consumer] We started with a queue size of " + startSize + " which is now " + queue.size());
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
