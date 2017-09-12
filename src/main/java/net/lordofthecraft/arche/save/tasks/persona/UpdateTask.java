package net.lordofthecraft.arche.save.tasks.persona;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.SQL.ArcheSQLiteHandler;
import net.lordofthecraft.arche.SQL.WhySQLHandler;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.PersonaField;
import net.lordofthecraft.arche.save.tasks.ArcheTask;
import net.lordofthecraft.arche.util.MessageUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class UpdateTask extends ArcheTask {
	private final Persona persona;
	private final PersonaField field;
	private final Object value;
	
	public UpdateTask(Persona persona, PersonaField field, Object value){
		this.persona = persona;
		this.field = field;
		this.value = value;
	}
	
	@Override
	public void run(){
        if (field == PersonaField.PERSONA_ID || field == PersonaField.SLOT) {
            ArcheCore.getPlugin().getLogger().warning("Warning! Attempted to set " + field.field() + " with UpdateTask for " + MessageUtil.identifyPersona(persona) + "! This is not supported!");
            return;
        }
        try{
			Connection c = handle.getConnection();
            c.setAutoCommit(true);
            PreparedStatement stat = field.getStatement(c);
            if (handle instanceof ArcheSQLiteHandler) {
                switch (field) {
                    case PREFIX:
                    case NAME:
                    case RACE:
                    case RACE_REAL:
                    case DESCRIPTION:
                    case TYPE:
                    case GENDER:
                    case WORLD:
                    case INV:
                    case ENDERINV:
                    case SKILL_SELECTED:
                    case POTIONS:
                        stat.setString(1, (String) value);
                        break;
                    case CURRENT:
                        stat.setBoolean(1, (boolean) value);
                        break;
                    case STAT_PLAYED:
                    case STAT_CHARS:
                    case STAT_PLAYTIME_PAST:
                    case ICON:
                    case X:
                    case Y:
                    case Z:
                    case FOOD:
                        stat.setInt(1, (int) value);
                        break;
                    case STAT_RENAMED:
                    case STAT_CREATION:
                        stat.setTimestamp(1, (Timestamp) value);
                        break;
                    case MONEY:
                    case FATIGUE:
                    case MAX_FATIGUE:
                    case HEALTH:
                        stat.setDouble(1, (double) value);
                        break;
                    case SATURATION:
                        stat.setFloat(1, (float) value);
                        break;
                }
            } else {
                stat.setObject(1, value, field.type);
            }
            stat.setInt(2, persona.getPersonaId());
            stat.executeUpdate();
            stat.close();
            if (handle instanceof WhySQLHandler) {
                c.close();
            }

        }catch(SQLException e){e.printStackTrace();}
	}
	

}
