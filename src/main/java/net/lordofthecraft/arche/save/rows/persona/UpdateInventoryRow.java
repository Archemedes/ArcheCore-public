package net.lordofthecraft.arche.save.rows.persona;

import net.lordofthecraft.arche.persona.PersonaInventory;
import net.lordofthecraft.arche.save.rows.SingleStatementRow;
import net.lordofthecraft.arche.util.InventoryUtil;

public class UpdateInventoryRow extends SingleStatementRow {
    private final PersonaInventory inv;

    public UpdateInventoryRow(PersonaInventory inv) {
        this.inv = inv;
    }

	@Override
	protected String getStatement() {
		return "UPDATE persona_vitals SET inv=?, ender_inv=? WHERE persona_id_fk=?";
	}

	@Override
	protected Object getValueFor(int index) {
		switch(index) {
		case 1: return InventoryUtil.isEmpty(inv.getInventory()) ? null : inv.getInvAsString();
		case 2: return InventoryUtil.isEmpty(inv.getEnderInventory()) ? null : inv.getEnderInvAsString();
		case 3: return inv.getPersona().getPersonaId();
		default: throw new IllegalArgumentException();
		}
	}
}
