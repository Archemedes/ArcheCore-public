package net.lordofthecraft.arche.interfaces;

import net.lordofthecraft.arche.magic.ArcheType;

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
}
