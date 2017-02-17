package fr.skybeast.commandcreator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Make an argument optional by adding this annotation to a parameter.
 * <p>
 * Optional arguments must be last.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Opt
{
}
