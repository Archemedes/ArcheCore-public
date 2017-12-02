package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.interfaces.Creature;
import net.lordofthecraft.arche.interfaces.Magic;
import net.lordofthecraft.arche.interfaces.Persona;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class PersonaMagics {

    private final Persona persona;
    private final List<MagicAttachment> magics = new ArrayList<>();
    private Creature creature = null;

    PersonaMagics(Persona pers) {
        this.persona = pers;
    }

    public void addMagicAttachment(MagicAttachment attachment) {
        magics.add(attachment);
    }

    public void removeMagicAttachment(MagicAttachment attachment) {
        magics.remove(attachment);
    }

    public void removeMagicAttachment(Magic magic) {
        magics.removeIf(m -> m.getMagic().equals(magic));
    }

    public boolean hasMagic(Magic magic) {
        return magics.stream().anyMatch(m -> m.getMagic().equals(magic));
    }

    public boolean achievedTier(Magic m, int tier) {
        if (!hasMagic(m)) {
            return false;
        }
        Optional<MagicAttachment> omag = getMagicAttachment(m);
        return omag.filter(magicAttachment -> magicAttachment.getTier() >= tier).isPresent();
    }

    public Optional<MagicAttachment> getMagicAttachment(Magic m) {
        return magics.stream().filter(ma -> ma.getMagic().equals(m)).findFirst();
    }

    public boolean isMagical() {
    	return !magics.isEmpty();
    }
    
    public BaseComponent[] getMagicText() {
        ComponentBuilder b = new ComponentBuilder("");
        AtomicBoolean start = new AtomicBoolean(true);
        magics.parallelStream().filter(MagicAttachment::isVisible).forEach(m -> {
            if (!start.get()) {
                b.append(new TextComponent[]{new TextComponent(ChatColor.DARK_GRAY + ",")});
            }
            b.append(m.getReadableLine());
            start.set(false);
        });
        return b.create();
    }

    public Persona getPersona() {
        return persona;
    }

    public Creature getCreature() {
        return creature;
    }

    public boolean hasCreature() {
        return creature != null;
    }

    public void setCreature(Creature creature) {
        this.creature = creature;

    }
}
