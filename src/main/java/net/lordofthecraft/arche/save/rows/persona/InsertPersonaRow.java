package net.lordofthecraft.arche.save.rows.persona;

import java.sql.Timestamp;
import java.util.UUID;

import org.bukkit.Location;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.rows.MultiStatementRow;
import net.lordofthecraft.arche.util.WeakBlock;

public class InsertPersonaRow extends MultiStatementRow {
    private final Persona persona;
    private final UUID world;
    private final WeakBlock location;
    private final Timestamp now;

    public InsertPersonaRow(Persona persona, Location l) {
        this.persona = persona;
        world = l.getWorld().getUID();
        location = new WeakBlock(l);
        now = now();
    }

    @Override
    protected String[] getStatements() {
        return new String[]{
                "INSERT INTO persona(persona_id,player_fk,slot,race,name,gender,last_played) VALUES (?,?,?,?,?,?,?)",
                "INSERT INTO persona_stats(persona_id_fk,renamed,date_created) VALUES (?,?,?)",
                "INSERT INTO persona_vitals(persona_id_fk,world,x,y,z) VALUES (?,?,?,?,?)"
        };
    }

    @Override
    protected Object getValueFor(int statement, int index) {
        if (statement == 1) { //The persona statement
            switch (index) {
                case 1:
                    return persona.getPersonaId();
                case 2:
                    return persona.getPlayerUUID();
                case 3:
                    return persona.getSlot();
                case 4:
                    return persona.getRace().name();
                case 5:
                    return persona.getName();
                case 6:
                    return persona.getGender();
                case 7:
                    return now;
                default:
                    throw new IllegalArgumentException();
            }
        } else if (statement == 2) { //The stats statement
            switch (index) {
                case 1:
                    return persona.getPersonaId();
                case 2:
                    return now;
                case 3:
                    return now;
                default:
                    throw new IllegalArgumentException();
            }
        } else { // persona vitals statement
            switch (index) {
            case 1: return persona.getPersonaId();
            case 2: return world;
            case 3: return location.getX();
            case 4: return location.getY();
            case 5: return location.getZ();
            default: throw new IllegalArgumentException();
            }
        }
    }

}
