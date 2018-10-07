package net.lordofthecraft.arche.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.var;
import net.lordofthecraft.arche.CoreLog;
import net.lordofthecraft.arche.command.RanCommand.CmdParserException;
import net.lordofthecraft.arche.command.annotate.Arg;
import net.lordofthecraft.arche.command.annotate.Cmd;
import net.lordofthecraft.arche.command.annotate.Default;
import net.lordofthecraft.arche.command.annotate.Flag;
import net.lordofthecraft.arche.command.annotate.Joined;
import net.lordofthecraft.arche.interfaces.CommandHandle;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.interfaces.Persona;

@RequiredArgsConstructor
public class AnnotatedCommandParser {
	private final Supplier<CommandTemplate> template;
	private final PluginCommand bukkitCommand;
	
	private boolean wantsCommandSenderAsFirstArg = false;
	
	public ArcheCommandBuilder invokeParse() {
		ArcheCommandBuilder acb = CommandHandle.builder(bukkitCommand);
		return parse(template, acb);
	}
	
	private ArcheCommandBuilder parse(Supplier<CommandTemplate> template, ArcheCommandBuilder acb) {
		Class<? extends CommandTemplate> c = template.get().getClass();
		
		addInvoke(c, template, acb);
		
		var cmds = Stream.of(c.getMethods()).filter(m->m.isAnnotationPresent(Cmd.class)).collect(Collectors.toList());
		
		for(Method method : cmds) checkForSubLayer(method, template, acb); //Note this recurses
		for(Method method : cmds) parseCommand(method, template, acb);
		
		return acb; //this::parse is recursive. So is ArcheCommandBuilder::build. Perfect synergy :)
	}
	
	private void addInvoke(Class<? extends CommandTemplate> c, Supplier<CommandTemplate> template, ArcheCommandBuilder acb) {
		//This breaks polymorphism but whatever
		for(Method m : c.getDeclaredMethods()) {
			if(m.getParameterCount() > 0 && Modifier.isPublic(m.getModifiers()) && m.getName().equals("invoke") && m.getReturnType() == Void.TYPE) {
				//This is a invoke method declared in the class, assumed this is what we want for the default invocation of the command
				//Due to logic of the ArcheCommandExecutor this still makes the no-argument default a help command
				parseCommandMethod(m, template, acb);
				return;
			}
		}
		
		//Fallback option, which does use polymorphism, specifically the CommandTemplate.invoke() method
		acb.run(rc->{ //This is default behavior when no arguments are given, usually refers to help file
			CommandTemplate t = template.get();
			t.setRanCommand(rc);
			t.invoke();
		});
	}
	
	private ArcheCommandBuilder constructSubBuilder(Method method, ArcheCommandBuilder parent) {
		String name = method.getName();
		
		Cmd anno = method.getAnnotation(Cmd.class);
		String desc = anno.value();
		String pex = anno.permission();
		
		var result = parent.subCommand(name, false);
		if(desc !=  null) result.description(desc);
		if(StringUtils.isNotEmpty(pex)) result.permission(pex);
		
		return result;
	}
	
	@SneakyThrows
	private void checkForSubLayer(Method method, Supplier<CommandTemplate> template, ArcheCommandBuilder acb) {
		if(!CommandTemplate.class.isAssignableFrom(method.getReturnType())) return;
		if(method.getParameterCount() > 0) throw new IllegalStateException("Methods returning CommandTemplate can't also have parameters");
		if(method.getName().equals("invoke")) throw new IllegalArgumentException("Don't annotate your invoke() methods. The method name is reserved!");
		
		ArcheCommandBuilder subbo = constructSubBuilder(method, acb);
		Supplier<CommandTemplate> chained = ()-> chainSupplier(method, template);
		parse(chained, subbo).build(); //We need to go deeper
	}
	
	@SneakyThrows
	private CommandTemplate chainSupplier(Method templateGetter, Supplier<CommandTemplate> theOldSupplier) {
		//Makes a NEW supplier which invokes the old supplier (which is one higher in the chain)
		//The supplied CommandTemplate has a particular method called via reflection
		//A method which we know to return CommandTemplate (checked above), so we cast it
		//This supplier is then used for subsequent checking.
		//Yes this is an abysmal piece of code. Let's never speak of it.
		return (CommandTemplate) templateGetter.invoke(theOldSupplier.get());
	}
	
	private void parseCommand(Method method, Supplier<CommandTemplate> template, ArcheCommandBuilder acb) {
		if(method.getReturnType() != Void.TYPE) return;
		if(method.getName().equals("invoke")) throw new IllegalArgumentException("Don't annotate your invoke() methods. The method name is reserved!");
		
		var subbo = constructSubBuilder(method, acb);
		parseCommandMethod(method, template, subbo);
		subbo.build();
	}

	private void parseCommandMethod(Method method, Supplier<CommandTemplate> template, ArcheCommandBuilder acb) {
		var flagsAnno = method.getAnnotation(Flag.List.class);
		if(flagsAnno != null) for(Flag flag : flagsAnno.value()) addFlag(acb, flag);
		else if(method.isAnnotationPresent(Flag.class)) addFlag(acb, method.getAnnotation(Flag.class));
		
		var params = method.getParameters();
		for (int i = 0; i < params.length; i++) {
			var param = params[i];
			var c = param.getType();

			if(i == 0) {
				//If first param is player/persona, it is taken as
				//the sender (or flagged player) rather than argument
				//The continue statements prevent the parameter to being resolved as an argument
				if(Persona.class.isAssignableFrom(c)){
					acb.requiresPersona().requiresPlayer();
					continue;
				} else if(Player.class.isAssignableFrom(c)) {
					acb.requiresPlayer();
					continue;
				} else if(CommandSender.class.isAssignableFrom(c)) {
					CoreLog.debug("Method " + method.getName() + " for cmd " + acb.mainCommand() + " has explicit sender arg");
					wantsCommandSenderAsFirstArg = true;
					continue;
				}
			}
			
			var argAnno = param.getAnnotation(Arg.class);
			ArgBuilder arg = argAnno == null? acb.arg() : acb.arg(argAnno.value());
			if(argAnno != null && !argAnno.description().isEmpty()) arg.description(argAnno.description());
			
			Default defaultInput = param.getAnnotation(Default.class);
			if(defaultInput != null) {
				String def = defaultInput.value();
				Validate.notNull(def);
				arg.defaultInput(def);
			}
			
			if(param.isAnnotationPresent(Joined.class))
				if(param.getType() == String.class) arg.asJoinedString();
				else throw new IllegalArgumentException("All JoinedString annotations must affect a String type parameter");
			else resolveArgType(method, param.getType(), arg);
		}
		
		makeCommandDoStuff(template, acb, method);
	}
	
	private void makeCommandDoStuff(Supplier<CommandTemplate> template, ArcheCommandBuilder acb, Method method) {
		//Make command actually do stuff
		acb.run(rc->{
			try {
				CommandTemplate t = template.get();
				t.setRanCommand(rc);
				Object[] args = rc.getArgResults().toArray();
				
				if(rc.getCommand().requiresPersona() || rc.getCommand().requiresPlayer()) {
					Object[] newArgs = insertFirst(args, rc.getCommand().requiresPersona()? rc.getPersona() : rc.getPlayer());
					method.invoke(t, newArgs);
				} else if (wantsCommandSenderAsFirstArg) {
					Object[] newArgs = insertFirst(args, rc.getSender());
					method.invoke(t, newArgs);
				}else {
					method.invoke(t, args);
				}
			} catch (InvocationTargetException ite) {
				if(ite.getCause() instanceof CmdParserException) {
					rc.error(ite.getCause().getMessage());
				} else {
					ite.printStackTrace();
					rc.error("An unhandled exception occurred. Contact a developer.");
				}
			} catch (Exception e) {
				e.printStackTrace();
				rc.error("An unhandled exception occurred. Contact a developer.");
			}
		});
	}
	
	private Object[] insertFirst(Object[] args, Object toAdd) {
		Object[] newArgs = new Object[args.length+1];
		System.arraycopy(args, 0, newArgs, 1, args.length);
		newArgs[0] = toAdd;
		return newArgs;
	}
	
	private void addFlag(ArcheCommandBuilder acb, Flag flag) {
		ArgBuilder flarg = acb.flag(flag.name(), flag.aliases());
		String desc = flag.description();
		if(!desc.isEmpty()) flarg.description(desc);
		resolveArgType(null, flag.type(), flarg);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" }) //For enum casting
	private void resolveArgType(Method m, Class<?> c, ArgBuilder arg) {
		if( c == Void.class) {
			arg.asVoid(); //Flags only
		}else if( c == String.class) {
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
			throw new IllegalStateException(String.format("Method %s has unrecognied type %s", m == null? "Flag":m.getName(), c.getSimpleName()));
		}
	}
	
	
}
