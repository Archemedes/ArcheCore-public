package net.lordofthecraft.arche.persona;

import com.google.common.collect.Sets;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.SaveHandler;
import net.lordofthecraft.arche.save.tasks.persona.ArchebuteInsertTask;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;

import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

public class PersonaAttributes {

    private final Persona persona;
    private final Set<ArchebuteModifier> mods = Sets.newConcurrentHashSet();

    PersonaAttributes(Persona persona) {
        this.persona = persona;
    }

    public boolean hasModForAttribute(Attribute attr) {
        return mods.stream().anyMatch(m -> m.getAttribute().equals(attr));
    }

    public boolean hasMod(Attribute attr, UUID modid) {
        return hasModForAttribute(attr) && mods.stream().anyMatch(m -> m.getMod().getUniqueId().equals(modid));
    }

    public boolean hasMod(Attribute attr, AttributeModifier mod) {
        return hasMod(attr, mod.getUniqueId());
    }

    public boolean addModifier(Attribute attr, AttributeModifier mod) {
        ArchebuteModifier archemod = new ArchebuteModifier(persona, attr, mod, new Timestamp(System.currentTimeMillis()));
        if (mods.contains(archemod)) {
            return false;
        }
        if (mods.stream().anyMatch(m -> m.getAttribute().equals(attr) && (m.getMod().equals(mod) || m.getMod().getUniqueId().equals(mod.getUniqueId())))) {
            return false;
        }
        mods.add(archemod);
        if (persona.isCurrent()) {
            archemod.applyToInstance();
        }
        SaveHandler.getInstance().put(new ArchebuteInsertTask(archemod));
        return true;
    }

    public void removeModifiers(Attribute attr) {
        mods.parallelStream().filter(mod -> mod.getAttribute().equals(attr)).forEach(mod -> {
            mods.remove(mod);
            mod.remove();
        });
    }

    public boolean removeModifier(ArchebuteModifier mod) {
        mods.remove(mod);
        mod.remove();
        return true;
    }

    public void applyToPlayer() {
        checkAndRemove();
        mods.forEach(ArchebuteModifier::applyToInstance);
    }

    public void removeFromPlayer() {
        checkAndRemove();
        mods.forEach(ArchebuteModifier::removeFromInstance);
    }

    protected void checkAndRemove() {
        mods.parallelStream().filter(ArchebuteModifier::shouldDecay).forEach(this::removeModifier);
    }

    protected void performSQLUpdate() {

    }

}
