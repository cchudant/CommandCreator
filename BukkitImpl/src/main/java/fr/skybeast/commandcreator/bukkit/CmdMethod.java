package fr.skybeast.commandcreator.bukkit;

import fr.skybeast.commandcreator.*;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A command wrapped on a method.
 * <p>
 * Almost everything is cached in the field, so less cpu-intensive. (Reflection is slow)
 */
@ToString
final class CmdMethod extends Cmd
{
	/*
	 * Parameters
	 */
	private final Method method; //The backing method
	private final int parametersCount; //The number of parameters
	private int optStart = -1; //At which argument starts optionals? -- -1 if never
	private CmdSenderType senderType; //The sender type allowed
	private TIntObjectMap<CommandSerializer> serializers; //All the serializers; key = parameter number
	private TIntObjectMap<Map<String, Object>> choiceLists; //All the choice lists; key = parameter number

	/*
	 * Array -- Only if last argument is array
	 */
	private Class<?> arrayType; //The array type
	private CommandSerializer<?> arraySerializer; //The array serializer
	private Map<String, Object> arrayChoice; //The array choice list

	/*
	 * Messages
	 */
	@Getter
	private String[] parametersUsage; //All the usages of the parameters
	@Getter
	private String simpleUsage; //The usages of this command

	/* --------------------- */
	/* ---- FIELD SETUP ---- */
	/* --------------------- */

	/**
	 * Create a new CmdMethod
	 *
	 * @param method     the method to wrap
	 * @param annotation the annotation of the command
	 * @throws ReflectiveOperationException reflection-related method
	 */
	CmdMethod(Method method, Command annotation)
			throws ReflectiveOperationException
	{
		super(annotation, method.getName()); //Call papa

		this.method = method;

		validateStatic();
		validateReturnType();

		Parameter[] parameters = method.getParameters();
		parametersCount = parameters.length - 1;
		setupSenderType();
		iterate();
	}

	/**
	 * Throw error if method is not static.
	 */
	private void validateStatic()
	{
		if (!isStatic(method.getModifiers()))
			throw new CommandCreationException(method + " must be static to be a command");
	}

	/**
	 * Throw error if invalid return type is found.
	 */
	private void validateReturnType()
	{
		Class<?> returnType = method.getReturnType();
		if (returnType != void.class && returnType != boolean.class)
			throw new CommandCreationException(method + " cannot return " + returnType);
	}

	/**
	 * Iterate over all the parameters
	 *
	 * @throws ReflectiveOperationException reflection-related method
	 */
	private void iterate()
			throws ReflectiveOperationException
	{
		Parameter[] parameters = method.getParameters();

		StringBuilder simpleUsage = new StringBuilder(label)
				.append(' '); //Used for simple usage message
		List<String> parametersUsage = new ArrayList<>(); //Used for parameters usage message

		boolean isOptionalDone = false; //Used to know if optional already started
		for (int i = 0; i < parameters.length - 1; i++)
		{
			Parameter param = parameters[i + 1]; //Don't use first parameter -- sender type
			Class<?> type = param.getType();


			//-- Serialization handling
			Serial serAnnotation = param.getAnnotation(Serial.class);
			String valueType = registerSerializer(serAnnotation, i, type, parameters.length - 1);


			//-- Arg annotation handling
			Arg argAnnotation = param.getAnnotation(Arg.class);

			//Get fields from annotation or default
			String argType = getArgAnnotationValue(argAnnotation, Arg::type, valueType);
			String argName = getArgAnnotationValue(argAnnotation, Arg::value, param.getName());

			StringBuilder paramUsageBuilder = new StringBuilder(argName)
					.append(": ")
					.append(argType);

			//If has description, append it
			if (argAnnotation != null && !argAnnotation.desc().isEmpty())
				paramUsageBuilder.append(CmdConfig.getDescriptionSeparator())
						.append(argAnnotation.desc());


			//-- Optional handling
			Opt optAnnotation = param.getAnnotation(Opt.class);

			boolean paramHasOptional = optAnnotation != null;
			if (paramHasOptional)
			{
				if (!isOptionalDone)
				{
					optStart = i; //Optional parameters from this index
					isOptionalDone = true;
				}

				if (type.isPrimitive()) //Primitives are not nullable, so cannot be optionals
					throw new CommandCreationException("Optional value(s) cannot be primitives");

				paramUsageBuilder.append(' ')
						.append(CmdConfig.getOptional());
			}
			else
			{
				if (isOptionalDone) //The value is not optional but the previous value was
					throw new CommandCreationException("Optional value(s) must be last value(s)");
			}

			simpleUsage.append(
					String.format(CmdConfig.getMessage(paramHasOptional ? "simpleUsageOptional" :
							"simpleUsageRequired"), argName));

			//Prepare for next iteration
			simpleUsage.append(' ');
			parametersUsage.add(paramUsageBuilder.toString());
		}

		this.simpleUsage = simpleUsage.toString();
		this.parametersUsage = parametersUsage.toArray(new String[parametersUsage.size()]);
	}

	/**
	 * Get a value from the annotation, or the default value if the annotation is null, or if the value the empty
	 * string.
	 *
	 * @param annotation   The Arg annotation
	 * @param fn           the function which takes the argument to return its value
	 * @param defaultValue the default value
	 * @return the final value
	 */
	private String getArgAnnotationValue(Arg annotation, Function<Arg, String> fn, String defaultValue)
	{
		return annotation == null ? defaultValue : getFirstOrDefault(fn.apply(annotation), defaultValue);
	}

	/**
	 * Get first parameter if not empty, else default.
	 *
	 * @param first        the first parameter
	 * @param defaultValue the default value
	 * @return the final value
	 */
	private String getFirstOrDefault(String first, String defaultValue)
	{
		return first.isEmpty() ? defaultValue : first;
	}

	/**
	 * Register a serializer.
	 *
	 * @param serAnnotation the serializer annotation if present
	 * @param i             the index of the parameter
	 * @param type          the type of the parameter
	 * @param max           the max index of the parameter
	 * @return the string representation of the serializer
	 * @throws ReflectiveOperationException reflection-related method
	 */
	private String registerSerializer(Serial serAnnotation, int i, Class<?> type, int max)
			throws ReflectiveOperationException
	{
		//-- Array handling
		if (i == max - 1 && type.isArray())
		{
			arrayType = type.getComponentType();

			//The array has a Serialize annotation
			if (serAnnotation != null)
			{
				arraySerializer = CmdSerializers.serializerOf(arrayType, serAnnotation.value());

				return valueTypeOfSerializer(arraySerializer, arrayType) + CmdConfig.getCompoundSuffix();
			}
			//The array is a ChoiceList array
			else if (arrayType.isEnum())
			{
				arrayChoice = CmdChoiceLists.getFromEnum(arrayType);

				return arrayChoice.keySet().stream().collect(
						Collectors.joining(CmdConfig.getSeparator())) + CmdConfig.getCompoundSuffix();
			}

			CommandSerializer componentSerializer = CmdSerializers.getSerializer(type);
			//The array needs default serializing
			if (componentSerializer != null)
				arraySerializer = componentSerializer;

			//The array is a String array
			return valueTypeOfSerializer(arraySerializer, arrayType) + CmdConfig.getCompoundSuffix();
		}

		//-- Serialize annotation handling
		if (serAnnotation != null)
		{
			initSerializers();
			CommandSerializer<?> serializer = CmdSerializers.serializerOf(type, serAnnotation.value());
			serializers.put(i, serializer);

			return valueTypeOfSerializer(serializer, type);
		}
		else if (type != String.class)
		{
			//-- ChoiceList handling
			if (type.isEnum())
			{
				initChoiceLists();
				choiceLists.put(i, CmdChoiceLists.getFromEnum(type));

				return type.getSimpleName();
			}

			CommandSerializer serializer = CmdSerializers.getSerializer(type);
			//-- Default serialization
			if (serializer != null)
			{
				initSerializers();
				serializers.put(i, serializer);

				return valueTypeOfSerializer(serializer, type);
			}

			throw new CommandCreationException("Don't know how to serialize " + type);
		}

		return CmdConfig.getStringSerializer();
	}

	/**
	 * Return the value type of a serializer.
	 *
	 * @param serializer the serializer or null
	 * @param clazz      the clazz of the parameter
	 * @return the value type
	 */
	private String valueTypeOfSerializer(CommandSerializer<?> serializer, Class<?> clazz)
	{
		if (serializer == null)
			return CmdConfig.getStringSerializer();

		String valueType = serializer.valueType();
		return valueType.isEmpty() ? clazz.getSimpleName() : valueType;
	}

	/**
	 * Lazy-create the serializer map.
	 */
	private void initSerializers()
	{
		if (serializers == null)
			serializers = new TIntObjectHashMap<>();
	}

	/**
	 * Lazy-create the choice lists map.
	 */
	private void initChoiceLists()
	{
		if (choiceLists == null)
			choiceLists = new TIntObjectHashMap<>();
	}

	/**
	 * Setup the sender type field.
	 */
	private void setupSenderType()
	{
		Class[] parameters = method.getParameterTypes();

		if (parameters.length < 2)
			throw new CommandCreationException("Method " + method + " don't accept any command sender");

		senderType = CmdSenderType.get(parameters[0]);
		if (senderType == null)
			throw new CommandCreationException("Command sender type " + parameters[0] + " cannot be resolved on" +
					" method " + method);
	}

	/**
	 * Get if modifiers contain static.
	 *
	 * @param modifiers the modifiers
	 * @return true if modifiers contain static
	 */
	private boolean isStatic(int modifiers)
	{
		return (modifiers & Modifier.STATIC) != 0;
	}

	/* ------------------ */
	/* ---- DISPATCH ---- */
	/* ------------------ */

	@Override
	public boolean dispatch(CommandSender sender, String[] cmd, int loc, String rootLabel)
			throws ReflectiveOperationException
	{
		if (!checkPrerequisites(sender)) //Prerequisites
			return false;

		int count = cmd.length - loc; //Argument count
		if (!checkArgumentCount(count, sender, cmd, loc, rootLabel))//Not enough / Too much arguments
			return false;

		Object[] args = new Object[parametersCount + 1];
		args[0] = sender; //First parameter of the method is the sender

		for (int i = 0; i < count; i++)
		{
			String arg = cmd[loc + i];

			//-- Serializer handling
			CommandSerializer<?> serializer = getSerializer(i);
			if (serializer != null)
			{
				try
				{
					args[i + 1] = serializer.serialize(arg);
					continue;
				}
				catch (CommandSerializationException e)
				{
					showError(sender, e);
					return false;
				}
			}

			//-- Choice list handling
			Map<String, Object> choice = getChoiceList(i);
			if (choice != null)
			{
				Object o = choice.get(arg);
				if (o == null)
				{
					showHelp(sender, cmd, loc, rootLabel);
					return false;
				}
				args[i + 1] = o;
				continue;
			}

			//-- Array handling
			if (arrayType != null && i == parametersCount - 1)
			{
				int size = count - i;
				Object array = Array.newInstance(arrayType, size);


				if (arraySerializer != null) //Serializer
					for (int j = 0; j < size; j++)
					{
						try
						{
							Array.set(array, j, arraySerializer.serialize(cmd[loc + i + j]));
						}
						catch (CommandSerializationException e)
						{
							showError(sender, e);
							return false;
						}
					}
				else if (arrayChoice != null) //Choice list
					for (int j = 0; j < size; j++)
					{
						Object o = arrayChoice.get(cmd[loc + i + j]);
						if (o == null)
						{
							showHelp(sender, cmd, loc, rootLabel);
							return false;
						}

						Array.set(array, j, o);
					}
				else
					for (int j = 0; j < size; j++) //String
						Array.set(array, j, cmd[loc + i + j]);

				args[parametersCount] = array;
				break;
			}

			//-- Arg is a String
			args[i + 1] = arg;
		}

		return dispatchToMethod(method, args);
	}

	/**
	 * Check the prerequisites before dispatching command.
	 *
	 * @param sender the sender
	 * @return true if the prerequisites were met
	 */
	private boolean checkPrerequisites(CommandSender sender)
	{
		if (!Main.checkPermission(sender, permissions))
		{
			noPermissionMessage(sender);
			return false;
		}

		if (!senderType.isInstance(sender))
		{
			invalidSenderTypeMessage(sender);
			return false;
		}
		return true;
	}

	/**
	 * Check the argument count before dispatching command.
	 *
	 * @param count     the number of parameters
	 * @param sender    the sender of the command
	 * @param cmd       the arguments
	 * @param loc       the location of the current argument
	 * @param rootLabel the original label
	 * @return true if the prerequisites were met
	 */
	private boolean checkArgumentCount(int count, CommandSender sender, String[] cmd, int loc, String rootLabel)
	{
		if ((optStart == -1 ? count < parametersCount : count < optStart) || (arrayType == null && count >
				parametersCount))
		{
			showHelp(sender, cmd, loc, rootLabel);
			return false;
		}
		return true;
	}

	/**
	 * Get a serializer from its position in the serializer map.
	 *
	 * @param i the position of the argument
	 * @return the serializer of the argument
	 */
	private CommandSerializer getSerializer(int i)
	{
		if (serializers != null)
			return serializers.get(i);
		return null;
	}

	/**
	 * Get a choice list from its position in the choice list map.
	 *
	 * @param i the position of the argument
	 * @return the serializer of the argument
	 */
	private Map<String, Object> getChoiceList(int i)
	{
		if (choiceLists != null)
			return choiceLists.get(i);
		return null;
	}

	/* ---------------------- */
	/* ---- TAB-COMPLETE ---- */
	/* ---------------------- */

	@Override
	public List<String> completeTab(CommandSender sender, String[] cmd, int loc)
	{
		int last = cmd.length - 1;
		int param = last - loc;
		String str = cmd[last];

		CommandSerializer<?> serializer = getTabSerializer(param);
		if (serializer == null)
		{
			Map<String, Object> choice = getTabChoiceList(param);
			if (choice == null)
				return null;

			if (str.isEmpty())
				return new ArrayList<>(choice.keySet());
			return choice.keySet().stream()
					.filter(label -> label.startsWith(str))
					.collect(Collectors.toList());
		}

		List<String> tabCompletes = serializer.getAllTabCompletes();

		if (tabCompletes == null)
			return null;

		if (str.isEmpty())
			return tabCompletes;

		return tabCompletes.stream()
				.filter(label -> label.startsWith(str))
				.collect(Collectors.toList());

	}

	/**
	 * Get a serializer from its position in the serializer map.
	 * If i > parametersCount-1, return the array's serializer.
	 *
	 * @param i the position of the argument
	 * @return the serializer of the argument
	 */
	private CommandSerializer getTabSerializer(int i)
	{
		if (i >= parametersCount - 1)
			return arraySerializer;
		return getSerializer(i);
	}

	/**
	 * Get a choice list from its position in the choice list map.
	 * If i > parametersCount-1, return the array's choice list.
	 *
	 * @param i the position of the argument
	 * @return the serializer of the argument
	 */
	private Map<String, Object> getTabChoiceList(int i)
	{
		if (i >= parametersCount - 1)
			return arrayChoice;
		return getChoiceList(i);
	}
}
