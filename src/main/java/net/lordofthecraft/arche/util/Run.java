package net.lordofthecraft.arche.util;

import java.sql.Connection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.var;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.save.rows.RunnerRow;

@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
public class Run {
	private final Plugin plugin;

	public static Run as(Plugin plugin) {
		return new Run(plugin);
	}
	
	public Executor syncExecutor() {
		return (r->scheduler().runTask(plugin, r));
	}
	
	public Executor asyncExecutor() {
		return (r->scheduler().runTaskAsynchronously(plugin, r));
	}
	
	public void delayed(long delay, Runnable r) {
		scheduler().runTaskLater(plugin, r, delay);
	}
	
	public void repeating(long timer, Runnable r) {
		repeating(1, timer, r);
	}
	
	public void repeating(long delay, long timer, Runnable r) {
		scheduler().runTaskTimer(plugin, r, delay, timer);
	}
	
	public void sync(Runnable r) {
		scheduler().runTask(plugin, r);
	}
	
	public void async(Runnable r) {
		scheduler().runTaskAsynchronously(plugin, r);
	}
	
	public CompletableFuture<Void> future(Runnable r) {
		return CompletableFuture.runAsync(r, asyncExecutor());
	}
	
	public <T> CompletableFuture<T> future(Supplier<T> r) {
		return CompletableFuture.supplyAsync(r, asyncExecutor());
	}
	
	public <T> AsyncRunner<T> async(Supplier<T> s) {
		return new AsyncRunner<>(plugin, s, null);
	}
	
	public <T> AsyncRunner<T> async(Function<Connection, T> f) {
		return new AsyncRunner<>(plugin, null, f);
	}
	
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class AsyncRunner<T> {
		private final Plugin plugin;
		private final Supplier<T> call;
		private final Function<Connection, T> fun;
		
		private Consumer<T> consumer;
		
		public void then(Consumer<T> consumer) {
			this.consumer = consumer;
			go();
		}
		
		private void go() {
			if(call == null) { //For the consumer
				var rr = new AsyncRunnerRow<>(this);
				ArcheCore.getConsumerControls().queueRow(rr);
			} else { //For async task
				Runnable r = ()->{
					T result = call.get();
					scheduler().runTask(plugin, ()->consumer.accept(result));
				};
				scheduler().runTaskAsynchronously(plugin, r);
			}
		}
	}
	
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	private static class AsyncRunnerRow<T> implements RunnerRow{
		private final AsyncRunner<T> runner;
		
		@Override
		public void run(Connection connection) {
			Runnable r = ()-> runner.consumer.accept(runner.fun.apply(connection));
			scheduler().runTask(runner.plugin, r);
		}
	}
	
	private static BukkitScheduler scheduler() {
		return Bukkit.getScheduler();
	}
	
}
