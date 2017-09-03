package net.lordofthecraft.arche.interfaces;

import net.lordofthecraft.arche.magic.ArcheType;

/**
 * Created on 8/5/2017
 *
 * @author 501warhead
 */
public interface MagicType {
    String getKey();

    String getName();

    ArcheType getParent();

    String getDescription();
}
