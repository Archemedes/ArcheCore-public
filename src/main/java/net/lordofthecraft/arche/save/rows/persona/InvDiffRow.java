package net.lordofthecraft.arche.save.rows.persona;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import co.lotc.core.bukkit.util.InventoryUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.experimental.FieldDefaults;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.save.rows.RunnerRow;

@AllArgsConstructor
@FieldDefaults(level=AccessLevel.PRIVATE,makeFinal=true)
public class InvDiffRow implements RunnerRow {
	int personaId;
	Inventory personInv;
	Inventory enderInv;

	@Override
	public void run(Connection connection) throws SQLException {
		List<ItemStack> invDel;
		List<ItemStack> endDel;

		@Cleanup
		PreparedStatement ps2 = connection.prepareStatement("SELECT inv,ender_inv FROM persona_vitals WHERE persona_id_fk=?");
		ps2.setInt(1, personaId);
		ResultSet rs = ps2.executeQuery();
		if(rs.next()) { //There's an entry for vitals
			invDel = getDiffWithStoredInventory(personInv, rs, "inv");
			endDel = getDiffWithStoredInventory(enderInv, rs, "ender_inv");
		} else { //assume empty inv, so just do all.
			invDel = Lists.newArrayList();
			endDel = Lists.newArrayList();
		}
		rs.close();
		ps2.close();

		queueDiffSave(invDel, endDel);
	}
	
	//This also removes certain items from the inventory due to removeItem
	//What remains in inv is assumed to be the newly added items
	List<ItemStack> getDiffWithStoredInventory(Inventory inv, ResultSet rs, String field) throws SQLException {
		String invString = rs.getString(field);
		if(invString == null) return Lists.newArrayList();
		
		List<ItemStack> oldItems = InventoryUtil.deserializeItems(invString)
				.stream().filter(Objects::nonNull).collect(Collectors.toList());
		return new ArrayList<>(inv.removeItem(oldItems.toArray(new ItemStack[0])).values());
	}
	
	void queueDiffSave(List<ItemStack> invDel, List<ItemStack> endDel) {
		List<ItemStack> invAdd = InventoryUtil.getItems(personInv);
		List<ItemStack> endAdd = InventoryUtil.getItems(enderInv);
		
		if(invAdd.isEmpty() && endAdd.isEmpty() && invDel.isEmpty() && endDel.isEmpty())
			return; //Dont queue if theres nothing to do.
		
		ArcheCore.getConsumerControls().insert("persona_invdiff")
		.set("time", Instant.now().toEpochMilli())
		.set("persona_id_fk", personaId)
		.set("inv_add", InventoryUtil.serializeItems(invAdd))
		.set("inv_del", InventoryUtil.serializeItems(invDel))
		.set("ender_add", InventoryUtil.serializeItems(endAdd))
		.set("ender_del", InventoryUtil.serializeItems(endDel))
		.queue();
	}


}
