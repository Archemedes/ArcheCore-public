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
	private	final boolean sync;
	
	public Waiter(T fulfilled) { //Constructor when the Persona or Account is already loaded
		result = fulfilled;
		uuid = null;
		id = -1;
		sync = true;
	}
	
	Waiter(UUID uuid) { this(uuid, true); }
	Waiter(int id) { this(id, true); }
	
	Waiter(UUID uuid, boolean sync){ //For Accounts
		this.uuid = uuid;
		this.id = -1;
		this.sync = sync;
	}
	
	Waiter(int id, boolean sync){ //For Personas
		this.uuid = null;
		this.id = id;
		this.sync = sync;
	}
	
	public void then(Consumer<T> what) {
		whatToDo = what;
		if(result != null) fulfil(result);
		else Bukkit.getScheduler().runTaskAsynchronously(ArcheCore.getPlugin(), ()->{}); //TODO
	}
	
	boolean isUUID(UUID uuid) {
		return uuid.equals(this.uuid);
	}
	
	boolean isId(int id) {
		return this.id == id;
	}
	
	void fulfil(T packet) {
		if(whatToDo == null) throw new IllegalStateException("The callback was never set. We have nothing to do!");
		
		if(sync) Bukkit.getScheduler().scheduleSyncDelayedTask(ArcheCore.getPlugin(), ()->whatToDo.accept(packet));
		else whatToDo.accept(packet);
	}
}