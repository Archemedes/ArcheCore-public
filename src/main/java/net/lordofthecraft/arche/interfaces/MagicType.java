package net.lordofthecraft.arche.interfaces;

/**
 * MagicType represents an {@link net.lordofthecraft.arche.magic.ArcheType}
 *
 * An archetype is a tree-style setup that houses several {@link net.lordofthecraft.arche.interfaces.Magic} beneath it or several other Archetypes.
 * Archetypes can have parent Archetype, such as "Evocation" might be an ArcheType and it's parent would be "Voidal"
 *
 * At the top level should ideally be: Dark, Holy, Voidal
 *
 * @author 501warhead
 */
public interface MagicType {
    /**
     * Get the underlying value used to reference this Archetype
     *
     * @return The underlying key
     */
    String getKey();

    /**
     * The readable name of this Archetype
     * @return The name for players to read
     */
    String getName();

    /**
     * Get the magic that this type is a child to. E.g. if this archetype is "Evocation" the parent might be "Voidal"
     *
     * @return The parent Archetype
     */
    MagicType getParent();

    /**
     * Get the human readable description for this Archetype
     * @return The description of this Archetype for players to read
     */
    String getDescription();
}
