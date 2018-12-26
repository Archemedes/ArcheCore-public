package net.lordofthecraft.arche.command.brigadier;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;

import lombok.RequiredArgsConstructor;
import lombok.var;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.command.ArcheCommand;
import net.lordofthecraft.arche.command.HelpCommand;
import net.lordofthecraft.arche.util.Run;

/**
 * Gonna need some German engineering to get this fucker to fall in line
 */
@RequiredArgsConstructor
public class Kommandant {
	private static final BrigadierProvider provider = new BrigadierProvider();
	private final ArcheCommand head;
	private final List<CommandNode<Object>> rootNodes = new ArrayList<>();
	
	public void addBrigadier() {
		if(!provider.isFunctional()) return;
				
		rootNodes.add(buildNode(head, null));
		
		runDelayedTasks();
	}
	
	private CommandNode<Object> buildNode(ArcheCommand cmd, CommandNode<Object> dad) {
		var node = LiteralArgumentBuilder.literal(cmd.getMainCommand()).build();
		
		for(var sub : cmd.getSubCommands()) {
			if(sub instanceof HelpCommand) continue;
			var subNode = buildNode(sub, node); //Recurses
			node.addChild(subNode);
		}
		
		CommandNode<Object> argument = null;
		for (var arg : cmd.getArgs()) {
			CommandNode<Object> nextArg = RequiredArgumentBuilder.argument(arg.getName(), arg.getBrigadierType()).build();
			if(argument == null) node.addChild(nextArg);
			else argument.addChild(nextArg);
			
			argument = nextArg;
		}
		
		redirectAliases(cmd, dad, node);
		return node;
	}
	
	private void redirectAliases(ArcheCommand cmd, CommandNode<Object> parent, CommandNode<Object> theOneTrueNode) {
		for(String alias : cmd.getAliases()) {
			if(alias.equalsIgnoreCase(cmd.getMainCommand())) continue;
			var node = LiteralArgumentBuilder.literal(alias).build();
			theOneTrueNode.getChildren().forEach(node::addChild);
			if(parent == null) rootNodes.add(node);
			else parent.addChild(node);
		}
	}
	
	private void runDelayedTasks() {
		Runnable r = ()->{
			var brigadier = provider.getBrigadier();
			for(var node : rootNodes) {
				String name = node.getName();
				despigot(brigadier, name);
				brigadier.getRoot().addChild(node);
			}
		};
		Run.as(ArcheCore.getPlugin()).sync(r);
	}
	
	private void despigot(CommandDispatcher<Object> brigadier, String alias) {
		var iter = brigadier.getRoot().getChild(alias).getChildren().iterator();
		while(iter.hasNext()) {
			var kid = iter.next(); //Search spigot's overruling Greedy String argument
			if(kid.getName().equals("args"))
				iter.remove(); //Removing it allows Brigadier's completion to show up
		}
	}
}