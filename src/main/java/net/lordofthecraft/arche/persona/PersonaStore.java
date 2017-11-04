package net.lordofthecraft.arche.persona;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
	
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.ArcheTimer;
import net.lordofthecraft.arche.SQL.SQLUtils;
import net.lordofthecraft.arche.enums.PersonaType;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.interfaces.Creature;
import net.lordofthecraft.arche.save.PersonaField;
import net.lordofthecraft.arche.save.PersonaTable;
import net.lordofthecraft.arche.skill.ArcheSkillFactory;
import net.lordofthecraft.arche.util.WeakBlock;

public class PersonaStore {
    private final String personaSelect;
    private final String offlinePersonaSelect;
	
    
    public PersonaStore() {
    	personaSelect = personaSelectStatement(false);
    	offlinePersonaSelect = personaSelectStatement(true);
    }
    
    private int max_persona_id = 0;
    
    private final Map<Integer, ArcheOfflinePersona> allPersonas = new HashMap<>();
	private final Map<UUID, ArchePersona[]> onlinePersonas = new HashMap<>();
	
	private final Map<UUID, ArchePersona[]> pendingBlobs = new ConcurrentHashMap<>();
	private final Map<UUID, Integer> pendingTasks = new ConcurrentHashMap<>();
	
    public Collection<ArcheOfflinePersona> getPersonas() {
        return Collections.unmodifiableCollection(allPersonas.values());
	}
    
    public Collection<ArchePersona> getLoadedPersonas() {
    	Collection<ArchePersona> result = new ArrayList<>();
    	onlinePersonas.values().stream().forEach( ps-> Arrays.stream(ps).filter(Objects::nonNull).forEach(result::add));
        return result;
	}

    public ArcheOfflinePersona getPersonaById(int persona_id) {
    	return allPersonas.get(persona_id);       
    }

	public ArchePersona getPersona(Player p){
		if(p == null) return null;
        ArchePersona[] prs = onlinePersonas.get(p.getUniqueId());

		if(prs == null) return null;
		for (ArchePersona pr : prs) {
			if (pr != null && pr.isCurrent()) return pr;
		}

		return null;
	}
	
	public ArchePersona getPersona(UUID uuid, int id){
        ArchePersona[] prs = onlinePersonas.get(uuid);
        if(prs != null) return prs[id];
		else return null;
	} 
	
	public ArchePersona getPersona(UUID uuid){
        ArchePersona[] prs = onlinePersonas.get(uuid);
		if(prs == null) return null;
		for (ArchePersona pr : prs) {
			if (pr != null && pr.isCurrent()) return pr;
		}
		return null;
	}
	
	public ArchePersona[] getAllPersonas(UUID uuid){
        ArchePersona[] prs = this.onlinePersonas.get(uuid);
        if (prs == null) return new ArchePersona[ArcheCore.getControls().personaSlots()];
		else return prs;
	}
	
	public ArchePersona registerPersona(ArchePersona persona) {
		Player player = persona.getPlayer();
		ArchePersona[] prs = onlinePersonas.get(player.getUniqueId());
		ArchePersona old;
		if(prs == null) {
			prs = new ArchePersona[ArcheCore.getControls().personaSlots()];
			onlinePersonas.put(player.getUniqueId(), prs);
			old = null;
		} else {
			old = prs[persona.getSlot()];
		}
		
		prs[persona.getSlot()] = persona;
		return old;
	}

	public void loadPersonas(String playerName, UUID uuid) { //Run this async	
		ArcheTimer timer = ArcheCore.getPlugin().getMethodTimer();
		if (timer != null) timer.startTiming("Loading Personas of " + playerName);
		
		ArchePersona[] prs = new ArchePersona[ArcheCore.getControls().personaSlots()];
		boolean hasCurrent = false, any = false;

		ResultSet res = null;
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = ArcheCore.getSQLControls().getConnection();
            statement = connection.prepareStatement(personaSelect);
            res = statement.executeQuery();

            while(res.next()) {
            	any = true;
            	ArchePersona blob = buildPersona(res, uuid, playerName);
            	prs[blob.getSlot()] = blob;
            	
            	if(blob.current) {
            		if(!hasCurrent) {
            			hasCurrent = true;
            		}else {
            			ArcheCore.getPlugin().getLogger().warning("Multiple Current Personas for " + playerName);
            			blob.current = false;
            		}
            	}
            }
		} catch (SQLException e1) {e1.printStackTrace();}
		finally{
			SQLUtils.close(res);
			SQLUtils.close(statement);
			SQLUtils.close(connection);
			
			if(any) {
				pendingBlobs.put(uuid, prs);
				int taskId = new BukkitRunnable(){
					@Override public void run() { pendingBlobs.remove(uuid); pendingTasks.remove(uuid); }
				}.runTaskLaterAsynchronously(ArcheCore.getPlugin(), 20*90).getTaskId();
				pendingTasks.put(uuid, taskId);
			}
        }
        
        if (timer != null) timer.stopTiming("Loading Personas of " + playerName);
	}
	
	public int getNextPersonaId() {
		return max_persona_id++;
	}

    public void initMaxPersonaId() {
        Connection connection = ArcheCore.getSQLControls().getConnection();
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.prepareStatement("SELECT MAX(persona_id) AS 'max_persona_id' FROM persona");
            rs = statement.executeQuery();
            if (rs.next()) {
                max_persona_id = rs.getInt(1);
                max_persona_id++;
            } else {
                ArcheCore.getPlugin().getLogger().warning("We could not retrieve the LAST_INSERT_ID for persona,"
                		+ " either there are no personas or there is an error."
                		+ " We'll be starting at 0. This will throw errors if there are actually personas in the Database.");
            }
            rs.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "We failed to set up our persona ID!!! We can't create personas!", e);
        } finally {
        	SQLUtils.close(rs);
        	SQLUtils.close(statement);
        	SQLUtils.close(connection);
        }

        ArcheCore.getPlugin().getLogger().info("[ArchePersonaHandler] Persona ID is now set at " + max_persona_id);
    }
	
	public void preload() {
		ArcheTimer timer = ArcheCore.getPlugin().getMethodTimer();
		if (timer != null) timer.startTiming("Preloading personas");
		
        Connection connection = null;
        PreparedStatement offlineSelectStat = null;
        
        ResultSet res = null;
        try{
            connection = ArcheCore.getSQLControls().getConnection();
            offlineSelectStat = connection.prepareStatement(offlinePersonaSelect);
            res = offlineSelectStat.executeQuery();

            while(res.next()){ //Looping for every player we know to have personas
                UUID uuid = UUID.fromString(res.getString("player_fk"));

                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            	ArcheOfflinePersona offline = buildOfflinePersona(res, player);
            	allPersonas.put(offline.getPersonaId(), offline);
            }
            
        }catch(SQLException e){e.printStackTrace();}
		finally{
            SQLUtils.close(res);
            SQLUtils.close(offlineSelectStat);
            SQLUtils.close(connection);
		}
        
        if (timer != null) timer.stopTiming("Preloading personas");
	}
	
    private ArcheOfflinePersona buildOfflinePersona(ResultSet res, OfflinePlayer pl) throws SQLException {
        int persona_id = res.getInt(PersonaField.PERSONA_ID.field());
        int slot = res.getInt(PersonaField.SLOT.field());
        String name = res.getString(PersonaField.NAME.field());
        boolean current = res.getBoolean(PersonaField.CURRENT.field());
        Race race = Race.valueOf(res.getString(PersonaField.RACE_REAL.field()));
        String gender = res.getString(PersonaField.GENDER.field());
        Timestamp creationTimeMS = res.getTimestamp(PersonaField.STAT_CREATION.field());
        Timestamp lastPlayed = res.getTimestamp(PersonaField.STAT_LAST_PLAYED.field());
        String type = res.getString(PersonaField.TYPE.field());
        PersonaType ptype = PersonaType.valueOf(type);
        ArcheOfflinePersona persona = new ArcheOfflinePersona(new ArchePersonaKey(persona_id, pl.getUniqueId(), slot), creationTimeMS, lastPlayed, current, race, gender, ptype, name);

        String invString = res.getString(PersonaField.INV.field());
        String enderinvString = res.getString(PersonaField.ENDERINV.field());
        persona.inv = PersonaInventory.restore(persona, invString, enderinvString);

        return persona;
    }
	
	private ArchePersona buildPersona(ResultSet res, UUID uuid, String playerName) throws SQLException{
        int persona_id = res.getInt(PersonaField.PERSONA_ID.field());
        int slot = res.getInt(PersonaField.SLOT.field());
		String name = res.getString(PersonaField.NAME.field());
		Race race = Race.valueOf(res.getString(PersonaField.RACE_REAL.field()));
		String rheader = res.getString(PersonaField.RACE.field());
        String gender = res.getString(PersonaField.GENDER.field());
        Timestamp creationTimeMS = res.getTimestamp(PersonaField.STAT_CREATION.field());
		String type = res.getString(PersonaField.TYPE.field());
		PersonaType ptype = PersonaType.valueOf(type);
        Timestamp lastPlayed = res.getTimestamp(PersonaField.STAT_LAST_PLAYED.field());

        ArchePersona persona = new ArchePersona(persona_id, uuid, slot, name, race, gender, creationTimeMS, lastPlayed, ptype);
        persona.player = playerName;

        if(rheader != null && !rheader.equals("null") && !rheader.isEmpty()){
			persona.raceHeader = rheader;
		}

		persona.description = res.getString(PersonaField.DESCRIPTION.field());
		persona.prefix = res.getString(PersonaField.PREFIX.field());
		persona.current = res.getBoolean(PersonaField.CURRENT.field());
		persona.fatigue = res.getInt(PersonaField.FATIGUE.field());
		persona.health = res.getDouble(PersonaField.HEALTH.field());
        persona.food = res.getInt(PersonaField.FOOD.field());
        persona.saturation = res.getFloat(PersonaField.SATURATION.field());

		persona.timePlayed.set(res.getInt(PersonaField.STAT_PLAYED.field()));
		persona.charactersSpoken.set(res.getInt(PersonaField.STAT_CHARS.field()));
		persona.lastRenamed = res.getTimestamp(PersonaField.STAT_RENAMED.field());

		persona.skills.setMainProfession(ArcheSkillFactory.getSkill(res.getString(PersonaField.SKILL_SELECTED.field())));
        Optional<Creature> creature = ArcheCore.getMagicControls().getCreatureById(res.getString("creature"));
        creature.ifPresent(persona.magics::setCreature);

		String wstr = res.getString(PersonaField.WORLD.field());
		if(!res.wasNull()){
			UUID wuuid = UUID.fromString(wstr);
			World w = Bukkit.getWorld(wuuid); //Yes this is thread-safe
			if(w != null){
				int x = res.getInt(PersonaField.X.field());
				int y = res.getInt(PersonaField.Y.field());
				int z = res.getInt(PersonaField.Z.field());
				persona.location = new WeakBlock(w, x, y, z);
			}
		}

		String invString = res.getString(PersonaField.INV.field());
        String enderinvString = res.getString(PersonaField.ENDERINV.field());
        persona.inv = PersonaInventory.restore(persona, invString, enderinvString);
        
        persona.loadPotionsFromString(res.getString(PersonaField.POTIONS.field()));
		if (ArcheCore.getControls().usesEconomy()) persona.money = res.getDouble(PersonaField.MONEY.field());
		persona.pastPlayTime = res.getInt(PersonaField.STAT_PLAYTIME_PAST.field());

        Connection connection = ArcheCore.getSQLControls().getConnection();
        persona.loadMagics(connection);
        persona.loadTags(connection);
        persona.loadAttributes(connection);
        persona.loadSkills(connection);
        persona.loadSkin(connection);
        connection.close();
        
		return persona;
	}
	
	public ArchePersona[] implementPersonas(Player player) {
		UUID uuid = player.getUniqueId();
		ArchePersona[] prs = pendingBlobs.remove(uuid);
		if(prs == null) {
			prs = onlinePersonas.get(uuid);
			if(prs == null) prs = new ArchePersona[ArcheCore.getControls().personaSlots()];
		} else {
			onlinePersonas.put(uuid, prs);
			Integer taskId = pendingTasks.get(uuid);
			Bukkit.getScheduler().cancelTask(taskId);
		}
		return prs;
	}
	
	private String personaSelectStatement(boolean forOffline) {
		List<String> fields = new ArrayList<>();
		if(forOffline) fields.add("player_fk");
		
		Set<PersonaTable> tables = EnumSet.noneOf(PersonaTable.class);
		for(PersonaField field : PersonaField.values()) {
			if(field.isForOfflinePersona() == forOffline) {
				fields.add(field.field());
				if(field.table != PersonaTable.MASTER) tables.add(field.table);
			}
		}
		StringBuilder result = new StringBuilder();
		result.append("SELECT ")
			.append(StringUtils.join(fields, ','))
			.append(" FROM PERSONA ");
		
		tables.stream().map(PersonaTable::getTable)
		.forEach(tab -> result.append(" JOIN ").append(tab)
				.append(" ON persona.persona_id=")
				.append(tab).append(".persona_id_fk")
				);
		if(!forOffline) result.append(" WHERE player_fk=?");
		return result.toString();
	}
}
