package fr.skybeast.commandcreator.bungee;

import fr.skybeast.commandcreator.Command;
import fr.skybeast.commandcreator.CommandCreationException;
import fr.skybeast.commandcreator.CommandCreator;
import lombok.Getter;
import lombok.ToString;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * Created by SkyBeast on 11/02/17.
 */
public final class Main extends Plugin
{
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
	@Getter
	private static Main instance;

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
	 * Register a command into BungeeCord.
	 *
	 * @param cmd    the command
	 * @param plugin the plugin of the command
	 * @throws ReflectiveOperationException reflection-related method
	 */
	private static void register(Cmd cmd, Plugin plugin)
			throws ReflectiveOperationException
	{
		CustomCommand command = new CustomCommand(cmd);
		ProxyServer.getInstance().getPluginManager().registerCommand(plugin, command);
	}

	/**
	 * Dispatch a command.
	 *
	 * @param sender the sender
	 * @param cmd    the command
	 * @param label  the label of the command
	 * @param args   the args of the command
	 * @return true if the command was dispatched correctly
	 */
	private static boolean dispatchCommand(CommandSender sender, Cmd cmd,
	                                       String label, String[] args)
	{
		try
		{
			return cmd.dispatch(sender, args, 0, label);
		}
		catch (InvocationTargetException e)
		{
			throw new CommandDispatchException("Error while dispatching command " + cmd, e.getCause());
		}
		catch (ReflectiveOperationException e)
		{
			throw new CommandDispatchException("Error while dispatching command " + cmd, e);
		}
	}

	/**
	 * Check permission of a sender.
	 *
	 * @param sender      the sender
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
	 * @param sender the sender
	 * @param cmd    the command
	 * @param label  the label of the command
	 * @param args   the args of the command
	 * @return the list of tab-completes
	 */
	private static List<String> completeTab(CommandSender sender, Cmd cmd,
	                                        String label, String[] args)
	{
		List<String> tabCompletes = cmd.completeTab(sender, args, 0);
		return tabCompletes == null ? Collections.emptyList() : tabCompletes;
	}

	@ToString
	private static class CustomCommand extends net.md_5.bungee.api.plugin.Command implements TabExecutor
	{
		final Cmd cmd;

		CustomCommand(Cmd cmd)
		{
			super(cmd.getLabel(), null, cmd.getAliases());
			this.cmd = cmd;
		}

		@Override
		public void execute(CommandSender sender, String[] args)
		{
			dispatchCommand(sender, cmd, getName(), args);
		}

		@Override
		public Iterable<String> onTabComplete(CommandSender sender, String[] args)
		{
			return completeTab(sender, cmd, getName(), args);
		}
	}
}
