package net.lordofthecraft.arche.menu;

import static net.md_5.bungee.api.ChatColor.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import co.lotc.core.bukkit.menu.MenuAction;
import co.lotc.core.bukkit.menu.MenuAgent;
import co.lotc.core.bukkit.menu.icon.Button;
import co.lotc.core.bukkit.util.ItemUtil;
import lombok.RequiredArgsConstructor;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.event.persona.PersonaWhoisEvent;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaHandler;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import net.lordofthecraft.arche.persona.CreationDialog;
import net.lordofthecraft.arche.skin.ArcheSkin;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

@RequiredArgsConstructor
public class PersonaButton extends Button {
	private static final Map<UUID, Long> switchCooldown = Maps.newConcurrentMap();
	
	private final int personaCount;
	private final int personaSwitchDelay;
	private final Persona persona;
	private final PersonaHandler handler = ArchePersonaHandler.getInstance();
	
	
	@Override
	public void click(MenuAction ma) {
		long now = System.currentTimeMillis();
		
		ma.getMenuAgent().close();
		
		Player p = persona.getPlayer();
		UUID u = p.getUniqueId();
		
		if(ma.getClick().isShiftClick()) {
			CreationDialog dialog = new CreationDialog();
			if(ma.getClick().isLeftClick()) { //Create new
				if (!ArcheCore.getControls().canCreatePersonas()) {
					p.sendMessage(ChatColor.RED + "Persona creation is disabled for this server, please go to the main server to create your persona.");
					return;
				}
				dialog.addPersona(p, persona.getSlot(), false);
			} else if(ma.getClick().isRightClick()) { //Remove
				if(personaCount > 1 || p.hasPermission("archecore.exempt")){
					dialog.removePersona(persona);
				} else {
					p.sendMessage(ChatColor.RED + "You may not remove your last Persona!");
				}
			}
		} else if (!persona.isCurrent()) {
			
			if(switchCooldown.containsKey(u) && switchCooldown.get(u) > now) {
				long still = (switchCooldown.get(u) - now)/60000l;
				p.sendMessage(RED+"You must wait another " + still + " minutes before you can switch personas.");
			} else {
				boolean suc = handler.switchPersona(p, persona.getSlot(), false);
				if (suc) p.sendMessage(AQUA + "You are now Roleplaying as: " + ChatColor.YELLOW + "" + ChatColor.ITALIC + persona.getName());

				if(personaSwitchDelay > 0) switchCooldown.put(u, now + 60*1000*personaSwitchDelay);
				else switchCooldown.remove(u);
			}
		} else {
			p.sendMessage(AQUA + " This is the persona you are currently using!");
		}
	}

	@Override
	public ItemStack getItemStack(MenuAgent ma) {
		ArcheSkin sk = persona.getSkin();
		ItemStack is = (sk != null ? sk.getHeadItem() : new ItemStack(Material.PLAYER_HEAD, 1));
		String name = ChatColor.YELLOW + "" + ChatColor.ITALIC + persona.getName();
		String gender = persona.getGender() == null? "" : persona.getGender();
		String desc = ChatColor.GRAY + persona.getRaceString(false) + " " + gender;

		List<BaseComponent> sent = Lists.newArrayList(TextComponent.fromLegacyText(desc));

		Player p = persona.getPlayer();
		boolean mod = p.hasPermission("archecore.mod") || p.hasPermission("archecore.admin");
		PersonaWhoisEvent event = new PersonaWhoisEvent(persona, p, sent, PersonaWhoisEvent.Query.BEACON_ICON, mod, false);
		Bukkit.getPluginManager().callEvent(event);

		if(event.isCancelled()) return ItemUtil.make(Material.ZOMBIE_HEAD, DARK_GRAY+"Unavailable"); //This means the persona pip won't show up on the select screen and persona wont be selectable

		List<String> finalLore = event.getSent().stream().map(bc -> bc.toLegacyText()).collect(Collectors.toList());
		finalLore.add( (persona.isCurrent()? ChatColor.DARK_GREEN + "Selected!": ChatColor.GREEN + "Click to select") );
		ItemUtil.decorate(is, name, finalLore.toArray(new String[finalLore.size()]));

		return is;
	}
	
}
