package net.lordofthecraft.arche.commands;

import static net.md_5.bungee.api.ChatColor.LIGHT_PURPLE;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import co.lotc.core.bukkit.menu.MenuAction;
import co.lotc.core.bukkit.menu.MenuAgent;
import co.lotc.core.bukkit.menu.MenuUtil;
import co.lotc.core.bukkit.menu.icon.Icon;
import co.lotc.core.bukkit.menu.icon.Slot;
import co.lotc.core.bukkit.util.ItemUtil;
import co.lotc.core.command.CommandTemplate;
import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import lombok.AllArgsConstructor;
import lombok.var;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.AccountHandler;

public class CommandItemCache extends CommandTemplate {
	private final AccountHandler accountHandler = ArcheCore.getControls().getAccountHandler();
	
	
	@Cmd("Modify an account's item cache")
	public void modify(Player p, @Arg("player") UUID u) {
		accountHandler.loadAccount(u).then(acc->{
			List<CacheSlot> slots = acc.getItemCache().stream().map(CacheSlot::new).collect(Collectors.toCollection(ArrayList::new));
			
			int size = roundUp(slots.size(), 45);
			if(size <= 45) size = 90;
			
			int extra = size - slots.size();
			IntStream.range(0, extra).forEach($->slots.add(new CacheSlot(null)));
			
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
		});
	}
	
	int roundUp(int numToRound, int multiple)
	{
	    if (multiple == 0)
	        return numToRound;

	    int remainder = numToRound % multiple;
	    if (remainder == 0)
	        return numToRound;

	    return numToRound + multiple - remainder;
	}
	
	//Menu stuff
	
	@AllArgsConstructor
	private static class CacheSlot extends Slot{
		private ItemStack item;

		@Override
		public void click(MenuAction action) {
			item = null;
		}

		@Override
		public ItemStack getItemStack(MenuAgent agent) {
			return item;
		}

	}
}
