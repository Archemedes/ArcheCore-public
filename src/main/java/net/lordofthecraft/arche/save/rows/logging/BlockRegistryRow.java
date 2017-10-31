package net.lordofthecraft.arche.save.rows.logging;

import java.sql.Timestamp;

import net.lordofthecraft.arche.save.rows.SingleStatementRow;
import net.lordofthecraft.arche.util.WeakBlock;

public class BlockRegistryRow extends SingleStatementRow {
	private final Timestamp now = now();
    private final WeakBlock block;
    String data = null;

    public BlockRegistryRow(WeakBlock block) {
        this.block = block;
    }

    public BlockRegistryRow(WeakBlock block, String data) {
        this.block = block;
        this.data = data;
    }

    @Override
    public boolean isUnique() {
        return data != null;
    }
    
	@Override
	protected String getStatement() {
		return "INSERT " + orIgnore() + " INTO blockregistry" + 
				(isUnique()? "(date,world,x,y,z,data) VALUES (?,?,?,?,?,?)" : "(date,world,x,y,z) VALUES (?,?,?,?,?)");
	}

	@Override
	protected Object getValueFor(int index) {
		switch(index) {
		case 1: return now;
		case 2: return block.getWorld();
		case 3: return block.getX();
		case 4: return block.getY();
		case 5: return block.getZ();
		case 6: return data;
		default: throw new IllegalArgumentException();
		}
	}
	
    @Override
    public String toString() {
        return "BlockRegisteryRow{" +
                "block=" + block +
                ", data='" + data + '\'' +
                '}';
    }

}
