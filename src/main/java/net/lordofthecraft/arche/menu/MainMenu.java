package net.lordofthecraft.arche.menu;

import static net.md_5.bungee.api.ChatColor.*;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import co.lotc.core.bukkit.menu.MenuBuilder;
import co.lotc.core.bukkit.menu.icon.Icon;
import co.lotc.core.bukkit.menu.icon.Pad;
import co.lotc.core.bukkit.util.ItemUtil;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.CoreLog;
import net.lordofthecraft.arche.interfaces.IArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaHandler;
import net.lordofthecraft.arche.persona.CreationDialog;

public class MainMenu {
	private final IArcheCore controls;
	private final int personaSwitchDelay;
	private final Icon[] icons = new Icon[18];

	public MainMenu(IArcheCore arche, int personaSwitchDelay) {
		this.controls = arche;
		this.personaSwitchDelay = personaSwitchDelay;
		addInitialIcons();
	}

	public void addIcon(int index, Icon icon){
		Validate.isTrue(index >= 0 && index < icons.length, "Index outside of the customizable menu range!");
		if(icons[index] != null) CoreLog.warning("Overriding the Main Menu icon present in slot " + index);
		icons[index] = icon;
	}

	public boolean isSlotTaken(int index) {
		Validate.isTrue(index >= 0 && index < icons.length, "Index outside of the customizable menu range!");
		return icons[index] != null;
	}

	private void addInitialIcons() {
		icons[2] = new AttributeIcon();
		icons[1] = new EnderChestButton();
	}


	public void openMenu(Player p) {
		if(!CreationDialog.mayConverse(p))
			return;

		PersonaHandler handler = controls.getPersonaHandler();
		if(handler.mayUse(p) || (handler.countPersonas(p) == 0 && !p.hasPermission("archecore.exempt"))){
			Persona[] prs = handler.getAllPersonas(p);
			if(prs == null){
				CoreLog.severe(" [Beacon] Player walking around without registered Personas File!");
				return;
			}

			int highestUsed = 0;
			int firstFree = -1;
			int count = 0;
			int current = -1;
			for(int i = 0; i < prs.length; i++){
				if(prs[i] != null){
					highestUsed = i;
					count++;
					if(prs[i].isCurrent())
						current = i;
				} else if(firstFree < 0){
					firstFree = i;
				}
			}

			if(count == 0) CoreLog.warning("Zero personas for: " + p.getName());
			else if(current < 0) CoreLog.warning("found no current persona for: " + p.getName());
			
			int max = handler.getAllowedPersonas(p);
			int absmax = ArcheCore.getControls().personaSlots();
			int requiredSize = requiredSize(highestUsed, max, absmax, firstFree);
			MenuBuilder mb = new MenuBuilder(controls.getCalendar().toPrettyString(), 3+requiredSize/9);

			//Add the icons to the menu
			for(int i = 0; i < icons.length; i++) {
				try {
					if(icons[i] != null) mb.icon(i, icons[i]);
				}catch(Exception e) {
					e.printStackTrace();
					continue;
				}
			}

			mb.icon(18, new PersonaInfoButton(max));


			//Buttons for switching Personas
			int freeSlots = ArcheCore.getControls().canCreatePersonas()? max - count : 0;
			boolean mayMakeMore = freeSlots > 0;

			for(int i = 0; i < requiredSize; i++){
				Persona a = prs[i];
				Icon icon = null;
				if(a == null){
					freeSlots--;
					if(mayMakeMore) {
						icon = new EmptyPersonaButton(i);
						mayMakeMore = false;
					} else if (freeSlots >= 0) {
						icon = new AvailablePersonaButton();
					}else {
						icon = lockedPersona();
					}
				} else {
					int switchDelay = p.hasPermission("archecore.persona.quickswitch")? 0 : this.personaSwitchDelay;
					icon = new PersonaButton(count, switchDelay, a);
				}

				mb.icon(19+i, icon);
			}
			
			mb.build().openSession(p);
		}
	}
	
	private Icon lockedPersona() {
		return new Pad(ItemUtil.decorate(
				new ItemStack(Material.WITHER_SKELETON_SKULL, 1), "Locked Slot",
				GRAY + "Please " + GREEN+""+ITALIC + "Purchase",
				GRAY + "You may purchase more personas in the store"
			));
	}

	private int requiredSize(int highestUsed, int allowedPersonas, int maxPersonas, int firstFree) {
		int result = highestUsed+1;

		if (firstFree > highestUsed && firstFree < allowedPersonas) {
			//Need an extra slot for a white skull (= open slot)
			result++;
		} else if (firstFree > highestUsed && maxPersonas > result && result != 8) {
			//Need an extra slot to tell people they can buy MORE
			result++;
		}

		return result;
	}
}
