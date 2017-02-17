package fr.skybeast.commandcreator.bukkit;

import fr.skybeast.commandcreator.Command;
import fr.skybeast.commandcreator.CommandCreationException;
import fr.skybeast.commandcreator.CommandCreator;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by SkyBeast on 11/02/17.
 */
public final class Main extends JavaPlugin
{
	/*
	 * Implementation
	 */
	private static final CommandCreator IMPL = new CommandCreator()
	{
		@Override
		protected void registerCommandsImpl(Class<?> clazz, Object plugin)
		{
			try
			{
				if (!(plugin instanceof Plugin))
					throw new CommandCreationException("Plugin given is not a Bukkit Plugin");

				register(clazz, (Plugin) plugin);
			}
			catch (ReflectiveOperationException e)
			{
				throw new CommandCreationException("Cannot create command", e);
			}
		}
	};

	/*
	 * Executors
	 */
	private static final org.bukkit.command.CommandExecutor COMMAND_EXECUTOR = Main::dispatchCommand;
	private static final TabCompleter TAB_COMPLETER = Main::completeTab;

	/*
	 * Cached reflection objects
	 */
	private static final Constructor<PluginCommand> PLUGIN_COMMAND_CONSTRUCTOR;
	private static final CommandMap COMMAND_MAP;


	/*
	 * Others
	 */
	private static final Map<PluginCommand, Cmd> BUKKIT_COMMANDS = new HashMap<>();
	@Getter
	private static Main instance;

	static
	{
		try
		{
			PLUGIN_COMMAND_CONSTRUCTOR = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			PLUGIN_COMMAND_CONSTRUCTOR.setAccessible(true);
			Field commandMap = SimplePluginManager.class.getDeclaredField("commandMap");
			commandMap.setAccessible(true);
			COMMAND_MAP = (CommandMap) commandMap.get(Bukkit.getPluginManager());
		}
		catch (ReflectiveOperationException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onEnable()
	{
		instance = this;
		CmdConfig.loadConfig();
	}

	/**
	 * Register class' commands.
	 *
	 * @param clazz  the class of the command
	 * @param plugin the plugin
	 * @throws ReflectiveOperationException reflection-related method
	 */
	private static void register(Class<?> clazz, Plugin plugin)
			throws ReflectiveOperationException
	{
		Command cmdAnnotation = clazz.getAnnotation(Command.class);

		if (cmdAnnotation != null) //Class is a command compound
			register(new CmdCompound(clazz, cmdAnnotation), plugin);
		else //Class has command methods
		{
			for (Method method : clazz.getMethods())
			{
				Command annotation = method.getAnnotation(Command.class);
				if (annotation != null)
					register(new CmdMethod(method, annotation), plugin);
			}
		}
	}

	/**
	 * Register a command into bukkit.
	 *
	 * @param cmd    the command
	 * @param plugin the plugin of the command
	 * @throws ReflectiveOperationException reflection-related method
	 */
	private static void register(Cmd cmd, Plugin plugin)
			throws ReflectiveOperationException
	{
		PluginCommand command = PLUGIN_COMMAND_CONSTRUCTOR.newInstance(cmd.getLabel(), plugin);
		command.getAliases().addAll(Arrays.asList(cmd.getAliases()));
		command.setExecutor(COMMAND_EXECUTOR);
		command.setTabCompleter(TAB_COMPLETER);
		COMMAND_MAP.register(plugin.getDescription().getName(), command);
		BUKKIT_COMMANDS.put(command, cmd);
	}

	/**
	 * Dispatch a command.
	 *
	 * @param sender  the sender
	 * @param command the bukkit command
	 * @param label   the label of the command
	 * @param args    the args of the command
	 * @return true if the command was dispatched correctly
	 */
	private static boolean dispatchCommand(CommandSender sender, org.bukkit.command.Command command,
	                                       String label, String[] args)
	{
		Cmd cmd = getCommand(command);

		try
		{
			return cmd.dispatch(sender, args, 0, label);
		}
		catch (InvocationTargetException e)
		{
			throw new CommandDispatchException("Error while dispatching command " + command, e.getCause());
		}
		catch (ReflectiveOperationException e)
		{
			throw new CommandDispatchException("Error while dispatching command " + command, e);
		}
	}

	/**
	 * Get a command from a bukkit command.
	 *
	 * @param command the bukkit command
	 * @return the command
	 */
	private static Cmd getCommand(org.bukkit.command.Command command)
	{
		if (!(command instanceof PluginCommand))
			throw new IllegalArgumentException("Cannot find command " + command);

		Cmd cmd = BUKKIT_COMMANDS.get(command);
		if (cmd == null)
			throw new IllegalArgumentException("Cannot find command " + command);

		return cmd;
	}

	/**
	 * Check permission of a sender.
	 * @param sender the sender
	 * @param permissions the permissions to check
	 * @return true if all permissions conditions were met
	 */
	static boolean checkPermission(CommandSender sender, String[] permissions)
	{
		if (permissions.length == 0)
			return true;

		for (String str : permissions)
			if (!sender.hasPermission(str))
				return false;

		return true;
	}

	/**
	 * Tab-complete a command.
	 *
	 * @param sender  the sender
	 * @param command the bukkit command
	 * @param label   the label of the command
	 * @param args    the args of the command
	 * @return the list of tab-completes
	 */
	private static List<String> completeTab(CommandSender sender, org.bukkit.command.Command command,
	                                        String label, String[] args)
	{
		Cmd cmd = getCommand(command);
		List<String> tabCompletes = cmd.completeTab(sender, args, 0);
		return tabCompletes == null ? Collections.emptyList() : tabCompletes;
	}
}
