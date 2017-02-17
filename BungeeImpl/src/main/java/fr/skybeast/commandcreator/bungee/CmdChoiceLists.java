package fr.skybeast.commandcreator.bungee;

import fr.skybeast.commandcreator.CommandCreationException;

import java.util.HashMap;
import java.util.Map;

/**
 * 'Static' class for handling choice lists.
 */
final class CmdChoiceLists
{
	private static final Map<Class<?>, Map<String, Object>> ENUMS = new HashMap<>();

	/**
	 * Get a choice list from an enum.
	 *
	 * @param clazz the enum
	 * @return the choice list
	 * @throws ReflectiveOperationException reflection-related method
	 */
	static Map<String, Object> getFromEnum(Class<?> clazz)
			throws ReflectiveOperationException
	{
		Map<String, Object> map = ENUMS.get(clazz);
		if (map == null)
		{
			map = iterate(clazz);
			ENUMS.put(clazz, map);
		}

		return map;
	}

	/**
	 * Iterate through the enum, find all constants and map them into a choice list.
	 *
	 * @param clazz the enum
	 * @return the enum constants as a choice list
	 * @throws ReflectiveOperationException reflection-related method
	 */
	private static Map<String, Object> iterate(Class<?> clazz)
			throws ReflectiveOperationException
	{
		if (!clazz.isEnum())
			throw new CommandCreationException(clazz + " is not an enum, so cannot be used as a ChoiceList");

		Map<String, Object> map = new HashMap<>();
		for (Object o : clazz.getEnumConstants())
			map.put(o.toString(), o);

		return map;
	}

	private CmdChoiceLists() {}
}
