package fr.skybeast.commandcreator;

/**
 * The API for easy command-creation.
 */
public abstract class CommandCreator
{
	private static CommandCreator impl;

	protected CommandCreator()
	{
		if (impl != null)
			throw new IllegalStateException("Cannot have multiple implementation");
		impl = this;
	}

	/**
	 * Register commands from a class.
	 * @param clazz the class
	 * @param plugin the command's plugin
	 */
	public static void registerCommands(Class<?> clazz, Object plugin)
	{
		impl.registerCommandsImpl(clazz, plugin);
	}

	protected abstract void registerCommandsImpl(Class<?> clazz, Object plugin);
}
