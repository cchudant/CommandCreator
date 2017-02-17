package fr.skybeast.commandcreator.testplugin.bukkit;

import fr.skybeast.commandcreator.CommandCreator;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by SkyBeast on 11/02/17.
 */
public class Main extends JavaPlugin
{
	@Override
	public void onEnable()
	{
		CommandCreator.registerCommands(MyCommand.class, this);
	}
}
