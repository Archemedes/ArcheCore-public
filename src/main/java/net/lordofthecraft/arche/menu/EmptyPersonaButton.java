package net.lordofthecraft.arche.menu;

import static net.md_5.bungee.api.ChatColor.*;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import co.lotc.core.bukkit.menu.MenuAction;
import co.lotc.core.bukkit.menu.MenuAgent;
import co.lotc.core.bukkit.menu.icon.Button;
import co.lotc.core.bukkit.util.ItemUtil;
import lombok.RequiredArgsConstructor;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.persona.CreationDialog;

@RequiredArgsConstructor
public class EmptyPersonaButton extends Button {
	private final int slot;

	@Override
	public void click(MenuAction ma) {
		ma.getMenuAgent().close();
		Player p = ma.getPlayer();
		Persona ps = ArcheCore.getPersona(p);
		
		if (!ArcheCore.getControls().canCreatePersonas()) {
			p.sendMessage(ChatColor.RED + "Persona creation is disabled for this server, please go to the main server to create your persona.");
		} else if (ps != null && ps.isNewbie()) {
			p.sendMessage(ChatColor.RED + "Let's get a bit more familliar with this server before we start creating additional personas.");
		} else {
			//Player may make new persona here, let's do so now.
			CreationDialog dialog = new CreationDialog();
			dialog.addPersona(p, slot, true);
		}
	}

	@Override
	public ItemStack getItemStack(MenuAgent arg0) {
		ItemStack is = new ItemStack(Material.SKELETON_SKULL, 1);
		return ItemUtil.decorate(is, "Empty Persona", GREEN+""+ITALIC + "Click here", GRAY + "To create a new Persona");
	}

}
