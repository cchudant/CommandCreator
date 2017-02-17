package fr.skybeast.commandcreator.bukkit;

import fr.skybeast.commandcreator.Command;
import fr.skybeast.commandcreator.CommandCreationException;
import fr.skybeast.commandcreator.CommandExecutor;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A compound of sub commands.
 */
@ToString
@Getter
final class CmdCompound extends Cmd
{
	private static final String[] EMPTY_STRING_ARRAY = new String[0];

	/*
	 * Default executor
	 */
	private CmdSenderType defaultExecutorSenderType; //The sender type of the default executor
	private Method defaultExecutor;

	/*
	 * Sub commands
	 */
	private final Map<String, Cmd> subCommands = new HashMap<>(); //The map of sub commands
	private final Map<String, Cmd> aliasMap = new HashMap<>(); //The map of aliases

	/*
	 * Messages
	 */
	@Getter
	private final String[] parametersUsage; //All the usages of the parameters
	@Getter
	private final String simpleUsage; //The usages of this command

	/* --------------------- */
	/* ---- FIELD SETUP ---- */
	/* --------------------- */

	CmdCompound(Class<?> clazz, Command annotation)
			throws ReflectiveOperationException
	{
		super(annotation, lowerFirst(clazz.getSimpleName())); //Call mama

		//Find sub commands in methods
		iterate(clazz.getMethods());

		//Find sub commands in inner classes
		iterate(clazz.getClasses());

		//Setup usages
		simpleUsage = setupSimpleUsage();
		parametersUsage = setupParametersUsage();
	}

	/**
	 * Lower the first letter of a String.
	 *
	 * @param str the String
	 * @return the new String
	 */
	private static String lowerFirst(String str)
	{
		char ch = Character.toLowerCase(str.charAt(0));
		return ch + str.substring(1, str.length());
	}

	/**
	 * Iterate through methods, and add sub commands.
	 *
	 * @param methods the methods
	 * @throws ReflectiveOperationException reflection-related method
	 */
	private void iterate(Method[] methods)
			throws ReflectiveOperationException
	{
		for (Method method : methods)
		{
			//Default executor
			if (method.getAnnotation(CommandExecutor.class) != null)
			{
				if (defaultExecutor != null)
					throw new CommandCreationException("Cannot have two CommandExecutor per command");

				if (method.getParameterCount() != 1)
					throw new CommandCreationException("A CommandExecutor should only have one argument");

				defaultExecutor = method;
				validateDefaultExecutor();
				continue;
			}

			//Sub command
			Command subAnnotation = method.getAnnotation(Command.class);
			if (subAnnotation == null)
				continue;

			addCommand(new CmdMethod(method, subAnnotation));
		}
	}

	/**
	 * Validate the default executor sender type.
	 */
	private void validateDefaultExecutor()
	{
		defaultExecutorSenderType = CmdSenderType.get(defaultExecutor.getParameterTypes()[0]);
		if (defaultExecutorSenderType == null)
			throw new CommandCreationException("Command sender type " + defaultExecutor.getParameterTypes()[0] +
					" cannot be resolved on method " + defaultExecutor);
	}

	/**
	 * Add a command to the command maps.
	 *
	 * @param cmd the command
	 */
	private void addCommand(Cmd cmd)
	{
		subCommands.put(cmd.getLabel(), cmd);
		for (String al : cmd.getAliases())
			aliasMap.put(al, cmd);
	}

	/**
	 * Iterate through classes, and add sub commands.
	 *
	 * @param classes the classes
	 * @throws ReflectiveOperationException reflection-related method
	 */
	private void iterate(Class[] classes)
			throws ReflectiveOperationException
	{
		for (Class<?> inner : classes)
		{
			//Sub command
			Command subAnnotation = inner.getAnnotation(Command.class);
			if (subAnnotation == null)
				continue;

			addCommand(new CmdCompound(inner, subAnnotation));
		}
	}

	/**
	 * Setup the simple usage.
	 *
	 * @return the simple usage
	 */
	private String setupSimpleUsage()
	{
		Collection<Cmd> commands = subCommands.values();

		if (commands.isEmpty())
			return label;

		if (commands.size() == 1)
			//noinspection OptionalGetWithoutIsPresent
			return label + ' '
					+ commands.stream().findFirst().get().getLabel()
					+ CmdConfig.getCompoundSuffix();

		int i = 0;
		StringBuilder b2 = new StringBuilder();
		for (Cmd cmd : commands)
		{
			b2.append(cmd.getLabel());
			if (i != commands.size() - 1)
				b2.append(CmdConfig.getSeparator());
			i++;
		}

		return label + ' ' + String.format(CmdConfig.getSimpleUsageCompound(), b2) + ' '
				+ CmdConfig.getCompoundSuffix();
	}

	/**
	 * Setup the parameters usage.
	 *
	 * @return the parameters usage
	 */
	private String[] setupParametersUsage()
	{
		Collection<Cmd> commands = subCommands.values();

		if (commands.isEmpty())
			return EMPTY_STRING_ARRAY;

		List<String> usages = new ArrayList<>();

		commands.forEach(
				cmd ->
				{
					StringBuilder builder = new StringBuilder(cmd.getSimpleUsage());
					String desc = cmd.getDescription();
					if (!desc.isEmpty())
						builder.append(CmdConfig.getDescriptionSeparator()).append(desc);

					usages.add(builder.toString());
				});

		return usages.toArray(new String[usages.size()]);
	}

	/* ------------------ */
	/* ---- DISPATCH ---- */
	/* ------------------ */

	@Override
	public boolean dispatch(CommandSender sender, String[] cmd, int loc, String rootLabel)
			throws ReflectiveOperationException
	{
		if (!checkPrerequisites(sender))
			return false;

		//Command called is this compound
		if (cmd.length == loc)
		{
			//Use default executor
			if (defaultExecutor != null)
			{
				if (!defaultExecutorSenderType.isInstance(sender))
				{
					invalidSenderTypeMessage(sender);
					return false;
				}

				return dispatchToMethod(defaultExecutor, sender);
			}

			//Or show help
			showHelp(sender, cmd, loc, rootLabel);
			return false;
		}

		//Command called is a sub command
		String string = cmd[loc];
		Cmd command = subCommands.get(string);
		if (command == null)
			command = aliasMap.get(string);

		if (command == null)
		{
			showHelp(sender, cmd, loc, rootLabel);
			return false;
		}

		//Dispatch to the sub command
		return command.dispatch(sender, cmd, loc + 1, rootLabel);
	}

	/**
	 * Check the prerequisites before dispatching command.
	 *
	 * @param sender the sender
	 * @return true if the prerequisites were met
	 */
	private boolean checkPrerequisites(CommandSender sender)
	{
		if (!Main.checkPermission(sender, permissions))
		{
			noPermissionMessage(sender);
			return false;
		}
		return true;
	}

	/* ---------------------- */
	/* ---- TAB-COMPLETE ---- */
	/* ---------------------- */

	@Override
	public List<String> completeTab(CommandSender sender, String[] cmd, int loc)
	{
		if (!Main.checkPermission(sender, permissions))
			return null;

		String str = cmd[loc];
		if (cmd.length == loc + 1)
		{
			if (str.isEmpty())
				return new ArrayList<>(subCommands.keySet());

			return subCommands.keySet().stream()
					.filter(label -> label.startsWith(str))
					.sorted()
					.collect(Collectors.toList());
		}

		Cmd c = getCommand(str);
		if (c == null)
			return null;

		return c.completeTab(sender, cmd, loc + 1);
	}

	/**
	 * Get a sub command from its label.
	 *
	 * @param label the label of the sub command
	 * @return the command or null if not found
	 */
	private Cmd getCommand(String label)
	{
		Cmd command = subCommands.get(label);
		if (command == null)
			return aliasMap.get(label);
		return command;
	}
}
