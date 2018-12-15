package net.lordofthecraft.arche.account;

import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;

import net.lordofthecraft.arche.ArcheCore;

public class Waiter<T> {
	private Consumer<T> whatToDo;
	
	private T result = null;
	
	private final UUID uuid;
	private final int id;
	
	public static <E> Waiter<E> wrap(E fulfilled){
		return new Waiter<>(fulfilled);
	}
	
	private Waiter(T fulfilled) { //Constructor when the Persona or Account is already loaded
		result = fulfilled;
		uuid = null;
		id = -1;
	}
	
	Waiter(UUID uuid){ //For Accounts
		this(uuid, -1);
	}
	
	Waiter(UUID uuid, int personaId){ //For Personas. Very deceptive constructor maybe but the id is NOT slot
		this.uuid = uuid;
		this.id = personaId;
	}
	
	public boolean isDone() {
		return result != null;
	}
	
	public T get() {
		return result;
	}
	
	public void then(Consumer<T> what) {
		whatToDo = what;
		if(result != null) {
			if(Bukkit.isPrimaryThread()) fulfil(result);
			else Bukkit.getScheduler().runTask(ArcheCore.getPlugin(), ()-> fulfil(result));
		} else {
			Bukkit.getScheduler().runTaskAsynchronously(ArcheCore.getPlugin(), ()->ArcheAccountHandler.getInstance().load(uuid, false));
		}
	}
	
	boolean isUUID(UUID uuid) {
		return uuid.equals(this.uuid);
	}
	
	boolean isId(int personaId) {
		return id == personaId;
	}
	
	void fulfil(T packet) {
		this.result = packet;
		whatToDo.accept(packet);
	}
}