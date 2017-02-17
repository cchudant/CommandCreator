package fr.skybeast.commandcreator.bungee;

import lombok.Getter;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * Enumeration of sender types.
 */
enum CmdSenderType
{
	PLAYER(ProxiedPlayer.class, "player"),
	CONSOLE(getConsoleCommandSender(), "console"),

	ALL(CommandSender.class, "all");

	private final Class<? extends CommandSender> clazz;
	@Getter
	private final String name;

	CmdSenderType(Class<? extends CommandSender> clazz, String config)
	{
		this.clazz = clazz;
		name = CmdConfig.getMessage("senderTypes." + config);
	}

	/**
	 * Get the console command sender class for BungeeCord.
	 *
	 * @return the console command sender class
	 */
	@SuppressWarnings("unchecked")
	private static Class<? extends CommandSender> getConsoleCommandSender()
	{
		try
		{
			return (Class<? extends CommandSender>) Class.forName("net.md_5.bungee.command.ConsoleCommandSender");
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Cannot find console command sender!", e);
		}
	}

	/**
	 * Get if a command sender is instance of this SenderType.
	 *
	 * @param sender the command sender
	 * @return true if the sender is instance of this SenderType
	 */
	boolean isInstance(CommandSender sender)
	{
		return clazz.isInstance(sender);
	}

	/**
	 * Get a sender type from its class.
	 *
	 * @param clazz the class
	 * @return the sender type or null if not found
	 */
	static CmdSenderType get(Class<?> clazz)
	{
		for (CmdSenderType type : values())
			if (type.clazz.isAssignableFrom(clazz))
				return type;

		return null;
	}
}
