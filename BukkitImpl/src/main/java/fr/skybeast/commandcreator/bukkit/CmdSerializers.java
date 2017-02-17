package fr.skybeast.commandcreator.bukkit;

import fr.skybeast.commandcreator.CommandSerializationException;
import fr.skybeast.commandcreator.CommandSerializer;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 'Static' class for handling serializers.
 */
final class CmdSerializers
{
	/**
	 * Used to cache all default serializers.
	 */
	private static final Map<Class<?>, CommandSerializer<?>> INSTANCES = new HashMap<>();

	/**
	 * Used to cache all custom serializers.
	 */
	private static final Map<Class<?>, CommandSerializer<?>> CACHE = new HashMap<>();


	/* ----------------------------- */
	/* ---- DEFAULT SERIALIZERS ---- */
	/* ----------------------------- */

	// Primitives

	static final CommandSerializer<Boolean> BOOLEAN = new ConstantTabCompleteCommandSerializer<>(
			Boolean::valueOf,
			name("boolean"),
			Arrays.asList("false", "true")
	);

	static final CommandSerializer<Byte> BYTE = new NoTabCommandSerializer<>(Byte::valueOf, name("byte"));
	static final CommandSerializer<Short> SHORT = new NoTabCommandSerializer<>(Short::valueOf, name("short"));
	static final CommandSerializer<Integer> INTEGER = new NoTabCommandSerializer<>(Integer::valueOf, name("integer"));
	static final CommandSerializer<Long> LONG = new NoTabCommandSerializer<>(Long::valueOf, name("long"));
	static final CommandSerializer<Float> FLOAT = new NoTabCommandSerializer<>(Float::valueOf, name("float"));
	static final CommandSerializer<Double> DOUBLE = new NoTabCommandSerializer<>(Double::valueOf, name("double"));
	static final CommandSerializer<Character> CHARACTER = new NoTabCommandSerializer<>(str -> str.charAt(0),
			name("character"));

	// Others

	static final CommandSerializer<Player> PLAYER = new CommandSerializerImpl<>(
			CmdSerializers::getPlayer,
			name("player"),
			() -> sort(getAllPlayers())
	);

	@SuppressWarnings("deprecation")
	static final CommandSerializer<OfflinePlayer> OFFLINE_PLAYER = new CommandSerializerImpl<>(
			Bukkit::getOfflinePlayer,
			name("offlinePlayer"),
			() -> sort(getAllPlayers())
	);

	static final CommandSerializer<CommandSender> COMMAND_SENDER = new CommandSerializerImpl<>(
			str -> "@CONSOLE".equalsIgnoreCase(str) ? Bukkit.getConsoleSender() : getPlayer(str),
			name("commandSender"),
			() -> sort(append(getAllPlayers(), "@CONSOLE"))
	);

	/*
	 * Setup the INSTANCES field.
	 */
	static
	{
		// Primitives
		INSTANCES.put(boolean.class, BOOLEAN);
		INSTANCES.put(Boolean.class, BOOLEAN);
		INSTANCES.put(byte.class, BYTE);
		INSTANCES.put(Byte.class, BYTE);
		INSTANCES.put(short.class, SHORT);
		INSTANCES.put(Short.class, SHORT);
		INSTANCES.put(int.class, INTEGER);
		INSTANCES.put(Integer.class, INTEGER);
		INSTANCES.put(long.class, LONG);
		INSTANCES.put(Long.class, LONG);
		INSTANCES.put(float.class, FLOAT);
		INSTANCES.put(Float.class, FLOAT);
		INSTANCES.put(double.class, DOUBLE);
		INSTANCES.put(Double.class, DOUBLE);
		INSTANCES.put(char.class, CHARACTER);
		INSTANCES.put(Character.class, CHARACTER);

		// Others
		INSTANCES.put(Player.class, PLAYER);
		INSTANCES.put(OfflinePlayer.class, OFFLINE_PLAYER);
		INSTANCES.put(CommandSender.class, COMMAND_SENDER);
	}

	/* ------------- */
	/* ---- API ---- */
	/* ------------- */

	/**
	 * Get a default serializer from its serialized class.
	 *
	 * @param <T>   the type of the serializer
	 * @param clazz the class
	 * @return the serializer
	 */
	@SuppressWarnings("unchecked")
	static <T> CommandSerializer<T> getSerializer(Class<T> clazz)
	{
		return (CommandSerializer<T>) INSTANCES.get(clazz);
	}

	/**
	 * Get a default serializer from its class and serialized class.
	 *
	 * @param clazz      the serialized class
	 * @param serializer the serializer class
	 * @param <T>        the type of the serialized class
	 * @return the command serializer
	 * @throws ReflectiveOperationException reflection-related method
	 */
	@SuppressWarnings("unchecked")
	static <T> CommandSerializer<T> serializerOf(Class<T> clazz, Class<? extends CommandSerializer> serializer)
			throws ReflectiveOperationException
	{
		CommandSerializer serial = CACHE.get(clazz);
		if (serial != null && serial.getClass() == serializer)
			return serial;
		serial = serializer.getConstructor().newInstance();
		CACHE.put(clazz, serial);
		return serial;
	}

	/* -------------------------- */
	/* ---- SERIALIZER UTILS ---- */
	/* -------------------------- */

	/**
	 * Get all online players' name.
	 *
	 * @return all online players' name into a modifiable list
	 */
	private static List<String> getAllPlayers()
	{
		return Bukkit.getOnlinePlayers().stream()
				.map(Player::getName)
				.collect(Collectors.toList());
	}

	/**
	 * Sort the list.
	 *
	 * @param list the list
	 * @return the list
	 */
	private static List<String> sort(List<String> list)
	{
		list.sort(null);
		return list;
	}

	/**
	 * Append a value to a list.
	 *
	 * @param list the list
	 * @param str  the value to append
	 * @return the list
	 */
	private static List<String> append(List<String> list, String str)
	{
		list.add(str);
		return list;
	}

	/**
	 * Get a serializer name from the config by its key.
	 *
	 * @param key the key
	 * @return the name
	 */
	private static String name(String key)
	{
		return CmdConfig.getMessage("serializerNames." + key);
	}

	/**
	 * Get a player from its name.
	 *
	 * @param str the name of the player
	 * @return the player
	 * @throws CommandSerializationException if the player was not found
	 */
	private static Player getPlayer(String str)
			throws CommandSerializationException
	{
		Player player = Bukkit.getPlayer(str);
		if (player == null)
			throw new CommandSerializationException("Cannot find player \"" + str + '"');
		return player;
	}

	/**
	 * Format an argument.
	 *
	 * @param formatter the formatter
	 * @param arg       the argument
	 * @param valueType the value type of the argument
	 * @param <T>       the type of argument
	 * @return the formatted argument
	 * @throws CommandSerializationException if error while formatting
	 */
	private static <T> T safeFormat(SafeFormatter<T> formatter, String arg, String valueType)
			throws CommandSerializationException
	{
		try
		{
			return formatter.format(arg);
		}
		catch (NumberFormatException ignored)
		{
			throw new CommandSerializationException("Input \"" + arg + "\" is not of type " + valueType);
		}
	}

	/**
	 * Argument-formatter.
	 *
	 * @param <T> the type of argument
	 */
	@FunctionalInterface
	private interface SafeFormatter<T>
	{
		T format(String arg) throws NumberFormatException, CommandSerializationException;
	}

	/**
	 * A serializer with no tab complete.
	 *
	 * @param <T> the type of argument
	 */
	@AllArgsConstructor
	private static class NoTabCommandSerializer<T> implements CommandSerializer<T>
	{
		final SafeFormatter<T> formatter;
		final String valueType;

		@Override
		public T serialize(String arg) throws CommandSerializationException
		{
			return safeFormat(formatter, arg, valueType);
		}

		@Override
		public String valueType()
		{
			return valueType;
		}
	}

	/**
	 * A serializer with tab completes.
	 *
	 * @param <T> the type of argument
	 */
	private static class CommandSerializerImpl<T> extends NoTabCommandSerializer<T>
	{
		final Supplier<List<String>> tabCompleter;

		CommandSerializerImpl(SafeFormatter<T> formatter, String valueType, Supplier<List<String>>
				tabCompleter)
		{
			super(formatter, valueType);
			this.tabCompleter = tabCompleter;
		}

		@Override
		public T serialize(String arg) throws CommandSerializationException
		{
			return formatter.format(arg);
		}

		@Override
		public List<String> getAllTabCompletes()
		{
			return tabCompleter.get();
		}
	}

	/**
	 * A serializer with constant tab completes.
	 *
	 * @param <T> the type of argument
	 */
	private static class ConstantTabCompleteCommandSerializer<T> extends NoTabCommandSerializer<T>
	{
		final List<String> tabCompletes;

		ConstantTabCompleteCommandSerializer(SafeFormatter<T> formatter, String valueType, List<String> tabCompletes)
		{
			super(formatter, valueType);
			this.tabCompletes = tabCompletes;
		}

		@Override
		public T serialize(String arg) throws CommandSerializationException
		{
			return formatter.format(arg);
		}

		@Override
		public List<String> getAllTabCompletes()
		{
			return tabCompletes;
		}
	}

	private CmdSerializers() {}
}
