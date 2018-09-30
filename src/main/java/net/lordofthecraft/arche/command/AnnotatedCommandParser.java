package net.lordofthecraft.arche.command;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.var;
import net.lordofthecraft.arche.command.annotate.Cmd;
import net.lordofthecraft.arche.command.annotate.DefaultInput;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.interfaces.Persona;

@RequiredArgsConstructor
public class AnnotatedCommandParser {
	private final CommandTemplate template;
	private final PluginCommand bukkitCommand;
	
	
	public ArcheCommandBuilder invokeParse() {
		ArcheCommandBuilder acb = ArcheCommand.builder(bukkitCommand);
		return parse(template, acb);
	}
	
	private ArcheCommandBuilder parse(CommandTemplate currentTemplate, ArcheCommandBuilder acb) {
		Class<? extends CommandTemplate> c = currentTemplate.getClass();
		
		var cmds = Stream.of(c.getMethods()).filter(m->m.isAnnotationPresent(Cmd.class)).collect(Collectors.toList());
		
		for(Method method : cmds) checkForSubLayers(method, currentTemplate, acb); //Note this recurses
		for(Method method: cmds) parseCommands(method, currentTemplate, acb);
		
		return acb; //this::parse is recursive. So is ArcheCommandBuilder::build. Perfect synergy :)
	}
	
	private ArcheCommandBuilder constructSubBuilder(Method method, ArcheCommandBuilder parent) {
		String name = method.getName();
		
		Cmd anno = method.getAnnotation(Cmd.class);
		String desc = anno.value();
		String pex = anno.permission();
		
		var result = parent.subCommand(name, false);
		if(desc !=  null) result.description(desc);
		if(pex != null) result.permission(pex);
		
		return result;
	}
	
	@SneakyThrows
	private void checkForSubLayers(Method method, CommandTemplate currentTemplate, ArcheCommandBuilder acb) {
		if(!CommandTemplate.class.isAssignableFrom(method.getReturnType())) return;
		if(method.getParameterCount() > 0) throw new IllegalStateException("Methods returning CommandTemplate can't also have parameters");
		
		ArcheCommandBuilder subbo = constructSubBuilder(method, acb);
		CommandTemplate lowerLayer = (CommandTemplate) method.invoke(currentTemplate);
		
		parse(lowerLayer, subbo).build(); //We need to go deeper
	}
	
	private void parseCommands(Method method, CommandTemplate currentTemplate, ArcheCommandBuilder acb) {
		if(method.getGenericReturnType() != Void.TYPE) return;

		var subbo = constructSubBuilder(method, acb);
		var params = method.getParameters();
		
		for (int i = 0; i < params.length; i++) {
			var param = params[i];
			var c = param.getClass();
			
			if(i == 0) {
				//If first param is player/persona, it is taken as
				//the sender (or flagged player) rather than argument
				//The continue statements prevent the parameter to being resolved as an argument
				if(Persona.class.isAssignableFrom(c)){
					acb.requiresPersona();
					acb.requiresPlayer();
					continue;
				} else if(Player.class.isAssignableFrom(c)) {
					acb.requiresPlayer();
					continue;
				}
			}
			
			ArgBuilder arg = subbo.arg(param.getName());

			DefaultInput defaultInput = param.getAnnotation(DefaultInput.class);
			if(defaultInput != null) {
				String def = defaultInput.value();
				Validate.notNull(def);
				arg.defaultInput(def);
			}
			resolveArgType(method, param, arg);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" }) //For enum casting
	private void resolveArgType(Method m, Parameter param, ArgBuilder arg) {
		Class<?> c = param.getClass();
		if( c == String.class) {
			arg.asString();
		}else if(c==int.class || c==Integer.class) {
			arg.asInt();
		} else if(c==Double.class || c ==double.class) {
			arg.asDouble();
		} else if(c.isEnum()) { //This is likely where it all goes to hell
			arg.asEnum((Class<Enum>) c);
		} else if(Persona.class.isAssignableFrom(c)) {
			arg.asPersona();
		} else if(OfflinePersona.class.isAssignableFrom(c)) { //Must go AFTER the Persona check
			arg.asOfflinePersona();
		} else if(Player.class.isAssignableFrom(c)) {
			arg.asPlayer();
		} else if(OfflinePlayer.class.isAssignableFrom(c)) {  //Must go AFTER the Player check
			arg.asOfflinePlayer();
		} else {
			throw new IllegalStateException(String.format("Parameter %s of Method %s is of unrecognied type %s", param.getName(), m.getName(), c.getSimpleName()));
		}
	}
	
	
}
