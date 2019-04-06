package net.lordofthecraft.arche.menu;

import static net.md_5.bungee.api.ChatColor.*;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import co.lotc.core.bukkit.menu.MenuAction;
import co.lotc.core.bukkit.menu.MenuAgent;
import co.lotc.core.bukkit.menu.icon.Button;
import co.lotc.core.bukkit.util.ItemUtil;
import lombok.RequiredArgsConstructor;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.help.HelpDesk;

@RequiredArgsConstructor
public class PersonaInfoButton extends Button {
	private final int max;
	
	@Override
	public void click(MenuAction ma) {
		ma.getMenuAgent().close();
		HelpDesk.getInstance().outputHelp("persona", ma.getPlayer());
	}

	@Override
	public ItemStack getItemStack(MenuAgent ma) {
		ItemStack is = new ItemStack(Material.COMPARATOR);
		ItemUtil.decorate(is, WHITE + "Your Personas to the right",
				GRAY + "Max Personas: " + ChatColor.LIGHT_PURPLE + max,
				DARK_GRAY + "Left Click to select", (ArcheCore.getControls().canCreatePersonas() ? DARK_GRAY + "SHIFT + Left Click: Create new" : RED + "Creating new personas is disabled on this server"),
				DARK_GRAY + "SHIFT + Right Click: Permakill Persona",
				ChatColor.GRAY + "Click me for more info.");
		return is;
	}
}
