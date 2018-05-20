package net.lordofthecraft.arche.command;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class CommandPart {

	private Predicate<RanCommand> tester;
	private Consumer<RanCommand> runner;
	
	public boolean test(RanCommand c) {
		return tester.test(c);
	}
	
	public void run(RanCommand c) {
		runner.accept(c);
	}
	
	private CommandPart(Predicate<RanCommand> p, Consumer<RanCommand> c) {
		this.tester = p;
		this.runner = c;
	}
	
	public static CommandPart asTester(Predicate<RanCommand> p) {
		return new CommandPart(p, $->{});
	}
	
	public static CommandPart asConsumer(Consumer<RanCommand> c) {
		return new CommandPart($->true, c);
	}
	
}
