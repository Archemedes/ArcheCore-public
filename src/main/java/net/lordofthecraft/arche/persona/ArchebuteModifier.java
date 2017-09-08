package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.SaveHandler;
import net.lordofthecraft.arche.save.tasks.persona.ArchebuteRemoveTask;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.sql.Timestamp;

/**
 * A super interface for {@link org.bukkit.attribute.AttributeModifier} to support {@link net.lordofthecraft.arche.persona.ArchePersona} based {@link org.bukkit.attribute.Attribute}s
 * <p>
 * Modifiers do not store references to the attribute in which they are created for, and can be used liberally for other {@link org.bukkit.attribute.AttributeInstance}
 *
 * @author 501warhead
 * @see net.lordofthecraft.arche.persona.PersonaAttributes
 */
public class ArchebuteModifier {

    private final Persona persona;
    private final Attribute attribute;
    private final AttributeModifier mod;
    private final Timestamp creation;
    //Can be null. Null = no decay.
    private Timestamp decayDate;

    public ArchebuteModifier(Persona persona, Attribute attribute, AttributeModifier mod, Timestamp creation) {
        this.persona = persona;
        this.attribute = attribute;
        this.mod = mod;
        this.creation = creation;
    }

    public ArchebuteModifier(Persona persona, Attribute attribute, AttributeModifier mod, Timestamp creation, Timestamp decayDate) {
        this.persona = persona;
        this.attribute = attribute;
        this.mod = mod;
        this.creation = creation;
        this.decayDate = decayDate;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public Persona getPersona() {
        return persona;
    }

    public AttributeModifier getMod() {
        return mod;
    }

    public Timestamp getCreation() {
        return creation;
    }

    public Timestamp getDecayDate() {
        return decayDate;
    }

    public boolean shouldDecay() {
        return decayDate == null || System.currentTimeMillis() > decayDate.getTime();
    }

    public AttributeInstance applyToInstance() {
        //Purge all modifiers that have the same UUID.

        Player pl = persona.getPlayer();
        if (pl == null) {
            return null;
        }
        AttributeInstance inst = pl.getAttribute(attribute);
        inst.getModifiers().parallelStream().filter(m -> m.getUniqueId().equals(mod.getUniqueId())).forEach(inst::removeModifier);

        return inst;
    }

    public AttributeInstance removeFromInstance() {
        Player pl = persona.getPlayer();
        if (pl == null) {
            return null;
        }
        AttributeInstance inst = pl.getAttribute(attribute);
        inst.removeModifier(mod);
        return inst;
    }

    public void remove() {
        SaveHandler.getInstance().put(new ArchebuteRemoveTask(this));
    }
}
