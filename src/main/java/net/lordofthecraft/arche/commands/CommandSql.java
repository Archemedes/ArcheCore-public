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

public class CommandSql implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length == 0 || !(sender instanceof Player)) return false;
		
		Player p = (Player) sender;
		if(p.hasPermission("archecore.arsql")){
			String statement = StringUtils.join(args, ' ', 0, args.length);
            //This removes comments from the SQL command.
            //Things like DR/**/OP DATABASE ARCHECORE; would be possible otherwise.
            statement = statement.replace("/*", "").replace("*/", "");
            statement = statement.substring(statement.lastIndexOf(';') + 1); //Preventing SELECT * FROM PERSONA; DROP PERSONA;
            try{
				Connection c = ArcheCore.getControls().getSQLHandler().getConnection();
                boolean query = statement.toUpperCase().contains("SELECT");
                boolean dangerous = statement.toUpperCase().contains("DROP") || statement.toUpperCase().contains("DELETE");
                if (!query) {
                    if (dangerous && !("2b8176ac-89fc-47c8-99a5-4ed206380c2b".equals(p.getUniqueId().toString()) || "0c4846c1-975f-493b-b931-91d725125e0f".equals(p.getUniqueId().toString()))) {
                        sender.sendMessage(ChatColor.RED + "Error: DROP and DELETE statements should be either run through command line or run by 501warhead. Contact 501warhead for information.");
                        return true;
                    }
                    int rows = c.createStatement().executeUpdate(statement);
                    sender.sendMessage("Rows Affected: " + rows);
                } else {
                    ResultSet rs = c.createStatement().executeQuery(statement);
                    int count = 1;
                    while (rs.next() && count < 11) {
                        p.sendMessage("===> Row " + count);
                        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                            p.sendMessage(rs.getMetaData().getColumnName(i) + ": " + rs.getObject(i));
                        }
                        count++;
                    }
                    rs.close();
                    if (count >= 11) {
                        p.sendMessage(ChatColor.RED + "Query was too large! Use command line for large sized queries! The rest has been truncated.");
                    }
                    sender.sendMessage("End of Results.");
                }
                /*boolean result = c.createStatement().execute(statement);
				sender.sendMessage("Returned boolean: " + result);*/
			} catch(SQLException e){sender.sendMessage("SQLException: " + e);}
		}
		
		return true;
	}
	
}
