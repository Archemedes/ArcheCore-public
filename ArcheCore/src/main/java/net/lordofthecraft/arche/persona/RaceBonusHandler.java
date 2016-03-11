package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.enums.Race;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.UUID;
import java.util.logging.Logger;

/*import net.lordofthecraft.arche.attributes.AttributeBase;
import net.lordofthecraft.arche.attributes.AttributeModifier;
import net.lordofthecraft.arche.attributes.AttributeType;
import net.lordofthecraft.arche.attributes.Operation;*/

public class RaceBonusHandler {

    private static final UUID UUID_ARCHE = UUID.fromString("22a6b068-6e20-4295-87e7-4fd0c176307e");

    private RaceBonusHandler() {
    }

    public static void reset(Player p) {
        //AttributeModifier a, b, c, d, e, f;
        for (Attribute at : Attribute.values()) {
            AttributeInstance inst = p.getAttribute(at);
            if (inst != null) {
                inst.getModifiers().stream().filter(mod -> mod.getUniqueId().equals(UUID_ARCHE)).forEach(inst::removeModifier);
            }
        }
        /*a = new AttributeModifier(UUID_ARCHE, AttributeType.MAX_HEALTH);
        b = new AttributeModifier(UUID_ARCHE, AttributeType.MOVEMENT_SPEED);
        c = new AttributeModifier(UUID_ARCHE, AttributeType.KNOCKBACK_RESISTANCE);
        d = new AttributeModifier(UUID_ARCHE, AttributeType.LUCK);
        e = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_SPEED);
        f = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_DAMAGE);
        //d = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_DAMAGE);
        AttributeBase.removeModifier(p, a);
        AttributeBase.removeModifier(p, b);
        AttributeBase.removeModifier(p, c);
        AttributeBase.removeModifier(p, d);
        AttributeBase.removeModifier(p, e);
        AttributeBase.removeModifier(p, f);
        //AttributeBase.removeModifier(p, d);*/
    }

    public static void apply(Player p, Race race) {
        //AttributeModifier a, b, c, d, e, f;
        if (race == null) {
            reset(p);
            return;
        }

        if (!ArcheCore.getPlugin().areRacialBonusesEnabled()) return;
        EnumMap<Attribute, AttributeModifier> vals = new EnumMap<>(Attribute.class);
        switch (race) {
            case ELF:
                vals.put(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID_ARCHE, "arche_speed", 0.10, AttributeModifier.Operation.ADD_SCALAR));
                /*a = new AttributeModifier(UUID_ARCHE, AttributeType.MAX_HEALTH);
                b = new AttributeModifier(UUID_ARCHE, "arche_speed", 0.10, Operation.MULTIPLY, AttributeType.MOVEMENT_SPEED);
                c = new AttributeModifier(UUID_ARCHE, AttributeType.KNOCKBACK_RESISTANCE);
                d = race.getLuckValue() != 0 ? new AttributeModifier(UUID_ARCHE, "501_luck", race.getLuckValue(), Operation.INCREMENT, AttributeType.LUCK) : new AttributeModifier(UUID_ARCHE, AttributeType.LUCK);
                e = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_SPEED);
                f = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_DAMAGE);
                //d = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_DAMAGE);
                AttributeBase.removeModifier(p, a);
                AttributeBase.addModifier(p, b);
                AttributeBase.removeModifier(p, c);
                if (race.getLuckValue() == 0) {
                    AttributeBase.removeModifier(p, d);
                } else {
                    AttributeBase.addModifier(p, d);
                }
                AttributeBase.removeModifier(p, e);
                AttributeBase.removeModifier(p, f);
                //AttributeBase.removeModifier(p, d);*/
                break;
            case CONSTRUCT:
                vals.put(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(UUID_ARCHE, "arche_healthboost", 0.5, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
                vals.put(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID_ARCHE, "arche_lessspeed", -0.40, AttributeModifier.Operation.ADD_SCALAR));
                vals.put(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new AttributeModifier(UUID_ARCHE, "arche_stalwart", 0.50, AttributeModifier.Operation.ADD_NUMBER));
                vals.put(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(UUID_ARCHE, "501_lessaspeed", -0.40, AttributeModifier.Operation.ADD_SCALAR));
                vals.put(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID_ARCHE, "501_flatdamage", 6, AttributeModifier.Operation.ADD_NUMBER));
                /*a = new AttributeModifier(UUID_ARCHE, "arche_healthboost", 0.5, Operation.MULTIPLY_ALL, AttributeType.MAX_HEALTH);
                b = new AttributeModifier(UUID_ARCHE, "arche_lessspeed", -0.40, Operation.MULTIPLY, AttributeType.MOVEMENT_SPEED);
                c = new AttributeModifier(UUID_ARCHE, "arche_stalwart", 0.50, Operation.INCREMENT, AttributeType.KNOCKBACK_RESISTANCE);
                d = race.getLuckValue() != 0 ? new AttributeModifier(UUID_ARCHE, "501_luck", race.getLuckValue(), Operation.INCREMENT, AttributeType.LUCK) : new AttributeModifier(UUID_ARCHE, AttributeType.LUCK);
                e = new AttributeModifier(UUID_ARCHE, "501_lessspeed", -1, Operation.INCREMENT, AttributeType.ATTACK_SPEED);
                f = new AttributeModifier(UUID_ARCHE, "501_moreattack", 6, Operation.INCREMENT, AttributeType.ATTACK_DAMAGE);
                AttributeBase.addModifier(p, a);
                AttributeBase.addModifier(p, b);
                AttributeBase.addModifier(p, c);
                if (race.getLuckValue() == 0) {
                    AttributeBase.removeModifier(p, d);
                } else {
                    AttributeBase.addModifier(p, d);
                }
                AttributeBase.addModifier(p, e);
                AttributeBase.addModifier(p, f);*/
                break;
            case NECROLYTE:
                /*a = new AttributeModifier(UUID_ARCHE, "arche_healthboost", 0.2, Operation.MULTIPLY_ALL, AttributeType.MAX_HEALTH);
                b = new AttributeModifier(UUID_ARCHE, "arche_speed", 0.15, Operation.MULTIPLY, AttributeType.MOVEMENT_SPEED);
                c = new AttributeModifier(UUID_ARCHE, AttributeType.KNOCKBACK_RESISTANCE);
                d = race.getLuckValue() != 0 ? new AttributeModifier(UUID_ARCHE, "501_luck", race.getLuckValue(), Operation.INCREMENT, AttributeType.LUCK) : new AttributeModifier(UUID_ARCHE, AttributeType.LUCK);
                e = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_SPEED);
                f = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_DAMAGE);
                AttributeBase.addModifier(p, a);
                AttributeBase.addModifier(p, b);
                AttributeBase.removeModifier(p, c);
                if (race.getLuckValue() == 0) {
                    AttributeBase.removeModifier(p, d);
                } else {
                    AttributeBase.addModifier(p, d);
                }
                AttributeBase.removeModifier(p, e);
                AttributeBase.removeModifier(p, f);*/
                vals.put(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(UUID_ARCHE, "arche_healthboost", 0.2, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
                vals.put(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID_ARCHE, "arche_speed", 0.15, AttributeModifier.Operation.ADD_SCALAR));
                break;
            case HIGH_ELF:
            case WOOD_ELF:
            case DARK_ELF:
                /*a = new AttributeModifier(UUID_ARCHE, AttributeType.MAX_HEALTH);
                b = new AttributeModifier(UUID_ARCHE, "arche_speed", 0.10, Operation.MULTIPLY, AttributeType.MOVEMENT_SPEED);
                c = new AttributeModifier(UUID_ARCHE, AttributeType.KNOCKBACK_RESISTANCE);
                d = race.getLuckValue() != 0 ? new AttributeModifier(UUID_ARCHE, "501_luck", race.getLuckValue(), Operation.INCREMENT, AttributeType.LUCK) : new AttributeModifier(UUID_ARCHE, AttributeType.LUCK);
                e = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_SPEED);
                f = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_DAMAGE);
                //d = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_DAMAGE);
                AttributeBase.removeModifier(p, a);
                AttributeBase.addModifier(p, b);
                AttributeBase.removeModifier(p, c);
                if (race.getLuckValue() == 0) {
                    AttributeBase.removeModifier(p, d);
                } else {
                    AttributeBase.addModifier(p, d);
                }
                AttributeBase.removeModifier(p, e);
                AttributeBase.removeModifier(p, f);
                //AttributeBase.removeModifier(p, d);*/
                vals.put(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID_ARCHE, "arche_speed", 0.10, AttributeModifier.Operation.ADD_SCALAR));
                break;
            case FOREST_DWARF:
                /*a = new AttributeModifier(UUID_ARCHE, AttributeType.MAX_HEALTH);
                b = new AttributeModifier(UUID_ARCHE, AttributeType.MOVEMENT_SPEED);
                c = new AttributeModifier(UUID_ARCHE, "arche_stalwart", 0.2, Operation.INCREMENT, AttributeType.KNOCKBACK_RESISTANCE);
                d = race.getLuckValue() != 0 ? new AttributeModifier(UUID_ARCHE, "501_luck", race.getLuckValue(), Operation.INCREMENT, AttributeType.LUCK) : new AttributeModifier(UUID_ARCHE, AttributeType.LUCK);
                e = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_SPEED);
                f = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_DAMAGE);
                //d = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_DAMAGE);
                AttributeBase.removeModifier(p, a);
                AttributeBase.removeModifier(p, b);
                AttributeBase.addModifier(p, c);
                if (race.getLuckValue() == 0) {
                    AttributeBase.removeModifier(p, d);
                } else {
                    AttributeBase.addModifier(p, d);
                }
                AttributeBase.removeModifier(p, e);
                AttributeBase.removeModifier(p, f);
                //AttributeBase.removeModifier(p, d);*/
                vals.put(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new AttributeModifier(UUID_ARCHE, "arche_stalwart", 0.2, AttributeModifier.Operation.ADD_NUMBER));
                vals.put(Attribute.GENERIC_LUCK, new AttributeModifier(UUID_ARCHE, "501_moreluck", 0.1, AttributeModifier.Operation.ADD_NUMBER));
                break;
            case CAVE_DWARF:
            case DWARF:
            case MOUNTAIN_DWARF:
            case DARK_DWARF:
                /*a = new AttributeModifier(UUID_ARCHE, "arche_lessspeed", -0.10, Operation.MULTIPLY, AttributeType.MOVEMENT_SPEED);
                b = new AttributeModifier(UUID_ARCHE, "arche_healthboost", 0.2, Operation.MULTIPLY_ALL, AttributeType.MAX_HEALTH);
                c = new AttributeModifier(UUID_ARCHE, "arche_stalwart", 0.2, Operation.INCREMENT, AttributeType.KNOCKBACK_RESISTANCE);
                d = race.getLuckValue() != 0 ? new AttributeModifier(UUID_ARCHE, "501_luck", race.getLuckValue(), Operation.INCREMENT, AttributeType.LUCK) : new AttributeModifier(UUID_ARCHE, AttributeType.LUCK);
                e = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_SPEED);
                f = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_DAMAGE);
                //501warhead - Lowering KBR to 0.15 from 0.30 12/17/2015
                //d = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_DAMAGE);
                AttributeBase.addModifier(p, a);
                AttributeBase.addModifier(p, b);
                AttributeBase.addModifier(p, c);
                if (race.getLuckValue() == 0) {
                    AttributeBase.removeModifier(p, d);
                } else {
                    AttributeBase.addModifier(p, d);
                }
                AttributeBase.removeModifier(p, e);
                AttributeBase.removeModifier(p, f);
                //AttributeBase.removeModifier(p, d);*/
                vals.put(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(UUID_ARCHE, "arche_healthboost", 0.2, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
                vals.put(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new AttributeModifier(UUID_ARCHE, "arche_stalwart", 0.2, AttributeModifier.Operation.ADD_NUMBER));
                vals.put(Attribute.GENERIC_LUCK, new AttributeModifier(UUID_ARCHE, "501_moreluck", 0.1, AttributeModifier.Operation.ADD_NUMBER));
                vals.put(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID_ARCHE, "arche_lessspeed", -0.10, AttributeModifier.Operation.ADD_SCALAR));
                break;
            case HALFLING:
                /*a = new AttributeModifier(UUID_ARCHE, AttributeType.MOVEMENT_SPEED);
                b = new AttributeModifier(UUID_ARCHE, "arche_lesshealth", -8, Operation.INCREMENT, AttributeType.MAX_HEALTH);
                c = new AttributeModifier(UUID_ARCHE, AttributeType.KNOCKBACK_RESISTANCE);
                d = race.getLuckValue() != 0 ? new AttributeModifier(UUID_ARCHE, "501_luck", race.getLuckValue(), Operation.INCREMENT, AttributeType.LUCK) : new AttributeModifier(UUID_ARCHE, AttributeType.LUCK);
                e = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_SPEED);
                f = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_DAMAGE);
                //d = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_DAMAGE);
                AttributeBase.removeModifier(p, a);
                AttributeBase.addModifier(p, b);
                AttributeBase.removeModifier(p, c);
                if (race.getLuckValue() == 0) {
                    AttributeBase.removeModifier(p, d);
                } else {
                    AttributeBase.addModifier(p, d);
                }
                AttributeBase.removeModifier(p, e);
                AttributeBase.removeModifier(p, f);
                //AttributeBase.removeModifier(p, d);*/
                vals.put(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(UUID_ARCHE, "arche_lesshealth", -8, AttributeModifier.Operation.ADD_NUMBER));
                vals.put(Attribute.GENERIC_LUCK, new AttributeModifier(UUID_ARCHE, "501_moreluck", 0.20, AttributeModifier.Operation.ADD_NUMBER));
                break;
            case GOBLIN:
                /*a = new AttributeModifier(UUID_ARCHE, "arche_speed", 0.10, Operation.MULTIPLY, AttributeType.MOVEMENT_SPEED);
                b = new AttributeModifier(UUID_ARCHE, "arche_lesshealth", -4, Operation.INCREMENT, AttributeType.MAX_HEALTH);
                c = new AttributeModifier(UUID_ARCHE, AttributeType.KNOCKBACK_RESISTANCE);
                d = race.getLuckValue() != 0 ? new AttributeModifier(UUID_ARCHE, "501_luck", race.getLuckValue(), Operation.INCREMENT, AttributeType.LUCK) : new AttributeModifier(UUID_ARCHE, AttributeType.LUCK);
                e = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_SPEED);
                f = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_DAMAGE);
                //d = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_DAMAGE);
                AttributeBase.addModifier(p, a);
                AttributeBase.addModifier(p, b);
                AttributeBase.removeModifier(p, c);
                if (race.getLuckValue() == 0) {
                    AttributeBase.removeModifier(p, d);
                } else {
                    AttributeBase.addModifier(p, d);
                }
                AttributeBase.removeModifier(p, e);
                AttributeBase.removeModifier(p, f);
                //AttributeBase.removeModifier(p, d);*/
                vals.put(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(UUID_ARCHE, "arche_lesshealth", -4, AttributeModifier.Operation.ADD_NUMBER));
                vals.put(Attribute.GENERIC_LUCK, new AttributeModifier(UUID_ARCHE, "501_moreluck", 0.20, AttributeModifier.Operation.ADD_NUMBER));
                vals.put(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID_ARCHE, "arche_morespeed", 0.10, AttributeModifier.Operation.ADD_SCALAR));
                break;
            case OLOG:
                /*a = new AttributeModifier(UUID_ARCHE, "arche_healthboost", 0.4, Operation.MULTIPLY_ALL, AttributeType.MAX_HEALTH);
                b = new AttributeModifier(UUID_ARCHE, AttributeType.MOVEMENT_SPEED);
                c = new AttributeModifier(UUID_ARCHE, AttributeType.KNOCKBACK_RESISTANCE);
                d = race.getLuckValue() != 0 ? new AttributeModifier(UUID_ARCHE, "501_luck", race.getLuckValue(), Operation.INCREMENT, AttributeType.LUCK) : new AttributeModifier(UUID_ARCHE, AttributeType.LUCK);
                e = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_SPEED);
                f = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_DAMAGE);
                //d = new AttributeModifier(UUID_ARCHE, "arche_blind", -0.2, Operation.MULTIPLY, AttributeType.ATTACK_DAMAGE);
                AttributeBase.addModifier(p, a);
                AttributeBase.addModifier(p, b);
                AttributeBase.removeModifier(p, c);
                if (race.getLuckValue() == 0) {
                    AttributeBase.removeModifier(p, d);
                } else {
                    AttributeBase.addModifier(p, d);
                }
                AttributeBase.removeModifier(p, e);
                AttributeBase.removeModifier(p, f);*/
                vals.put(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(UUID_ARCHE, "arche_healthboost", 0.4, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
                vals.put(Attribute.GENERIC_LUCK, new AttributeModifier(UUID_ARCHE, "501_lessluck", -0.1, AttributeModifier.Operation.ADD_NUMBER));
                break;
            case ORC:
                /*a = new AttributeModifier(UUID_ARCHE, AttributeType.MAX_HEALTH);
                b = new AttributeModifier(UUID_ARCHE, AttributeType.MOVEMENT_SPEED);
                c = new AttributeModifier(UUID_ARCHE, AttributeType.KNOCKBACK_RESISTANCE);
                d = race.getLuckValue() != 0 ? new AttributeModifier(UUID_ARCHE, "501_luck", race.getLuckValue(), Operation.INCREMENT, AttributeType.LUCK) : new AttributeModifier(UUID_ARCHE, AttributeType.LUCK);
                e = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_SPEED);
                f = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_DAMAGE);
                //d = new AttributeModifier(UUID_ARCHE, "arche_blind", -0.2, Operation.MULTIPLY, AttributeType.ATTACK_DAMAGE);
                AttributeBase.removeModifier(p, a);
                AttributeBase.removeModifier(p, b);
                AttributeBase.removeModifier(p, c);
                if (race.getLuckValue() == 0) {
                    AttributeBase.removeModifier(p, d);
                } else {
                    AttributeBase.addModifier(p, d);
                }
                AttributeBase.removeModifier(p, e);
                AttributeBase.removeModifier(p, f);*/
                vals.put(Attribute.GENERIC_LUCK, new AttributeModifier(UUID_ARCHE, "501_lessluck", -0.1, AttributeModifier.Operation.ADD_NUMBER));
                break;
            case KHARAJYR:
            case KHA_LEPARDA:
            case KHA_CHEETRAH:
            case KHA_PANTERA:
            case KHA_TIGRASI:
                /*a = new AttributeModifier(UUID_ARCHE, "arche_speed", 0.12, Operation.MULTIPLY, AttributeType.MOVEMENT_SPEED);
                b = new AttributeModifier(UUID_ARCHE, "arche_lesshealth", -2, Operation.INCREMENT, AttributeType.MAX_HEALTH);
                c = new AttributeModifier(UUID_ARCHE, AttributeType.KNOCKBACK_RESISTANCE);
                d = race.getLuckValue() != 0 ? new AttributeModifier(UUID_ARCHE, "501_luck", race.getLuckValue(), Operation.INCREMENT, AttributeType.LUCK) : new AttributeModifier(UUID_ARCHE, AttributeType.LUCK);
                e = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_SPEED);
                f = new AttributeModifier(UUID_ARCHE, AttributeType.ATTACK_DAMAGE);
                //d = new AttributeModifier(UUID_ARCHE, "arche_sharp", 0.25, Operation.MULTIPLY, AttributeType.ATTACK_DAMAGE);
                AttributeBase.addModifier(p, a);
                AttributeBase.addModifier(p, b);
                AttributeBase.removeModifier(p, c);
                if (race.getLuckValue() == 0) {
                    AttributeBase.removeModifier(p, d);
                } else {
                    AttributeBase.addModifier(p, d);
                }
                AttributeBase.removeModifier(p, e);
                AttributeBase.removeModifier(p, f);
                //AttributeBase.addModifier(p, d);*/
                vals.put(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID_ARCHE, "arche_speed", 0.12, AttributeModifier.Operation.ADD_SCALAR));
                vals.put(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(UUID_ARCHE, "arche_lesshealth", -2, AttributeModifier.Operation.ADD_NUMBER));
                vals.put(Attribute.GENERIC_LUCK, new AttributeModifier(UUID_ARCHE, "501_moreluck", 0.1, AttributeModifier.Operation.ADD_NUMBER));
                break;
            default: //Basic Humans, Default/Unset
                break;
        }
        reset(p);
        vals.entrySet().stream().forEach(ent -> {
            AttributeInstance inst = p.getAttribute(ent.getKey());
            if (inst != null) {
                inst.addModifier(ent.getValue());
            }
        });
        if (ArcheCore.getPlugin().debugMode()) {
            Logger log = ArcheCore.getPlugin().getLogger();
            log.info("Resultant stats for the user "+p.getName());
            Arrays.asList(Attribute.values()).stream()
                    .forEach(attr -> {
                        AttributeInstance inst = p.getAttribute(attr);
                        if (inst != null) {
                            log.info("Attribute instance is "+inst.getAttribute().name()+" and it's final value is "+inst.getValue());
                            log.info("modifiers are: ");
                            inst.getModifiers().stream().forEach(mod -> log.info("Modifier operation is "+mod.getOperation().name()+" and it's value is "+mod.getAmount()+". Name is "+mod.getName()));
                        }
                    });
        }
    }
}
