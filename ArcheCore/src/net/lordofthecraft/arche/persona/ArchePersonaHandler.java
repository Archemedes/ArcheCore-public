package net.lordofthecraft.arche.persona;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.WeakBlock;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.SQL.Syntax;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.event.PersonaActivateEvent;
import net.lordofthecraft.arche.event.PersonaCreateEvent;
import net.lordofthecraft.arche.event.PersonaDeactivateEvent;
import net.lordofthecraft.arche.event.PersonaRemoveEvent;
import net.lordofthecraft.arche.event.PersonaSwitchEvent;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaHandler;
import net.lordofthecraft.arche.interfaces.PersonaKey;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.save.SaveHandler;
import net.lordofthecraft.arche.save.tasks.ArcheTask;
import net.lordofthecraft.arche.save.tasks.DataTask;
import net.lordofthecraft.arche.save.tasks.InsertTask;
import net.lordofthecraft.arche.skill.ArcheSkill;
import net.lordofthecraft.arche.skill.ArcheSkillFactory;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.time.DateUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ArchePersonaHandler implements PersonaHandler {
	//private final ArchePersonaExtender extender = new ArchePersonaExtender();
	private SaveHandler buffer = SaveHandler.getInstance();
	private boolean displayName = false;

	private static final ArchePersonaHandler instance = new ArchePersonaHandler();
	private final Map<UUID, ArchePersona[]> personas = new HashMap<UUID, ArchePersona[]>(Bukkit.getServer().getMaxPlayers());

	private PreparedStatement selectStatement = null;

	public static ArchePersonaHandler getInstance(){
		return instance;
	}

	private ArchePersonaHandler(){
		//Do nothing
	}

	@Override
	public void setModifyDisplayNames(boolean will){
		displayName = will;
	}

	@Override
	public boolean willModifyDisplayNames(){
		return displayName;
	}

	@Override
	public boolean mayUse(Player p){
		return p.hasPermission("archecore.mayuse");
	}

	@Override
	public int getAllowedPersonas(Player p){
		if(!mayUse(p)) return 0;

		for(int i = 4; i > 1; i--){
			if(p.hasPermission("archecore.personas." + i))
				return i;
		}

		return 1;
	}

	public Collection<ArchePersona[]> getPersonas(){
		return Collections.unmodifiableCollection(personas.values());
	}

	@Override
	public ArchePersona getPersona(Player p){
		if(p == null) return null;
		ArchePersona[] prs = personas.get(p.getUniqueId());

		if(prs == null) return null;

		for(int i = 0; i < prs.length; i++){
			if(prs[i] != null && prs[i].isCurrent()) return prs[i];
		}

		//ArcheCore.getPlugin().getLogger().warning("Found Player without a current Persona: " + p.getName());
		return null;
	}

	@Override
	public ArchePersona getPersona(PersonaKey key){
		if(key == null) return null;

		return getPersona(key.getPlayerUUID(), key.getPersonaId());
	}

	@Override
	public ArchePersona getPersona(UUID uuid, int id){
		ArchePersona[] prs = personas.get(uuid);
		if(prs != null) return prs[id];
		else return null;
	} 

	public ArchePersona getPersona(UUID uuid){
		ArchePersona[] prs = personas.get(uuid);

		if(prs == null) return null;

		for(int i = 0; i < prs.length; i++){
			if(prs[i] != null && prs[i].isCurrent()) return prs[i];
		}

		//ArcheCore.getPlugin().getLogger().warning("Found Player without a current Persona: " + p.getName());
		return null;
	}

	@Override
	public ArchePersona getPersona(OfflinePlayer p){
		if(p == null) return null;
		return getPersona(p.getUniqueId());
	}

	@Override
	public boolean hasPersona(Player p){
		return getPersona(p) != null;
	}

	@Override
	public ArchePersona[] getAllPersonas(OfflinePlayer p){
		return getAllPersonas(p.getUniqueId());
	}

	@Override
	public ArchePersona[] getAllPersonas(UUID uuid){
		ArchePersona[] prs = this.personas.get(uuid);     
		if (prs == null) return new ArchePersona[4];    
		else return prs;
	}

	@Override
	public int countPersonas(UUID uuid) {
		return countPersonas(getAllPersonas(uuid));
	}

	@Override
	public int countPersonas(Player p){
		return countPersonas(getAllPersonas(p));
	}

	public void unload(UUID uuid){
		Player p = Bukkit.getPlayer(uuid);
		if(p == null){
			personas.remove(uuid);
		}

		if(ArcheCore.getPlugin().debugMode())
			ArcheCore.getPlugin().getLogger().info("[Debug] Unloaded player '" + uuid + "' who was null: " + (p == null) + ". Loaded players now " + personas.size());
	}

	private int countPersonas(ArchePersona[] prs){
		int result = 0;
		for(int i = 0; i < prs.length; i++){
			if(prs[i] != null) result++;
		}

		return result;
	}

	@Override
	public void switchPersona(final Player p, int id){
		if(id < 0 || id > 3) throw new IllegalArgumentException("Only Persona IDs 0-3 are allowed.");

		ArchePersona before=null;
		ArchePersona[] prs = personas.get(p.getUniqueId());
		ArchePersona after = prs[id];

		PersonaSwitchEvent event = new PersonaSwitchEvent(prs[id]);
		Bukkit.getPluginManager().callEvent(event);

		if(event.isCancelled()) return;

		for(int i = 0; i < prs.length; i++){
			if(prs[i] != null){
				boolean setAs = prs[i].getId() == id;
				if(before == null && prs[i].current && !setAs) before = prs[i];
				prs[i].setCurrent(setAs);
			}
		}

		Bukkit.getPluginManager().callEvent(new PersonaActivateEvent(after, PersonaActivateEvent.Reason.SWITCH));
		if(before != null) Bukkit.getPluginManager().callEvent(new PersonaDeactivateEvent(before, PersonaDeactivateEvent.Reason.SWITCH));

		//Store and switch Persona-related specifics: Location and Inventory.
		if(before != null && before != after){
			before.saveMinecraftSpecifics(p);
		}

		after.restoreMinecraftSpecifics(p);
	}


	@Override
	public ArchePersona createPersona(Player p, int id, String name, Race race, int gender, int age, boolean autoAge){

		ArchePersona[] prs = personas.get(p.getUniqueId());
		if(prs == null){
			prs = new ArchePersona[4];
			personas.put(p.getUniqueId(), prs);
		}

		//Check for old Persona
		if(prs[id] != null){
			PersonaRemoveEvent event2 = new PersonaRemoveEvent(prs[id], true);
			Bukkit.getPluginManager().callEvent(event2);

			if(event2.isCancelled()) return null;

			//This should no longer be necessary because of unique constraints
			//buffer.put(new DataTask(DataTask.DELETE, "persona", null, prs[id].sqlCriteria));
			buffer.put(new DataTask(DataTask.DELETE, "persona_names", null, prs[id].sqlCriteria));

			//delete all skill records
			deleteSkills(prs[id]);
		}

		ArchePersona persona = new ArchePersona(p, id, name, race, gender, age);
		persona.autoAge = autoAge;

		PersonaCreateEvent event = new PersonaCreateEvent(persona, prs[id]);
		Bukkit.getPluginManager().callEvent(event);

		//if(event.isCancelled()) return null;

		//Delete old Persona
		if(prs[id] != null){
			//This should no longer be necessary because of unique constraints
			//buffer.put(new DataTask(DataTask.DELETE, "persona", null, prs[id].sqlCriteria));
			buffer.put(new DataTask(DataTask.DELETE, "persona_names", null, prs[id].sqlCriteria));

			//delete all skill records
			deleteSkills(prs[id]);
		}

		//Add this Persona into its slot
		prs[id] = persona;

		//Load skills for the Persona
		for(ArcheSkill s : ArcheSkillFactory.getSkills().values()){
			persona.addSkill(s, null);
		}

		String uuid = p.getUniqueId().toString();
		for(PotionEffect ps : p.getActivePotionEffects())
			p.removePotionEffect(ps.getType());

		ArcheTask task = new InsertTask(uuid, id, name, age, race, gender, autoAge);
		buffer.put(task);

		RaceBonusHandler.apply(p, race);
		persona.updateDisplayName(p);

		switchPersona(p, id); //This teleport will fail due to the Location being null still

		if(ArcheCore.getControls().teleportNewPersonas()){ //new Personas may get teleported to spawn 
			World w = ArcheCore.getControls().getNewPersonaWorld();
			Location to = w == null? p.getWorld().getSpawnLocation() : w.getSpawnLocation();
			p.teleport(to);
		}

		return persona;
	}

	@Override
	public List<String> whois(Persona p){
		List<String> result = Lists.newArrayList();

		if(p == null) return result;

		String r = ChatColor.RESET+"";
		String c = ChatColor.BLUE+"";
		String l = ChatColor.GRAY+"";

		result.add(l+"~~~~ " + r + p.getPlayerName() + "'s Roleplay Persona" + l + " ~~~~");

		if(p.getTimePlayed() < ArcheCore.getPlugin().getNewbieProtectDelay()){
			Player player = ArcheCore.getPlayer(p.getPlayerUUID());
			if(player != null && !player.hasPermission("archecore.persona.nonewbie"))
				result.add(ChatColor.LIGHT_PURPLE + "((Persona was recently made and can't engage in PvP))");
			else
				result.add(ChatColor.DARK_RED + "((Please remember not to metagame this information))");
		} else if (ArcheCore.getPlugin().getNewbieNotificationDelay() > 0 && p.getTimePlayed() < 600){
			Player player = ArcheCore.getPlayer(p.getPlayerUUID());
			long age = player == null? Integer.MAX_VALUE : System.currentTimeMillis() - player.getFirstPlayed();
			int mins = (int) (age / DateUtils.MILLIS_PER_MINUTE);
			if(ArcheCore.getPlugin().getNewbieNotificationDelay() > mins && !player.hasPermission("archecore.persona.nonewbie"))
				result.add(ChatColor.AQUA + "((This player is new to the server))");
			else 
				result.add(ChatColor.DARK_RED + "((Please remember not to metagame this information))");
		} else result.add(ChatColor.DARK_RED + "((Please remember not to metagame this information))");

		result.add(c + "Name: " + r + p.getName());

		String race = p.getRaceString();
		if(!race.equals("Unset")) result.add(c + "Race: " + r + race);

		String gender = p.getGender();
		if(gender != null) result.add(c + "Gender: " + r + p.getGender());

		boolean aa = p.doesAutoAge();
		if(p.getAge() > 0 || aa)
			result.add((aa? c : (ChatColor.DARK_RED)) + "Age: " + r + p.getAge());
		String desc = p.getDescription();

		if(desc != null)
			result.add(c + "Description: " + r + desc);

		Skill prof = p.getMainSkill();

		if(prof != null){
			String title = prof.getSkillTier(p).getTitle();
			result.add(c + "Profession: " + r + title + " " + WordUtils.capitalize(prof.getName()));
		}

		return result;
	}

	@Override
	public List<String> whois(Player p){
		return whois(getPersona(p));
	}

	public void initPlayer(Player p){

		if(personas.containsKey(p.getUniqueId())){//Already present in Persona list (quick relog?)
			ArcheCore plug = ArcheCore.getPlugin();

			if(!plug.willCachePersonas()){
				plug.getLogger().warning("Player " + p.getName() + " logged in while already being registered. Quick relog?");
				plug.getLogger().warning("Currently have " + personas.size() + " persona files for " + Bukkit.getOnlinePlayers().size() + " players." );
			}

			//Maybe player has changed their name with Mojang? Update to be sure
			ArchePersona[] perses = personas.get(p.getUniqueId());
			for(ArchePersona pers : perses){
				if(pers != null) pers.player = p.getName();
			}

			ArchePersona current = getPersona(p);
			if(current != null){
				if(ArcheCore.getPlugin().areRacialBonusesEnabled())
					RaceBonusHandler.apply(p, current.getRace());
				else RaceBonusHandler.reset(p);

				current.updateDisplayName(p);
			} else {
				if(plug.debugMode()) plug.getLogger().info("[DEBUG] Player " +p.getName()+ " was preloaded and did not have current persona.");
				ensureValidPersonaRecord(p, perses, false);
			}

			return;	//Don't need to take further action on this
		}

		ArchePersona[] prs = new ArchePersona[4];

		SQLHandler handler = ArcheCore.getPlugin().getSQLHandler();
		boolean hasCurrent = false;
		ResultSet res = null;
		try {
			if(selectStatement == null) selectStatement = handler.getSQL().getConnection().prepareStatement("SELECT * FROM persona WHERE player = ?");
			selectStatement.setString(1, p.getUniqueId().toString());
			res = selectStatement.executeQuery();

			while(res.next()){

				ArchePersona persona = buildPersona(res, p);
				prs[persona.getId()] = persona;

				if(persona.current){ 
					if(!hasCurrent){
						hasCurrent = true;

						//Update the display name, if enabled
						persona.updateDisplayName(p);

						//CURRENT persona gets racial bonuses applied
						if(ArcheCore.getPlugin().areRacialBonusesEnabled())
							RaceBonusHandler.apply(p, persona.getRace());
						else
							RaceBonusHandler.reset(p);

					} else {
						ArcheCore.getPlugin().getLogger().warning("Player " + p.getName() + " has simultaneous current Personas. Fixing now...");
						persona.setCurrent(false);
					}
				}
			}

		} catch (SQLException e1) {e1.printStackTrace();}
		finally{
			if(res != null){
				try {res.close();} catch (SQLException e) {e.printStackTrace();}
			}

			//See about having no Personas, no current Personas, etc
			ensureValidPersonaRecord(p, prs, hasCurrent);

			//Crucial that this part happens for obvious reasons.
			personas.put(p.getUniqueId(), prs);
		}	
	}

	private void ensureValidPersonaRecord(final Player p, ArchePersona[] prs, boolean hasCurrent){
		if(countPersonas(prs) == 0){
			//Clear Racial Bonuses, if any...
			RaceBonusHandler.reset(p);
			if(p.hasPermission("archecore.mayuse")){
				if(p.hasPermission("archecore.exempt")){
					if(p.hasPermission("archecore.command.beaconme")) p.sendMessage(ChatColor.LIGHT_PURPLE + "No Personas found. Maybe use " + ChatColor.ITALIC + "/beaconme");
				}else{
					if(ArcheCore.getControls().teleportNewPersonas()){
						World w = ArcheCore.getControls().getNewPersonaWorld();
						Location l = w == null? p.getWorld().getSpawnLocation() : w.getSpawnLocation();
						p.teleport(l);
					}
					Bukkit.getScheduler().scheduleSyncDelayedTask(ArcheCore.getPlugin(), new Runnable(){
						@Override
						public void run(){
							new CreationDialog().makeFirstPersona(p);
						}
					}, 30L);
				}
			}
		}else if(!hasCurrent){
			ArcheCore.getPlugin().getLogger().warning("Player " + p.getName() + " logged in with no Persona set as current. Fixing now.");
			for(int i = 0 ; i < prs.length; i++){
				if(prs[i] != null){
					prs[i].setCurrent(true);
					if(!ArcheCore.getPlugin().areRacialBonusesEnabled())
						RaceBonusHandler.reset(p);
					//p.setDisplayName(prs[i].getName()); <--Already done within setCurrent
					break;
				}
			}
		}
	}

	private ArchePersona buildPersona(ResultSet res, OfflinePlayer p) throws SQLException{
		int id = res.getInt(2);
		String name = res.getString(3);
		int age = res.getInt(4);
		Race race = Race.valueOf(res.getString(5));
		String rheader = res.getString(6);
		int gender = res.getInt(7);

		ArchePersona persona = new ArchePersona(p, id, name, race, gender, age);
		//prs[id] = persona;

		if(rheader != null && !rheader.equals("null") && !rheader.isEmpty()){
			persona.raceHeader = rheader;
		}

		persona.description = res.getString(8);
		persona.prefix = res.getString(9);
		persona.current = res.getBoolean(10);

		persona.autoAge = res.getBoolean(11);
		persona.timePlayed.set(res.getInt(12));
		persona.charactersSpoken.set(res.getInt(13));
		persona.lastRenamed = res.getLong(14);
		persona.gainsXP = res.getBoolean(15);
		persona.profession = ArcheSkillFactory.getSkill(res.getString(16));

		String wstr = res.getString(17);
		if(!res.wasNull()){
			UUID wuuid = UUID.fromString(wstr);
			World w = Bukkit.getWorld(wuuid);
			if(w != null){
				int x = res.getInt(18);
				int y = res.getInt(19);
				int z = res.getInt(20);
				WeakBlock loc = new WeakBlock(w, x, y, z);
				persona.location = loc;
			}
		}

		String invString = res.getString(21);
		if(!res.wasNull()){
			try {persona.inv = PersonaInventory.restore(invString);} catch (InvalidConfigurationException e) {e.printStackTrace();}
		}

		if(ArcheCore.getControls().usesEconomy()) persona.money = res.getDouble(22);

		persona.professions[0] = ArcheSkillFactory.getSkill(res.getString(23));
		persona.professions[1] = ArcheSkillFactory.getSkill(res.getString(24));
		persona.professions[2] = ArcheSkillFactory.getSkill(res.getString(25));

		//String skinURL = res.getString(26);
		
		//if(!res.wasNull()){
		//	persona.skin = new PersonaSkin(skinURL);
		//}

		//We now let all Personas load their skills (albeit lazily). Let's do this now
		persona.loadSkills();

		return persona;
	}

	public void initPreload(int range){
		SQLHandler handler = ArcheCore.getPlugin().getSQLHandler();
		long time = System.currentTimeMillis();

		try{
			ResultSet res = handler.query("SELECT * FROM persona");
			while(res.next()){
				UUID uuid = UUID.fromString(res.getString(1));
				OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);

				//Have we loaded a Persona for this player already?
				//If so we apparently should preload for this player
				ArchePersona prs[] = personas.get(uuid);

				if(prs == null){ //Apparently not, see if we should based on player login time
					long days = (time - p.getLastPlayed()) / (1000L * 3600L * 24L);
					if(days > range) continue; //Player file too old, don't preload

					//Preload, generate a Persona file
					prs = new ArchePersona[4];
					personas.put(uuid, prs);
				}

				ArchePersona persona = buildPersona(res, p);
				prs[persona.getId()] = persona;
			}

		}catch(SQLException e){e.printStackTrace();}
		finally{
			for(ArchePersona[] prs : getPersonas()){
				boolean current = false;
				for(ArchePersona p : prs){
					if(p == null) continue;

					if(p.isCurrent()){
						if(!current){
							current = true;
						}else{
							ArcheCore.getPlugin().getLogger().warning("Player " + p.getPlayerName() + " has simultaneous current Personas. Fixing now...");
							p.setCurrent(false);
						}
					}
				}

				if(!current){
					for(ArchePersona p : prs){
						if(p != null){
							ArcheCore.getPlugin().getLogger().warning("Player " + p.getPlayerName() + " was preloaded with no Persona set as current. Fixing now.");
							p.setCurrent(true);
							break;
						}
					}
				}
			}


		}
	}

	void deleteSkills(ArchePersona p){
		for(String sname : ArcheSkillFactory.getSkills().keySet()){
			buffer.put(new DataTask(DataTask.DELETE, "sk_" + sname, null, p.sqlCriteria));

		}
	}

	@Override
	public void ageUs(){
		Map<String, Object> crits = Maps.newHashMap();
		crits.put("autoage", 1);

		Map<String, Object> vals = Maps.newHashMap();
		vals.put("age", new Syntax("age+1"));

		DataTask s = new DataTask(DataTask.UPDATE, "persona", vals, crits);
		buffer.put(s);

		for(ArchePersona[] prs : getPersonas()){
			if(prs == null) continue;
			for(ArchePersona p : prs){
				if(p == null) continue;
				if(p.doesAutoAge()){
					p.age++;
				}
			}
		}
	}

}