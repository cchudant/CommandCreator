package fr.skybeast.commandcreator.testplugin.bungee;

import fr.skybeast.commandcreator.CommandCreator;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * Created by SkyBeast on 11/02/17.
 */
public class Main extends Plugin
{
	@Override
	public void onEnable()
	{
		CommandCreator.registerCommands(MyCommand.class, this);
	}
}
