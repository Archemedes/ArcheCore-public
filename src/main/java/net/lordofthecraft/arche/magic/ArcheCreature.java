package net.lordofthecraft.arche.magic;

import com.google.common.collect.Sets;
import net.lordofthecraft.arche.interfaces.Creature;
import net.lordofthecraft.arche.interfaces.Magic;

import java.sql.JDBCType;
import java.sql.SQLType;
import java.util.Collections;
import java.util.Set;

/**
 * Created on 7/13/2017
 *
 * @author 501warhead
 */
public class ArcheCreature implements Creature {

    public enum Field {
        NAME("name", "magic_creatures", JDBCType.VARCHAR, false),
        DESCRIPTION("descr", "magic_creatures", JDBCType.VARCHAR, false),
        ABILITY("ability", "creature_abilities", JDBCType.VARCHAR, true),
        CREATOR("magic_id_fk", "creature_creators", JDBCType.VARCHAR, true);

        public final String field;
        public final String table;
        public final SQLType type;
        public final boolean insert;

        Field(String field, String table, SQLType type, boolean insert) {
            this.field = field;
            this.table = table;
            this.type = type;
            this.insert = insert;
        }
    }

    private final String id;
    private Set<Magic> creators;
    private String name;
    private String description;
    private Set<String> abilities;

    ArcheCreature(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        creators = Sets.newConcurrentHashSet();
        abilities = Sets.newConcurrentHashSet();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Set<Magic> getCreators() {
        return Collections.unmodifiableSet(creators);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Set<String> getAbilities() {
        return Collections.unmodifiableSet(abilities);
    }

    public void addCreator(Magic m) {
        creators.add(m);
    }

    public void removeCreator(Magic m) {
        creators.remove(m);
    }

    public void addAbility(String s) {
        abilities.add(s);
    }

    public void removeAbility(String s) {
        abilities.remove(s);
    }
}
