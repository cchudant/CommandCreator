package fr.skybeast.commandcreator;

/**
 * A custom Exception thrown when parsing illegal 'syntax' in a command.
 */
public class CommandCreationException extends RuntimeException
{
	/**
	 * {@inheritDoc}
	 */
	public CommandCreationException() {}

	/**
	 * {@inheritDoc}
	 */
	public CommandCreationException(String var1) {super(var1);}

	/**
	 * {@inheritDoc}
	 */
	public CommandCreationException(String var1, Throwable var2) {super(var1, var2);}

	/**
	 * {@inheritDoc}
	 */
	public CommandCreationException(Throwable var1) {super(var1);}
}
