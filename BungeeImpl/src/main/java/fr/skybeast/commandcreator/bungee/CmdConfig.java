package fr.skybeast.commandcreator.bungee;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Created by SkyBeast on 13/02/17.
 */
final class CmdConfig
{
	private static final File FILE = new File(Main.getInstance().getDataFolder(), "messages.yml");
	private static Configuration config;

	/**
	 * Load the config.
	 */
	static void loadConfig()
	{
		try
		{
			if (!FILE.exists())
			{
				InputStream link = (CmdConfig.class.getResourceAsStream("/messages.yml"));
				//noinspection ResultOfMethodCallIgnored
				FILE.getParentFile().mkdirs();

				Files.copy(link, FILE.getAbsoluteFile().toPath());
			}

			config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(FILE);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Cannot load messages", e);
		}
	}

	/**
	 * Get a message from its key.
	 *
	 * @param key the key
	 * @return the message
	 */
	static String getMessage(String key)
	{
		return config.getString(key, key);
	}

	/*
	 * Messages
	 */

	static String getHelpHeader()
	{
		return getMessage("helpHeader");
	}

	static String getHelpEntry()
	{
		return getMessage("helpEntry");
	}

	static String getOptional()
	{
		return getMessage("optional");
	}

	static String getSimpleUsageCompound()
	{
		return getMessage("simpleUsageCompound");
	}

	static String getSimpleUsageRequired()
	{
		return getMessage("simpleUsageRequired");
	}

	static String getSimpleUsageOptional()
	{
		return getMessage("simpleUsageOptional");
	}

	static String getCompoundSuffix()
	{
		return getMessage("compoundSuffix");
	}

	static String getSeparator()
	{
		return getMessage("separator");
	}

	static String getDescriptionSeparator()
	{
		return getMessage("descriptionSeparator");
	}

	static String getStringSerializer()
	{
		return getMessage("serializerNames.string");
	}

	static String getInvalidSenderTypeMessage()
	{
		return getMessage("invalidSenderTypeMessage");
	}

	static String getNoPermissionMessage()
	{
		return getMessage("noPermissionMessage");
	}

	static String getSerializationError()
	{
		return getMessage("serializationError");
	}

	private CmdConfig() {}
}
