package net.lordofthecraft.arche.magic;

import net.lordofthecraft.arche.interfaces.MagicType;

import java.sql.JDBCType;
import java.sql.SQLType;

/**
 * Created on 7/12/2017
 *
 * @author 501warhead
 */
public class ArcheType implements MagicType {

    private final String key;
    private String name;
    private ArcheType parent;
    private String description;

    public enum Field {
        NAME("name", JDBCType.VARCHAR),
        PARENT_TYPE("parent_type", JDBCType.VARCHAR),
        DESCR("descr", JDBCType.VARCHAR);

        public final String name;
        public final SQLType datatype;

        Field(String name, SQLType datatype) {
            this.name = name;
            this.datatype = datatype;
        }
    }

    ArcheType(String key) {
        this.key = key;
    }

    ArcheType(String key, String name, ArcheType parent, String description) {
        this.key = key;
        this.name = name;
        this.parent = parent;
        this.description = description;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ArcheType getParent() {
        return parent;
    }

    @Override
    public String getDescription() {
        return description;
    }

    protected void performSQLUpdate(Object data, Field field) {
        //TODO
    }
}
