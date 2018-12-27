package net.lordofthecraft.arche.commands;

import org.bukkit.Material;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;

import lombok.var;
import net.lordofthecraft.arche.command.CommandTemplate;
import net.lordofthecraft.arche.command.annotate.Arg;
import net.lordofthecraft.arche.command.annotate.Cmd;
import net.lordofthecraft.arche.command.brigadier.BrigadierProvider;

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
	public void manga(Material xxx, int i, Material jx) {
		msg(xxx);
		msg(jx);
	}
	
	private void printOrGo(CommandNode<Object> node) {
		System.out.println(node.getName() + " IS " + node);
		node.getChildren().forEach(this::printOrGo);
	}
	
}
