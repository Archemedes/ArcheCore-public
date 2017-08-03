package net.lordofthecraft.arche.interfaces;

import net.lordofthecraft.arche.magic.ArcheType;
import org.bukkit.entity.Player;

/**
 * Created on 7/12/2017
 *
 * @author 501warhead
 */
public interface Magic {
    String getLabel();

    String getDescription();

    boolean isTeachable();

    int getDaysToMaxTier();

    int getDaysToBonusTier();

    ArcheType getType();

    String getName();

    int getMaxTier();

    boolean isSelfTeachable();

    boolean hasLearned(Player p);

    boolean hasLearned(Persona p);

    boolean isVisible(Player p);

    boolean isVisible(Persona p);

    int getTier(Player p);

    int getTier(Persona p);

    boolean achievedTier(Player p, int tier);

    boolean achievedTier(Persona p, int tier);

    void setTier(Persona p, int tier);
}
