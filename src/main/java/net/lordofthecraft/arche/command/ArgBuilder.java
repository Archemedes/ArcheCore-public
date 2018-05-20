package net.lordofthecraft.arche.command;

import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.stream.Stream;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.var;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.util.CommandUtil;


@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Accessors(fluent= true)
public class ArgBuilder {
	@Getter private final ArcheCommand command;
	@Getter private final String defaultInput;
	
	@Setter private String description  = null;
	@Setter private String name  = null;
	@Setter private String errorMessage = null;

	public ArcheCommand asInt(){
		asIntInternal();
		return command;
	}
	
	public ArcheCommand asInt(int min){
		var arg = asIntInternal();
		arg.setFilter(i->i>=min);
		return command;
	}
	
	public ArcheCommand asInt(int min, int max){
		var arg = asIntInternal();
		arg.setFilter(i->(i>=min && i <= max));
		return command;
	}
	
	public ArcheCommand asInt(IntPredicate filter) {
		var arg = asIntInternal();
		arg.setFilter(i->filter.test(i));
		return command;
	}
	
	
	private CmdArg<Integer> asIntInternal(){
		CmdArg<Integer> arg = build(Integer.class);
		arg.setMapper(Ints::tryParse);
		command.addArg(arg);
		return arg;
	}
	
	public ArcheCommand asDouble() {
		asDoubleInternal();
		return command;
	}
	
	public ArcheCommand asDouble(double min){
		var arg = asDoubleInternal();
		arg.setFilter(d->d>=min);
		return command;
	}
	
	public ArcheCommand asDouble(double min, double max){
		var arg = asDoubleInternal();
		arg.setFilter(d->(d>=min && d <= max));
		return command;
	}
	
	public ArcheCommand asDouble(DoublePredicate filter) {
		var arg = asDoubleInternal();
		arg.setFilter(i->filter.test(i));
		return command;
	}
	
	private CmdArg<Double> asDoubleInternal(){
		CmdArg<Double> arg = build(Double.class);
		arg.setMapper(Doubles::tryParse);
		command.addArg(arg);
		return arg;
	}
	
	public ArcheCommand asString(){
		var arg = build(String.class);
		command.addArg(arg);
		return command;
	}
	
	public ArcheCommand asString(String... options){
		var arg = build(String.class);
		arg.setFilter( s-> Stream.of(options).filter(s2->s2.equalsIgnoreCase(s)).findAny().isPresent() );
		command.addArg(arg);
		return command;
	}
	
	public <T extends Enum<T>> ArcheCommand asEnum(Class<T>  clazz) {
		var arg = build(clazz);
		arg.setMapper(s->{
			try{ return Enum.valueOf(clazz, s); }
			catch(IllegalArgumentException e) {return null;}
		});
		command.addArg(arg);
		return command;
	}
	
	public ArcheCommand asPersona() {
		var arg = build(Persona.class);
		arg.setMapper(CommandUtil::personaFromArg);
		command.addArg(arg);
		return command;
	}
	
	public ArcheCommand asOfflinePersona() {
		var arg = build(OfflinePersona.class);
		arg.setMapper(CommandUtil::offlinePersonaFromArg);
		command.addArg(arg);
		return command;
	}
	
	
	private <T> CmdArg<T> build(Class<T> clazz){
		return new CmdArg<>(name, description, errorMessage, defaultInput);
	}
	
}
