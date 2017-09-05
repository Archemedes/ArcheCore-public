package net.lordofthecraft.arche.skin;

import com.google.common.collect.Lists;
import com.mojang.authlib.exceptions.AuthenticationException;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.skin.MojangCommunicator.MinecraftAccount;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class SkinRefresher extends BukkitRunnable {
	SkinCache cache;
	List<MinecraftAccount> accounts = Lists.newArrayList();

    public SkinRefresher(SkinCache cache, String username, String password) {
        this.cache = cache;
		
		MinecraftAccount acc1 = new MinecraftAccount();
        acc1.username = username;
        acc1.password = password;
        accounts.add(acc1);
	}
	
	@Override
	public void run() {
		Logger log = ArcheCore.getPlugin().getLogger();
		int time = cache.checkRefreshTime(accounts.size()); //This lets us know at what time to start refreshing
		log.info("Going to refresh skins for cached files older than " + time + " hours");

		for(MinecraftAccount acc : accounts) {
			try {
				log.info("Using acc " + acc.username + " to refresh a skin!");
				ArcheSkin refreshed = cache.grabOneSkinAndRefresh(acc);
				if(refreshed == null) {
					log.info("There was nothing more to refresh!");
					break;
				}else {
					log.info("Skin refreshed: " + refreshed.getOwner() + " @index " + refreshed.getIndex());
					log.info("View skin at " + refreshed.getURL());
				}
			} catch (IOException e) {
				log.warning("Http connection failed! Can you connect to Mojang servers?");
			} catch (ParseException e) {
				log.warning("Parsing has failed! Possible Mojang changed its API Json format!");
			} catch (AuthenticationException e) {
				log.warning("On skin refresh: account " + acc.username + " seems to experience issues!");
			}
		}
	}

}
