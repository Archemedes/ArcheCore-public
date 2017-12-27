package net.lordofthecraft.arche.attributes;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.rows.attribute.AttributeRemoveRow;
import net.lordofthecraft.arche.save.rows.attribute.AttributeUpdateRow;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.scheduler.BukkitRunnable;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.UUID;

/** 
 * Slightly more data for temp atts, which is not serialized through the bukkit method
 * Note that by default this is a permanent attribute
 */
public class ExtendedAttributeModifier extends AttributeModifier implements Cloneable {

	private final boolean lostOnDeath;
	private final boolean save; 

	//Variables to keep track of AttributeModifier removal time
	private final Decay decay;
	private AttributeModifierRemover task = null;
	private long ticksRemaining = -1;

    public ExtendedAttributeModifier(AttributeModifier other) {
        this(other.getUniqueId(), other.getName(), other.getAmount(), other.getOperation());
    }

    public ExtendedAttributeModifier(String name, double amount, Operation operation) {
        this(name, amount, operation, true);
    }
    
    public ExtendedAttributeModifier(String name, double amount, Operation operation, boolean save) {
        this(UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)), name, amount, operation, save);
    }
    
    public ExtendedAttributeModifier(UUID uuid, String name, double amount, Operation operation) {
        this(uuid, name, amount, operation, true);
    }
    
    public ExtendedAttributeModifier(UUID uuid, String name, double amount, Operation operation, boolean save) {
        this(uuid, name, amount, operation, save, false);
    }
    
    public ExtendedAttributeModifier(UUID uuid, String name, double amount, Operation operation, boolean save, boolean lostOnDeath) {
        super(uuid, name, amount, operation);
        this.save = save;
        this.lostOnDeath = lostOnDeath;
        this.decay = Decay.NEVER;
    }
    
    public ExtendedAttributeModifier(String name, double amount, Operation operation, Decay decay, long ticksRemaining) {
        this(name, amount, operation, decay, ticksRemaining, false);
    }

    public ExtendedAttributeModifier(String name, double amount, Operation operation, Decay decay, long ticksRemaining, boolean lostOnDeath) {
        this(UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)), name, amount, operation, decay, ticksRemaining, lostOnDeath);
    }
    
    public ExtendedAttributeModifier(UUID uuid, String name, double amount, Operation operation, Decay decay, long ticksRemaining) {
        this(uuid, name, amount, operation, decay, ticksRemaining, false);
    }
    
    public ExtendedAttributeModifier(UUID uuid, String name, double amount, Operation operation, Decay decay, long ticksRemaining, boolean lostOnDeath) {
    	this(uuid, name, amount, operation, true, decay, ticksRemaining, lostOnDeath);
    }
    
    public ExtendedAttributeModifier(UUID uuid, String name, double amount, Operation operation, boolean save, Decay decay, long ticksRemaining, boolean lostOnDeath) {
        super(uuid, name, amount, operation);
        this.save = save;
        this.decay = decay;
        this.ticksRemaining = ticksRemaining;
        this.lostOnDeath = lostOnDeath;
    }

    public ExtendedAttributeModifier clone() {
        return new ExtendedAttributeModifier(this.getUniqueId(), this.getName(), this.getAmount(), this.getOperation(), this.decay, this.ticksRemaining, this.lostOnDeath);
    }

    public boolean isLostOnDeath() {
        return lostOnDeath;
	}
    
    public boolean willSave() {
    	return save;
    }
	
	public Decay getDecayStrategy() {
		return decay;
	}

    public long getTicksRemaining() {
        return ticksRemaining;
    }
    
    public void init(ArcheAttribute a, Persona ps) {
    	if(decay == Decay.OFFLINE || (decay == Decay.ACTIVE && ps.isCurrent() && ps.getPlayer() != null)) {
    		setupTask(a, ps);
    	}
    }
	
	public void handleSwitch(ArcheAttribute a, Persona ps, boolean current) {
		if(decay == Decay.ACTIVE) {
			if(current) {
				if(this.ticksRemaining > 0) {
					setupTask(a, ps);
				}
			} else if(task != null) { //Interrupt ongoing tasks
				long progress = System.currentTimeMillis() - task.creation.getTime();
				this.ticksRemaining -= (progress / 50); //Amount of ticks that we progressed during active Persona time
				task.cancel();
				task = null;
				if(save) ArcheCore.getConsumerControls().queueRow(new AttributeUpdateRow(this, ps, a));
			}
		}
	}
	
	public void remove(Persona ps, ArcheAttribute aa) {
		if(task != null) task.cancel();
        if (save) ArcheCore.getConsumerControls().queueRow(new AttributeRemoveRow(this, aa, ps));
    }
	
	public void setupTask(ArcheAttribute a, Persona ps) {
		task = new AttributeModifierRemover(a, this, ps);
		task.runTaskLater(ArcheCore.getPlugin(), this.ticksRemaining);
	}
	
	public static String readablePercentage(AttributeModifier mod, ArcheAttribute aa) {
		boolean negative = mod.getAmount() < 0;
		ChatColor cc = negative? (aa.isHigherBetter()? ChatColor.RED : ChatColor.BLUE) :
						(aa.isHigherBetter()? ChatColor.BLUE : ChatColor.RED);
							
		char plusle = negative? Character.MIN_VALUE : '+';
				
		boolean addNumber = mod.getOperation() == Operation.ADD_NUMBER;
		String value = addNumber? Double.toString(mod.getAmount()) : 
			Integer.toString( (int) (mod.getAmount() * 100 ));
		char percent = addNumber? Character.MIN_VALUE : '%';
		return cc.toString() + plusle + value + percent;
	}
	
	public String asReadablePercentage(ArcheAttribute aa) {
		return readablePercentage(this, aa);
	}

    @Override
    public String toString() {
        return "ExtendedAttributeModifier{" +
                "lostOnDeath=" + lostOnDeath +
                ", save=" + save +
                ", decay=" + decay +
                ", task=" + task +
                ", ticksRemaining=" + ticksRemaining +
                ", superModifier=" + super.toString() +
                '}';
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
