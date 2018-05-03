package net.lordofthecraft.arche.persona;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.DateUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.CoreLog;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.SQL.WhySQLHandler;
import net.lordofthecraft.arche.attributes.AttributeRegistry;
import net.lordofthecraft.arche.enums.PersonaType;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.event.persona.PersonaActivateEvent;
import net.lordofthecraft.arche.event.persona.PersonaCreateEvent;
import net.lordofthecraft.arche.event.persona.PersonaDeactivateEvent;
import net.lordofthecraft.arche.event.persona.PersonaRemoveEvent;
import net.lordofthecraft.arche.event.persona.PersonaSwitchEvent;
import net.lordofthecraft.arche.event.persona.PersonaWhoisEvent;
import net.lordofthecraft.arche.event.persona.PersonaWhoisEvent.Query;
import net.lordofthecraft.arche.interfaces.IConsumer;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaHandler;
import net.lordofthecraft.arche.interfaces.PersonaTags;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.save.PersonaField;
import net.lordofthecraft.arche.save.rows.persona.DeletePersonaRow;
import net.lordofthecraft.arche.save.rows.persona.InsertPersonaRow;
import net.lordofthecraft.arche.save.rows.persona.UpdatePersonaRow;
import net.lordofthecraft.arche.skin.ArcheSkin;
import net.lordofthecraft.arche.skin.SkinCache;
import net.lordofthecraft.arche.util.MessageUtil;
import net.lordofthecraft.arche.util.SQLUtil;
import net.lordofthecraft.arche.util.WeakBlock;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ArchePersonaHandler implements PersonaHandler {
	private static final ArchePersonaHandler instance = new ArchePersonaHandler();

    private final PersonaStore store = new PersonaStore();
    private final IConsumer consumer = ArcheCore.getControls().getConsumer();
	private boolean displayName = false;
	private Map<Race, Location> racespawns = Maps.newHashMap();

	private ArchePersonaHandler() {
		//Do nothing
	}

	public static ArchePersonaHandler getInstance(){
		return instance;
	}

    public PersonaStore getPersonaStore() {
        return store;
    }

    public void onEnable() {
        store.initMaxPersonaId();
        store.preload();
        this.initRacespawns();
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
    public Collection<ArcheOfflinePersona> getPersonas() {
        return store.getPersonas();
    }

    @Override
    public ArcheOfflinePersona getPersonaById(int persona_id) {
        return store.getPersonaById(persona_id);
    }

	@Override
	public ArchePersona getPersona(Player p){
        return store.getPersona(p);
    }

	@Override
	public ArchePersona getPersona(UUID uuid, int id){
        return store.getPersona(uuid, id);
    }

	public ArchePersona getPersona(UUID uuid){
        return store.getPersona(uuid);
    }

	@Override
	public ArcheOfflinePersona getOfflinePersona(UUID player) {
		return store.getOfflinePersona(player);
	}
	
	@Override
	public ArcheOfflinePersona getOfflinePersona(UUID player, int slot) {
		return store.getOfflinePersona(player, slot);
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
	public ArchePersona[] getAllPersonas(Player p){
		return getAllPersonas(p.getUniqueId());
	}
	
	@Override
	public ArcheOfflinePersona[] getAllPersonas(OfflinePlayer p){
		return getAllOfflinePersonas(p.getUniqueId());
	}

	@Override
	public ArcheOfflinePersona[] getAllOfflinePersonas(UUID uuid){
        return store.getAllOfflinePersonas(uuid);
    }
	
	@Override
	public ArchePersona[] getAllPersonas(UUID uuid){
        return store.getAllPersonas(uuid);
    }
	
	@Override
	public Collection<Persona> getPersonasUnordered(Player p){
		return store.getPersonasUnordered(p.getUniqueId());
	}
	
	@Override
	public Collection<OfflinePersona> getPersonasUnordered(OfflinePlayer p){
		return store.getOfflinePersonasUnordered(p.getUniqueId());
	}
	
	@Override
	public Collection<OfflinePersona> getPersonasUnordered(UUID uuid){
		return store.getOfflinePersonasUnordered(uuid);
	}


	@Override
	public int countPersonas(UUID uuid) {
		return countPersonas(getAllPersonas(uuid));
	}

	@Override
	public int countPersonas(Player p){
		return countPersonas(getAllPersonas(p));
	}

	private int countPersonas(ArchePersona[] prs){
		if(prs == null) return 0;
		
		int result = 0;
		for (ArchePersona pr : prs) {
			if (pr != null) result++;
		}

		return result;
	}

	@Override
	public boolean switchPersona(final Player p, int id, boolean force){
		int slots = ArcheCore.getControls().personaSlots();
        Validate.isTrue(id >= 0 && id < slots, "Only Persona IDs at least 0 and at most " + slots + " are allowed.");

        ArchePersona[] prs = getAllPersonas(p.getUniqueId());
        ArchePersona after = prs[id];

		PersonaSwitchEvent event = new PersonaSwitchEvent(prs[id], force);
		Bukkit.getPluginManager().callEvent(event);

		if(event.isCancelled() && !force) return false;

		after.setCurrent(true);
        
        ArchePersona before = (ArchePersona) event.getOriginPersona();
        if (before != null) {
            Validate.isTrue(before != after, "Player tried to switch to same persona!");
            before.setCurrent(false);
            before.endSession();
            Bukkit.getPluginManager().callEvent(new PersonaDeactivateEvent(before, PersonaDeactivateEvent.Reason.SWITCH));
            ArcheCore.getConsumerControls().queueRow(new UpdatePersonaRow(before, PersonaField.STAT_LAST_PLAYED, new Timestamp(System.currentTimeMillis())));
            
            //Store and switch Persona-related specifics: Location and Inventory.
            before.saveMinecraftSpecifics(p);
            before.attributes().handleSwitch(false);
            
			//Transfer fatigue from previous persona to new persona IF previous value was higher
			//This should prevent some alt abuse where players chain their fatigue bars to grind
			if(before.getFatigue() > after.getFatigue()) {
				//This will call a set fatigue event this way
				//Field can be changed directly, but this wont do SQL
				after.setFatigue(before.getFatigue());
			}
		}

        activate(after);
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

    public int getNextPersonaId() {
        return store.getNextPersonaId();
    }

    @Override
    public void registerPersona(Persona pers) {
    	ArchePersona persona = (ArchePersona) pers;
    	CoreLog.debug("Persona is being created: " + MessageUtil.identifyPersona(persona));

    	ArcheOfflinePersona oldPersona = store.registerPersona(persona);

    	PersonaCreateEvent event3 = new PersonaCreateEvent(persona, oldPersona);
    	Bukkit.getPluginManager().callEvent(event3);

    	Player p = persona.getPlayer();
    	boolean forceSwitch = oldPersona != null && oldPersona.isCurrent(); 
    	//Expected switch restoreMinecraftSpecifics behavior:
    	//health, saturation, hunger set to persona defaults
    	//Inventories, potion effects cleared.
    	//This teleport will fail due to the Location being null still
    	boolean couldSwitch = p == null? false : switchPersona(p, persona.getSlot(), forceSwitch);

    	if (oldPersona != null) {
    		PersonaRemoveEvent event2 = new PersonaRemoveEvent(oldPersona, true);
    		Bukkit.getPluginManager().callEvent(event2);

    		consumer.queueRow(new DeletePersonaRow(oldPersona));
    		if(oldPersona.isLoaded()) SkinCache.getInstance().clearSkin(oldPersona.getPersona());
    	}

    	Location to = racespawns.get(persona.getRace());
    	if(to == null) {
    		World w = ArcheCore.getControls().getNewPersonaWorld();
    		to = w != null? w.getSpawnLocation() : 
    			p != null? p.getWorld().getSpawnLocation() : 
    				Bukkit.getWorlds().get(0).getSpawnLocation();
    	}

    	consumer.queueRow(new InsertPersonaRow(persona, to));
    	persona.location = new WeakBlock(to);
    	if(couldSwitch && ArcheCore.getControls().teleportNewPersonas()) p.teleport(to);
    }

	@Override
    public List<BaseComponent> whois(OfflinePersona op, CommandSender whosAsking) {
        List<BaseComponent> result = Lists.newArrayList();

        if (op == null) return result;

        String r = ChatColor.RESET + "";
        String c = ChatColor.BLUE + "";
        String u = ChatColor.DARK_GRAY + "";
        boolean mod = whosAsking.hasPermission("archecore.admin") || whosAsking.hasPermission("archecore.mod.other");
        result.add(getPersonaHeader(op, mod));
        result.add(getPersonaTypeHeader(op, whosAsking));

        //Now we add all the actual relevant Persona tags in a list called subresult.
        List<BaseComponent> subresult = Lists.newArrayList();

        boolean disguised = op.isLoaded()? !canPerceive(op.getPersona(), whosAsking) : false;
        if(!disguised || mod) {
        	subresult.add(new TextComponent(c + "Name: " + r + op.getName()));
        	int birthyear = op.getDateOfBirth();
        	int age = op.getAge();
        	if(birthyear > 0) subresult.add(new TextComponent(c + "Age: " + r + age + u + " (born in " + r + birthyear + u + ")"));

        	String gender = op.getGender();
        	if (gender != null && !"Other".equals(gender)) subresult.add(new TextComponent(c + "Gender: " + r + op.getGender()));

        	if (op.isLoaded()) {
        		Persona p = op.getPersona();
        		String race = p.getRaceString(mod);
        		if (race != null && !race.isEmpty()) {
        			subresult.add(new TextComponent(c + "Race: " + r + race));
        		}

        		BaseComponent profession = getProfessionWhois(p);
        		if (profession != null) subresult.add(profession);

        		String desc = p.getDescription();

        		if (desc != null)
        			subresult.add(new TextComponent(c + "Description: " + r + desc));
        	}
        }
        
        //Having added EVERYTHING relevant into subresult, we call the event around
        //Plugins are allowed to modify the info in the event tags, though not in the header
        //They can cancel the event also in which case we show nothing (return empty list)
        PersonaWhoisEvent event = new PersonaWhoisEvent(op, whosAsking, subresult, Query.BASIC, mod, disguised);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) { //Event is cancelled show nothing
            result = Lists.newArrayList();
        } else {
            result.addAll(subresult);

            //Check if we should show a "click for more..." button
            //Aka check if there is any extended info for this Persona
            List<BaseComponent> extendedWhois = Lists.newArrayList(); //Can need an EXTENDED WHOIS
            event = new PersonaWhoisEvent(op, whosAsking, extendedWhois, Query.EXTENDED_PROBE, mod, disguised);
            Bukkit.getPluginManager().callEvent(event);
            
            if (!event.isCancelled() && !event.getSent().isEmpty()) {
                result.add(new ComponentBuilder("Click for more...")
                        .color(MessageUtil.convertColor(ChatColor.GRAY))
                        .italic(true)
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pers more " + op.getPlayerName() + "@" + op.getSlot()))
                        .event(MessageUtil.hoverEvent(HoverEvent.Action.SHOW_TEXT, "Click to show extended persona information."))
                        .create()[0]);
            }
        }

		return result;
	}

	@Override
	public List<BaseComponent> whois(Player p, Player whosAsking) {
		return whois(getPersona(p), whosAsking);
	}

	private BaseComponent getPersonaHeader(OfflinePersona op, boolean mod) {
        String r = ChatColor.RESET + "";
        String l = ChatColor.GRAY + "";
        String u = ChatColor.DARK_GRAY + "";
		
		return new TextComponent(l + "~~~~ " + r + op.getPlayerName()
        		+ "'s Roleplay Persona" + (mod? u+"(id:"+op.getPersonaId()+")" : "") + l + " ~~~~");
	}
	
	private boolean canPerceive(Persona hider, CommandSender viewer) {
		if(!(viewer instanceof Player)) return true; //Console is omniscient
		Player p = (Player) viewer;
		if(p == hider.getPlayer()) return true; //Can always see own personas
		Persona ps = ArcheCore.getPersona(p);
		if(ps == null) return false;
		
		double shroud = hider.attributes().getAttributeValue(AttributeRegistry.SHROUD);
		double perception = ps.attributes().getAttributeValue(AttributeRegistry.PERCEPTION);
		
		int baseChance = (int) (shroud - perception);
		if(baseChance <= 0) return true;
		
		long today = System.currentTimeMillis() / TimeUnit.DAYS.toMillis(1);
		long seed = (hider.getPersonaId() * 13l) + (ps.getPersonaId() * 3l) + today;
		int roll = new Random(seed).nextInt(100);
		
		return roll>=baseChance;
	}
	
	private BaseComponent getPersonaTypeHeader(OfflinePersona op, CommandSender whosAsking) {
		Persona p = op.getPersona();
		if (op.getPersonaType() != PersonaType.NORMAL) {
			return new TextComponent(op.getPersonaType().personaViewLine);
		} else if (op.isLoaded() && p.getTotalPlaytime() < ArcheCore.getPlugin().getNewbieProtectDelay()) {
			Player player = ArcheCore.getPlayer(op.getPlayerUUID());
			if (player != null && !player.hasPermission("archecore.persona.nonewbie"))
				return new TextComponent(ChatColor.LIGHT_PURPLE + "((Persona was recently made and can't engage in PvP))");
			else
				return new TextComponent(op.getPersonaType().personaViewLine);
		} else if (op.isLoaded() && ArcheCore.getPlugin().getNewbieNotificationDelay() > 0 && p.getTotalPlaytime() < 600) {
			OfflinePlayer player = ArcheCore.getPlayer(op.getPlayerUUID());
			long age = player == null ? Integer.MAX_VALUE : System.currentTimeMillis() - player.getFirstPlayed();
			int mins = (int) (age / DateUtils.MILLIS_PER_MINUTE);
			if (ArcheCore.getPlugin().getNewbieNotificationDelay() > mins && !(player != null && player.isOnline() && player.getPlayer().hasPermission("archecore.persona.nonewbie")))
				return new TextComponent(ChatColor.AQUA + "((This player is new to the server))");
			else
				return new TextComponent(op.getPersonaType().personaViewLine);
		} else if(op.isLoaded() && op.getPersona().getPlayer() != whosAsking && !canPerceive(op.getPersona(), whosAsking)) {
			return new TextComponent(ChatColor.DARK_GRAY + "((Persona is disguised and cannot be recognized by you))");
		} else if(op.isLoaded() && op.getPersona().getPlayer() != whosAsking && op.getPersona().attributes().getAttributeValue(AttributeRegistry.SHROUD) > 0) {
			return new TextComponent(ChatColor.GRAY + "((Persona unsuccessfully disguised themselves from you))");
		}else return new TextComponent(op.getPersonaType().personaViewLine);
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
	public List<BaseComponent> whoisMore(OfflinePersona p, CommandSender whosAsking) {
		List<BaseComponent> extendedWhois = Lists.newArrayList();
    boolean mod = whosAsking.hasPermission("archecore.admin") || whosAsking.hasPermission("archecore.mod.other");
    boolean disguised = p.isLoaded()? canPerceive(p.getPersona(), whosAsking) : false;

		PersonaWhoisEvent event = new PersonaWhoisEvent(p, whosAsking, extendedWhois, Query.EXTENDED, mod, disguised);
		Bukkit.getPluginManager().callEvent(event);

		if(event.isCancelled() || event.getSent().isEmpty()) {
			return Lists.newArrayList(); //Event was cancelled, so sent an empty list
		}

		List<BaseComponent> result = Lists.newArrayList();

		result.add(getPersonaHeader(p, mod));
		result.add(getPersonaTypeHeader(p, whosAsking));

		result.addAll(event.getSent());

		result.add(new ComponentBuilder("Click for less...")
				.color(MessageUtil.convertColor(ChatColor.GRAY))
				.italic(true)
				.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pers view " + p.getPlayerName() + "@" + p.getSlot()))
				.event(MessageUtil.hoverEvent(HoverEvent.Action.SHOW_TEXT, "Click to show basic persona information."))
				.create()[0]);
		return result;
	}

	public void loadPlayer(UUID uuid, String playerName) {
		store.loadPersonas(playerName, uuid);
	}

	public void joinPlayer(Player p) {
		ArchePersona[] prs = store.implementPersonas(p);
		RaceBonusHandler.reset(p);
		ArcheCore.getPlugin().updateNameMap(p);

		if (countPersonas(prs) == 0) {
			if (p.hasPermission("archecore.mayuse")) {
				if (p.hasPermission("archecore.exempt")) {
					if (p.hasPermission("archecore.command.beaconme"))
						p.sendMessage(ChatColor.LIGHT_PURPLE + "No Personas found. Maybe use " + ChatColor.ITALIC + "/beaconme");
				} else {
					if (ArcheCore.getControls().teleportNewPersonas()) {
						World w = ArcheCore.getControls().getNewPersonaWorld();
						Location l = w == null ? p.getWorld().getSpawnLocation() : w.getSpawnLocation();
						p.teleport(l);
					}
					Bukkit.getScheduler().scheduleSyncDelayedTask(ArcheCore.getPlugin(), () -> new CreationDialog().makeFirstPersona(p), 30L);
				}
			}
		} else {
			ArchePersona ps = getPersona(p);
			if (ps == null) {
				Arrays.stream(prs).filter(Objects::nonNull).findFirst().ifPresent(pers -> pers.setCurrent(true));
				ps = getPersona(p);
				ps.restoreMinecraftSpecifics(p);
				CoreLog.debug("No current Persona on login, so switched to " + MessageUtil.identifyPersona(ps));
			}

			if (ps.tags().removeTag(PersonaTags.REFRESH_MC_SPECIFICS)) ps.restoreMinecraftSpecifics(p);
			activate(ps);
		}
	}

	private void activate(ArchePersona ps) {
		Bukkit.getPluginManager().callEvent(new PersonaActivateEvent(ps, PersonaActivateEvent.Reason.LOGIN));
		ArcheCore.getConsumerControls().queueRow(new UpdatePersonaRow(ps, PersonaField.STAT_LAST_PLAYED, new Timestamp(System.currentTimeMillis())));
		ps.initSession();
		
		RaceBonusHandler.apply(ps);
		ps.attributes().handleSwitch(false);
		ps.updateDisplayName();
		ArcheCore.getControls().getFatigueHandler().showFatigueBar(ps);
	}

	public void leavePlayer(Player p) {
		ArchePersona ps = getPersona(p);

		//Attribute Bonuses stick around forever. To prevent lingering ones, just in
		//case the plugin is to be removed, we perform this method.
		RaceBonusHandler.reset(p);
		if (ps != null) {
			Bukkit.getPluginManager().callEvent(new PersonaDeactivateEvent(ps, PersonaDeactivateEvent.Reason.LOGOUT));
			ps.endSession(); //Causes Persona play session to be put in the log
			
			//Attribute bonuses form the Persona Handler, similarly, linger around
			//We want these cleanly removed from Players on shutdown
			//As a side-effect, this is also a good time to save them for current Personas
			ps.attributes().handleSwitch(true);

			ps.saveMinecraftSpecifics(p);

			ArcheCore.getConsumerControls().queueRow(new UpdatePersonaRow(ps, PersonaField.STAT_LAST_PLAYED, new Timestamp(System.currentTimeMillis())));
		}
	}

	public void initRacespawns() {
		SQLHandler handler = ArcheCore.getPlugin().getSQLHandler();
		ResultSet rs = null;
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
			rs.next();
			rs.getStatement().close();
			if (handler instanceof WhySQLHandler) {
				rs.getStatement().getConnection().close();
			}
			if (!toRemove.isEmpty()) {
				PreparedStatement stat = handler.getConnection().prepareStatement("DELETE FROM persona_race_spawns WHERE race=?");
				for (String ss : toRemove) {
					stat.setString(1, ss);
					stat.execute();
				}
				stat.close();
				if (handler instanceof WhySQLHandler) {
					stat.getConnection().close();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			SQLUtil.closeStatement(rs);
		}
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
	public Map<Race, Location> getRacespawns() {
		return Collections.unmodifiableMap(racespawns);
	}

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
	}


}