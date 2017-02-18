package fr.skybeast.commandcreator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A command.
 * Can be put on a method or a class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Command
{
	/**
	 * @return the name of the command
	 */
	String value() default "";

	/**
	 * @return the name of the command
	 */
	String description() default "";

	/**
	 * @return the permissions needed to execute the command
	 */
	String[] permissions() default {};

	/**
	 * @return the aliases of the command
	 */
	String[] aliases() default {};
}
