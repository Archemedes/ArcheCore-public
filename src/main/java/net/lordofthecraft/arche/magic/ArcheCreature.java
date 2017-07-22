package net.lordofthecraft.arche.magic;

import java.util.Collections;
import java.util.Set;

/**
 * Created on 7/13/2017
 *
 * @author 501warhead
 */
public class ArcheCreature {

    private final String id;
    private Set<ArcheMagic> creators;
    private String name;
    private String description;

    public ArcheCreature(String id) {
        this.id = id;
    }


    public String getId() {
        return id;
    }

    public Set<ArcheMagic> getCreators() {
        return Collections.unmodifiableSet(creators);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
