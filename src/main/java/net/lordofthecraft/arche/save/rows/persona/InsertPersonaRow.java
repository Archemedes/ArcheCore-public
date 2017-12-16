package net.lordofthecraft.arche.save.rows.persona;

import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.rows.MultiStatementRow;
import net.lordofthecraft.arche.util.WeakBlock;
import org.bukkit.Location;

import java.sql.Timestamp;
import java.util.UUID;

public class InsertPersonaRow extends MultiStatementRow {
    private final int persona;
    private final UUID player;
    private final int slot;
    private final Race race;
    private final String name;
    private final String gender;
    private final boolean current;
    
    private final UUID world;
    private final WeakBlock location;
    private final Timestamp now;

    public InsertPersonaRow(Persona persona, Location l) {
        this.persona = persona.getPersonaId();
        this.player = persona.getPlayerUUID();
        this.slot = persona.getSlot();
        this.race = persona.getRace();
        this.name = persona.getName();
        this.gender = persona.getGender();
        this.current = persona.isCurrent();
        world = l.getWorld().getUID();
        location = new WeakBlock(l);
        now = now();
    }

    @Override
    protected String[] getStatements() {
        return new String[]{
                "INSERT INTO persona(persona_id,player_fk,slot,race,name,gender,curr,last_played) VALUES (?,?,?,?,?,?,?,?)",
                "INSERT INTO persona_stats(persona_id_fk,renamed,date_created) VALUES (?,?,?)",
                "INSERT INTO persona_vitals(persona_id_fk,world,x,y,z) VALUES (?,?,?,?,?)"
        };
    }

    @Override
    protected Object getValueFor(int statement, int index) {
        if (statement == 1) { //The persona statement
            switch (index) {
                case 1: return persona;
                case 2: return player;
                case 3: return slot;
                case 4: return race.name();
                case 5: return name;
                case 6: return gender;
                case 7: return current;
                case 8: return now;
                default: throw new IllegalArgumentException();
            }
        } else if (statement == 2) { //The stats statement
            switch (index) {
                case 1:
                    return persona;
                case 2:
                    return now;
                case 3:
                    return now;
                default:
                    throw new IllegalArgumentException();
            }
        } else { // persona vitals statement
            switch (index) {
            case 1: return persona;
            case 2: return world;
            case 3: return location.getX();
            case 4: return location.getY();
            case 5: return location.getZ();
            default: throw new IllegalArgumentException();
            }
        }
    }

    @Override
    public String toString() {
        return "InsertPersonaRow{" +
                "persona=" + persona +
                ", player=" + player +
                ", slot=" + slot +
                ", race=" + race +
                ", name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", current=" + current +
                ", world=" + world +
                ", location=" + location +
                ", now=" + now +
                '}';
    }
}
