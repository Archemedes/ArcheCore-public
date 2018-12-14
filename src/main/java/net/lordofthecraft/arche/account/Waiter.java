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
	
	public Waiter(T fulfilled) { //Constructor when the Persona or Account is already loaded
		result = fulfilled;
		uuid = null;
		id = -1;
	}
	
	Waiter(UUID uuid){ //For Accounts
		this.uuid = uuid;
		this.id = -1;
	}
	
	Waiter(int id){ //For Personas
		this.uuid = null;
		this.id = id;
	}
	
	public void then(Consumer<T> what) {
		whatToDo = what;
		if(result != null) {
			if(Bukkit.isPrimaryThread()) whatToDo.accept(result);
			else fulfil(result);
		} else {
			Bukkit.getScheduler().runTaskAsynchronously(ArcheCore.getPlugin(), ()->{}); //TODO
		}
	}
	
	boolean isUUID(UUID uuid) {
		return uuid.equals(this.uuid);
	}
	
	boolean isId(int id) {
		return this.id == id;
	}
	
	void fulfil(T packet) {
		if(whatToDo != null) whatToDo.accept(packet);
	}
}