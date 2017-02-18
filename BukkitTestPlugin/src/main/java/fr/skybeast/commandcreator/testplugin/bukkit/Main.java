package fr.skybeast.commandcreator.testplugin.bukkit;

import org.bukkit.plugin.java.JavaPlugin;

//The demo is available in Demo1.java and Demo2.java
public class Main extends JavaPlugin
{
	@Override
	public void onEnable()
	{
		Demo1.register(this);
		Demo2.register(this);
	}
}
