package net.lordofthecraft.arche.save.tasks.persona;

import net.lordofthecraft.arche.persona.PersonaInventory;
import net.lordofthecraft.arche.save.tasks.StatementTask;

import java.sql.SQLException;
import java.util.UUID;

public class UpdateVitalsTask extends StatementTask {

    private final int persona_id;
    private final UUID world;
    private final int x;
    private final int y;
    private final int z;
    private final double health;
    private final float saturation;
    private final int hunger;
    private final PersonaInventory inv;
    private final String potions;

    public UpdateVitalsTask(int persona_id, UUID world, int x, int y, int z, double health, float saturation, int hunger, PersonaInventory inv, String potions) {
        this.persona_id = persona_id;
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
    protected void setValues() throws SQLException {
        stat.setString(1, world.toString());
        stat.setInt(2, x);
        stat.setInt(3, y);
        stat.setInt(4, z);
        stat.setDouble(5, health);
        stat.setFloat(6, saturation);
        stat.setInt(7, hunger);
        stat.setString(8, inv.getInvAsString());
        stat.setString(9, inv.getEnderInvAsString());
        stat.setString(10, potions);
        stat.setInt(11, persona_id);
    }

    @Override
    protected String getQuery() {
        return "UPDATE persona_vitals SET world=? AND x=? AND y=? AND z=? AND health=? AND saturation=? AND hunger=? AND inv=? AND ender_inv=? AND potions=? WHERE persona_id_fk=?";
    }
}
