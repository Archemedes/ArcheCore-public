package net.lordofthecraft.arche.interfaces;

import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Represents a magic which can be learned, casted, leveled, and mastered.
 *
 * Magics have a {@link net.lordofthecraft.arche.interfaces.MagicType} which will show what type of magic it is (Holy, Voidal, Evocation)
 * Magics might be able to create {@link net.lordofthecraft.arche.interfaces.Creature}s
 *
 * @author 501warhead
 */
public interface Magic {

    /**
     * Check to see if this magic is weak against another magic
     *
     * @param m The magic to see if it is strong against this magic
     * @return Whether or not this magic is weak to the specified magic.
     */
    boolean isWeakAgainst(Magic m);

    Map<Magic, Double> getWeaknesses();

    /**
     * Check how weak this magic is to another magic
     * @param m The magic to check against
     * @return The weakness modifier against the specified magic.
     */
    double getWeaknessModifier(Magic m);

    /**
     * Retrieve the readable name of this magic
     * @return The readable name of this magic
     */
    String getLabel();

    /**
     * Retrieve the readable description of this magic
     * @return The readable description of this magic
     */
    String getDescription();

    /**
     * Check to see if this magic can be taught to other players
     * @return Whether or not this magic can be taught to other players
     */
    boolean isTeachable();

    /**
     * Get the number of days one must have on a persona with this magic to reach the "maximum" tier
     * @return The days needed to achieve the highest tier
     */
    int getDaysToMaxTier();

    /**
     * Get the days required to reach a "bonus" tier, or end game if applicable.
     * @return The days needed to achieve the bonus tier, 0 if there is no bonus tier.
     */
    int getDaysToBonusTier();

    /**
     * Get the ArcheType that this magic is a part of, e.g. Holy, Dark, Diety, Voidal, Evocation, etc.
     * @return The Archetype this magic falls under
     */
    MagicType getType();

    /**
     * The underlying name of this magic
     * @return The underlying name value
     */
    String getName();

    /**
     * Retrieve the maximum tier this skill can reach
     * @return The maximum tier
     */
    int getMaxTier();

    /**
     * Check whether or not this magic can be learned without a teacher
     * @return Whether or not this magic can be learned without a teacher
     */
    boolean isSelfTeachable();

    /**
     * Check if a player has learned this magic
     * @param p The player to check
     * @return Whether or not the player has learned this magic
     */
    boolean hasLearned(Player p);

    /**
     * Check whether or not a persona has learned this magic
     * @param p The persona to check
     * @return Whether or not the persona has learned this magic
     */
    boolean hasLearned(Persona p);

    /**
     * Check whether or not this magic is visible on the persona card of this player
     * @param p The player to check
     * @return Whether or not the magic is visible, false if the magic has not been learned.
     */
    boolean isVisible(Player p);

    /**
     * Check whether or not this magic is visible on this persona's card
     * @param p The persona to check
     * @return Whether or not the magic is visible, false if the magic has not been learned
     */
    boolean isVisible(Persona p);

    /**
     * Get the tier this player has achieved in this magic
     * @param p The player to check
     * @return The tier the player has achieved in this magic, 0 if the magic is not learned.
     */
    int getTier(Player p);

    /**
     * Get the tier this persona has achieved in this magic
     * @param p The persona to check
     * @return The tier the persona has achieved in this magic, 0 if the magic is not learned
     */
    int getTier(Persona p);

    /**
     * Check to see if a player has achieved a specific tier in this magic
     * @param p The player to check
     * @param tier The tier to check for
     * @return Whether or not the tier has been achieved. False if the magic has not been learned.
     */
    boolean achievedTier(Player p, int tier);

    /**
     * Check to see if a persona has achieved a specific tier in this magic
     * @param p The player to check
     * @param tier The tier to check for
     * @return Whether or not the tier has been achieved. False if the magic has not been learned.
     */
    boolean achievedTier(Persona p, int tier);

    /**
     * Set the tier of this player
     * @param p The player to set
     * @param tier The tier to set the player to. Clamped between 0 and {@link #getMaxTier()}
     */
    void setTier(Persona p, int tier);
}
