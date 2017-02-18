# CommandCreator
Creating commands in BungeeCord and Bukkit was never this simple!

With CommandCreator, you can create custom commands for your Bukkit server, or your BungeeCord proxy.

## Demonstration

List of features:
- Tab completion
- Custom help messages
- Dispatch command to your methods
- All messages are configurables
- Player-only commands (And Console-only commands)
- Custom argument parsing
- Default argument parsing (All primitives, Player, CommandSender, OfflinePlayer)
- Optional arguments
- 'Array as last argument'
- Choice lists (enum)
- Per-argument description, type, name
- Source code fully (yes, fully) documented
- [Command compounds](../src/main/java/fr/skybeast/commandcreator/testplugin/bukkit/Demo2.java)

[Demo1]: ../master/BukkitTestPlugin/src/main/java/fr/skybeast/commandcreator/testplugin/bukkit/Demo1.java
[Demo2]: ../master/BukkitTestPlugin/src/main/java/fr/skybeast/commandcreator/testplugin/bukkit/Demo2.java

Exemple:
```java
@Command("myTestCommand") //-- Usage: /myTestCommand <name|testCompound> ...
public final class MyTestCommand
{
	@Command(value = "name", description = "Description!", aliases = {"tp"})
	//-- Usage: /myTestCommand name {index} {player} [index2] [action]
	public static void test1(CommandSender sender, //Sender of the command
	                         @Arg("index") int i1, //Primitive serialization
	                         @Arg(value = "player", desc = "Awesome!") Player player, //Default serialization
	                         @Opt @Arg("index2") Integer i2, //@Opt = Optional
	                         @Opt @Arg("action") Action choice) //Enum choice list
	{/* Whatever */}


	@Command(aliases = {"testcmp"}) //-- Usage: /myTestCommand testCompound <subCommand|whisper> ...
	public static final class TestCompound
	{
		@Command //-- Usage: /myTestCommand testCompound subCommand {uuid}
		public static void subCommand(Player sender, //Only player can send messages
		                              @Arg("uuid") @Serial(MyObjectSerializer.class) UUID uuid) //Custom serialization
		{/* Whatever */}

		@Command
		public static void whisper(CommandSender sender,
		                           @Arg("receiver") Player receiver,
		                           @Arg("message") String... message) //Array as last argument
		{receiver.sendMessage(String.join(" ", message));}
	}

	// +-----------------+

	public enum Action //Custom choice list
	{
		CHOICE_1("choice1"),
		CHOICE_2("choice2"),
		CHOICE_3("choice3");

		private final String name;

		Action(String name) {this.name = name;}

		@Override
		public String toString() {return name;}
	}

	public static class MyObjectSerializer implements CommandSerializer<UUID> //Custom serializer
	{
		@Override
		public UUID serialize(String arg) throws CommandSerializationException
		{
			try {return UUID.fromString(arg);}
			catch (IllegalArgumentException ignored) {throw new CommandSerializationException("Invalid UUID");}
		}

		@Override
		public String valueType() {return "UUID";}
	}
}
```

Register command:
```java
CommandCreator.registerCommands(MyTestCommand.class, plugin);
```

### You can find a full demonstration [here][Demo1] and [here][Demo2].

---------------

As I suck at creating neat colored text, I would love your contribution for the default configuration.

I don't think this color config is really great right now, but everything you see is configurable.

- Root command:

![Root command](http://i.imgur.com/zV1wGUD.png)

- Simple command:

![Simple command](http://i.imgur.com/5c4EFed.png)

- Simple compound:

![Simple compound](http://i.imgur.com/ke7CzqF.png)

## Installation

### Plugin

To install CommandCreator on your server/proxy, you need to download a release.

Compatibility:
- Bukkit
- BungeeCord

[[Latest release](../releases/latest)]

### API

The API for Bukkit and BungeeCord is the same.

To add the CommandCreator API to your plugin, use this dependency:

#### Maven:

- In repositories:

```xml

<repository>
  <id>skybeast-repo</id>
  <url>https://dl.bintray.com/skybeastmc/maven</url>
</repository>
```

- In dependencies:

```xml
<dependency>
  <groupId>fr.skybeast</groupId>
  <artifactId>commandcreator-api</artifactId>
  <version>1.1</version>
</dependency>
```

#### Gradle:

- In repositories:

```groovy
maven {
    name = 'skybeast-repo'
    url = 'https://dl.bintray.com/skybeastmc/maven'
}
```

- In dependencies:

```groovy
compile 'fr.skybeast:commandcreator-api:1.1'
```
