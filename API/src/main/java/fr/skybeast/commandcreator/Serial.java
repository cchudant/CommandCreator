package fr.skybeast.commandcreator;

import java.lang.annotation.*;

/**
 * Add a custom serializer to an argument by adding this annotation to a parameter.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Serial
{
	/**
	 * @return the serializer for this parameter.
	 */
	Class<? extends CommandSerializer> value();
}
