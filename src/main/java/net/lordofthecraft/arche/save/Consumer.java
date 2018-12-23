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
import java.util.logging.Level;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.interfaces.IConsumer;
import net.lordofthecraft.arche.save.rows.ArcheRow;
import net.lordofthecraft.arche.save.rows.FlexibleDeleteRow;
import net.lordofthecraft.arche.save.rows.FlexibleInsertRow;
import net.lordofthecraft.arche.save.rows.FlexibleUpdateRow;
import net.lordofthecraft.arche.save.rows.RunnerRow;
import net.lordofthecraft.arche.save.rows.StatementRow;
import net.lordofthecraft.arche.util.SQLUtil;

public final class Consumer extends TimerTask implements IConsumer {
	private final Queue<ArcheRow> queue = new LinkedBlockingQueue<>();
	private final SQLHandler handler;
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

	@Override
	public FlexibleInsertRow insert(String table) {
		return new FlexibleInsertRow(table, FlexibleInsertRow.Mode.INSERT);
	}

	@Override
	public FlexibleInsertRow insertIgnore(String table) {
		return new FlexibleInsertRow(table, FlexibleInsertRow.Mode.IGNORE);
	}

	@Override
	public FlexibleInsertRow replace(String table) {
		return new FlexibleInsertRow(table, FlexibleInsertRow.Mode.REPLACE);
	}

	@Override
	public FlexibleDeleteRow delete(String table) {
		return new FlexibleDeleteRow(table);
	}

	@Override
	public FlexibleUpdateRow update(String table) {
		return new FlexibleUpdateRow(table);
	}

	public synchronized void bypassForce() {
		bypassForce = true;
	}

	@Override
	public boolean isDebugging() {
		return debugConsumer;
	}

	@Override
	public void queueRow(ArcheRow row) {
		queue.add(row);
	}

	@Override
	public int getQueueSize() {
		return queue.size();
	}

	@Override
	public synchronized void runForced() {
		this.bypassForce = true;
		run();
		this.bypassForce = false;
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

		final long starttime = System.currentTimeMillis();
		if (queue.size() >= warningSize) {
			pl.getLogger().warning("[Consumer] Warning! The Consumer Queue is overloaded! The size of the queue is " + queue.size() + " which is " + (queue.size() - warningSize) + " over our set threshold of " + warningSize + "! We're still running, but this should be looked into!");
		}

		int count = 0;
		try(Connection conn = handler.getConnection(); Statement state = conn.createStatement()) {
			conn.setAutoCommit(false);

			PreparedStatement[] pending = null;

			while (bypassForce || System.currentTimeMillis() - starttime < timePerRun|| count < forceToProcess*(pending == null? 1:1.5) ) {
				ArcheRow row = queue.peek();
				if (row == null) break;

				if (debugConsumer) try {
					pl.getLogger().info("[Consumer] Beginning process for " + row.toString());
				} catch(RuntimeException e) {
					pl.getLogger().info("[Consumer] Beginning process for FAULTY " + row.getClass().getSimpleName());
				}

				long taskstart = System.currentTimeMillis();

				try {
					if (row instanceof RunnerRow) {
						((RunnerRow) row).run(conn);
					} else if (row instanceof StatementRow) {
						StatementRow sRow = (StatementRow) row;
						if(debugConsumer) pl.getLogger().info("StatementRow Origin Trace:\n" + sRow.getOriginStackTrace());
						boolean inBatch = pending != null;

						if (!inBatch) pending = sRow.prepare(conn);
						sRow.setValues(pending);

						ArcheRow other;
						if (!sRow.isUnique() && (other = queue.peek()) != null && other.getClass() == sRow.getClass()
								&& !((StatementRow) other).isUnique()) {
							//At least one more of this Row type is behind in queue
							for (PreparedStatement s : pending) s.addBatch();
						} else { //None of this Row behind in queue
							for (PreparedStatement s : pending) {
								if (inBatch) {
									s.addBatch();
									s.executeBatch();
								} else {
									s.execute();
								}
							}
							pending = null;
						}
					} else {
						for (final String toinsert : row.getInserts()) {
							state.execute(toinsert);
						}
					}
				} catch (SQLException e) {
					if (e.getErrorCode() == 5 || e.getErrorCode() == 6) { // SQL_BUSY, SQL_LOCKED
						pl.getLogger().log(Level.WARNING, "[Consumer] SQL database is locked, retrying...");
					} else {
						pl.getLogger().log(Level.SEVERE, "[Consumer] SQL exception on " + row.getClass().getSimpleName() + ": ", e);
						pl.getLogger().log(Level.SEVERE, "[Consumer] Statement body: " + row.toString());
						if(pending != null) {
							Arrays.stream(pending).forEach(ps -> {
								pl.getLogger().severe("Lost Statement: " + String.valueOf(ps));
								SQLUtil.close(ps);
							});
							pending = null;
						}
						queue.poll(); // bad statement, remove from queue
					}

					continue;
				} finally {
					if (conn.isClosed()) {
						pl.getLogger().severe("[Consumer] Connection found to be closed after handling a " + row.getClass().getSimpleName());
						pl.getLogger().severe("[Consumer] Cannot recover. Will abort the saving process.");
						break;
					} else if (conn.isReadOnly()) {
						pl.getLogger().warning("[Consumer] Connection found to be readOnly after handling a " + row.getClass().getSimpleName());
						conn.setReadOnly(false);
					} else if (conn.getAutoCommit()) {
						pl.getLogger().warning("[Consumer] Connection auto commit: " + row.getClass().getSimpleName());
						conn.setAutoCommit(false);
					}
					queue.poll();
				}

				count++;
				if (debugConsumer) pl.getLogger().info("[Consumer] took " + (System.currentTimeMillis() - taskstart) + "ms for " + row.getClass().getSimpleName());
			}
			conn.commit();
		} catch (final SQLException ex) {
			pl.getLogger().log(Level.SEVERE, "[Consumer] We failed to complete Consumer SQL Processes.", ex);
		} finally {
			StatementRow.close();

			long time = System.currentTimeMillis() - starttime;
			pl.getLogger().info("[Consumer] Finished handling " + count + " rows in " + time + "ms.");
			if (debugConsumer && count > 0) {
				pl.getLogger().log(Level.INFO, "[Consumer] Total rows processed: " + count + ". " + queue.size() + " rows left in queue");
			} else if (count == 0) {
				pl.getLogger().warning("[Consumer] Ran with 0 tasks, this shouldn't happen?");
			}
		}

	}

	public void writeToFile() throws FileNotFoundException {
		final long time = System.currentTimeMillis();

		int counter = 0;
		new File(String.format("plugins%cArcheCore%<cimport%<c", File.separatorChar)).mkdirs();
		PrintWriter writer = new PrintWriter(new File(String.format("plugins%cArcheCore%<cimport%<cqueue-%d-0.sql", File.separatorChar, time)));
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
				writer = new PrintWriter(new File(String.format("plugins%cArcheCore%<cimport%<cqueue-%d-%d.sql", File.separatorChar, time, counter / 1000)));
			}
		}
		writer.close();
	}
}
