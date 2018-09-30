package net.lordofthecraft.arche.command.annotate;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(METHOD)
@Repeatable(Flags.class)
public @interface Flag {
	//TODO
	String value();
	String description() default "A flag";
}
