package net.lordofthecraft.arche.commands;

import org.bukkit.command.*;

import java.util.*;

import net.lordofthecraft.arche.save.*;
import net.lordofthecraft.arche.save.tasks.*;

import java.sql.*;

import org.bukkit.*;

import java.io.*;

public class CommandNamelog implements CommandExecutor
{
    @SuppressWarnings("deprecation")
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length > 0) {
            OfflinePlayer play;
            try {
                play = Bukkit.getOfflinePlayer(UUID.fromString(args[0]));
            }
            catch (IllegalArgumentException e) {
                play = Bukkit.getOfflinePlayer(args[0]);
            }
            if (play == null) {
                sender.sendMessage(ChatColor.RED + "No player was found with the provided name");
            }
            else {
                final UUID uuid = play.getUniqueId();
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Retrieving a Persona name history for " + ChatColor.WHITE + play.getName());
                SaveHandler.getInstance().put(new ArcheTask() {
                    @Override
                    public void run() {
                        try {
                            final ResultSet res = handle.query("SELECT id,name FROM persona_names WHERE player='" + uuid.toString() + "' ORDER BY id;");
                            int id = 0;
                            StringBuilder b = new StringBuilder();
                            while (res.next()) {
                                final int nid = res.getInt(1);
                                if (nid > id) {
                                    if (b.length() > 0) {
                                        final char a = (char)(49 + id);
                                        final ChatColor c = ChatColor.getByChar(a);
                                        sender.sendMessage(ChatColor.GRAY + "[" + id + "] " + c + b.toString());
                                        b = new StringBuilder();
                                    }
                                    id = nid;
                                }
                                final String n = res.getString(2);
                                b.append(n + ", ");
                            }
                            res.close();
                            if (b.length() > 0) {
                                final char a2 = (char)(49 + id);
                                final ChatColor c2 = ChatColor.getByChar(a2);
                                sender.sendMessage(ChatColor.GRAY + "[" + id + "] " + c2 + b.toString());
                            }
                        }
                        catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            return true;
        }
        return false;
    }
}
