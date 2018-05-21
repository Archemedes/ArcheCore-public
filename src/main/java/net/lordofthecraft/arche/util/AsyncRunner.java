package net.lordofthecraft.arche.util;

import java.sql.Connection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import lombok.RequiredArgsConstructor;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.save.rows.RunnerRow;

@RequiredArgsConstructor
public abstract class AsyncRunner {
	private final Plugin plugin;
	protected Connection connection = null;
	
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
	
	public static <T> PartialRunner<T> doAsync(Plugin plugin, Function<Connection, T> funct){
		return new PartialFunctionRunner<>(plugin, funct);
	}
	
	public static <T> PartialRunner<T> doAsync(Plugin plugin, Supplier<T> supp){
		return new PartialSupplierRunner<>(plugin, supp);
	}
	
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
	
	
	private static abstract class ConsumerRunner<T> extends AsyncRunner{
		private ConsumerRunner(Plugin plugin, Consumer<T> sync) {
			super(plugin);
			this.sync = sync;
		}
		protected T fetched;
 		private final Consumer<T> sync;
		
 		@Override
 		protected void andThen() {
 			sync.accept(fetched);
 		}
	}

	
	
	private static class SupplierRunner<T> extends ConsumerRunner<T>{
		private SupplierRunner(Plugin plugin, Consumer<T> sync, Supplier<T> async) {
			super(plugin, sync);
			this.async = async;
		}
		
		private final Supplier<T> async;
		
		@Override
		public void doAsync() {
			fetched = async.get();
		}
	}
	
	private static class ConnectionRunner<T> extends ConsumerRunner<T>{
		private ConnectionRunner(Plugin plugin, Consumer<T> sync, Function<Connection,T> async) {
			super(plugin, sync);
			this.async = async;
		}
		
		private final Function<Connection, T> async;
		
		@Override
		public void doAsync() {
			Connection c = this.connection;
			boolean supplied = c != null;
			try{
				if(!supplied) c = ArcheCore.getSQLControls().getConnection();
				fetched = async.apply(c);
			}catch(Exception e) { e.printStackTrace(); }
			finally { if(!supplied) SQLUtil.close(c);}
		}
	}
	
	@RequiredArgsConstructor
	public static abstract class PartialRunner<T> {
		protected final Plugin plugin;
		public abstract AsyncRunner andThen(Consumer<T> c);
	}
	
	private static class PartialSupplierRunner<T> extends PartialRunner<T>{
		private final Supplier<T> s;
		private PartialSupplierRunner(Plugin plugin, Supplier<T> s) {
			super(plugin);
			this.s = s;
		}
		
		@Override
		public AsyncRunner andThen(Consumer<T> c) {
			return new SupplierRunner<T>(plugin, c, s);
		}
	};
	
	private static class PartialFunctionRunner<T> extends PartialRunner<T>{
		private final Function<Connection, T> s;
		private PartialFunctionRunner(Plugin plugin, Function<Connection, T> s) {
			super(plugin);
			this.s = s;
		}
		
		@Override
		public AsyncRunner andThen(Consumer<T> c) {
			return new ConnectionRunner<T>(plugin, c, s);
		}
	};
	
}
