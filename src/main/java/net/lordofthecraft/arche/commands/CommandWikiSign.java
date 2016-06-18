package net.lordofthecraft.arche.commands;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.enums.ChatBoxAction;
import net.lordofthecraft.arche.signs.WhySign;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Sean on 5/30/2016.
 */
public class CommandWikiSign implements CommandExecutor {

    public CommandWikiSign() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player p = (Player) sender;
        p.getLocation().getBlock().setType(Material.SIGN_POST);
        WhySign ws = WhySign.newInstance();
        Bukkit.getScheduler().scheduleSyncDelayedTask(ArcheCore.getPlugin(), () -> {
            ws.setLineText(1, "This is a sign")
                    .setLineBold(1, true)
                    .setLineItalic(1, true)
                    .setClickEvent(ChatBoxAction.RUN_COMMAND, "sudo @p[r=5] archehelp alras", 1)
                    .applyChatColor(1, ChatColor.YELLOW)
                    .setLineText(2, "more text")
                    .setLineStrike(2, true)
                    .setLineUnderlined(2, true)
                    .build(p.getLocation().getBlock());
        }, 2);
        return true;
    }
}
