package net.lordofthecraft.arche.commands;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mojang.brigadier.tree.CommandNode;

import net.lordofthecraft.arche.command.CommandTemplate;
import net.lordofthecraft.arche.command.annotate.Arg;
import net.lordofthecraft.arche.command.annotate.Cmd;

public class CommandTestudo extends CommandTemplate {

	public void invoke(int i, Material mm, @Arg("kiddo") String how) {
		msg(i);
		msg(mm);
		msg(how);
		
	}
	
	@Cmd("You aren't making it easy")
	public void anime(Material xxx) {
		msg(xxx);
	}
	
	@Cmd("b-baka")
	public void manga(CommandSender s, Player xxx, int i, int i2, @Arg("AnotherPlayer") Player x) {
		msg(xxx);
		msg(x);
	}
	
	private void printOrGo(CommandNode<Object> node) {
		System.out.println(node.getName() + " IS " + node);
		node.getChildren().forEach(this::printOrGo);
	}
	
}
