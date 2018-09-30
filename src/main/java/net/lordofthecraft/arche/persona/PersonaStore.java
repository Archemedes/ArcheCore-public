package net.lordofthecraft.arche.persona;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.ArcheTimer;
import net.lordofthecraft.arche.CoreLog;
import net.lordofthecraft.arche.attributes.ArcheAttribute;
import net.lordofthecraft.arche.attributes.AttributeRegistry;
import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier;
import net.lordofthecraft.arche.enums.PersonaType;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.event.persona.AsyncPersonaLoadEvent;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.save.PersonaField;
import net.lordofthecraft.arche.save.PersonaTable;
import net.lordofthecraft.arche.save.rows.attribute.AttributeRemoveRow;
import net.lordofthecraft.arche.save.rows.persona.UpdatePersonaRow;
import net.lordofthecraft.arche.skill.ArcheSkillFactory;
import net.lordofthecraft.arche.skin.ArcheSkin;
import net.lordofthecraft.arche.skin.SkinCache;
import net.lordofthecraft.arche.util.MessageUtil;
import net.lordofthecraft.arche.util.SQLUtil;
import net.lordofthecraft.arche.util.WeakBlock;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.sql.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class PersonaStore {
    final String personaSelect;
    private final String offlinePersonaSelect;
    private final Semaphore loginThrottle = new Semaphore(2, true);
    
    public PersonaStore() {
        personaSelect = personaSelectStatement(false);
        offlinePersonaSelect = personaSelectStatement(true);
    }
    
    private int max_persona_id = 0;

    private final Map<Integer, ArcheOfflinePersona> allPersonas = new HashMap<>();
    private final Multimap<UUID, ArcheOfflinePersona> offlinePersonas =
    		MultimapBuilder.hashKeys().arrayListValues(ArcheCore.getControls().personaSlots()).build();
    private final Map<UUID, ArchePersona[]> onlinePersonas = new HashMap<>();

    private final Set<UUID> loadedThisSession = ConcurrentHashMap.newKeySet();
    private final Map<UUID, ArchePersona[]> pendingBlobs = new ConcurrentHashMap<>();
    
    public Collection<ArcheOfflinePersona> getPersonas() {
        return Collections.unmodifiableCollection(allPersonas.values());
    }
    
    public ArcheOfflinePersona getPersonaById(int persona_id) {
        return allPersonas.get(persona_id);
    }
    
    public ArcheOfflinePersona getOfflinePersona(UUID player) {
    	return offlinePersonas.get(player).stream()
    	.filter(ArcheOfflinePersona::isCurrent)
    	.findAny().orElse(null);
    }
    
    public ArcheOfflinePersona getOfflinePersona(UUID player, int slot) {
    	return offlinePersonas.get(player).stream()
    	.filter(op->op.getSlot() == slot)
    	.findAny().orElse(null);
    }

    public ArchePersona getPersona(Player p) {
        if (p == null) return null;
        ArchePersona[] prs = onlinePersonas.get(p.getUniqueId());

        if (prs == null) return null;
        for (ArchePersona pr : prs) {
            if (pr != null && pr.isCurrent()) return pr;
        }

        return null;
    }
    
    public Collection<ArchePersona[]> getOnlinePersonas(){
    	return onlinePersonas.entrySet().stream()
    			.filter(entry -> Bukkit.getPlayer(entry.getKey()) != null )
    			.map(Entry::getValue)
    			.collect(Collectors.toList());
    }
    
    public Collection<ArchePersona[]> getOnlineImplementedPersonas(){
    	return Collections.unmodifiableCollection(onlinePersonas.values());
    }
    
    public ArchePersona getPersona(UUID uuid, int id) {
        ArchePersona[] prs = onlinePersonas.get(uuid);
        if (prs != null) return prs[id];
        else return null;
    }

    public ArchePersona getPersona(UUID uuid) {
        ArchePersona[] prs = onlinePersonas.get(uuid);
        if (prs == null) return null;
        for (ArchePersona pr : prs) {
            if (pr != null && pr.isCurrent()) return pr;
        }
        return null;
    }

    public Collection<Persona> getPersonasUnordered(UUID uuid){
    	if(!onlinePersonas.containsKey(uuid)) return Collections.emptyList();
    	return Arrays.stream(onlinePersonas.get(uuid))
    			.filter(Objects::nonNull)
    			.collect(Collectors.toList());
    }
    
    public Collection<OfflinePersona> getOfflinePersonasUnordered(UUID uuid){
    	if(!offlinePersonas.containsKey(uuid)) return Collections.emptyList();
    	return Collections.unmodifiableCollection(offlinePersonas.get(uuid));
    }

    public ArcheOfflinePersona[] getAllOfflinePersonas(UUID uuid) {
    	ArcheOfflinePersona[] result = new ArcheOfflinePersona[ArcheCore.getControls().personaSlots()];
    	offlinePersonas.get(uuid).forEach(aop -> result[aop.getSlot()] = aop);
    	return result;
    }
    
    public ArchePersona[] getAllPersonas(UUID uuid) {
        ArchePersona[] prs = this.onlinePersonas.get(uuid);
        if (prs == null) return new ArchePersona[ArcheCore.getControls().personaSlots()];
        else return prs;
    }

    public void loadPersonas(String playerName, UUID uuid) { //Run this async
        //We don't unload personas for players once loaded since we have memory for miles
        //So instead once a player logged in, they remain loaded for the session
        //We obviously won't have to bother reloading their personas another time then
        if (loadedThisSession.contains(uuid)) return;
        loginThrottle.acquireUninterruptibly();
        
        ArcheTimer timer = ArcheCore.getPlugin().getMethodTimer();
        if (timer != null) timer.startTiming("Loading Personas of " + playerName);

        ArchePersona[] prs = new ArchePersona[ArcheCore.getControls().personaSlots()];
        boolean hasCurrent = false;

        ResultSet res = null;
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = ArcheCore.getSQLControls().getConnection();
            CoreLog.debug("personaSelect: " + personaSelect);
            statement = connection.prepareStatement(personaSelect);
            statement.setString(1, uuid.toString());
            res = statement.executeQuery();

            while (res.next()) {
                ArcheOfflinePersona op = buildOfflinePersona(res, uuid);
                ArchePersona blob = buildPersona(res, op);
                prs[blob.getSlot()] = blob;

                if (blob.current) {
                    if (!hasCurrent) {
                        hasCurrent = true;
                    } else {
                        CoreLog.warning("Multiple Current Personas for " + playerName);
                        blob.current = false;
                    }
                }
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
            if (e1.getMessage().contains("The database file is locked")) {
            	ArcheCore.getPlugin().setDevMode(true);
            }
        } finally {
            SQLUtil.close(res);
            SQLUtil.close(statement);
            SQLUtil.close(connection);

            pendingBlobs.put(uuid, prs);
            loadedThisSession.add(uuid);
        }
        
        loginThrottle.release();
        if (timer != null) timer.stopTiming("Loading Personas of " + playerName);
    }
    
    public boolean isLoadedThisSession(Player p) {
    	return loadedThisSession.contains(p.getUniqueId());
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
                CoreLog.warning("We could not retrieve the LAST_INSERT_ID for persona,"
                        + " either there are no personas or there is an error."
                        + " We'll be starting at 0. This will throw errors if there are actually personas in the Database.");
            }
            rs.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            CoreLog.log(Level.SEVERE, "We failed to set up our persona ID!!! We can't create personas!", e);
        } finally {
            SQLUtil.close(rs);
            SQLUtil.close(statement);
            SQLUtil.close(connection);
        }

        CoreLog.debug("[ArchePersonaHandler] Persona ID is now set at " + max_persona_id);
    }

    public void preload() {
        ArcheTimer timer = ArcheCore.getPlugin().getMethodTimer();
        if (timer != null) timer.startTiming("Preloading personas");
        int amount = 0;
        
        Connection connection = null;
        PreparedStatement offlineSelectStat = null;

        ResultSet res = null;
        try {
            connection = ArcheCore.getSQLControls().getConnection();
            offlineSelectStat = connection.prepareStatement(offlinePersonaSelect);
            res = offlineSelectStat.executeQuery();

            while (res.next()) { //Looping for every player we know to have personas
            	amount++;
                UUID uuid = UUID.fromString(res.getString("player_fk"));

                ArcheOfflinePersona offline = buildOfflinePersona(res, uuid);
                allPersonas.put(offline.getPersonaId(), offline);
                offlinePersonas.put(offline.getPlayerUUID(), offline);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SQLUtil.close(res);
            SQLUtil.close(offlineSelectStat);
            SQLUtil.close(connection);
        }
        CoreLog.debug("Successfully preloaded " + amount + " personas.");
        if (timer != null) timer.stopTiming("Preloading personas");
    }

    private ArcheOfflinePersona buildOfflinePersona(ResultSet res, UUID pUUID) throws SQLException {
        int persona_id = res.getInt(PersonaField.PERSONA_ID.field());
        int slot = res.getInt(PersonaField.SLOT.field());
        String name = res.getString(PersonaField.NAME.field());
        boolean current = res.getBoolean(PersonaField.CURRENT.field());
        Race race = Race.valueOf(res.getString(PersonaField.RACE_REAL.field()));
        String type = res.getString(PersonaField.TYPE.field());
        int birthdate = res.getInt(PersonaField.DATE_OF_BIRTH.field());
        String gender = res.getString(PersonaField.GENDER.field());
        String raceString = res.getString(PersonaField.RACE.field());
        Timestamp creationTimeMS = res.getTimestamp(PersonaField.STAT_CREATION.field());
        Timestamp lastPlayed = res.getTimestamp(PersonaField.STAT_LAST_PLAYED.field());
        int played = res.getInt(PersonaField.STAT_PLAYED.field());
        
        PersonaType ptype = PersonaType.valueOf(type);
        ArcheOfflinePersona persona = new ArcheOfflinePersona(
        		new ArchePersonaKey(persona_id, pUUID, slot),
        		creationTimeMS, lastPlayed, played, current, race,
        		birthdate, gender, ptype, name, raceString);

        String wstr = res.getString(PersonaField.WORLD.field());
        if (!res.wasNull()) {
            UUID wuuid = UUID.fromString(wstr);
            World w = Bukkit.getWorld(wuuid); //Yes this is thread-safe
            if (w != null) {
                int x = res.getInt(PersonaField.X.field());
                int y = res.getInt(PersonaField.Y.field());
                int z = res.getInt(PersonaField.Z.field());
                persona.location = new WeakBlock(w, x, y, z);
            }
        }

        Connection connection = ArcheCore.getSQLControls().getConnection();
        loadTags(persona, connection, true);
        SQLUtil.close(connection);
        return persona;
    }

    ArchePersona buildPersona(ResultSet res, ArcheOfflinePersona op) throws SQLException {

        ArchePersona persona = new ArchePersona(
                op.getPersonaId(),
                op.getPlayerUUID(),
                op.getSlot(),
                op.getName(),
                op.getRace(),
                op.getDateOfBirth(),
                op.getGender(),
                op.getCreationTime(),
                op.lastPlayed,
                op.getTimePlayed(),
                op.getPersonaType(),
                op.getRaceString()
        );

        persona.location = op.location;
        persona.current = op.current;

        persona.description = res.getString(PersonaField.DESCRIPTION.field());
        persona.prefix = res.getString(PersonaField.PREFIX.field());
        persona.fatigue = res.getInt(PersonaField.FATIGUE.field());
        persona.health = res.getDouble(PersonaField.HEALTH.field());
        persona.food = res.getInt(PersonaField.FOOD.field());
        persona.saturation = res.getFloat(PersonaField.SATURATION.field());

        persona.charactersSpoken.set(res.getInt(PersonaField.STAT_CHARS.field()));
        persona.renamed = res.getTimestamp(PersonaField.STAT_RENAMED.field());

        persona.skills().setMainProfession(ArcheSkillFactory.getSkill(res.getString(PersonaField.SKILL_SELECTED.field())));

        String invString = res.getString(PersonaField.INV.field());
        String enderinvString = res.getString(PersonaField.ENDERINV.field());
        persona.inv = PersonaInventory.restore(persona, invString, enderinvString);
        
        persona.loadPotionsFromString(res.getString(PersonaField.POTIONS.field()));
        if (ArcheCore.getControls().usesEconomy()) persona.money = res.getDouble(PersonaField.MONEY.field());
        persona.pastPlayTime = res.getInt(PersonaField.STAT_PLAYTIME_PAST.field());

        int skinId = res.getInt(PersonaField.SKIN.field());
        if (skinId >= 0) {
            ArcheSkin skin = SkinCache.getInstance().getSkinAtSlot(op.getPlayerUUID(), skinId);
            if (skin != null) persona.skin = skin;
            else ArcheCore.getConsumerControls().queueRow(new UpdatePersonaRow(persona, PersonaField.SKIN, -1));
        }

        Connection connection = ArcheCore.getSQLControls().getConnection();
        loadTags(persona, connection, false);
        loadAttributes(persona, connection);
        loadSkills(persona, connection);
        loadNamelog(persona, connection);
        
        Event event = new AsyncPersonaLoadEvent(persona, connection);
        CoreLog.debug("AsyncPersonaLoadEvent firing. Is actually async: " + event.isAsynchronous());
        Bukkit.getPluginManager().callEvent(event);
        if(connection.isClosed()) CoreLog.severe("A consumer of AsyncPersonaLoadEvent is closing the provided connection!!");
        else connection.close();

        return persona;
    }

    private void loadTags(ArcheOfflinePersona persona, Connection c, boolean offline) {
        String sql = "SELECT tag_key,tag_value FROM persona_tags WHERE persona_id_fk=" + persona.getPersonaId()
                + " AND " + (offline ? "" : "NOT ") + "offline";
        try (Statement stat = c.createStatement(); ResultSet rs = stat.executeQuery(sql)) {
            persona.tags.init(rs, offline);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAttributes(ArchePersona persona, Connection c) {
        String sql = "SELECT mod_uuid,attribute_type,mod_name,mod_value,operation,created,decayticks,decaytype,lostondeath FROM persona_attributes WHERE persona_id_fk=" + persona.getPersonaId();

        try (Statement stat = c.createStatement(); ResultSet rs = stat.executeQuery(sql)) {
            AttributeRegistry reg = AttributeRegistry.getInstance();
            while (rs.next()) {
                ArcheAttribute att = reg.getAttribute(rs.getString("attribute_type"));
                UUID id = UUID.fromString(rs.getString("mod_uuid"));
                String name = rs.getString("mod_name");
                double amount = rs.getDouble("mod_value");
                AttributeModifier.Operation op = AttributeModifier.Operation.valueOf(rs.getString("operation"));
                long created = rs.getTimestamp("created").getTime();
                long ticks = rs.getLong("decayticks");
                ExtendedAttributeModifier.Decay decaytype = ExtendedAttributeModifier.Decay.valueOf(rs.getString("decaytype"));
                boolean lostondeath = rs.getBoolean("lostondeath");
                
                boolean decayOffline = decaytype == ExtendedAttributeModifier.Decay.OFFLINE;
                if(decayOffline) ticks -= (System.currentTimeMillis() - created) / 50;
                ExtendedAttributeModifier eam = new ExtendedAttributeModifier(id, name, amount, op, decaytype, ticks, lostondeath);
                
                if(decayOffline) {
                	if(ticks > 200) {
                		eam.setupTask(att, persona);
                	} else { //Don't bother doing all this nonsense on logged in Personas for <10 seconds of mod
                		ArcheCore.getConsumerControls().queueRow(new AttributeRemoveRow(eam, att, persona));
                        CoreLog.debug("Clearing attribute which has decayed while offline: " + persona.attributes().toString(eam));
                		continue; //Not adding it to the list
                	}
                }
                
                CoreLog.debug("SQL-adding attribute: " + persona.attributes().toString(eam));
                persona.attributes().getInstance(att).fromSQL(eam);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSkills(ArchePersona persona, Connection c) {
        String sql = "SELECT skill_id_fk,xp,visible FROM persona_skills WHERE persona_id_fk=" + persona.getPersonaId();
        try (Statement stat = c.createStatement(); ResultSet rs = stat.executeQuery(sql)) {
            while (rs.next()) {
                String skill_id = rs.getString(1);
                double xp = rs.getDouble(2);
                boolean visible = rs.getBoolean(3);
                Skill skill = ArcheSkillFactory.getSkill(skill_id);

                SkillAttachment attach = new SkillAttachment(skill, persona, xp, visible);
                persona.skills().addSkillAttachment(attach);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void loadNamelog(ArchePersona persona, Connection c) {
    	String sql = "SELECT name FROM persona_names WHERE persona_id_fk=" + persona.getPersonaId();
        try (Statement stat = c.createStatement(); ResultSet rs = stat.executeQuery(sql)) {
            while (rs.next()) {
                String name = rs.getString(1);
                persona.namelog.add(name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArchePersona[] implementPersonas(Player player) {
        UUID uuid = player.getUniqueId();
        ArchePersona[] prs = pendingBlobs.remove(uuid);
        
        if (prs == null) {
        	CoreLog.debug("Player " + player.getName() + " logged in without pending Personas." );
        	prs = onlinePersonas.get(uuid);
        	if(prs != null) {
        		CoreLog.debug("Player had online personas files. Likely he rejoined from earlier this session" );
        	} else {
        		CoreLog.severe("Player " + player.getName() + " DOES NOT have a Personas file anywhere! Dependent plugin might be to blame!");
        	}
        } else {
        	onlinePersonas.put(uuid, prs);
        	
            //Pre-populate based on offlinePersonas that are already loaded
            //Pre-loaded personas may come from plugins loading personas
            //Or from personas being newly created while player is offline
            ArchePersona[] preloaded = new ArchePersona[ArcheCore.getControls().personaSlots()];
            offlinePersonas.get(uuid).stream()
            	.filter(OfflinePersona::isLoaded).map(ArcheOfflinePersona::getPersona)
            	.forEach(x->preloaded[x.getSlot()] = x);
        	
            for (int i = 0; i < prs.length; i++) {
            	if(preloaded[i] != null) {
            		prs[i] = preloaded[i];
            		continue;
            	}
                if (prs[i] == null) continue;
                //Method returns input Persona IF input Persona has been used by store as the Persona on record
                //In this case prs[i] is replaced by itself, no actual change
                //Returns other persona if allPersonas already had an online Persona on file
                //In this case the prs[] array takes this on-file one instead of the one that was loaded from SQL
                prs[i] = addOnlinePersona(prs[i]);
            }
        }
        return prs;
    }

    public ArchePersona addOnlinePersona(ArchePersona persona) {
        ArcheOfflinePersona old = allPersonas.get(persona.getPersonaId());
        if (old.isLoaded()) return old.getPersona(); //Persona was force-loaded by ways that isn't player joining
        persona.tags.merge(old.tags);
        allPersonas.put(persona.getPersonaId(), persona);
        
        UUID uuid = persona.getPlayerUUID();
        offlinePersonas.remove(uuid,
        		offlinePersonas.get(uuid).stream()
        		.filter(op -> op.getPersonaId() == persona.getPersonaId())
        		.findAny().get()
        		);
        offlinePersonas.put(uuid, persona);
        return persona;
    }

    public ArcheOfflinePersona registerPersona(ArchePersona persona) {
        UUID uuid = persona.getPlayerUUID();
        ArchePersona[] prs = onlinePersonas.get(uuid);
        ArcheOfflinePersona old;
        if (prs == null) {
        	CoreLog.debug("Registering " + MessageUtil.identifyPersona(persona) + " while player not yet implemented.");
            //No nice array available to check against unfortunately
        	//We resort to using a slightly more intensive method
        	old = offlinePersonas.get(uuid).stream()
        		.filter(x -> x.getSlot() == persona.getSlot())
        		.findAny().orElse(null);
        	
        } else {
            old = prs[persona.getSlot()];
            prs[persona.getSlot()] = persona;
        }
        
        if(old != null) {
        	offlinePersonas.remove(uuid, old);
        	allPersonas.remove(old.getPersonaId());
        }

        allPersonas.put(persona.getPersonaId(), persona);
        offlinePersonas.put(uuid, persona);
        
        return old;
    }
    
    void removePersona(ArcheOfflinePersona toRemove){
    	allPersonas.remove(toRemove.getPersonaId());
    	
    	UUID u = toRemove.getPlayerUUID();
    	offlinePersonas.remove(u, toRemove);
    	ArchePersona[] prs = onlinePersonas.get(u);
    	if(prs != null) prs[toRemove.getSlot()] = null;
    }


    private String personaSelectStatement(boolean forOffline) {
        List<String> fields = new ArrayList<>();
        if (forOffline) fields.add("player_fk");

        Set<PersonaTable> tables = EnumSet.noneOf(PersonaTable.class);
        for (PersonaField field : PersonaField.values()) {
            if (!forOffline || field.isForOfflinePersona()) {
                fields.add(field.field());
                if (field.table != PersonaTable.MASTER) tables.add(field.table);
            }
        }
        StringBuilder result = new StringBuilder();
        result.append("SELECT ")
                .append(StringUtils.join(fields, ','))
                .append(" FROM persona ");

        tables.stream().map(PersonaTable::getTable)
                .forEach(tab -> result.append(" LEFT JOIN ").append(tab)
                        .append(" ON persona.persona_id=")
                        .append(tab).append(".persona_id_fk")
                );
        if (!forOffline) result.append(" WHERE player_fk=?");
        return result.toString();
    }
}
