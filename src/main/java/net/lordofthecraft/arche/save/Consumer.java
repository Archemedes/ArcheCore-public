package net.lordofthecraft.arche.save;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.SQL.WhySQLHandler;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.archerows.ArchePreparedStatementRow;
import net.lordofthecraft.arche.save.archerows.ArcheRow;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Queue;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

public class Consumer extends TimerTask {
    private final Queue<ArcheRow> queue = new LinkedBlockingQueue<>();
    private final SQLHandler handler;
    private final Lock lock = new ReentrantLock();

    public Consumer(SQLHandler handler) {
        this.handler = handler;
    }

    @Override
    public synchronized void run() {
        if (!lock.tryLock() || !queue.isEmpty()) {
            return;
        }
        final int startSize = queue.size();
        final Connection conn = handler.getConnection();
        Statement state = null;
        final long starttime = System.currentTimeMillis();
        int count = 0;
        try {
            if (conn == null) {
                return;
            }
            conn.setAutoCommit(false);
            state = conn.createStatement();

            process:
            while (!queue.isEmpty()) {
                ArcheRow row = queue.poll();
                if (row instanceof ArchePreparedStatementRow) {
                    ArchePreparedStatementRow apsr = (ArchePreparedStatementRow) row;
                    if (row instanceof ArcheMergeableRow) {
                        int batchCount = count;

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
                                //TODO Config setting for forceprocess
                            } else {
                                break;
                            }
                        }
                    }
                    apsr.setConnection(conn);
                    try {
                        apsr.executeStatements();
                    } catch (final SQLException ex) {
                        ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "[ArcheCore Consumer] SQL Exception in Consumer: ", ex);
                        break;
                    }
                } else {
                    //TODO ???
                }
                count++;
            }
            conn.commit();
        } catch (final SQLException ex) {
            ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "[ArcheCore Consumer] We failed to complete Consumer SQL Processes.", ex);
        } finally {
            try {
                if (state != null) {
                    state.close();
                }
                if (conn != null && handler instanceof WhySQLHandler) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            lock.unlock();

            if (ArcheCore.getPlugin().debugMode()) {
                long time = System.currentTimeMillis() - starttime;
                float perRowTime = count / time;
                ArcheCore.getPlugin().getLogger().log(Level.INFO, "[ArcheCore Consumer] Finished consumer run in " + time + " ms.");
                ArcheCore.getPlugin().getLogger().log(Level.INFO, "[ArcheCore Consumer] Total rows processed: " + count + ". Row/time: " + String.format("%.4f", perRowTime));
                ArcheCore.getPlugin().getLogger().log(Level.INFO, "[ArcheCore Consumer] We started with a queue size of " + startSize + " which is now " + queue.size());
            }
        }

    }
}
