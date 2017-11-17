package net.lordofthecraft.arche.save.rows.persona;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.persona.PersonaInventory;
import net.lordofthecraft.arche.save.rows.SingleStatementRow;

import java.util.UUID;

public class UpdateVitalsRow extends SingleStatementRow {
    private final Persona persona;
    private final UUID world;
    private final int x;
    private final int y;
    private final int z;
    private final double health;
    private final float saturation;
    private final int hunger;
    private final PersonaInventory inv;
    private final String potions;

    public UpdateVitalsRow(Persona persona, UUID world, int x, int y, int z, double health, float saturation, int hunger, PersonaInventory inv, String potions) {
        this.persona = persona;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.health = health;
        this.saturation = saturation;
        this.hunger = hunger;
        this.inv = inv;
        this.potions = potions;
    }

    @Override
    protected String getStatement() {
        return "UPDATE persona_vitals SET world=?, x=?, y=?, z=?, health=?, saturation=?, hunger=?, inv=?, ender_inv=?, potions=? WHERE persona_id_fk=?";
    }

    @Override
    protected Object getValueFor(int index) {
        switch (index) {
            case 1:
                return world.toString();
            case 2:
                return x;
            case 3:
                return y;
            case 4:
                return z;
            case 5:
                return health;
            case 6:
                return saturation;
            case 7:
                return hunger;
            case 8:
                return inv == null ? null : inv.getInvAsString();
            case 9:
                return inv == null ? null : inv.getEnderInvAsString();
            case 10:
                return potions;
            case 11:
                return persona.getPersonaId();
            default:
                throw new IllegalArgumentException();
        }
    }
}
