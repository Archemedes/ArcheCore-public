package net.lordofthecraft.arche.commands;

import net.lordofthecraft.arche.ArcheCore;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CommandSql implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length == 0 || !(sender instanceof Player)) return false;
		
		Player p = (Player) sender;
		if(p.hasPermission("archecore.arsql")){
			String statement = StringUtils.join(args, ' ', 0, args.length);
            //TODO further injection prevention
            //This removes comments from the SQL command.
            //Things like DR/**/OP DATABASE archecore; would be possible otherwise.
            statement = statement.replace("/*", "").replace("*/", "");
            if(statement.contains(";")) statement = statement.substring(0, statement.indexOf(";"))+1; //Preventing SELECT * FROM persona;DROP persona;
            try(Connection c = ArcheCore.getControls().getSQLHandler().getConnection(); Statement stat = c.createStatement()){
                	if (!("2b8176ac-89fc-47c8-99a5-4ed206380c2b".equals(p.getUniqueId().toString()) //501warhead
                			|| "eab9533c-9961-4e7d-aa0a-cc3e21fe8d48".equals(p.getUniqueId().toString())) //Awe
                			) {
                	sender.sendMessage(ChatColor.RED + "Error: DROP and DELETE statements should be either run through command line or run by 501warhead/Sporadic. Contact 501warhead or Sporadic for information.");
                	sender.sendMessage(ChatColor.GRAY + "Note: This is to prevent commands from being run where you cannot see the full command and may make an error. You are fully capable of running this command from commandline and are encouraged to do so.");
                	return true;
                }
                
                boolean query = statement.toUpperCase().contains("SELECT");
                if (!query) {
                    int rows = stat.executeUpdate(statement);
                    sender.sendMessage("Rows Affected: " + rows);
                } else {
                    ResultSet rs = stat.executeQuery(statement);
                    int count = 1;
                    while (rs.next() && count < 11) {
                        p.sendMessage(ChatColor.GOLD + "===> Row " + count);
                        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                            p.sendMessage(ChatColor.BLUE + rs.getMetaData().getColumnName(i) + ChatColor.DARK_GRAY + "(" + rs.getMetaData().getColumnTypeName(i) + ")" + ChatColor.BLUE + ": " + ChatColor.GRAY + rs.getObject(i));
                        }
                        count++;
                    }
                    rs.close();
                    if (count >= 11) {
                        p.sendMessage(ChatColor.RED + "Query was too large! Use command line for large sized queries! The rest has been truncated.");
                    }
                    sender.sendMessage("End of Results.");
                }
            } catch (SQLException e) {
                sender.sendMessage("SQLException: " + e);
            }
        }
		
		return true;
	}
	
}
