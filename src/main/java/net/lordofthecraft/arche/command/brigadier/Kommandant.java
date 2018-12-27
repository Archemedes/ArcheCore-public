package net.lordofthecraft.arche.command.brigadier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;

import lombok.RequiredArgsConstructor;
import lombok.var;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.command.ArcheCommand;
import net.lordofthecraft.arche.command.CmdArg;
import net.lordofthecraft.arche.command.HelpCommand;
import net.lordofthecraft.arche.util.Run;

/**
 * Gonna need some German engineering to get this fucker to fall in line
 */
@RequiredArgsConstructor
public class Kommandant {
	private static final BrigadierProvider provider = BrigadierProvider.get();
	private final ArcheCommand head;
	private final List<CommandNode<Object>> rootNodes = new ArrayList<>();
	
	public void addBrigadier() {
		if(!provider.isFunctional()) return;
				
		rootNodes.add(buildNode(head, null));
		
		runDelayedTasks();
	}
	
	private CommandNode<Object> buildNode(ArcheCommand cmd, CommandNode<Object> dad) {
		var builder = LiteralArgumentBuilder.literal(cmd.getMainCommand());
		if(!cmd.hasArgs() && !cmd.isEmptyCommand()) builder.executes($->0);
		var node = builder.build();
		
		for(var sub : cmd.getSubCommands()) {
			if(sub instanceof HelpCommand) continue;
			var subNode = buildNode(sub, node); //Recurses
			node.addChild(subNode);
		}
		
		CommandNode<Object> argument = null;
		Map<String, Integer> namesUsed = new HashMap<>();
		var queue = new LinkedList<>(cmd.getArgs());
		while(!queue.isEmpty()) {
			var arg = queue.poll();
			var next = queue.peek();
			boolean executes = next == null || next.hasDefaultInput();
			
			//Adds numbers to duplicate names: prevents crashes and gray-text rubbish
			String name = arg.getName();
			Integer value = namesUsed.compute(name, (k,v)->v==null? 1 : v+1);
			if(value > 1) name = name+value;
			
			CommandNode<Object> nextArg = buildNodeForArg(name, arg, executes);
			if(argument == null) node.addChild(nextArg);
			else argument.addChild(nextArg);
			
			argument = nextArg;
		}
		 
		redirectAliases(cmd, dad, node);
		return node;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private CommandNode<Object> buildNodeForArg(String name, CmdArg<?> arg, boolean executes){
		var builder = RequiredArgumentBuilder.argument(name, arg.getBrigadierType());
		if(executes) builder.executes( $->0 );
		if(arg.hasCustomCompleter()) builder.suggests(new ArcheSuggestionProvider<>(arg));
		return builder.build();
	}
	
	private void redirectAliases(ArcheCommand cmd, CommandNode<Object> parent, CommandNode<Object> theOneTrueNode) {
		for(String alias : cmd.getAliases()) {
			if(alias.equalsIgnoreCase(cmd.getMainCommand())) continue;
			var node = LiteralArgumentBuilder.literal(alias).redirect(theOneTrueNode).build();
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
		var iter = brigadier.getRoot().getChildren().iterator();
		while(iter.hasNext()) {
			var kid = iter.next(); //Search spigot's attempt at registering the argument
			if(kid.getName().equals(alias))
				iter.remove(); //Killing the skeletal framework of spigot gives us full Brigadier power
		}
	}
}