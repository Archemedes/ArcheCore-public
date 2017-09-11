package net.lordofthecraft.arche.attributes;

import java.sql.Timestamp;
import java.util.UUID;

import org.bukkit.attribute.AttributeModifier;
import org.bukkit.scheduler.BukkitRunnable;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;

//Slightly more data for temp atts, which is not serialized through the bukkit method
//Note that by default this is a permanent attribute
public class ExtendedAttributeModifier extends AttributeModifier {
	public boolean lostOnDeath = false;
	public boolean save = true; 

	//Variables to keep track of AttributeModifier removal time
	private Decay decay = Decay.NEVER;
	private AttributeModifierRemover task = null;
	private long ticksRemaining = -1;
	
	public ExtendedAttributeModifier(AttributeModifier other) {
		super(other.getUniqueId(), other.getName(), other.getAmount(), other.getOperation());
	}
	
	public ExtendedAttributeModifier(UUID uuid, String name, double amount, Operation operation) {
		super(uuid, name, amount, operation);
	}
	
	public boolean isLostOnDeath() {
		return lostOnDeath;
	}
	
	public Decay getDecayStrategy() {
		return decay;
	}
	
	public void setLostOnDeath(boolean lost) {
		lostOnDeath = lost;
	}
	
	public void setDecayStrategy(ArcheAttribute a, Persona ps, Decay decay, long ticksFromNow) {
		if(this.decay != Decay.NEVER) {
			stopDecay();	
		}
		
		this.decay = decay;
		this.ticksRemaining = ticksFromNow;
		setupTask(a, ps);
	}
	
	public void stopDecay() {
		decay = Decay.NEVER;
		if(task != null) {
			task.cancel();
			task = null;
			ticksRemaining = -1;
		}
	}
	
	public void handleSwitch(ArcheAttribute a, Persona ps) {
		if(decay == Decay.ACTIVE) {
			if(ps.isCurrent()) {
				if(this.ticksRemaining > 0) {
					setupTask(a, ps);
				}
			} else {
				interruptTask();
			}
		}
		
		//Most important time to save is as a persona is switched away from
		//It is thus handled as if it were a Minecraft vitals component
		//Note however that ACTUAL vanilla attmods are not made persistent in AC
		if(!ps.isCurrent()) {
			trySave();
		}
	}
	
	public void handleLogoff() {
		if(decay == Decay.ACTIVE && task != null) {
			interruptTask();
		}
		trySave();
	}
	
	private void interruptTask() {
		if(task != null) {
			long progress = System.currentTimeMillis() - task.creation.getTime();
			this.ticksRemaining -= (progress / 50); //Amount of ticks that we progressed during active Persona time
			task.cancel();
			task = null;
		}
	}
	
	public void setShouldSave(boolean save) {
		this.save = save;
	}
	
	private void trySave() {
		if(save) {
			//TODO: mysql logic to insert this Mod into the db
		}
	}
	
	public void remove() {
		if(task != null) task.cancel();	
		if(save) save = true;//TODO remove this attmod from DB here
	}
	
	private void setupTask(ArcheAttribute a, Persona ps) {
		task = new AttributeModifierRemover(a, this, ps);
		task.runTaskLater(ArcheCore.getPlugin(), this.ticksRemaining);
	}
	
	
	public enum Decay { NEVER, ACTIVE, OFFLINE }
	
	private static class AttributeModifierRemover extends BukkitRunnable {
		private final ArcheAttribute attribute;
		private final ExtendedAttributeModifier modifier;
		private final Persona ps;
		private final Timestamp creation;
		
		private AttributeModifierRemover(ArcheAttribute a, ExtendedAttributeModifier m, Persona ps) {
			attribute = a;
			modifier = m;
			this.ps = ps;
			this.creation = new Timestamp(System.currentTimeMillis());
		}
		
		@Override
		public void run() {
			ps.attributes().removeModifier(attribute, modifier);
		}
	}

}
