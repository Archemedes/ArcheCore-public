package net.lordofthecraft.arche.persona;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.WeakBlock;
import net.lordofthecraft.arche.enums.PersonaType;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.event.*;
import net.lordofthecraft.arche.event.PersonaWhoisEvent.Query;
import net.lordofthecraft.arche.interfaces.*;
import net.lordofthecraft.arche.save.SaveHandler;
import net.lordofthecraft.arche.save.tasks.DataTask;
import net.lordofthecraft.arche.save.tasks.skills.SkillDeleteTask;
import net.lordofthecraft.arche.skill.ArcheSkill;
import net.lordofthecraft.arche.skill.ArcheSkillFactory;
import net.lordofthecraft.arche.skin.ArcheSkin;
import net.lordofthecraft.arche.skin.SkinCache;
import net.lordofthecraft.arche.util.MessageUtil;
import net.md_5.bungee.api.chat.*;
import org.apache.commons.lang.time.DateUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import javax.annotation.Nonnull;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public class ArchePersonaHandler implements PersonaHandler {
	private static final ArchePersonaHandler instance = new ArchePersonaHandler();
	private final Map<UUID, ArchePersona[]> personas = new HashMap<>(Bukkit.getServer().getMaxPlayers());
	//private final ArchePersonaExtender extender = new ArchePersonaExtender();
	private SaveHandler buffer = SaveHandler.getInstance();
	private Connection personaConnection = null;
	private boolean displayName = false;
	private PreparedStatement selectStatement = null;
	private Map<Race, Location> racespawns = Maps.newHashMap();

	private boolean preloading = false;

	private ArchePersonaHandler() {
		//Do nothing
	}

	public static ArchePersonaHandler getInstance(){
		return instance;
	}

	public void setPersonaConnection(Connection connection) {
		if (personaConnection == null) {
			personaConnection = connection;
		}
	}

	public boolean isPreloading() {
		return preloading;
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

		int max = ArcheCore.getControls().personaSlots();

		for(int i = max; i > 1; i--){
			if(p.hasPermission("archecore.personas." + i))
				return i;
		}

		return 1;
	}

	@Override
	public Collection<ArchePersona[]> getPersonas(){
		return Collections.unmodifiableCollection(personas.values());
	}

    @Override
    public Optional<ArchePersona> getPersona(int persona_id) {
        return personas.values().stream()
                .flatMap(Arrays::stream)
                .filter(p -> p.getPersonaId() == persona_id)
                .findFirst();
    }

	@Override
	public ArchePersona getPersona(Player p){
		if(p == null) return null;
		ArchePersona[] prs = personas.get(p.getUniqueId());

		if(prs == null) return null;

		for (ArchePersona pr : prs) {
			if (pr != null && pr.isCurrent()) return pr;
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

		for (ArchePersona pr : prs) {
			if (pr != null && pr.isCurrent()) return pr;
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
		if (prs == null) return new ArchePersona[ArcheCore.getControls().personaSlots()];
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
		for (ArchePersona pr : prs) {
			if (pr != null) result++;
		}

		return result;
	}

	@Override
	public boolean switchPersona(final Player p, int id){
		int slots = ArcheCore.getControls().personaSlots();
		if(id < 0 || id > slots) throw new IllegalArgumentException("Only Persona IDs higher than 0 and at most "+slots+" are allowed.");

		ArchePersona before=null;
		ArchePersona[] prs = personas.get(p.getUniqueId());
		ArchePersona after = prs[id];

		PersonaSwitchEvent event = new PersonaSwitchEvent(prs[id]);
		Bukkit.getPluginManager().callEvent(event);

		if(event.isCancelled()) return false;

		for (ArchePersona pr : prs) {
			if (pr != null) {
				boolean setAs = pr.getId() == id;
				if (before == null && pr.current && !setAs) before = pr;
				pr.setCurrent(setAs);
			}
		}

		Bukkit.getPluginManager().callEvent(new PersonaActivateEvent(after, PersonaActivateEvent.Reason.SWITCH));
		if(before != null) Bukkit.getPluginManager().callEvent(new PersonaDeactivateEvent(before, PersonaDeactivateEvent.Reason.SWITCH));

		if(before != null && before != after){
			//Store and switch Persona-related specifics: Location and Inventory.
			before.saveMinecraftSpecifics(p);

			//Transfer fatigue from previous persona to new persona IF previous value was higher
			//This should prevent some alt abuse where players chain their fatigue bars to grind
			if(before.getFatigue() > after.getFatigue()) {
				//This will call a set fatigue event this way
				//Field can be changed directly, but this wont do SQL
				after.setFatigue(before.getFatigue());
			}
		}

		after.restoreMinecraftSpecifics(p);

		//Check if switched-to Persona will require a different skin from storage
		SkinCache cache = ArcheCore.getControls().getSkinCache();
        ArcheSkin skBefore = (before == null ? null : before.getSkin());
        ArcheSkin skAfter = after.getSkin();
        if( skBefore != skAfter ) {
			cache.refreshPlayer(p);
		}

		return true;
	}

	public boolean registerPersona(Player p, ArchePersona persona) {
		ArchePersona[] prs = personas.computeIfAbsent(
				p.getUniqueId(),
				k -> new ArchePersona[ArcheCore.getControls().personaSlots()]
		);

		if (prs[persona.getId()] != null) {
			PersonaRemoveEvent event2 = new PersonaRemoveEvent(prs[persona.getId()], true);
			Bukkit.getPluginManager().callEvent(event2);

			if (event2.isCancelled()) return false;

			//This should no longer be necessary because of unique constraints
			//buffer.put(new DataTask(DataTask.DELETE, "persona", null, prs[id].sqlCriteria));
			buffer.put(new DataTask(DataTask.DELETE, "persona_names", null, prs[persona.getId()].sqlCriteria));

			//delete all skill records
			deleteSkills(prs[persona.getId()]);
			SkinCache.getInstance().clearSkin(prs[persona.getId()]);
		}
		//Add this Persona into its slot
		prs[persona.getId()] = persona;

		//Load skills for the Persona
		for(ArcheSkill s : ArcheSkillFactory.getSkills().values()){
			persona.addSkill(s, null);
		}

		persona.createEmptyTags();

		for(PotionEffect ps : p.getActivePotionEffects())
			p.removePotionEffect(ps.getType());

		//ArcheTask task = new InsertTask(uuid, id, name, race, gender,creationTime);
		//buffer.put(task);

		RaceBonusHandler.apply(p, persona.getRace());
		persona.updateDisplayName(p);

		switchPersona(p, persona.getId()); //This teleport will fail due to the Location being null still

		if (ArcheCore.getControls().teleportNewPersonas()) { //new Personas may get teleported to spawn
			Location to;
			try {
				if (!racespawns.containsKey(persona.getRace())) {
					World w = ArcheCore.getControls().getNewPersonaWorld();
					to = w == null ? p.getWorld().getSpawnLocation() : w.getSpawnLocation();
				} else {
					to = racespawns.get(persona.getRace());
				}
				p.teleport(to);
			}catch (Exception e){
				Bukkit.getLogger().info("Could not tp player to race spawn!");
			}
		}

		return true;
	}

	@Override
	public List<BaseComponent> whois(Persona p, boolean mod) {
		List<BaseComponent> result = Lists.newArrayList();

		if(p == null) return result;

		boolean masked = p.hasTagKey("masked");

		String r = ChatColor.RESET+"";
		String c = ChatColor.BLUE+"";
		String l = ChatColor.GRAY+"";

		result.add(new TextComponent(l + "~~~~ " + r + ((masked) ? p.getName() : p.getPlayerName()) + ((mod && masked) ? l + "(" + p.getPlayerName() + ")" + r : "") + "'s Roleplay Persona" + l + " ~~~~"));

		if (p.getPersonaType() != PersonaType.NORMAL) {
			result.add(new TextComponent(p.getPersonaType().personaViewLine));
		} else if (p.getTotalPlaytime() < ArcheCore.getPlugin().getNewbieProtectDelay()) {
			Player player = ArcheCore.getPlayer(p.getPlayerUUID());
			if(player != null && !player.hasPermission("archecore.persona.nonewbie"))
				result.add(new TextComponent(ChatColor.LIGHT_PURPLE + "((Persona was recently made and can't engage in PvP))"));
			else
				result.add(new TextComponent(p.getPersonaType().personaViewLine));
		} else if (ArcheCore.getPlugin().getNewbieNotificationDelay() > 0 && p.getTotalPlaytime() < 600){
			Player player = ArcheCore.getPlayer(p.getPlayerUUID());
			long age = player == null? Integer.MAX_VALUE : System.currentTimeMillis() - player.getFirstPlayed();
			int mins = (int) (age / DateUtils.MILLIS_PER_MINUTE);
            if (ArcheCore.getPlugin().getNewbieNotificationDelay() > mins && !(player != null && player.hasPermission("archecore.persona.nonewbie")))
                result.add(new TextComponent(ChatColor.AQUA + "((This player is new to the server))"));
			else
				result.add(new TextComponent(p.getPersonaType().personaViewLine));
		} else result.add(new TextComponent(p.getPersonaType().personaViewLine));

		//----End of header----
		//Now we add all the actual relevant Persona tags in a list called subresult.

		List<BaseComponent> subresult = Lists.newArrayList();

		subresult.add(new TextComponent(c + "Name: " + r + p.getName()));

        String race = p.getRaceString(mod);
        if (race != null && !race.isEmpty()) {
            subresult.add(new TextComponent(c + "Race: " + r + race));
        }

		String gender = p.getGender();
		if(gender != null) subresult.add(new TextComponent(c + "Gender: " + r + p.getGender()));

		BaseComponent profession = getProfessionWhois(p);
		if(profession != null) subresult.add(profession);

		String desc = p.getDescription();

		if(desc != null)
			subresult.add(new TextComponent(c + "Description: " + r + desc));

		//Having added EVERYTHING relevant into subresult, we call the event around
		//Plugins are allowed to modify the info in the event tags, though not in the header
		//They can cancel the event also in which case we show nothing (return empty list)
		PersonaWhoisEvent event = new PersonaWhoisEvent(p, subresult, Query.BASIC, mod);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) { //Event is cancelled show nothing
			result = Lists.newArrayList();
		} else {
			result.addAll(subresult);

			//Check if we should show a "click for more..." button
			//Aka check if there is any extended info for this Persona
			List<BaseComponent> extendedWhois = getExtendedWhoisInfo(p, mod);
			event = new PersonaWhoisEvent(p, extendedWhois, Query.EXTENDED_PROBE, mod);
			Bukkit.getPluginManager().callEvent(event);

			if(!event.isCancelled() && !event.getSent().isEmpty()) {
				result.add(new ComponentBuilder("Click for more...")
						.color(MessageUtil.convertColor(ChatColor.GRAY))
						.italic(true)
						.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pers more " + p.getPlayerName() + "@" + p.getId()))
						.event(MessageUtil.hoverEvent(HoverEvent.Action.SHOW_TEXT, "Click to show extended persona information."))
						.create()[0]);
			}
		}

		return result;

	}

	@Override
	public List<BaseComponent> whois(Player p, boolean mod) {
		return whois(getPersona(p), mod);
	}

	private BaseComponent getProfessionWhois(Persona p) {
		String r = ChatColor.RESET+"";
		String b = ChatColor.BLUE+"";

		Skill prof = p.getMainSkill();
		if(prof != null){
			String title = prof.getSkillTier(p).getTitle() + " ";
			if(title.length() == 1) title = "";
			boolean female = p.getGender().equals("Female");
			String profname = prof.getProfessionalName(female);

			return new ComponentBuilder(b + "Profession: " + r + title + profname)
					.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/archehelp " + (profname.equals("None") ? "professions" : profname)))
					.event(MessageUtil.hoverEvent(HoverEvent.Action.SHOW_TEXT, ChatColor.GRAY + "Click for info..."))
					.create()[0];
		} else {
			return null;
		}
	}

	@Override
	public List<BaseComponent> whoisMore(Persona p, boolean mod, boolean self) {
		List<BaseComponent> extendedWhois = getExtendedWhoisInfo(p, mod);
		PersonaWhoisEvent event = new PersonaWhoisEvent(p, extendedWhois, Query.EXTENDED_PROBE, mod);
		Bukkit.getPluginManager().callEvent(event);

		if(event.isCancelled() || event.getSent().isEmpty()) {
			return Lists.newArrayList(); //Event was cancelled, so sent an empty list
		}

		List<BaseComponent> result = Lists.newArrayList();
		if(p == null) return result;

		String r = ChatColor.RESET+"";
		String l = ChatColor.GRAY+"";

		result.add(new TextComponent(l+"~~~~ " + r + p.getPlayerName() + "'s Extended Roleplay Persona" + l + " ~~~~"));
		result.add(new TextComponent(ChatColor.DARK_RED + "((Please remember not to metagame this information))"));

		result.addAll(event.getSent());

		result.add(new ComponentBuilder("Click for less...")
				.color(MessageUtil.convertColor(ChatColor.GRAY))
				.italic(true)
				.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pers view " + p.getPlayerName() + "@" + p.getId()))
				.event(MessageUtil.hoverEvent(HoverEvent.Action.SHOW_TEXT, "Click to show basic persona information."))
				.create()[0]);
		return result;
	}

	private List<BaseComponent> getExtendedWhoisInfo(Persona p, boolean mod){
		//Possible to move 'profession' back here
        List<BaseComponent> components = Lists.newArrayList();
        components.addAll(Arrays.asList(p.getMagics().getMagicText()));
        return components;
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

		ArchePersona[] prs = new ArchePersona[ArcheCore.getControls().personaSlots()];

		boolean hasCurrent = false;
		ResultSet res = null;
		try {
			if (selectStatement == null)
                selectStatement = personaConnection.prepareStatement(personaSelect);
            selectStatement.clearParameters();
			selectStatement.setString(1, p.getUniqueId().toString());
			res = selectStatement.executeQuery();

			while(res.next()){

				ArchePersona persona = buildPersona(res, p);
				if(persona.getId() >= prs.length) {
					Logger logger = ArcheCore.getPlugin().getLogger();
					logger.severe(p.getName() + " has personas that go above the server's set maximum persona count at: " + persona.getId());
					logger.severe("We currently have no good way to solve this. "
							+ "Consider raising the max persona slots in config. "
							+ "Persona has NOT been loaded from the database and is inaccessible");
				}else {
					prs[persona.getId()] = persona;

					if (persona.current) {
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
					Bukkit.getScheduler().scheduleSyncDelayedTask(ArcheCore.getPlugin(), () -> new CreationDialog().makeFirstPersona(p), 30L);
				}
			}
		}else if(!hasCurrent){
			ArcheCore.getPlugin().getLogger().warning("Player " + p.getName() + " logged in with no Persona set as current. Fixing now.");
			for (ArchePersona pr : prs) {
				if (pr != null) {
					pr.setCurrent(true);
					if (!ArcheCore.getPlugin().areRacialBonusesEnabled())
						RaceBonusHandler.reset(p);
					//p.setDisplayName(prs[i].getName()); <--Already done within setCurrent
					break;
				}
			}
		}
	}

	private ArchePersona buildPersona(ResultSet res, OfflinePlayer p) throws SQLException{
		int persona_id = res.getInt("persona_id");
		int slot = res.getInt("slot");
		String name = res.getString("name");
		Race race = Race.valueOf(res.getString("race"));
		String rheader = res.getString("race_header");
        //I just want to say this triggers the fuck out of me.
        //It's not that I really care about gender morality, you can go out and be anything you want to be
        //But if I see literal memes in gender:
        //I'm going to be SO TRIGGERED :FeelsRageMan: REEEEEEEEEEEEEEEEEEEEEEEEEEEEE
        //-501
        String gender = res.getString("gender");
        Timestamp creationTimeMS = res.getTimestamp("date_created");
		String type = res.getString("p_type");
		PersonaType ptype = PersonaType.valueOf(type);

		ArchePersona persona = new ArchePersona(persona_id, p.getUniqueId(), slot, name, race, gender, creationTimeMS, ptype);
		persona.player = p.getName();
		//prs[id] = persona;

		if(rheader != null && !rheader.equals("null") && !rheader.isEmpty()){
			persona.raceHeader = rheader;
		}

		persona.description = res.getString("descr");
		persona.prefix = res.getString("prefix");
		persona.current = res.getBoolean("curr");
		persona.fatigue = res.getInt("fatigue");
		persona.maxFatigue = res.getInt("max_fatigue");
		persona.health = res.getDouble("health");
		persona.food = res.getInt("food");
        persona.saturation = res.getFloat("saturation");

		persona.timePlayed.set(res.getInt("played"));
		persona.charactersSpoken.set(res.getInt("chars"));
		persona.lastRenamed = res.getTimestamp("renamed");
		persona.lastPlayed = res.getTimestamp("last_played");
		//persona.gainsXP = res.getBoolean(15);
		persona.skills.setMainProfession(ArcheSkillFactory.getSkill(res.getString("profession")));
        Optional<Creature> creature = ArcheCore.getMagicControls().summonCreature(res.getString("creature"));
        creature.ifPresent(persona.magics::setCreature);

		String wstr = res.getString("world");
		if(!res.wasNull()){
			UUID wuuid = UUID.fromString(wstr);
			World w = Bukkit.getWorld(wuuid);
			if(w != null){
				int x = res.getInt("x");
				int y = res.getInt("y");
				int z = res.getInt("z");
				persona.location = new WeakBlock(w, x, y, z);
			}
		}

		String invString = res.getString("inv");
		String enderinvString = res.getString("enderinv");
		if(!res.wasNull()){
			try {
                persona.inv = PersonaInventory.restore(invString, enderinvString);
            } catch (InvalidConfigurationException e) {
				ArcheCore.getPlugin().getLogger().severe("Unable to restore Persona Inventory from database: (" + p.getName() + ";" + name + ")");
				e.printStackTrace();
			}
		}

		if (ArcheCore.getControls().usesEconomy()) persona.money = res.getDouble("money");
		persona.pastPlayTime = res.getInt("playtime_past");

		//We now let all Personas load their skills (albeit lazily). Let's do this now
		persona.loadSkills();

		persona.loadMagics();

		persona.loadTags();

		return persona;
	}

	public void initPreload(int range){
		SQLHandler handler = ArcheCore.getPlugin().getSQLHandler();
		long time = System.currentTimeMillis();
		preloading = true;
		try{
			if (selectStatement == null)
                selectStatement = personaConnection.prepareStatement(personaSelect);


			ResultSet res = personaConnection.createStatement().executeQuery("SELECT player,preload_force FROM players");
			while(res.next()){
				UUID uuid = UUID.fromString(res.getString("player"));
				OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);

				//Have we loaded a Persona for this player already?
				//If so we apparently should preload for this player
				ArchePersona prs[] = personas.get(uuid);

				if(prs == null){ //Apparently not, see if we should based on player login time
					long days = (time - p.getLastPlayed()) / (1000L * 3600L * 24L);
					if (days > range && !res.getBoolean("preload_force")) continue; //Player file too old, don't preload

					//Preload, generate a Persona file
					prs = new ArchePersona[ArcheCore.getControls().personaSlots()];
					personas.put(uuid, prs);
				}
				selectStatement.clearParameters();
				selectStatement.setString(1, p.getUniqueId().toString());
				ArchePersona persona = buildPersona(selectStatement.executeQuery(), p);
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
			/*
			 * TODO
			 * Move this out of initPreload, or if presonas aren't preloaded it will throw NPE since racespawns will be null.
			 * - Kowaman
			 * 
			 */
			ResultSet rs;
			try {
				rs = handler.query("SELECT * FROM persona_race_spawns");
				List<String> toRemove = Lists.newArrayList();
				while (rs.next()) {
					Race r = Race.valueOf(rs.getString(1));
					World w = Bukkit.getWorld(rs.getString(2));
                    if (w == null) {
                        toRemove.add(rs.getString(1));
					} else {
						int x = rs.getInt(3);
						int y = rs.getInt(4);
						int z = rs.getInt(5);
						float yaw = rs.getFloat(6);
						Location l = new Location(w, x, y, z, yaw, 0);
						racespawns.put(r, l);
					}
				}
				if (!toRemove.isEmpty()) {
					PreparedStatement stat = handler.getConnection().prepareStatement("DELETE FROM persona_race_spawns WHERE race=?");
					for (String ss : toRemove) {
						stat.setString(1, ss);
						stat.execute();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			preloading = false;
		}
	}

	void deleteSkills(ArchePersona p){
		for(String sname : ArcheSkillFactory.getSkills().keySet()){
			buffer.put(new SkillDeleteTask(sname, p.getPersonaId()));
		}
	}

	public void removeMagic(Magic magic) {
		getPersonas().forEach(pers -> {
			for (Persona p : pers) {
				((ArchePersona) p).removeMagicAttachment(magic);
			}
		});
	}

	@Override
	public List<Persona> getAllActivePersonas() {
		List<Persona> pers = Lists.newArrayList();
		for (Player p : Bukkit.getServer().getOnlinePlayers()){
			if (hasPersona(p)){
				pers.add(getPersona(p));
			}
		}
		return pers;
	}

	@Override
	public double getLuck(@Nonnull Player p) {
		AttributeInstance instance = p.getAttribute(Attribute.GENERIC_LUCK);
		if (instance != null) {
			return instance.getValue();
		} else {
			return 0.0d;
		}
	}

	@Override
	public Map<Race, Location> getRacespawns() {
		return Collections.unmodifiableMap(racespawns);
	}

	//0
	//90
	//-180
	//-90
	public boolean addRaceSpawn(Race r, Location l) {
		boolean erased = false;
		SQLHandler handler = ArcheCore.getPlugin().getSQLHandler();
		if (racespawns.containsKey(r)) {
			removeRaceSpawn(r);
			erased = true;
		}
		float yaw = l.getYaw();
		float newYaw;
		if (Math.abs(yaw) <= 45) {
			newYaw = 0;
		} else if (Math.abs(yaw) >= 90 + 45) {
			newYaw = -180;
		} else if (yaw > 0) {
			newYaw = 90;
		} else {
			newYaw = -90;
		}
		Location loc = new Location(l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ(), newYaw, 0);
		racespawns.put(r, loc);
		Map<String, Object> toIn = Maps.newLinkedHashMap();
		toIn.put("race", r.name());
		toIn.put("world", loc.getWorld().getName());
		toIn.put("x", loc.getBlockX());
		toIn.put("y", loc.getBlockY());
		toIn.put("z", loc.getBlockZ());
		toIn.put("yaw", loc.getYaw());
		handler.insert("persona_race_spawns", toIn);
		return erased;
	}

	public void removeRaceSpawn(Race r) {
		racespawns.remove(r);
		Map<String, Object> map = Maps.newHashMap();
		map.put("race", r.name());
		ArcheCore.getControls().getSQLHandler().remove("persona_race_spawns", map);
		//ArcheCore.getControls().getSQLHandler().execute("DELETE FROM persona_race_spawns WHERE race='" + r.getName() + "'");
	}

}