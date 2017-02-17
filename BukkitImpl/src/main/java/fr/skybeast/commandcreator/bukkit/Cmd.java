package fr.skybeast.commandcreator.bukkit;

import fr.skybeast.commandcreator.Command;
import fr.skybeast.commandcreator.CommandSerializationException;
import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by SkyBeast on 11/02/17.
 */
@Getter
abstract class Cmd
{
	protected String label;
	protected String[] permissions;
	protected String[] aliases;
	protected String description;

	/**
	 * Init the fields with annotation's parameters.
	 * If label is missing, default to fallbackLabel.
	 *
	 * @param annotation    the @Command annotation
	 * @param fallbackLabel the fallback label
	 */
	protected Cmd(Command annotation, String fallbackLabel)
	{
		label = annotation.value();
		if (label.isEmpty()) label = fallbackLabel;
		permissions = annotation.permissions();
		aliases = annotation.aliases();
		description = annotation.description();
	}

	/**
	 * Dispatch the command.
	 *
	 * @param sender    the sender of the command
	 * @param cmd       the arguments
	 * @param loc       the location of the argument to dispatch
	 * @param rootLabel the original label
	 * @return true if the command was properly dispatched
	 * @throws ReflectiveOperationException reflection-related method
	 */
	abstract boolean dispatch(CommandSender sender, String[] cmd, int loc, String rootLabel)
			throws ReflectiveOperationException;

	/**
	 * Tab-complete the command.
	 *
	 * @param sender the sender of the command
	 * @param cmd    the arguments
	 * @param loc    the location of the argument to tab-complete
	 * @return the sorted list of tab completes
	 */
	abstract List<String> completeTab(CommandSender sender, String[] cmd, int loc);

	/**
	 * Get the simple usage of the command.
	 *
	 * @return the simple usage of the command
	 */
	abstract String getSimpleUsage();

	/**
	 * Get the simple usage of the command.
	 *
	 * @return the simple usage of the command
	 */
	abstract String[] getParametersUsage();

	/**
	 * Show help for current method.
	 *
	 * @param sender    the sender
	 * @param cmd       the arguments
	 * @param loc       the location of the argument to dispatch
	 * @param rootLabel the original label
	 */
	protected void showHelp(CommandSender sender, String[] cmd, int loc, String rootLabel)
	{
		StringBuilder builder = new StringBuilder();

		if (loc != 0)
		{
			builder.append(rootLabel)
					.append(' ');

			for (int i = 0; i < loc - 1; i++)
				builder.append(cmd[i])
						.append(' ');
		}

		String simpleUsage = builder.append(getSimpleUsage())
				.toString();

		sender.sendMessage(String.format(CmdConfig.getHelpHeader(), simpleUsage));

		for (String usage : getParametersUsage())
			sender.sendMessage(String.format(CmdConfig.getHelpEntry(), usage));
	}

	/**
	 * Show message for invalid sender type.
	 *
	 * @param sender the sender
	 */
	protected void invalidSenderTypeMessage(CommandSender sender)
	{
		CmdSenderType senderType = CmdSenderType.get(sender.getClass());
		String name = senderType == null ? CmdConfig.getMessage("senderTypes.unknown") : senderType.getName();

		sender.sendMessage(String.format(CmdConfig.getInvalidSenderTypeMessage(), name));
	}

	/**
	 * Show message for lack of permission(s).
	 *
	 * @param sender the sender
	 */
	protected void noPermissionMessage(CommandSender sender)
	{
		sender.sendMessage(CmdConfig.getNoPermissionMessage());
	}

	/**
	 * Show message for serialization error.
	 *
	 * @param sender the sender
	 * @param err    the serialization error
	 */
	protected void showError(CommandSender sender, CommandSerializationException err)
	{
		sender.sendMessage(String.format(CmdConfig.getSerializationError(), err.getMessage()));
	}

	/**
	 * Dispatch parameters to method.
	 *
	 * @param method     the method
	 * @param parameters the parameters
	 * @return what the method returned, or true if return type is void
	 * @throws ReflectiveOperationException reflection-related method
	 */
	protected boolean dispatchToMethod(Method method, Object... parameters)
			throws ReflectiveOperationException
	{
		//Return type can be boolean or void
		Object ret = method.invoke(null, parameters);
		return ret == null || (Boolean) ret;
	}
}
