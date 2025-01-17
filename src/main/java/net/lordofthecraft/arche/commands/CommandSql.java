package net.lordofthecraft.arche.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.util.SQLUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class CommandSql implements CommandExecutor {

	private ArrayList<UUID> authorized = Lists.newArrayList(
			UUID.fromString("2b8176ac-89fc-47c8-99a5-4ed206380c2b"), //501
			UUID.fromString("0c4846c1-975f-493b-b931-91d725125e0f"), //Theryn
			UUID.fromString("f9501b86-bed3-4704-abd9-675d5c6e55f7"), //Llir
			UUID.fromString("eab9533c-9961-4e7d-aa0a-cc3e21fe8d48") //Awe
			);

	private final int MAX_QUERY = 30; // why 11 lmao

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length == 0 || !(sender instanceof Player)) return false;

		Player p = (Player) sender;
		if(p.hasPermission("archecore.arsql")){
			
			if (args.length > 0 && args[0].equalsIgnoreCase("devmode")) {
				ArcheCore.getPlugin().setDevMode(!ArcheCore.getPlugin().isDevModeEnabled());
				p.sendMessage(ChatColor.YELLOW + "ArcheCore devmode: " + ArcheCore.getPlugin().isDevModeEnabled());
				return true;
			}
			
			String statement = StringUtils.join(args, ' ');
			//TODO further injection prevention
			//This removes comments from the SQL command.
			//Things like DR/**/OP DATABASE archecore; would be possible otherwise.
			statement = statement.replace("/*", "").replace("*/", "");
			statement = statement.split(";")[0]; //Preventing SELECT * FROM persona;DROP persona;
			statement += ";";
			final String execute = statement;
			sender.sendMessage(ChatColor.YELLOW + "Attempting to execute: \n" + ChatColor.GRAY + statement);
			ArcheCore.getPlugin().getServer().getScheduler().runTaskAsynchronously(ArcheCore.getPlugin(), () -> {
				try (Connection c = ArcheCore.getSQLControls().getConnection()) {
					boolean query = execute.toUpperCase().contains("SELECT");
					boolean dangerous = execute.toUpperCase().contains("DROP") || execute.toUpperCase().contains("DELETE");
					if (!query) {
						if (dangerous && !isAuthorized(p)) {
							sender.sendMessage(ChatColor.RED + "Error: DROP and DELETE statements should be either run through command line or run by 501warhead/Sporadic/Llir. Contact the aforementioned for information.");
							sender.sendMessage(ChatColor.GRAY + "Note: This is to prevent commands from being run where you cannot see the full command and may make an error. You are fully capable of running this command from commandline and are encouraged to do so."); // lol this isn't even true because you don't let console run this command
							return;
						} else if (!hasVerified(p, execute)){
							sender.sendMessage(ChatColor.RED + "Warning! Executing DROP/DELETE, if you are sure, type this command again.");
							return;
						}
						verifying.remove(p.getUniqueId());
						int rows = c.createStatement().executeUpdate(execute);
						sender.sendMessage(ChatColor.GREEN + "Rows Affected: " + ChatColor.GRAY + rows);
					} else {
						ResultSet rs = c.createStatement().executeQuery(execute);
						int count = 1;
						while (rs.next() && count < MAX_QUERY) {
							p.sendMessage(ChatColor.GOLD + "===> Row " + count);
							for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
								p.sendMessage(ChatColor.BLUE + rs.getMetaData().getColumnName(i) + ChatColor.DARK_GRAY + "(" + rs.getMetaData().getColumnTypeName(i) + ")" + ChatColor.BLUE + ": " + ChatColor.GRAY + rs.getObject(i));
							}
							count++;
						}
						SQLUtil.close(rs);
						if (count >= MAX_QUERY) {
							p.sendMessage(ChatColor.RED + "Query was too large! Use command line for large sized queries! The rest has been truncated."); //also a lie
						}
						sender.sendMessage("End of Results.");
					}
				} catch (SQLException e) {
					sender.sendMessage("SQLException: " + e);
				}
			});
		}

		return true;
	}

	private HashMap<UUID, String> verifying = Maps.newHashMap();

	private boolean hasVerified(Player p, String statement) {
		if (!verifying.containsKey(p.getUniqueId())) {
			verifying.put(p.getUniqueId(), statement);
			return false;
		} else if (!verifying.get(p.getUniqueId()).equals(statement)) {
			verifying.replace(p.getUniqueId(), statement);
			return false;
		} else {
			return true;
		}
	}

	public boolean isAuthorized(Player p) {
		return (this.authorized.contains(p.getUniqueId()));
	}

}
