package net.lordofthecraft.arche.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandNamelog implements CommandExecutor {
	//TODO make this work with consumer
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
		
		if(args.length > 0){
			OfflinePlayer play;
			try{ play = Bukkit.getOfflinePlayer(UUID.fromString(args[0])); }
			catch(IllegalArgumentException e){play = Bukkit.getOfflinePlayer(args[0]);}
			
			if(play == null){
				sender.sendMessage(ChatColor.RED + "No player was found with the provided name");
			} else {
/*				final UUID uuid = play.getUniqueId();
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "Retrieving a Persona name history for " + ChatColor.WHITE + play.getName());
                ArcheExecutor.getInstance().put(new ArcheTask() {
                    @Override
                    public void run(){
						try{
							ResultSet res = handle.query("SELECT id,name FROM persona_names WHERE player='" + uuid.toString() + "' ORDER BY id;");
							int id = 0;
							StringBuilder b = new StringBuilder();
							while(res.next()){
								int nid = res.getInt(1);
								if(nid > id){ 
									if(b.length() > 0){
										char a = (char)((int)'1' + id);
										ChatColor c = ChatColor.getByChar(a);
										
										sender.sendMessage(ChatColor.GRAY + "[" + id + "] " + c + b.toString());
										
										b = new StringBuilder();
									}
									id = nid;
								}
								
								String n = res.getString(2);
								b.append(n).append(", ");
							}
							res.close();
                            res.getStatement().close();
                            if (handle instanceof WhySQLHandler) {

                                res.getStatement().getConnection().close();
                            }

                            if(b.length() > 0){
								char a = (char)((int)'1' + id);
								ChatColor c = ChatColor.getByChar(a);
								
								sender.sendMessage(ChatColor.GRAY + "[" + id + "] " + c + b.toString());
							}
							
						}catch(SQLException e){e.printStackTrace();}
					}
				});*/
			}
			return true;
		}
		
		return false;
	}
	
}
