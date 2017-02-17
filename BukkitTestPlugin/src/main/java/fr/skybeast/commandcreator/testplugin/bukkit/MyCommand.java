package fr.skybeast.commandcreator.testplugin.bukkit;

import fr.skybeast.commandcreator.*;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Created by SkyBeast on 11/02/17.
 */
// command '/test'
@Command("tt")
public final class MyCommand
{
	private MyCommand() {}

	//@CommandExecutor
	// command '/test'
	// Usage: /test
	// return optional boolean
	public static void execute(CommandSender sender)
	{
		sender.sendMessage("execute");
	}

	@Command
	// command '/test put'
	// Usage: /test put {key} {value}
	// only player
	public static boolean put(Player player, String key, int value)
	{
		player.sendMessage("put " + key + ", " + value);
		return true;
	}

	// command '/test admin'
	// Usage: /test admin {test|testOptional} ...
	@Command
	public static final class Admin
	{
		private Admin() {}

		// command '/test admin test ...'
		// Usage: /test admin test {slt} {object} {add|remove}
		// only console command sender
		@Command
		public static void test(CommandSender sender,
		                        @Arg(value = "slt", desc = "Go die", type = "NOT a String") String slt,
		                        @Opt @Serial(MyObjectSerializer.class) MyObject object,
		                        @Opt TestChoice... choice)
		{
			sender.sendMessage("slt=" + slt + ", object=" + object + ", choice=" + Arrays.toString(choice));
		}

		// command '/test admin test ...'
		// Usage: /test admin test {slt} {object} {add|remove}
		// only console command sender
		@Command
		public static void testd(CommandSender sender, String... slt)
		{
			sender.sendMessage("slt=" + Arrays.toString(slt));
		}

		// command '/test admin testOptional ...'
		// Usage: /test admin testOptional [str]
		// all sort of command sender
		@Command
		public static void testOptional(ConsoleCommandSender sender, int integer)
		{
			sender.sendMessage("integer=" + integer);
		}

		// command '/test admin testOptional ...'
		// Usage: /test admin testOptional [str]
		// all sort of command sender
		@Command(value = "uuu", aliases = {"slt", "123"})
		public static void hoyo(Player sender, Player ss)
		{
			sender.sendMessage("ss=" + ss);
		}

	}

	/**
	 * Sample object.
	 */
	public static class MyObject
	{
	}

	public static class MyObjectSerializer implements CommandSerializer<MyObject>
	{
		private final List<String> list = Arrays.asList("slt", "myObject");

		public MyObjectSerializer()
		{
			list.sort(null);
		}

		@Override
		public MyObject serialize(String arg)
		{
			return new MyObject();
		}

		@Override
		public String valueType()
		{
			return "MyObject";
		}

		@Override
		public List<String> getAllTabCompletes()
		{
			return list;
		}
	}

	public enum TestChoice
	{
		ADD("add"),
		REMOVE("remove");

		private final String name;

		TestChoice(String name)
		{
			this.name = name;
		}

		public String toString()
		{
			return name;
		}
	}
}
