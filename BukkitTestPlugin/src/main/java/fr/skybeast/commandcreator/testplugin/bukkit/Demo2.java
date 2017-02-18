package fr.skybeast.commandcreator.testplugin.bukkit;

import fr.skybeast.commandcreator.Command;
import fr.skybeast.commandcreator.CommandCreator;
import fr.skybeast.commandcreator.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

//Second file for CommandCreator's demo.
//Read the first one first ;p
//Here we will cover command compounds.

//As you can see this class is annotated with @Command.
//This class is ITSELF a command.

@Command("myplugin")
public final class Demo2
{
	//There is, of course, tab completion for all sub commands.

	//Command /myplugin reload
	@Command(aliases = "rl") //Permissions and aliases supported in sub commands.
	public static void reload(CommandSender sender)
	{
		sender.sendMessage("You want to reload, isn't it?");
	}

	//-- Optional:
	//If you specify a @CommandExecutor, the method will be the default executor for the command.
	//Then when you try to execute the command without any argument, this executor will be called.
	@CommandExecutor
	public static void execute(CommandSender sender)
	{
		sender.sendMessage("Hello from the default executor");
	}

	//Every feature is supported in sub commands.
	//Even sub commands in sub commands work:

	//Command /myplugin admin ... (notice that the first letter is lowercase)
	@Command
	public static final class Admin
	{
		//Command /myplugin admin mode {boolean}
		@Command(description = "Sets admin mode")
		public static void mode(Player sender,
		                        boolean adminMode)
		{
			sender.sendMessage("You want to set the admin mode to " + adminMode);
		}

		//Let's get crazy!

		//Command /myplugin admin inner ...
		@Command
		public static final class Inner
		{
			//Command /myplugin admin inner inner1 ...
			@Command
			public static final class Inner1
			{
				@Command
				public static void sayHey(CommandSender sender) {sender.sendMessage("Hey!");}

				private Inner1() {}
			}

			//Command /myplugin admin inner inner2 ...
			@Command
			public static final class Inner2
			{
				@Command
				public static void saySomething(CommandSender sender) {sender.sendMessage("Something!");}

				private Inner2() {}
			}

			private Inner() {}
		}

		private Admin() {}
	}

	// +-------------------+ //

	static void register(Plugin plugin)
	{
		CommandCreator.registerCommands(Demo2.class, plugin); //Register the compound class.
	}

	private Demo2() {}

	//That's it.
	//Less poce ble
}
