package net.lordofthecraft.arche.interfaces;

import java.util.Set;

/**
 * Created on 8/5/2017
 *
 * @author 501warhead
 */
public interface Creature {
    String getId();

    Set<Magic> getCreators();

    String getName();

    String getDescription();

    Set<String> getAbilities();
}
