package fr.skybeast.commandcreator;

import java.util.List;

/**
 * A custom command serializer to parse arguments.
 *
 * @param <T> the type of the serializer
 */
public interface CommandSerializer<T>
{
	/**
	 * @param arg the argument to serialize
	 * @return the serialized argument
	 * @throws CommandSerializationException thrown if illegal syntax is found
	 */
	T serialize(String arg)
			throws CommandSerializationException;

	/**
	 * Get the value type of the serializer.
	 *
	 * @return the value type of the serializer
	 */
	String valueType();

	/**
	 * Get tab completes for this serializers.
	 * <p>
	 * Warning: These tab complete must be sorted.
	 *
	 * @return the tab completes for this serializers
	 */
	default List<String> getAllTabCompletes()
	{
		return null;
	}
}
