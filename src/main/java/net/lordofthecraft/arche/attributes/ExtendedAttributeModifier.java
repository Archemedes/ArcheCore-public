package net.lordofthecraft.arche.attributes;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.SaveHandler;
import net.lordofthecraft.arche.save.tasks.attribute.ArcheAttributeRemoveTask;
import net.lordofthecraft.arche.save.tasks.attribute.ArcheAttributeUpdateTask;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Timestamp;
import java.util.UUID;

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

    public ExtendedAttributeModifier(UUID uuid, String name, double amount, Operation operation, Decay decay, long ticksRemaining, boolean lostOnDeath) {
        super(uuid, name, amount, operation);
        this.decay = decay;
        this.ticksRemaining = ticksRemaining;
        this.lostOnDeath = lostOnDeath;
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

    public long getTicksRemaining() {
        return ticksRemaining;
    }

    public void setDecayStrategy(ArcheAttribute a, Persona ps, Decay decay, long ticksFromNow) {
        if(this.decay != Decay.NEVER) {
            stopDecay();
        }
		
		this.decay = decay;
		this.ticksRemaining = ticksFromNow;
		setupTask(a, ps);
        trySave(ps, a);
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
			trySave(ps, a);
		}
	}
	
	public void handleLogoff(Persona ps, ArcheAttribute a) {
		if(decay == Decay.ACTIVE && task != null) {
			interruptTask();
		}
		trySave(ps, a);
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
	
	private void trySave(Persona ps, ArcheAttribute aa) {
		if(save) {
            SaveHandler.getInstance().put(new ArcheAttributeUpdateTask(this, ps, aa));
        }
	}
	
	
	public void remove(Persona ps, ArcheAttribute aa) {
		if(task != null) task.cancel();
        if (save) {
            save = true;
            SaveHandler.getInstance().put(new ArcheAttributeRemoveTask(this, ps, aa));
        }
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
