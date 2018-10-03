package net.lordofthecraft.arche.command.annotate;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(METHOD)
@Repeatable(Flag.List.class)
public @interface Flag {
	String value();
	String[] aliases() default {};
	String description() default "";
	Class<?> type();
	
	@Retention(RUNTIME)
	@Target(METHOD)
  @interface List { Flag[] value(); }
}
