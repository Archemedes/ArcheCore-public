package net.lordofthecraft.arche.commands;

import static net.md_5.bungee.api.ChatColor.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import co.lotc.core.CoreLog;
import co.lotc.core.bukkit.menu.MenuAction;
import co.lotc.core.bukkit.menu.MenuAgent;
import co.lotc.core.bukkit.menu.MenuUtil;
import co.lotc.core.bukkit.menu.icon.Icon;
import co.lotc.core.bukkit.menu.icon.Slot;
import co.lotc.core.bukkit.util.InventoryUtil;
import co.lotc.core.bukkit.util.ItemUtil;
import co.lotc.core.bukkit.util.Run;
import co.lotc.core.command.CommandTemplate;
import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import lombok.AllArgsConstructor;
import lombok.var;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Account;
import net.lordofthecraft.arche.interfaces.AccountHandler;
import net.lordofthecraft.arche.interfaces.Persona;

public class CommandItemCache extends CommandTemplate {
	private final AccountHandler accountHandler = ArcheCore.getControls().getAccountHandler();
	
	
	@Cmd("Claim items from your own item cache")
	public void claim(Player p) {
		Account acc = accountHandler.getAccount(p);
		
		List<ItemStack> itemCache = acc.getItemCache();
		validate(!itemCache.isEmpty(), "There are no items inside your Account's item cache");
		
		//If errors are ever thrown this makes the cache empty
		//Players will then bitch to us about bugs instead of duping to their heart's content
		//It's just better for the game in the long run
		acc.setItemCache(Lists.newArrayList());
		
		msg(LIGHT_PURPLE + "Adding the cached items to your inventory now...");
		var leftover = InventoryUtil.addItem(p.getInventory(), itemCache);
		
		if(!leftover.isEmpty()) {
			msg(RED + "Some items could not be added due to lack of space! Make space in your inventory and rerun this command.");
			acc.setItemCache(new ArrayList<>(leftover.values()));
		}
	}
	
	@Cmd(value="Modify an account's item cache",permission="archecore.admin")
	public void modify(Player p, @Arg("player") UUID u) {
		accountHandler.loadAccount(u).then(acc->openCache(p, acc, true));
	}
	
	private void openCache(Player p, Account acc, boolean admin) {
		List<CacheSlot> slots = acc.getItemCache().stream()
				.map(is->new CacheSlot(is, admin))
				.collect(Collectors.toCollection(ArrayList::new));
		
		int size = roundUp(slots.size(), 45);
		if(size <= 45) size = 90;
		
		int extra = size - slots.size();
		IntStream.range(0, extra).forEach($->slots.add(new CacheSlot(null, admin)));
		
		var menus = MenuUtil.createMultiPageMenu(null, LIGHT_PURPLE + "Item Cache", slots);
		MenuAgent agent = menus.get(0).openSession(p);
		agent.onSessionClose(context->{
			List<ItemStack> newCache = new ArrayList<>();
			menus.forEach(m->{
				for(int i = 0; i < m.getSize(); i++) {
					Icon x = m.getIcon(i);
					if(x instanceof CacheSlot) {
						ItemStack is = m.getInventory().getItem(i);
						if(ItemUtil.exists(is)) newCache.add(is);
					}
				}
			});
			
			acc.setItemCache(newCache);
		});
	}
	
	int roundUp(int numToRound, int multiple){
		if (multiple == 0)
			return numToRound;

		int remainder = numToRound % multiple;
		if (remainder == 0)
			return numToRound;

		return numToRound + multiple - remainder;
	}
	
	
	private enum Type{PLAYER,ENDER};
	
	@Cmd(value="Get a snapshot of persona inventory at a certain time",permission="archecore.admin")
	public void snapshot(Player p, Type type, Persona target, Instant when) {
		String inv = type == Type.PLAYER? "inv":"ender";
		Inventory whichInv = type == Type.PLAYER? target.getInventory() : target.getEnderChest();
		
		Run.as(ArcheCore.getPlugin())
		.async(()->getInventorySnapshot(target.getPersonaId(), whichInv, when, inv))
		.then(snapshot->{
			p.openInventory(snapshot);
			var date = LocalDateTime.ofInstant(when, ZoneId.systemDefault());
			p.sendMessage(AQUA + "Snapshot of " + WHITE+ target.identify() + AQUA + " at " + WHITE + date.withNano(0).withSecond(0));
		});
	}
	
	private Inventory getInventorySnapshot(int personaId, Inventory whichInv, Instant when, String inv) {
		String invAdd = inv + "_add";
		String invDel = inv + "_del";
		
		Inventory copy = Bukkit.createInventory(null, 54, "Snapshot of type: " + inv);
		copy.setContents(whichInv.getContents());
		
		try(Connection c = ArcheCore.getSQLControls().getConnection(); Statement s = c.createStatement()){
			ResultSet rs = s.executeQuery("SELECT "+invAdd+","+invDel + " FROM persona_invdiff WHERE persona_id_fk="
					+ personaId + " AND time>"+ when.toEpochMilli() +" ORDER BY time DESC");
			
			while(rs.next()) {
				List<ItemStack> wasAdded = InventoryUtil.deserializeItems(rs.getString(invAdd));
				List<ItemStack> wasRemoved = InventoryUtil.deserializeItems(rs.getString(invDel));
				
				var result = copy.removeItem(wasAdded.toArray(new ItemStack[0]));
				if(!result.isEmpty()) CoreLog.warning("Failed to reconstruct inventory snapshot during removal: \n" + StringUtils.join(result.values(), "\n"));
				result = copy.addItem(wasRemoved.toArray(new ItemStack[0]));
				if(!result.isEmpty()) CoreLog.warning("Failed to reconstruct inventory snapshot during addition: \n" + StringUtils.join(result.values(), "\n"));
			}
			
			rs.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}
		
		return copy;
	}
	
	@AllArgsConstructor
	private static class CacheSlot extends Slot{
		private ItemStack item;
		private boolean admin;
		
		@Override
		public void click(MenuAction action) {
			item = null;
		}

		@Override
		public ItemStack getItemStack(MenuAgent agent) {
			return item;
		}
		
		@Override
		public boolean mayInteract(ItemStack moved) {
			return admin || moved.getType() == Material.AIR;
		}

	}
}
