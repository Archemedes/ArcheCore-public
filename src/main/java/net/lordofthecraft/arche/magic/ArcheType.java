package net.lordofthecraft.arche.magic;

/**
 * Created on 7/12/2017
 *
 * @author 501warhead
 */
public class ArcheType {

    private final String key;
    private String name;
    private ArcheType parent;
    private String description;

    public ArcheType(String key) {
        this.key = key;
    }

    public ArcheType(String key, String name, ArcheType parent, String description) {
        this.key = key;
        this.name = name;
        this.parent = parent;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public ArcheType getParent() {
        return parent;
    }

    public String getDescription() {
        return description;
    }
}
