package net.lordofthecraft.arche.event.persona;

import com.google.common.collect.Lists;
import net.lordofthecraft.arche.attributes.ArcheAttribute;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.interfaces.Persona;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RaceBonusApplyEvent extends PersonaEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Map<ArcheAttribute, List<AttributeModifier>> mods;
    private final Race race;
    private boolean cancelled = false;

    public RaceBonusApplyEvent(Persona persona, Map<ArcheAttribute, List<AttributeModifier>> mods, Race race) {
        super(persona);
        this.mods = mods;
        this.race = race;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public Race getRace() {
        return race;
    }

    public Map<ArcheAttribute, List<AttributeModifier>> getMods() {
        return Collections.unmodifiableMap(mods);
    }

    public void addModifier(ArcheAttribute attribute, AttributeModifier mod) {
        if (!mods.containsKey(attribute)) {
            mods.put(attribute, Lists.newArrayList(mod));
        } else {
            List<AttributeModifier> lmods = mods.get(attribute);
            lmods.add(mod);
            mods.put(attribute, lmods);
        }
    }

    public void removeModifiers(ArcheAttribute attribute) {
        mods.remove(attribute);
    }

    public void removeModifier(ArcheAttribute attribute, AttributeModifier mod) {
        if (mods.containsKey(attribute)) {
            List<AttributeModifier> lmods = mods.get(attribute);
            lmods.remove(mod);
            mods.put(attribute, lmods);
        }
    }

    public void removeModifier(ArcheAttribute attribute, String name) {
        if (mods.containsKey(attribute)) {
            List<AttributeModifier> lmods = mods.get(attribute);
            lmods.removeIf(mod -> mod.getName().equalsIgnoreCase(name));
            mods.put(attribute, lmods);
        }
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
