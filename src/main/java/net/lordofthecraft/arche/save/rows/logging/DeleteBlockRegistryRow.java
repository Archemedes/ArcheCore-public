package net.lordofthecraft.arche.save.rows.logging;

import co.lotc.core.bukkit.util.WeakBlock;
import net.lordofthecraft.arche.save.rows.SingleStatementRow;

public class DeleteBlockRegistryRow extends SingleStatementRow {
    final WeakBlock wb;

    public DeleteBlockRegistryRow(WeakBlock wb) {
        this.wb = wb;
    }

    @Override
    protected String getStatement() {
        return "DELETE FROM blockregistry WHERE world=? AND x=? AND y=? AND z=?";
    }

    @Override
    protected Object getValueFor(int index) {
        switch (index) {
            case 1:
                return wb.getWorld();
            case 2:
                return wb.getX();
            case 3:
                return wb.getY();
            case 4:
                return wb.getZ();
            default:
                throw new IllegalArgumentException();
        }
    }

}
