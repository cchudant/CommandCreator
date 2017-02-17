package fr.skybeast.commandcreator.bungee;

/**
 * A custom Exception to rethrow errors in commands.
 */
final class CommandDispatchException extends RuntimeException
{
	/**
	 * {@inheritDoc}
	 */
	CommandDispatchException() {}

	/**
	 * {@inheritDoc}
	 */
	CommandDispatchException(String var1) {super(var1);}

	/**
	 * {@inheritDoc}
	 */
	CommandDispatchException(String var1, Throwable var2) {super(var1, var2);}

	/**
	 * {@inheritDoc}
	 */
	CommandDispatchException(Throwable var1) {super(var1);}
}
