package fr.skybeast.commandcreator.testplugin.bukkit;

import fr.skybeast.commandcreator.*;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//First file for CommandCreator's demo.
//Here we will talk about most of the features of CommandCreator.
//The command compound feature is explained in Demo2.java.
public final class Demo1
{
	//Hide the constructor because the constructor is useless.
	private Demo1() {}

	//Called from the main class.
	static void register(Plugin plugin)
	{
		CommandCreator.registerCommands(Demo1.class, plugin); //As simple as that :p

		//You don't have to register your command in your plugin.yml!
	}


	//Let's start creating commands! :p


	//Simple teleport command.
	@Command(value = "teleport", description = "Teleport a player to another player",
			permissions = {"demo.teleport"}, aliases = {"tp"})
	//WARNING: Never forget to add the 'static' modifier.
	public static void teleport(CommandSender sender, //First parameter is not an argument,
	                            // but always the sender of the command
	                            Player player1, //Of course, there is tab for this argument type completion in-game.
	                            Player player2)
	{
		player1.teleport(player2);
	}

	//Default serialization is supported for:
	//- All primitives & their wrappers
	//- Booleans w/ tab completion
	//- Command sender (Why not?) w/ tab completion
	//- Player w/ tab completion
	//- Offline Player w/ tab completion (Tab completion for only online players)

	//Let's make another command, but with optional arguments.

	@Command //If you don't specify the label, the method's name will be used.
	public static void giveMeDirt(Player sender, //The sender MUST be a Player
	                              @Opt Integer amount) //The amount is optional
	//-- Warning, you cannot infer primitives as optional arguments, because they can not be null.
	//Optional args must be last args.
	{
		int a = amount == null ? 42 : amount; //If no amount set, defaults to 42.

		sender.getInventory().addItem(new ItemStack(Material.DIRT, a));
	}

	//Every argument can be optional.

	//Now let's make a custom serializer!
	@Command
	public static void giveMe(Player sender,
	                          @Serial(MaterialSerializer.class) Material material, //Material is an enum, but @Serial
	                          // override choice list (see below)
	                          int amount)
	{
		sender.getInventory().addItem(new ItemStack(material, amount));
	}

	//My custom serializer.
	public static class MaterialSerializer implements CommandSerializer<Material>
	{
		private static final List<String> TAB_COMPLETES = Collections.unmodifiableList(
				Stream.of(Material.values())
						.map(Material::toString)
						.sorted()
						.collect(Collectors.toList())); //Optional - Don't bother about this if you don't want tab
		// completion.

		//Parse the String into a material or throw an exception.
		@Override
		public Material serialize(String arg) throws CommandSerializationException
		{
			Material material = Material.getMaterial(arg);

			if (material == null)
				throw new CommandSerializationException("Material ID " + arg + " don't exist.");

			return material;
		}

		//The value type -- Used to generate help messages.
		@Override
		public String valueType()
		{
			return "Material";
		}

		//Optional - Don't override for no tab completion.
		@Override
		public List<String> getAllTabCompletes()
		{
			return TAB_COMPLETES; //This list MUST be sorted.
		}
	}

	//Now, let's talk about Choice Lists.

	//This command sets a field for a player.
	@Command
	public static void setMeAField(Player sender,
	                               PlayerField field, //Tab completion in-game, of course.
	                               int value)
	{
		switch (field) //Remember: No default case because field cannot be null (non-optional)
		{
			case EXP:
				sender.setExp(value);
				break;
			case LVL:
				sender.setLevel(value);
				break;
		} //Izi - Switch-case powaa
	}

	//The choice list
	public enum PlayerField
	{
		EXP("exp"),
		LVL("lvl");

		private final String name;

		PlayerField(String name) {this.name = name;}

		//You don't have to, but redefining the toString() method with another field name may be useful, mostly if
		// you want to keep the Java constant naming convention, but don't want an ugly name in-game.
		@Override
		public String toString()
		{
			return name;
		}
	}

	//We are not even touching the end of the demo.
	//Still not convinced? Here is more.

	//Let's implement a whisper command.
	@Command
	public static void whisper(CommandSender sender,
	                           Player receiver,
	                           String... message) //An array can be last argument.
	//Arrays as last argument are compatible with @Serial, choice lists and @Opt. (And @Arg of course :p)
	{
		String fullMessage = String.join(" ", message); //Simple, no?
		receiver.sendMessage(fullMessage);
	}

	//Want argument-specific descriptions & names?
	//No problem.
	@Command
	public static void iRanOutOfIdea(
			CommandSender sender,
			@Arg(value = "name", desc = "This argument is cool!", type = "Not a STRING") String argument,
			@Arg("simplerArg") String arg2
	)
	{
		sender.sendMessage(argument); //Best command ever.
	}

	//WARNING: By default, the argument name is given by Reflection on the parameter.
	//Some compilers have a feature to disable these name, so the names may look like 'arg1' or 'arg2'...
	//This is ugly I know, that's why I made the @Arg feature.

	// +------------------------------------+ MORE FEATURES! +---------------------------------------------+
	//The command compound feature is shown in the file Demo2.java.
	//You can create commands which can be called only by the ConsoleCommandSender (kinda useless? why not.)
	//Every message in configurable in the config of the plugin.
}
