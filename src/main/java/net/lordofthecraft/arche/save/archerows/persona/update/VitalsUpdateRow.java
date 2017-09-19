package net.lordofthecraft.arche.save.archerows.persona.update;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.persona.PersonaInventory;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.archerows.ArchePersonaRow;
import net.lordofthecraft.arche.util.MessageUtil;
import net.lordofthecraft.arche.util.SQLUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class VitalsUpdateRow implements ArcheMergeableRow, ArchePersonaRow {

    final Persona persona;
    final UUID world;
    final int x;
    final int y;
    final int z;
    final double health;
    final float saturation;
    final int hunger;
    final PersonaInventory inv;
    final String potions;
    private Connection connection;

    public VitalsUpdateRow(Persona persona, UUID world, int x, int y, int z, double health, float saturation, int hunger, PersonaInventory inv, String potions) {
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
    public boolean isUnique() {
        return true;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return !row.isUnique() && row instanceof VitalsUpdateRow;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        if (second.isUnique()) {
            throw new IllegalArgumentException("Cannot merge a unique row");
        }
        return new MultiVitalsUpdateRow(this, (VitalsUpdateRow) second);
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void executeStatements() throws SQLException {
        /*PreparedStatement statement = connection.prepareStatement("UPDATE persona_vitals SET persona_vitals.world=? AND x=? AND y=? AND z=? AND health=? AND saturation=? AND hunger=? AND persona_vitals.inv=? AND persona_vitals.ender_inv=? AND persona_vitals.potions=? WHERE persona_id_fk=?");
        ArcheCore.getPlugin().getLogger().info("World is "+world.toString()+"... is this correct? Here is the concat: "+world);
        statement.setString(1, String.valueOf(world));
        statement.setInt(2, x);
        statement.setInt(3, y);
        statement.setInt(4, z);
        statement.setDouble(5, health);
        statement.setFloat(6, saturation);
        statement.setInt(7, hunger);
        statement.setString(8, inv == null ? null : inv.getInvAsString());
        statement.setString(9, inv == null ? null : inv.getEnderInvAsString());
        statement.setString(10, potions);
        statement.setInt(11, persona.getPersonaId());
        ArcheCore.getPlugin().getLogger().info("Executing the following statement: "+statement.toString());
        for (String s : getInserts()) {
            ArcheCore.getPlugin().getLogger().info(s);
        }
        statement.executeUpdate();*/
        Statement statement = connection.createStatement();
        for (String insert : getInserts()) {
            statement.executeUpdate(insert);
        }
        statement.close();
    }

    @Override
    public Persona[] getPersonas() {
        return new Persona[]{persona};
    }

    @Override
    public String[] getInserts() {
        return new String[]{
                "UPDATE persona_vitals SET world='" + world + "' AND x=" + x + " AND y=" + y + " AND z=" + z + " AND health=" + health + " AND saturation=" + saturation + " AND hunger=" + hunger + " AND inv='" + SQLUtil.mysqlTextEscape(inv.getInvAsString()) + "' AND ender_inv='" + SQLUtil.mysqlTextEscape(inv.getEnderInvAsString()) + "' AND potions='" + SQLUtil.mysqlTextEscape(potions) + "' WHERE persona_id_fk=" + persona.getPersonaId() + ";"
        };
    }

    @Override
    public String toString() {
        return "VitalsUpdateRow{" +
                "persona=" + MessageUtil.identifyPersona(persona) +
                ", world=" + world +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", health=" + health +
                ", saturation=" + saturation +
                ", hunger=" + hunger +
                ", potions='" + potions + '\'' +
                '}';
    }
}
