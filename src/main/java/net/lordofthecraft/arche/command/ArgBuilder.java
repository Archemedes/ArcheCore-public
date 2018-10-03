package net.lordofthecraft.arche.command;

import java.util.UUID;
import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.experimental.Accessors;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.CoreLog;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.util.CommandUtil;
import net.md_5.bungee.api.ChatColor;

@Accessors(fluent= true)
public class ArgBuilder {
	
	//Either one of these is set, depending on what kind of arg
	private final ArcheCommandBuilder command;
	@Getter(AccessLevel.PACKAGE) private final CmdFlag flag;

	@Setter private String defaultInput;
	@Setter private String name = null;
	@Setter private String errorMessage = null;
	@Setter private String description = null;
	
	ArgBuilder(ArcheCommandBuilder command) {
		this(command, null);
	}
	
	ArgBuilder(ArcheCommandBuilder command, CmdFlag flag) {
		this.command = command;
		this.flag = flag;
	}

	public ArcheCommandBuilder asInt(){
		asIntInternal();
		return command;
	}
	
	public ArcheCommandBuilder asInt(int min){
		defaults("#","Must be a valid integer of %d or higher", min);
		val arg = asIntInternal();
		arg.setFilter(i->i>=min);
		return command;
	}
	
	public ArcheCommandBuilder asInt(int min, int max){
		defaults("#","Must be a valid integer between %d ad %d", min, max);
		val arg = asIntInternal();
		arg.setFilter(i->(i>=min && i <= max));
		return command;
	}
	
	public ArcheCommandBuilder asInt(IntPredicate filter) {
		val arg = asIntInternal();
		arg.setFilter(i->filter.test(i));
		return command;
	}
	
	
	private CmdArg<Integer> asIntInternal(){
		defaults("#","Not an accepted integer");
		CmdArg<Integer> arg = build(Integer.class);
		arg.setMapper(Ints::tryParse);
		
		return arg;
	}
	
	public ArcheCommandBuilder asDouble() {
		asDoubleInternal();
		return command;
	}
	
	public ArcheCommandBuilder asDouble(double min){
		val arg = asDoubleInternal();
		arg.setFilter(d->d>=min);
		return command;
	}
	
	public ArcheCommandBuilder asDouble(double min, double max){
		val arg = asDoubleInternal();
		arg.setFilter(d->(d>=min && d <= max));
		return command;
	}
	
	public ArcheCommandBuilder asDouble(DoublePredicate filter) {
		val arg = asDoubleInternal();
		arg.setFilter(i->filter.test(i));
		return command;
	}
	
	private CmdArg<Double> asDoubleInternal(){
		defaults("#","Not an accepted number");
		CmdArg<Double> arg = build(Double.class);
		arg.setMapper(Doubles::tryParse);
		if(command != null) command.addArg(arg);
		return arg;
	}
	
	public ArcheCommandBuilder asString(){
		build(String.class);
		return command;
	}
	
	public ArcheCommandBuilder asString(String... options){
		defaults("*","Must be one of these: " + ChatColor.WHITE + StringUtils.join(options, ", "));
		val arg = build(String.class);
		arg.setFilter( s-> Stream.of(options).filter(s2->s2.equalsIgnoreCase(s)).findAny().isPresent() );
		return command;
	}
	
	
	public ArcheCommandBuilder asMaterial() {
		defaults("Material", "Please specify a valid item name");
		val arg = build(Material.class);
		arg.setMapper(s->{
			Material m = Material.matchMaterial(s);
/*			if(m == null) {
				Stream.of(Material.values())
					.map(ItemStack::new)
					.map(ItemUtil::getItemEnglishName)
					.map(xx->xx.replace(' ', '_'))
					.filter(s::equalsIgnoreCase)
					.findFirst().orElse(null);
			}*/
			return m;
		});
		return command;
	}
	
	public <T extends Enum<T>> ArcheCommandBuilder asEnum(Class<T>  clazz) {
		if(clazz == Material.class) return asMaterial();
		
		defaults(clazz.getSimpleName(),"Not a valid " + clazz.getSimpleName());
		val arg = build(clazz);
		arg.setMapper(s->{
			try{ return Enum.valueOf(clazz, s.toUpperCase()); }
			catch(IllegalArgumentException e) {return null;}
		});
		return command;
	}
	
	public ArcheCommandBuilder asPlayer() {
		defaults("player","You must provide an online Player");
		val arg = build(Player.class);
		arg.setMapper(s->{
			if(s.length() == 36) {
				try {return Bukkit.getPlayer(UUID.fromString(s));}
				catch(IllegalArgumentException e) {return null;}
			} else {
				return Bukkit.getPlayer(s);
			}
		});
		return command;
	}
	
	public ArcheCommandBuilder asOfflinePlayer() {
		defaults("player","You must provide a Player");
		val arg = build(OfflinePlayer.class);
		arg.setMapper(s->{
			if(s.length() == 36) {
				try {return Bukkit.getOfflinePlayer(UUID.fromString(s));}
				catch(IllegalArgumentException e) {return null;}
			} else {
				return Bukkit.getOfflinePlayer( ArcheCore.getControls().getPlayerUUIDFromName(s) );
			}
		});
		return command;
	}
	
	public ArcheCommandBuilder asPersona() {
		defaults("persona","You must provide a valid Persona");
		val arg = build(Persona.class);
		arg.setMapper(CommandUtil::personaFromArg);
		return command;
	}
	
	public ArcheCommandBuilder asOfflinePersona() {
		defaults("persona","You must provide a valid Persona (online or offline)");
		val arg = build(OfflinePersona.class);
		arg.setMapper(CommandUtil::offlinePersonaFromArg);
		return command;
	}
	
	public ArcheCommandBuilder asBoolean() {
		return asBoolean(false);
	}
	
	public ArcheCommandBuilder asBoolean(boolean def) {
		this.defaultInput = def? "y":"n";
		defaults("y/n","Please provide either yes/no.");
		val arg = build(Boolean.class);
		arg.setMapper(s -> {
			if(Stream.of("true","yes","y").anyMatch(s::equalsIgnoreCase)) return true;
			else if(Stream.of("false","no","n").anyMatch(s::equalsIgnoreCase)) return false;
			else return null;
		});
		
		return command;
	}
	
	public ArcheCommandBuilder asJoinedString() {
		if(flag != null) throw new IllegalStateException("Cannot use joined arguments for parameters/flags");
		
		defaults("**", "Provide any sentence, spaces allowed.");
		JoinedArg arg = new JoinedArg(name, errorMessage, defaultInput, description);
		command.noMoreArgs = true;
		command.addArg(arg);
		return command;
	}
	
	
	private void defaults(String name, String err, Object... formats) {
		if(this.name == null) this.name = name;
		if(errorMessage == null) this.errorMessage = String.format(err, formats);
	}
	
	private <T> CmdArg<T> build(Class<T> clazz){
		CoreLog.debug("Building arg for class: " + clazz.getSimpleName());
		
		CmdArg<T> arg = new CmdArg<>(name, errorMessage, defaultInput, description);
		if(flag == null) command.addArg(arg);
		else flag.setArg(arg);
		return arg;
	}
	
}
