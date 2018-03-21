package net.lordofthecraft.arche.save.rows;


public class FlexibleDeleteRow extends FlexibleRow {
	
	public FlexibleDeleteRow(String table) {
		super(table, "DELETE FROM");
	}

	
	@Override
	protected String getStatement() {
		return prefix() + "WHERE " + whereFromVars();
	}

	@Override
	protected Object getValueFor(int index) {
		return valueAtIndex(index);
	}

}
