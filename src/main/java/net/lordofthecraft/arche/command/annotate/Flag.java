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
	String name();
	String[] aliases() default {};
	String description() default "";
	Class<?> type() default Void.class;
	String permission() default "";
	
	@Retention(RUNTIME)
	@Target(METHOD)
  @interface List { Flag[] value(); }
}
