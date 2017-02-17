package fr.skybeast.commandcreator.bukkit;

import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Enumeration of sender types.
 */
enum CmdSenderType
{
	PLAYER(Player.class, "player"),
	CONSOLE(ConsoleCommandSender.class, "console"),
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
