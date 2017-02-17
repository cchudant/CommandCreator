package fr.skybeast.commandcreator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Customize an argument by adding this annotation to a parameter.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Arg
{
	/**
	 * @return the name of the argument
	 */
	String value();

	/**
	 * @return the description of the argument
	 */
	String desc() default "";

	/**
	 * @return the type of the argument
	 */
	String type() default "";
}
