package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.interfaces.Persona;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.util.UUID;

import static org.bukkit.attribute.Attribute.*;

public class RaceBonusHandler {

	private static final UUID UUID_ARCHE = UUID.fromString("22a6b068-6e20-4295-87e7-4fd0c176307e");

	private RaceBonusHandler() {
	}

	public static void reset(Player p) {
		for (Attribute at : Attribute.values()) {
			AttributeInstance inst = p.getAttribute(at);
			if (inst != null) {
				inst.getModifiers().stream().filter(mod -> mod.getUniqueId().equals(UUID_ARCHE)).forEach(inst::removeModifier);
			}
		}
	}


	public static void apply(Persona p) {

		if (!ArcheCore.getPlugin().areRacialBonusesEnabled()) return;


		Race race = p.getRace();
		Player player = p.getPlayer();
		reset(player);
		switch (race) {
		case HIGH_ELF:
		case WOOD_ELF:
		case DARK_ELF:
		case ELF:
			player.getAttribute(GENERIC_MOVEMENT_SPEED).addModifier(new AttributeModifier(UUID_ARCHE, "arche_speed", 0.10, AttributeModifier.Operation.ADD_SCALAR));
			break;
		case FOREST_DWARF:
			player.getAttribute(GENERIC_LUCK).addModifier(new AttributeModifier(UUID_ARCHE, "501_moreluck", 0.1, AttributeModifier.Operation.ADD_NUMBER));
			break;
		case CAVE_DWARF:
		case DWARF:
		case MOUNTAIN_DWARF:
		case DARK_DWARF:
			player.getAttribute(GENERIC_MAX_HEALTH).addModifier(new AttributeModifier(UUID_ARCHE, "arche_healthboost", 0.2, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
			player.getAttribute(GENERIC_LUCK).addModifier(new AttributeModifier(UUID_ARCHE, "501_moreluck", 0.1, AttributeModifier.Operation.ADD_NUMBER));
			player.getAttribute(GENERIC_MOVEMENT_SPEED).addModifier(new AttributeModifier(UUID_ARCHE, "arche_lessspeed", -0.10, AttributeModifier.Operation.ADD_SCALAR));
			break;
		case HALFLING:
			player.getAttribute(GENERIC_MAX_HEALTH).addModifier(new AttributeModifier(UUID_ARCHE, "arche_lesshealth", -8, AttributeModifier.Operation.ADD_NUMBER));
			player.getAttribute(GENERIC_LUCK).addModifier(new AttributeModifier(UUID_ARCHE, "501_moreluck", 0.10, AttributeModifier.Operation.ADD_NUMBER));
			break;
		case GOBLIN:
			player.getAttribute(GENERIC_MAX_HEALTH).addModifier(new AttributeModifier(UUID_ARCHE, "arche_lesshealth", -4, AttributeModifier.Operation.ADD_NUMBER));
			player.getAttribute(GENERIC_LUCK).addModifier(new AttributeModifier(UUID_ARCHE, "501_moreluck", 0.10, AttributeModifier.Operation.ADD_NUMBER));
			player.getAttribute(GENERIC_MOVEMENT_SPEED).addModifier(new AttributeModifier(UUID_ARCHE, "arche_morespeed", 0.10, AttributeModifier.Operation.ADD_SCALAR));
			break;
		case OLOG:
			player.getAttribute(GENERIC_MAX_HEALTH).addModifier(new AttributeModifier(UUID_ARCHE, "arche_healthboost", 0.2, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
			player.getAttribute(GENERIC_ATTACK_SPEED).addModifier(new AttributeModifier(UUID_ARCHE, "501_lessaspeed", -0.10, AttributeModifier.Operation.ADD_SCALAR));
			break;
		default: //Basic Humans, Default/Unset
			break;
		}
	}
}
