# CommandCreator
Creating commands in BungeeCord and Bukkit was never this simple!

With CommandCreator, you can create custom commands for your Bukkit server, or your BungeeCord proxy.

## Demonstration

You can find a full demonstration [here][Demo1]
and [here](../src/main/java/fr/skybeast/commandcreator/testplugin/bukkit/Demo2.java).

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

[Demo1]: ../BukkitTestPlugin/src/main/java/fr/skybeast/commandcreator/testplugin/bukkit/Demo1.java
[Demo2]: ../BukkitTestPlugin/src/main/java/fr/skybeast/commandcreator/testplugin/bukkit/Demo2.java

As I suck at creating neat colored text, I would love your contribution for the default configuration.

I don't think this color config is really great right now, but everything you see is configurable.

- Command compound:

![Command compound](http://i.imgur.com/hG7TjZs.png)

- Simple command:

![Simple command](http://i.imgur.com/15Q0gf5.png)

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

Maven:
```xml
<dependency>
  <groupId>fr.skybeast</groupId>
  <artifactId>commandcreator-api</artifactId>
  <version>1.1</version>
</dependency>
```

Gradle:
```groovy
compile 'fr.skybeast:commandcreator-api:1.1'
```

Ivy:
```xml
<dependency org='fr.skybeast' name='commandcreator-api' rev='1.1'>
  <artifact name='commandcreator-api' ext='pom' ></artifact>
</dependency>
```
