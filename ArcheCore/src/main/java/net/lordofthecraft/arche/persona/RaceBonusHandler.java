package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.attributes.AttributeBase;
import net.lordofthecraft.arche.attributes.AttributeModifier;
import net.lordofthecraft.arche.attributes.AttributeType;
import net.lordofthecraft.arche.attributes.Operation;
import net.lordofthecraft.arche.enums.Race;
import org.bukkit.entity.Player;

import java.util.UUID;

public class RaceBonusHandler {

    private static final UUID UUID_ARCHE = UUID.fromString("22a6b068-6e20-4295-87e7-4fd0c176307e");

    public static void reset(Player p) {
        AttributeModifier a, b, c, d, e, f;

        a = new AttributeModifier(UUID_ARCHE, AttributeType.MAX_HEALTH);
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
        //AttributeBase.removeModifier(p, d);
    }


    public static void apply(Player p, Race race) {
        AttributeModifier a, b, c, d, e, f;

        if (race == null) {
            reset(p);
            return;
        }

        if (!ArcheCore.getPlugin().areRacialBonusesEnabled()) return;

        switch (race) {

            case ELF:
                a = new AttributeModifier(UUID_ARCHE, AttributeType.MAX_HEALTH);
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
                //AttributeBase.removeModifier(p, d);
                break;
            case CONSTRUCT:
                a = new AttributeModifier(UUID_ARCHE, "arche_healthboost", 0.5, Operation.MULTIPLY_ALL, AttributeType.MAX_HEALTH);
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
                AttributeBase.addModifier(p, f);
                break;
            case NECROLYTE:
                a = new AttributeModifier(UUID_ARCHE, "arche_healthboost", 0.2, Operation.MULTIPLY_ALL, AttributeType.MAX_HEALTH);
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
                AttributeBase.removeModifier(p, f);
                break;
            case HIGH_ELF:
            case WOOD_ELF:
            case DARK_ELF:
                a = new AttributeModifier(UUID_ARCHE, AttributeType.MAX_HEALTH);
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
                //AttributeBase.removeModifier(p, d);
                break;

            case FOREST_DWARF:
                a = new AttributeModifier(UUID_ARCHE, AttributeType.MAX_HEALTH);
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
                //AttributeBase.removeModifier(p, d);
                break;
            case CAVE_DWARF:
            case DWARF:
            case MOUNTAIN_DWARF:
            case DARK_DWARF:
                a = new AttributeModifier(UUID_ARCHE, "arche_lessspeed", -0.10, Operation.MULTIPLY, AttributeType.MOVEMENT_SPEED);
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
                //AttributeBase.removeModifier(p, d);
                break;
            case HALFLING:
                a = new AttributeModifier(UUID_ARCHE, AttributeType.MOVEMENT_SPEED);
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
                //AttributeBase.removeModifier(p, d);
                break;
            case GOBLIN:
                a = new AttributeModifier(UUID_ARCHE, "arche_speed", 0.10, Operation.MULTIPLY, AttributeType.MOVEMENT_SPEED);
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
                //AttributeBase.removeModifier(p, d);
                break;
            case OLOG:
                a = new AttributeModifier(UUID_ARCHE, "arche_healthboost", 0.4, Operation.MULTIPLY_ALL, AttributeType.MAX_HEALTH);
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
                AttributeBase.removeModifier(p, f);
                break;
            case ORC:
                a = new AttributeModifier(UUID_ARCHE, AttributeType.MAX_HEALTH);
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
                AttributeBase.removeModifier(p, f);
                break;
            case KHARAJYR:
            case KHA_LEPARDA:
            case KHA_CHEETRAH:
            case KHA_PANTERA:
            case KHA_TIGRASI:
                a = new AttributeModifier(UUID_ARCHE, "arche_speed", 0.12, Operation.MULTIPLY, AttributeType.MOVEMENT_SPEED);
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
                //AttributeBase.addModifier(p, d);
                break;
            default: //Basic Humans, Default/Unset
                reset(p);
                break;
        }
    }

    private RaceBonusHandler() {
    }
}
