package net.lordofthecraft.arche.command.brigadier;

import static net.lordofthecraft.arche.command.brigadier.BrigadierUtil.literal;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;

import lombok.RequiredArgsConstructor;
import lombok.var;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.command.ArcheCommandBuilder;
import net.lordofthecraft.arche.util.Run;

/**
 * Gonna need some German engineering to get this fucker to fall in line
 */
@RequiredArgsConstructor
public class Kommandant {
	private static final BrigadierProvider provider = new BrigadierProvider();
	private final ArcheCommandBuilder builder;
	private final List<CommandNode<Object>> rootNodes = new ArrayList<>();
	
	public void addBrigadier() {
		if(!provider.isFunctional()) return;
		
		
		runDelayedTasks();
	}
	
	
	private void redirectAliases(CommandNode<Object> parent, CommandNode<Object> theOneTrueNode) {
		for(String alias : builder.aliases()) {
			if(alias.equalsIgnoreCase(builder.mainCommand())) continue;
			var node = literal(alias).redirect(theOneTrueNode).build();
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