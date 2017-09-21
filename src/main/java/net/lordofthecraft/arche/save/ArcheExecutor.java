package net.lordofthecraft.arche.save;

import net.lordofthecraft.arche.save.tasks.ArcheTask;
import net.lordofthecraft.arche.save.tasks.EndOfStreamTask;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class ArcheExecutor {

	//private BlockingDeque<ArcheTask> saveQueue = new LinkedBlockingDeque<>();
	private ExecutorService service = Executors.newSingleThreadExecutor(new ArcheThreadFactory());

    public static ArcheExecutor getInstance() {
        return SingletonHolder.INSTANCE;
	}

	public Future<?> put(ArcheTask s) {
		if (s instanceof EndOfStreamTask) {
			Future<?> f = service.submit(s);
			shutdown();
			return f;
		}
		return service.submit(s);
	}

	public boolean isShutdown() {
		return service.isShutdown();
	}

	public <T> Future<T> prepareCallable(Callable<T> call) {
		return service.submit(call);
	}

	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection) throws InterruptedException {
		return service.invokeAll(collection);
	}

	public <T> T invokeAny(Collection<? extends Callable<T>> collection) throws InterruptedException, ExecutionException {
		return service.invokeAny(collection);
	}

	protected void shutdown() {
        service.shutdown();
        try {
            if (!service.awaitTermination(5, TimeUnit.SECONDS)) {
                service.shutdownNow();
            }
        } catch (InterruptedException e) {
            service.shutdownNow();
        }
    }
	
	private static class SingletonHolder {
        private static final ArcheExecutor INSTANCE = new ArcheExecutor();
    }
}
