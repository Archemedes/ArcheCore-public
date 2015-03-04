package net.lordofthecraft.arche.save.tasks;

import com.google.common.collect.*;
import java.util.*;

public class DataTask extends ArcheTask
{
    public static final int CREATE = 1;
    public static final int INSERT = 2;
    public static final int UPDATE = 3;
    public static final int DELETE = 4;
    private final int operation;
    private final String table;
    private final Map<String, Object> values;
    private final Map<String, Object> criteria;
    
    public DataTask(final int operation, final String table, final Map<String, Object> values, final Map<String, Object> criteria) {
        super();
        this.table = table;
        this.operation = operation;
        this.values = values;
        this.criteria = criteria;
    }
    
    public int getOperation() {
        return this.operation;
    }
    
    @Override
    public void run() {
        switch (this.operation) {
            case 1: {
                final Map<String, String> newValues = Maps.newHashMap();
                for (final Map.Entry<String, Object> entry : this.criteria.entrySet()) {
                    newValues.put(entry.getKey(), entry.getValue().toString());
                }
                DataTask.handle.createTable(this.table, newValues);
                break;
            }
            case 2: {
                DataTask.handle.insert(this.table, this.values);
                break;
            }
            case 3: {
                DataTask.handle.update(this.table, this.values, this.criteria);
                break;
            }
            case 4: {
                DataTask.handle.remove(this.table, this.criteria);
                break;
            }
            default: {
                throw new IllegalArgumentException("This operation type couldn't be executed");
            }
        }
    }
}
