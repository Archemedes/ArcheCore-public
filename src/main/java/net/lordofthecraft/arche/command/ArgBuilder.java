package net.lordofthecraft.arche.command;

import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.var;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.util.CommandUtil;
import net.md_5.bungee.api.ChatColor;

@Accessors(fluent= true)
public class ArgBuilder {
	
	//Either one of these is set, depending on what kind of arg
	@Getter private final ArcheCommandBuilder command;
	@Getter private final CmdFlag flag;

	@Setter private String defaultInput;
	@Setter private String name = null;
	@Setter private String errorMessage = null;

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
		defaultError("Must be a valid integer of %d or higher", min);
		var arg = asIntInternal();
		arg.setFilter(i->i>=min);
		return command;
	}
	
	public ArcheCommandBuilder asInt(int min, int max){
		defaultError("Must be a valid integer between %d ad %d", min, max);
		var arg = asIntInternal();
		arg.setFilter(i->(i>=min && i <= max));
		return command;
	}
	
	public ArcheCommandBuilder asInt(IntPredicate filter) {
		var arg = asIntInternal();
		arg.setFilter(i->filter.test(i));
		return command;
	}
	
	
	private CmdArg<Integer> asIntInternal(){
		defaultError("Not an accepted integer");
		CmdArg<Integer> arg = build(Integer.class);
		arg.setMapper(Ints::tryParse);
		if(name == null) name = "#";
		
		return arg;
	}
	
	public ArcheCommandBuilder asDouble() {
		asDoubleInternal();
		return command;
	}
	
	public ArcheCommandBuilder asDouble(double min){
		var arg = asDoubleInternal();
		arg.setFilter(d->d>=min);
		return command;
	}
	
	public ArcheCommandBuilder asDouble(double min, double max){
		var arg = asDoubleInternal();
		arg.setFilter(d->(d>=min && d <= max));
		return command;
	}
	
	public ArcheCommandBuilder asDouble(DoublePredicate filter) {
		var arg = asDoubleInternal();
		arg.setFilter(i->filter.test(i));
		return command;
	}
	
	private CmdArg<Double> asDoubleInternal(){
		defaultError("Not an accepted number");
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
		defaultError("Must be one of these: " + ChatColor.WHITE + StringUtils.join(options, ", "));
		var arg = build(String.class);
		arg.setFilter( s-> Stream.of(options).filter(s2->s2.equalsIgnoreCase(s)).findAny().isPresent() );
		return command;
	}
	
	public <T extends Enum<T>> ArcheCommandBuilder asEnum(Class<T>  clazz) {
		defaultError("Not a valid " + clazz.getSimpleName());
		var arg = build(clazz);
		arg.setMapper(s->{
			try{ return Enum.valueOf(clazz, s); }
			catch(IllegalArgumentException e) {return null;}
		});
		return command;
	}
	
	public ArcheCommandBuilder asPersona() {
		defaultError("You must provide a valid Persona");
		var arg = build(Persona.class);
		arg.setMapper(CommandUtil::personaFromArg);
		return command;
	}
	
	public ArcheCommandBuilder asOfflinePersona() {
		defaultError("You must provide a valid Persona (online or offline");
		var arg = build(OfflinePersona.class);
		arg.setMapper(CommandUtil::offlinePersonaFromArg);
		return command;
	}
	
	private void defaultError(String err, Object... formats) {
		if(errorMessage == null) this.errorMessage = String.format(err, formats);
	}
	
	private <T> CmdArg<T> build(Class<T> clazz){
		CmdArg<T> arg = new CmdArg<>(name, errorMessage, defaultInput);
		if(flag == null) command.addArg(arg);
		else flag.setArg(arg);
		return arg;
	}
	
}
