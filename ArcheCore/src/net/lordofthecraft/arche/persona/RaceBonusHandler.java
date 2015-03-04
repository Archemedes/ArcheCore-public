package net.lordofthecraft.arche.persona;

import java.util.*;
import org.bukkit.entity.*;
import net.lordofthecraft.arche.enums.*;
import net.lordofthecraft.arche.*;
import net.lordofthecraft.arche.attributes.*;

public class RaceBonusHandler
{
    private static final UUID UUID_ARCHE;
    
    public static void reset(final Player p) {
        final AttributeModifier a = new AttributeModifier(RaceBonusHandler.UUID_ARCHE, AttributeType.MAX_HEALTH);
        final AttributeModifier b = new AttributeModifier(RaceBonusHandler.UUID_ARCHE, AttributeType.MOVEMENT_SPEED);
        final AttributeModifier c = new AttributeModifier(RaceBonusHandler.UUID_ARCHE, AttributeType.KNOCKBACK_RESISTANCE);
        AttributeBase.removeModifier((LivingEntity)p, a);
        AttributeBase.removeModifier((LivingEntity)p, b);
        AttributeBase.removeModifier((LivingEntity)p, c);
    }
    
    public static void apply(final Player p, final Race race) {
        if (race == null) {
            reset(p);
            return;
        }
        if (!ArcheCore.getPlugin().areRacialBonusesEnabled()) {
            return;
        }
        switch (race) {
            case ELF: {
                final AttributeModifier a = new AttributeModifier(RaceBonusHandler.UUID_ARCHE, AttributeType.MAX_HEALTH);
                final AttributeModifier b = new AttributeModifier(RaceBonusHandler.UUID_ARCHE, "arche_speed", 0.1, Operation.MULTIPLY, AttributeType.MOVEMENT_SPEED);
                final AttributeModifier c = new AttributeModifier(RaceBonusHandler.UUID_ARCHE, AttributeType.KNOCKBACK_RESISTANCE);
                AttributeBase.removeModifier((LivingEntity)p, a);
                AttributeBase.addModifier((LivingEntity)p, b);
                AttributeBase.removeModifier((LivingEntity)p, c);
                break;
            }
            case FOREST_DWARF: {
                final AttributeModifier a = new AttributeModifier(RaceBonusHandler.UUID_ARCHE, AttributeType.MAX_HEALTH);
                final AttributeModifier b = new AttributeModifier(RaceBonusHandler.UUID_ARCHE, AttributeType.MOVEMENT_SPEED);
                final AttributeModifier c = new AttributeModifier(RaceBonusHandler.UUID_ARCHE, "arche_stalwart", 0.3, Operation.INCREMENT, AttributeType.KNOCKBACK_RESISTANCE);
                AttributeBase.removeModifier((LivingEntity)p, a);
                AttributeBase.removeModifier((LivingEntity)p, b);
                AttributeBase.addModifier((LivingEntity)p, c);
                break;
            }
            case CAVE_DWARF:
            case DWARF:
            case MOUNTAIN_DWARF: {
                final AttributeModifier a = new AttributeModifier(RaceBonusHandler.UUID_ARCHE, "arche_lessspeed", -0.1, Operation.MULTIPLY, AttributeType.MOVEMENT_SPEED);
                final AttributeModifier b = new AttributeModifier(RaceBonusHandler.UUID_ARCHE, "arche_healthboost", 0.2, Operation.MULTIPLY_ALL, AttributeType.MAX_HEALTH);
                final AttributeModifier c = new AttributeModifier(RaceBonusHandler.UUID_ARCHE, "arche_stalwart", 0.3, Operation.INCREMENT, AttributeType.KNOCKBACK_RESISTANCE);
                AttributeBase.addModifier((LivingEntity)p, a);
                AttributeBase.addModifier((LivingEntity)p, b);
                AttributeBase.addModifier((LivingEntity)p, c);
                break;
            }
            case HALFLING: {
                final AttributeModifier a = new AttributeModifier(RaceBonusHandler.UUID_ARCHE, AttributeType.MOVEMENT_SPEED);
                final AttributeModifier b = new AttributeModifier(RaceBonusHandler.UUID_ARCHE, "arche_lesshealth", -8.0, Operation.INCREMENT, AttributeType.MAX_HEALTH);
                final AttributeModifier c = new AttributeModifier(RaceBonusHandler.UUID_ARCHE, AttributeType.KNOCKBACK_RESISTANCE);
                AttributeBase.removeModifier((LivingEntity)p, a);
                AttributeBase.addModifier((LivingEntity)p, b);
                AttributeBase.removeModifier((LivingEntity)p, c);
                break;
            }
            case GOBLIN: {
                final AttributeModifier a = new AttributeModifier(RaceBonusHandler.UUID_ARCHE, "arche_speed", 0.1, Operation.MULTIPLY, AttributeType.MOVEMENT_SPEED);
                final AttributeModifier b = new AttributeModifier(RaceBonusHandler.UUID_ARCHE, "arche_lesshealth", -4.0, Operation.INCREMENT, AttributeType.MAX_HEALTH);
                final AttributeModifier c = new AttributeModifier(RaceBonusHandler.UUID_ARCHE, AttributeType.KNOCKBACK_RESISTANCE);
                AttributeBase.addModifier((LivingEntity)p, a);
                AttributeBase.addModifier((LivingEntity)p, b);
                AttributeBase.removeModifier((LivingEntity)p, c);
                break;
            }
            case OLOG: {
                final AttributeModifier a = new AttributeModifier(RaceBonusHandler.UUID_ARCHE, "arche_healthboost", 0.4, Operation.MULTIPLY_ALL, AttributeType.MAX_HEALTH);
                final AttributeModifier b = new AttributeModifier(RaceBonusHandler.UUID_ARCHE, AttributeType.MOVEMENT_SPEED);
                final AttributeModifier c = new AttributeModifier(RaceBonusHandler.UUID_ARCHE, AttributeType.KNOCKBACK_RESISTANCE);
                AttributeBase.addModifier((LivingEntity)p, a);
                AttributeBase.addModifier((LivingEntity)p, b);
                AttributeBase.removeModifier((LivingEntity)p, c);
                break;
            }
            default: {
                reset(p);
                break;
            }
        }
    }
    
    static {
        UUID_ARCHE = UUID.fromString("22a6b068-6e20-4295-87e7-4fd0c176307e");
    }
}
