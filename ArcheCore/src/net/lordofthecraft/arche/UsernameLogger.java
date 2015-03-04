package net.lordofthecraft.arche;

import java.util.*;
import org.bukkit.*;

public class UsernameLogger
{
    public UUID findUUID(final String username) {
        final OfflinePlayer play = Bukkit.getOfflinePlayer(username);
        if (play == null) {
            return null;
        }
        return play.getUniqueId();
    }
    
    public String findLastUsername(final UUID uuid) {
        final OfflinePlayer play = Bukkit.getOfflinePlayer(uuid);
        if (play == null) {
            return null;
        }
        return play.getName();
    }
}
