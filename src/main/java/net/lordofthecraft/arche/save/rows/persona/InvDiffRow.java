package net.lordofthecraft.arche.save.rows.persona;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
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
		ResultSet rs;
		
		List<ItemStack> invDel;
		List<ItemStack> endDel;
		
		@Cleanup
		PreparedStatement ps1 = connection.prepareStatement("SELECT COUNT(persona_id) FROM persona_invdiff WHERE persona_id=?");
		
		//we want to check if this persona has any snapshots saved
		//This is because personas from BEFORE this addition wont have logs
		//So their stored inventory wont have been diffed before, but it must
		//So that we can later on reconstruct snapshots properly
		ps1.setInt(1, personaId);
		rs = ps1.executeQuery();
		rs.next();
		boolean isInit = rs.getInt(1) > 0; //Logs found if more than 0
		rs.close();
		ps1.close();

		if(!isInit) { //No previous entries, so just make all
			invDel = Lists.newArrayList();
			endDel = Lists.newArrayList();
		} else { //make a diff of the last state of the persona inv and how it is now
			
			@Cleanup
			PreparedStatement ps2 = connection.prepareStatement("SELECT inv,ender_inv FROM persona_vitals WHERE persona_id_fk=?");
			ps2.setInt(1, personaId);
			if(rs.next()) { //There's an entry for vitals
				invDel = getDiffWithStoredInventory(personInv, rs, "inv");
				endDel = getDiffWithStoredInventory(enderInv, rs, "ender_inv");
			} else { //assume empty inv, so just do all.
				invDel = Lists.newArrayList();
				endDel = Lists.newArrayList();
			}
			rs.close();
			ps2.close();
		}

		queueDiffSave(invDel, endDel);
	}
	
	//This also removes certain items from the inventory due to removeItem
	//What remains in inv is assumed to be the newly added items
	List<ItemStack> getDiffWithStoredInventory(Inventory inv, ResultSet rs, String field) throws SQLException {
		String invString = rs.getString(field);
		List<ItemStack> oldItems = InventoryUtil.deserializeItems(invString);
		return inv.removeItem(oldItems.toArray(new ItemStack[0]))
			.values().stream().collect(Collectors.toList());
	}
	
	void queueDiffSave(List<ItemStack> invDel, List<ItemStack> endDel) {
		List<ItemStack> invAdd = InventoryUtil.getItems(personInv);
		List<ItemStack> endAdd = InventoryUtil.getItems(enderInv);
		
		if(invAdd.isEmpty() && endAdd.isEmpty() && invDel.isEmpty() && endDel.isEmpty())
			return; //Dont queue if theres nothing to do.
		
		ArcheCore.getConsumerControls().insert("persona_invdiff")
		.set("time", Instant.now().toEpochMilli())
		.set("persona_id", personaId)
		.set("inv_add", InventoryUtil.serializeItems(invAdd))
		.set("inv_del", InventoryUtil.serializeItems(invDel))
		.set("ender_add", InventoryUtil.serializeItems(endAdd))
		.set("ender_del", InventoryUtil.serializeItems(endDel))
		.queue();
	}


}
