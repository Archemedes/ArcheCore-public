package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.ArcheCore;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.UUID;
import java.util.logging.Logger;

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

    public static void apply(Player p, Race race) {
        if (race == null) {
            reset(p);
            return;
        }

        if (!ArcheCore.getPlugin().areRacialBonusesEnabled()) return;
        EnumMap<Attribute, AttributeModifier> vals = new EnumMap<>(Attribute.class);
        switch (race.getRaceId()) {
            case "ELF":
                vals.put(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID_ARCHE, "arche_speed", 0.10, AttributeModifier.Operation.ADD_SCALAR));
                break;
            case "CONSTRUCT":
                vals.put(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(UUID_ARCHE, "arche_healthboost", 0.5, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
                vals.put(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID_ARCHE, "arche_lessspeed", -0.40, AttributeModifier.Operation.ADD_SCALAR));
                vals.put(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new AttributeModifier(UUID_ARCHE, "arche_stalwart", 0.50, AttributeModifier.Operation.ADD_NUMBER));
                vals.put(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(UUID_ARCHE, "501_lessaspeed", -0.60, AttributeModifier.Operation.ADD_SCALAR));
                vals.put(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID_ARCHE, "501_flatarmor", 10, AttributeModifier.Operation.ADD_NUMBER));
                break;
            case "NECROLYTE":
                vals.put(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(UUID_ARCHE, "arche_healthboost", 0.2, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
                vals.put(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID_ARCHE, "arche_speed", 0.15, AttributeModifier.Operation.ADD_SCALAR));
                break;
            case "HIGH_ELF":
            case "WOOD_ELF":
            case "DARK_ELF":
            case "SNOW_ELF":
                vals.put(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID_ARCHE, "arche_speed", 0.10, AttributeModifier.Operation.ADD_SCALAR));
                break;
            case "FOREST_DWARF":
                vals.put(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new AttributeModifier(UUID_ARCHE, "arche_stalwart", 0.2, AttributeModifier.Operation.ADD_NUMBER));
                vals.put(Attribute.GENERIC_LUCK, new AttributeModifier(UUID_ARCHE, "501_moreluck", 0.1, AttributeModifier.Operation.ADD_NUMBER));
                break;
            case "CAVE_DWARF":
            case "DWARF":
            case "MOUNTAIN_DWARF":
            case "DARK_DWARF":
                vals.put(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(UUID_ARCHE, "arche_healthboost", 0.2, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
                vals.put(Attribute.GENERIC_LUCK, new AttributeModifier(UUID_ARCHE, "501_moreluck", 0.1, AttributeModifier.Operation.ADD_NUMBER));
                vals.put(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID_ARCHE, "arche_lessspeed", -0.10, AttributeModifier.Operation.ADD_SCALAR));
                break;
            case "HALFLING":
                vals.put(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(UUID_ARCHE, "arche_lesshealth", -8, AttributeModifier.Operation.ADD_NUMBER));
                vals.put(Attribute.GENERIC_LUCK, new AttributeModifier(UUID_ARCHE, "501_moreluck", 0.10, AttributeModifier.Operation.ADD_NUMBER));
                break;
            case "GOBLIN":
                vals.put(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(UUID_ARCHE, "arche_lesshealth", -4, AttributeModifier.Operation.ADD_NUMBER));
                vals.put(Attribute.GENERIC_LUCK, new AttributeModifier(UUID_ARCHE, "501_moreluck", 0.10, AttributeModifier.Operation.ADD_NUMBER));
                vals.put(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID_ARCHE, "arche_morespeed", 0.10, AttributeModifier.Operation.ADD_SCALAR));
                break;
            case "OLOG":
            //case ORC:
                vals.put(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(UUID_ARCHE, "arche_healthboost", 0.2, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
                vals.put(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(UUID_ARCHE, "501_lessaspeed", -0.10, AttributeModifier.Operation.ADD_SCALAR));
                //vals.put(Attribute.GENERIC_LUCK, new AttributeModifier(UUID_ARCHE, "501_lessluck", -0.05, AttributeModifier.Operation.ADD_NUMBER));
                break;
            case "KHARAJYR":
            case "KHA_LEPARDA":
            case "KHA_CHEETRAH":
            case "KHA_PANTERA":
            case "KHA_TIGRASI":
                vals.put(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID_ARCHE, "arche_speed", 0.12, AttributeModifier.Operation.ADD_SCALAR));
                vals.put(Attribute.GENERIC_LUCK, new AttributeModifier(UUID_ARCHE, "501_moreluck", 0.1, AttributeModifier.Operation.ADD_NUMBER));
                break;
            case "HOUZI_LAO":
            case "HOUZI_FEI":

                vals.put(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID_ARCHE, "arche_speed", 0.12, AttributeModifier.Operation.ADD_SCALAR));
                vals.put(Attribute.GENERIC_LUCK, new AttributeModifier(UUID_ARCHE, "501_moreluck", 0.1, AttributeModifier.Operation.ADD_NUMBER));

                break;
            case "HOUZI_HEI":
                vals.put(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(UUID_ARCHE, "arche_healthboost", 0.2, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
                vals.put(Attribute.GENERIC_LUCK, new AttributeModifier(UUID_ARCHE, "501_moreluck", 0.1, AttributeModifier.Operation.ADD_NUMBER));
                break;
            default: //Basic Humans, Default/Unset
                break;
        }
        reset(p);
        vals.forEach((key, value) -> {
            AttributeInstance inst = p.getAttribute(key);
            if (inst != null) {
                inst.addModifier(value);
            }
        });
        if (ArcheCore.getPlugin().debugMode()) {
            Logger log = ArcheCore.getPlugin().getLogger();
            log.info("Resultant stats for the user " + p.getName());
            Arrays.stream(Attribute.values())
                    .forEach(attr -> {
                        AttributeInstance inst = p.getAttribute(attr);
                        if (inst != null) {
                            log.info("Attribute instance is " + inst.getAttribute().name() + " and it's final value is " + inst.getValue());
                            log.info("modifiers are: ");
                            inst.getModifiers().forEach(mod -> log.info("Modifier operation is " + mod.getOperation().name() + " and it's value is " + mod.getAmount() + ". Name is " + mod.getName()));
                        }
                    });
        }
    }
}
