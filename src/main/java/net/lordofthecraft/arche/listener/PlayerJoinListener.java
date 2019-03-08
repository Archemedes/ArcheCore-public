package net.lordofthecraft.arche.listener;

import java.util.Collection;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.ArcheTimer;
import net.lordofthecraft.arche.CoreLog;
import net.lordofthecraft.arche.account.ArcheAccountHandler;
import net.lordofthecraft.arche.persona.ArcheOfflinePersona;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;

public class PlayerJoinListener implements Listener {
	private final ArchePersonaHandler handler;
	private final ArcheAccountHandler accounts;
	private final ArcheTimer timer;

	public PlayerJoinListener(ArchePersonaHandler handler, ArcheAccountHandler accounts){
		this.handler = handler;
		this.accounts = accounts;
		timer = ArcheCore.getPlugin().getMethodTimer();
	}


	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(AsyncPlayerPreLoginEvent e) {
		if (e.getLoginResult() == Result.ALLOWED)
			accounts.load(e.getUniqueId());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onDevmodeJoin(PlayerJoinEvent e){
		Player p = e.getPlayer();
		if (ArcheCore.getPlugin().isDevModeEnabled()) {
			if (!p.hasPermission("archecore.arsql")) {
				p.kickPlayer(ChatColor.RED + "An error has occured.\n"
						+ (p.hasPermission("archecore.mod") ? ChatColor.GOLD + "Contact a Developer immidiately."
								: ChatColor.GRAY + "We are looking into it, please login later."));
			}
			return;
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent e){
		e.setJoinMessage(null);
		Player p = e.getPlayer();

		p.setExhaustion(3.5f); //This fixes an old bug where exhaustion got set to NAN or similaly stupid
		if (timer != null) timer.startTiming("login " + p.getName());
		if(!accounts.isLoaded(p.getUniqueId())) {
			CoreLog.warning("Player " + p.getName() + " was NOT loaded Async. This is likely a late-bind issue");
			accounts.load(p.getUniqueId());
		}
		
		ArcheCore.getPlugin().joinPlayer(p);

		if (timer != null) timer.stopTiming("login " + p.getName());
		if (ArcheCore.getPlugin().debugMode()) {
			Collection<ArcheOfflinePersona> personas = handler.getPersonas();
			int offlines = personas.stream().filter(ps -> ps.getClass() == ArcheOfflinePersona.class).collect(Collectors.toList()).size();
			int onlines = personas.stream().filter(ps -> ps.getClass() == ArchePersona.class).collect(Collectors.toList()).size();
			int playerCount = handler.getPersonaStore().getOnlineImplementedPersonas().size();
			CoreLog.info("[Login] " + personas.size() + " persona files (" + offlines + " offl. / " + onlines + "onl.) for "+Bukkit.getOnlinePlayers().size()+" (" + playerCount + ") players.");
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onLeave(PlayerQuitEvent e) {
		e.setQuitMessage(null);
		if (timer != null) timer.startTiming("logout");

		Player p = e.getPlayer();
		ArcheCore.getPlugin().leavePlayer(p);

		if(timer != null){
			timer.stopTiming("logout");
			int size = handler.getPersonas().size();
			CoreLog.info("[Debug] Seen Personas of " + size + " players at logout of " + p.getName());
		}
	}
}
