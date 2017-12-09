package net.lordofthecraft.arche.util;

import java.sql.Connection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.save.rows.RunnerRow;

public abstract class AsyncRunner {
	private final Plugin plugin;
	private Connection connection = null;
	
	public AsyncRunner(Plugin plugin) {
		this.plugin = plugin;
	}
	
	public void go() {
		new BukkitRunnable() {

			@Override
			public void run() {
				doAsync();
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->andThen());
			}
			
		}.runTaskAsynchronously(plugin);
	}
	
	public void goConsumer() {
		ArcheCore.getConsumerControls().queueRow(new AsyncRunnerRow(this));
	}
	
	protected Connection getConnection() {
		if(connection != null) return connection;
		else return ArcheCore.getSQLControls().getConnection();
	}
	
	protected <T> T fetchFromMain(Callable<T> task) {
		try {
			return callSyncMethod(task).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException("Error while trying to fetch from main inside ArcheCore AsyncRunner", e);
		}
	}
	
	protected <T> Future<T> callSyncMethod(Callable<T> task) {
		return Bukkit.getScheduler().callSyncMethod(plugin, task);
	}
	
	protected abstract void doAsync();
	
	
	protected abstract void andThen();
	
	private static class AsyncRunnerRow implements RunnerRow{
		private final AsyncRunner runner;
		
		private AsyncRunnerRow(AsyncRunner runner) {
			this.runner = runner;
		}
		
		@Override
		public void run(Connection connection) {
			runner.connection = connection;
			runner.doAsync();
			Bukkit.getScheduler().scheduleSyncDelayedTask(runner.plugin, ()->runner.andThen());
		}
	}
	
	
}
