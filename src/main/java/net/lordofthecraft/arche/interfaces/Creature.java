package net.lordofthecraft.arche.interfaces;

import java.util.Set;

/**
 * Represents a "creature"
 *
 * A creature is a "Super" race that goes over the underlying race, E.g. a "Frost Witch" is a creature, but a frost witch might be a Dark Elf or a Human.
 * Creatures are a way to mechanically and dynamically upscale races
 *
 * @author 501warhead
 */
public interface Creature {
    /**
     * Gets the underlying ID of this creature.
     *
     * @return The Creature ID
     */
    String getId();

    /**
     * Get the magics which can create this creature.
     * @return The set of creators which can make this
     */
    Set<Magic> getCreators();

    /**
     * Get this creatures visible name
     * @return The name of this creature
     */
    String getName();

    /**
     * Get the description of this creature to be shown to players.
     * @return The description of this creature
     */
    String getDescription();

    /**
     * Get the abilities of this creature. NYI.
     * @return Set of strings which represent abilities
     */
    Set<String> getAbilities();
}
