package net.lordofthecraft.arche.menu;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import co.lotc.core.bukkit.menu.MenuAction;
import co.lotc.core.bukkit.menu.MenuAgent;
import co.lotc.core.bukkit.menu.icon.Button;
import lombok.RequiredArgsConstructor;
import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier;
import net.lordofthecraft.arche.help.HelpDesk;
import net.lordofthecraft.arche.interfaces.Persona;

@RequiredArgsConstructor
public class AttributeIcon extends Button {
	private final Persona pers;
	
	@Override
	public void click(MenuAction ma) {
		ma.getMenuAgent().close();
		Player p = ma.getPlayer();
		HelpDesk.getInstance().outputHelp("Attributes", p);
	}

	@Override
	public ItemStack getItemStack(MenuAgent ma) {
		ItemStack icon = new ItemStack(Material.GOLDEN_APPLE);
		ItemMeta m = icon.getItemMeta();
		m.setDisplayName(ChatColor.AQUA + "Persona Modifiers:");

		List<String> lore = new ArrayList<>();
		pers.attributes().getExistingInstances().stream().forEach(aa ->
		pers.attributes().getInstance(aa).getModifiers().stream()
		.map(ExtendedAttributeModifier.class::cast)
		.filter(mod -> StringUtils.isEmpty(mod.getName()) || !mod.getName().startsWith("/"))
		.forEach(mod->{
			String modName = mod.getName();
			boolean isCommented = StringUtils.isEmpty(modName) || modName.startsWith("#");
			lore.add(	mod.asReadablePercentage(aa) + ' ' + aa.getReadableName() +
					(isCommented? "" : ( " " + ChatColor.GRAY + "" + ChatColor.ITALIC + '(' + mod.getName() + ')'))
					);
		})
				);
		lore.add(ChatColor.GRAY + "Click to learn about attributes");
		m.setLore(lore);
		icon.setItemMeta(m);
		return icon;
	}

}
