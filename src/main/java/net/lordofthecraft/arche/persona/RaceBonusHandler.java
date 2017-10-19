package net.lordofthecraft.arche.persona;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.attributes.ArcheAttribute;
import net.lordofthecraft.arche.attributes.AttributeRegistry;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.event.persona.RaceBonusApplyEvent;
import net.lordofthecraft.arche.interfaces.Persona;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class RaceBonusHandler {

    private static final UUID UUID_ARCHE = UUID.fromString("22a6b068-6e20-4295-87e7-4fd0c176307e");
    public static final UUID UUID_RACIAL_SCORE = UUID.fromString("aba33abe-febe-4ef7-af85-ec052f1158ba");

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


    public static void apply(Persona p, Race race) {
        if (race == null) {
            return;
        }

        if (!ArcheCore.getPlugin().areRacialBonusesEnabled()) return;
        Map<ArcheAttribute, List<AttributeModifier>> vals = Maps.newHashMap();
        switch (race) {
            case ELF:
                vals.put(AttributeRegistry.getSVanillaAttribute(Attribute.GENERIC_MOVEMENT_SPEED), Lists.newArrayList(new AttributeModifier(UUID_ARCHE, "arche_speed", 0.10, AttributeModifier.Operation.ADD_SCALAR)));
                break;
            case CONSTRUCT:
                vals.put(AttributeRegistry.getSVanillaAttribute(Attribute.GENERIC_MAX_HEALTH), Lists.newArrayList(new AttributeModifier(UUID_ARCHE, "arche_healthboost", 0.5, AttributeModifier.Operation.MULTIPLY_SCALAR_1)));
                vals.put(AttributeRegistry.getSVanillaAttribute(Attribute.GENERIC_MOVEMENT_SPEED), Lists.newArrayList(new AttributeModifier(UUID_ARCHE, "arche_lessspeed", -0.40, AttributeModifier.Operation.ADD_SCALAR)));
                break;
            case NECROLYTE:
                vals.put(AttributeRegistry.getSVanillaAttribute(Attribute.GENERIC_MAX_HEALTH), Lists.newArrayList(new AttributeModifier(UUID_ARCHE, "arche_healthboost", 0.2, AttributeModifier.Operation.MULTIPLY_SCALAR_1)));
                vals.put(AttributeRegistry.getSVanillaAttribute(Attribute.GENERIC_MOVEMENT_SPEED), Lists.newArrayList(new AttributeModifier(UUID_ARCHE, "arche_speed", 0.15, AttributeModifier.Operation.ADD_SCALAR)));
                break;
            case HIGH_ELF:
            case WOOD_ELF:
            case DARK_ELF:
            case SNOW_ELF:
                vals.put(AttributeRegistry.getSVanillaAttribute(Attribute.GENERIC_MOVEMENT_SPEED), Lists.newArrayList(new AttributeModifier(UUID_ARCHE, "arche_speed", 0.10, AttributeModifier.Operation.ADD_SCALAR)));
                break;
            case FOREST_DWARF:
                vals.put(AttributeRegistry.getSVanillaAttribute(Attribute.GENERIC_LUCK), Lists.newArrayList(new AttributeModifier(UUID_ARCHE, "501_moreluck", 0.1, AttributeModifier.Operation.ADD_NUMBER)));
                break;
            case CAVE_DWARF:
            case DWARF:
            case MOUNTAIN_DWARF:
            case DARK_DWARF:
                vals.put(AttributeRegistry.getSVanillaAttribute(Attribute.GENERIC_MAX_HEALTH), Lists.newArrayList(new AttributeModifier(UUID_ARCHE, "arche_healthboost", 0.2, AttributeModifier.Operation.MULTIPLY_SCALAR_1)));
                vals.put(AttributeRegistry.getSVanillaAttribute(Attribute.GENERIC_LUCK), Lists.newArrayList(new AttributeModifier(UUID_ARCHE, "501_moreluck", 0.1, AttributeModifier.Operation.ADD_NUMBER)));
                vals.put(AttributeRegistry.getSVanillaAttribute(Attribute.GENERIC_MOVEMENT_SPEED), Lists.newArrayList(new AttributeModifier(UUID_ARCHE, "arche_lessspeed", -0.10, AttributeModifier.Operation.ADD_SCALAR)));
                break;
            case HALFLING:
                vals.put(AttributeRegistry.getSVanillaAttribute(Attribute.GENERIC_MAX_HEALTH), Lists.newArrayList(new AttributeModifier(UUID_ARCHE, "arche_lesshealth", -8, AttributeModifier.Operation.ADD_NUMBER)));
                vals.put(AttributeRegistry.getSVanillaAttribute(Attribute.GENERIC_LUCK), Lists.newArrayList(new AttributeModifier(UUID_ARCHE, "501_moreluck", 0.10, AttributeModifier.Operation.ADD_NUMBER)));
                break;
            case GOBLIN:
                vals.put(AttributeRegistry.getSVanillaAttribute(Attribute.GENERIC_MAX_HEALTH), Lists.newArrayList(new AttributeModifier(UUID_ARCHE, "arche_lesshealth", -4, AttributeModifier.Operation.ADD_NUMBER)));
                vals.put(AttributeRegistry.getSVanillaAttribute(Attribute.GENERIC_LUCK), Lists.newArrayList(new AttributeModifier(UUID_ARCHE, "501_moreluck", 0.10, AttributeModifier.Operation.ADD_NUMBER)));
                vals.put(AttributeRegistry.getSVanillaAttribute(Attribute.GENERIC_MOVEMENT_SPEED), Lists.newArrayList(new AttributeModifier(UUID_ARCHE, "arche_morespeed", 0.10, AttributeModifier.Operation.ADD_SCALAR)));
                break;
            case OLOG:
            //case ORC:
                vals.put(AttributeRegistry.getSVanillaAttribute(Attribute.GENERIC_MAX_HEALTH), Lists.newArrayList(new AttributeModifier(UUID_ARCHE, "arche_healthboost", 0.2, AttributeModifier.Operation.MULTIPLY_SCALAR_1)));
                vals.put(AttributeRegistry.getSVanillaAttribute(Attribute.GENERIC_ATTACK_SPEED), Lists.newArrayList(new AttributeModifier(UUID_ARCHE, "501_lessaspeed", -0.10, AttributeModifier.Operation.ADD_SCALAR)));
                //vals.put(Attribute.GENERIC_LUCK, new AttributeModifier(UUID_ARCHE, "501_lessluck", -0.05, AttributeModifier.Operation.ADD_NUMBER));
                break;
            case KHARAJYR:
            case KHA_LEPARDA:
            case KHA_CHEETRAH:
            case KHA_PANTERA:
            case KHA_TIGRASI:
                vals.put(AttributeRegistry.getSVanillaAttribute(Attribute.GENERIC_MOVEMENT_SPEED), Lists.newArrayList(new AttributeModifier(UUID_ARCHE, "arche_speed", 0.12, AttributeModifier.Operation.ADD_SCALAR)));
                vals.put(AttributeRegistry.getSVanillaAttribute(Attribute.GENERIC_LUCK), Lists.newArrayList(new AttributeModifier(UUID_ARCHE, "501_moreluck", 0.1, AttributeModifier.Operation.ADD_NUMBER)));
                break;
            case HOUZI_LAO:
            case HOUZI_FEI:

                vals.put(AttributeRegistry.getSVanillaAttribute(Attribute.GENERIC_MOVEMENT_SPEED), Lists.newArrayList(new AttributeModifier(UUID_ARCHE, "arche_speed", 0.12, AttributeModifier.Operation.ADD_SCALAR)));
                vals.put(AttributeRegistry.getSVanillaAttribute(Attribute.GENERIC_LUCK), Lists.newArrayList(new AttributeModifier(UUID_ARCHE, "501_moreluck", 0.1, AttributeModifier.Operation.ADD_NUMBER)));

                break;
            case HOUZI_HEI:
                vals.put(AttributeRegistry.getSVanillaAttribute(Attribute.GENERIC_MAX_HEALTH), Lists.newArrayList(new AttributeModifier(UUID_ARCHE, "arche_healthboost", 0.2, AttributeModifier.Operation.MULTIPLY_SCALAR_1)));
                vals.put(AttributeRegistry.getSVanillaAttribute(Attribute.GENERIC_LUCK), Lists.newArrayList(new AttributeModifier(UUID_ARCHE, "501_moreluck", 0.1, AttributeModifier.Operation.ADD_NUMBER)));
                break;
            default: //Basic Humans, Default/Unset
                break;
        }
        RaceBonusApplyEvent event = new RaceBonusApplyEvent(p, vals, race);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        event.getMods().forEach((key, value) -> value.forEach(val -> p.attributes().addModifier(key, val)));
        //vals.forEach((key, value) -> p.attributes().addModifier(key, value));
        if (ArcheCore.getPlugin().debugMode()) {
            Logger log = ArcheCore.getPlugin().getLogger();
            log.info("Resultant stats for the user " + p.getName());
            /*Arrays.stream(Attribute.values())
                    .forEach(attr -> {
                        AttributeInstance inst = p.getAttribute(attr);
                        if (inst != null) {
                            log.info("Attribute instance is " + inst.getAttribute().name() + " and it's final value is " + inst.getValue());
                            log.info("modifiers are: ");
                            inst.getModifiers().forEach(mod -> log.info("Modifier operation is " + mod.getOperation().name() + " and it's value is " + mod.getAmount() + ". Name is " + mod.getName()));
                        }
                    });*/
        }
    }
}
