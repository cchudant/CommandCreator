package fr.skybeast.commandcreator.bukkit;

/**
 * A custom Exception to rethrow errors in commands.
 */
final class CommandDispatchException extends RuntimeException
{
	CommandDispatchException() {}

	CommandDispatchException(String var1) {super(var1);}

	CommandDispatchException(String var1, Throwable var2) {super(var1, var2);}

	CommandDispatchException(Throwable var1) {super(var1);}
}
