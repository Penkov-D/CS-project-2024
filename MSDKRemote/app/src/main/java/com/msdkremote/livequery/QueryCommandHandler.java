package com.msdkremote.livequery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.msdkremote.commandserver.CommandHandler;
import com.msdkremote.commandserver.CommandServer;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import dji.sdk.keyvalue.key.DJIKeyInfo;

public class QueryCommandHandler implements CommandHandler
{
    // Commands
    private static final String COMMAND_GET = "GET";
    private static final String COMMAND_LISTEN = "LISTEN";
    private static final String COMMAND_CANCEL_LISTEN = "UNLISTEN";
    private static final String COMMAND_SET = "SET";
    private static final String COMMAND_ACTION = "ACTION";
    private static final String COMMAND_HELP = "HELP";

    private final KeysManager keysManager;

    // Initialize KeysManager
    public QueryCommandHandler() {
        this.keysManager = KeysManager.getInstance();
    }

    /**
     * Handles command from the user.
     *
     * @param commandServer the server from which this call was made.
     * @param command the command that was received.
     */
    @Override
    public void onCommand(@NonNull CommandServer commandServer, @NonNull String command)
    {
        // Split command to words.
        String[] words = command.split(" ", 4);

        // Command sub components.
        String commandMethod = words[0];
        String moduleName = words.length >= 2 ? words[1] : "";
        String keyName = words.length >= 3 ? words[2] : "";
        String param = words.length == 4 ? words[3] : "";

        // If command is 'help', it can be parsed in several ways.
        if (commandMethod.toUpperCase(Locale.ENGLISH).equals(COMMAND_HELP))
        {
            // 'help' without arguments - return list of modules
            if (moduleName.isEmpty())
                commandHelp(commandServer);

            // 'help <module>' - return keys inside the module.
            else if (keyName.isEmpty())
                commandHelp(commandServer, moduleName);

            // 'help <module> <key>' - return description about the key.
            else
                commandHelp(commandServer, moduleName, keyName);

            return;
        }

        // Try to find the key by module name and key name.
        KeyItem<?,?> keyItem = getKeyWithMessage(commandServer, moduleName, keyName);
        if (keyItem == null) return;

        switch (commandMethod.toUpperCase(Locale.ENGLISH))
        {
            // Command - GET <identifier> <module> <key>
            case COMMAND_GET:
                keyItem.commandGet(commandServer);
                break;

            // Command - LISTEN <identifier> <module> <key>
            case COMMAND_LISTEN:
                keyItem.commandListen(commandServer);
                break;

            // Command - UNLISTEN <identifier> <module> <key>
            case COMMAND_CANCEL_LISTEN:
                keyItem.commandUnlisten(commandServer);
                break;

            // Command - SET <identifier> <module> <key> <parameter>
            case COMMAND_SET:
                keyItem.commandSet(commandServer, param);
                break;

            // Command - ACTION <identifier> <module> <key>
            // Command - ACTION <identifier> <module> <key> <parameter>
            case COMMAND_ACTION:
                if (param.isEmpty())
                    keyItem.commandAction(commandServer);
                else
                    keyItem.commandAction(commandServer, param);
                break;

            // Unknown command
            default:
                commandServer.sendMessage("Unknown command: " + commandMethod);
                break;
        }
    }


    /**
     * Gets specific key, with common message when the key or module not found.
     *
     * @param commandServer the command server to send the result on.
     * @param moduleName the name of the desired module.
     * @param keyName  the name of the desired key.
     * @return DJIKeyInfo if key found, null otherwise.
     */
    @Nullable
    private KeyItem<?,?> getKeyWithMessage(
            @NonNull CommandServer commandServer,
            @NonNull String moduleName,
            @NonNull String keyName)
    {
        KeyItem<?,?> key = null;

        // Get the key
        try {
            key = this.keysManager.getKeyInfo(moduleName, keyName);
        }
        // Module not found
        catch (UnknownModuleException ignored) {
            commandServer.sendMessage("Unknown module name: " + moduleName);
        }
        // Key not found.
        catch (UnknownKeyException ignored) {
            commandServer.sendMessage("Unknown key name: " + keyName);
        }

        return key;
    }


    /**
     * Send list of all available modules.
     *
     * @param commandServer the command server to send the result on.
     */
    private void commandHelp(
            @NonNull CommandServer commandServer)
    {
        // String builder for returned text
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('{');

        // Append all the available modules
        for (String moduleName : this.keysManager.getAvailableModules())
            stringBuilder.append('\"').append(moduleName).append('\"').append(',');

        // Change last comma to '}'
        stringBuilder.setCharAt(stringBuilder.length() - 1, '}');

        // Send list of available modules.
        commandServer.sendMessage(stringBuilder.toString());
    }


    /**
     * Send list of all available keys inside a module.
     *
     * @param commandServer the command server to send the result on.
     * @param moduleName the name of the desired module.
     */
    private void commandHelp(
            @NonNull CommandServer commandServer,
            @NonNull String moduleName)
    {
        // Stores all available keys.
        String[] keys;

        try {
            keys = this.keysManager.getAvailableKeys(moduleName);
        }
        catch (UnknownModuleException ignore) {
            commandServer.sendMessage("Unknown module name: " + moduleName);
            return;
        }

        // String builder for returned text
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('{');

        // Append all the available modules
        for (String key : keys)
            stringBuilder.append('\"').append(key).append('\"').append(',');

        // Change last comma to '}'
        stringBuilder.setCharAt(stringBuilder.length() - 1, '}');

        // Send list of available modules.
        commandServer.sendMessage(stringBuilder.toString());
    }


    /**
     * Send information about specific key.
     *
     * @param commandServer the command server to send the result on.
     * @param moduleName the name of the desired module.
     * @param keyName  the name of the desired key.
     */
    private void commandHelp(
            @NonNull CommandServer commandServer,
            @NonNull String moduleName,
            @NonNull String keyName)
    {
        // Get the desired key
        KeyItem<?,?> keyItem = getKeyWithMessage(commandServer, moduleName, keyName);
        if (keyItem == null) return;

        DJIKeyInfo<?> key = keyItem.getRawKeyInfo();

        // Basic information
        StringBuilder message = new StringBuilder("{");
        message.append("module:'").append(keyItem.getModuleName()).append("'")
                .append(", key:'").append(keyItem.getKeyName()).append("'")
                .append(", CanGet:").append(key.isCanGet())
                .append(", CanSet:").append(key.isCanSet())
                .append(", CanListen:").append(key.isCanListen())
                .append(", CanAction:").append(key.isCanPerformAction());


        // Result type information

        // If converter is null, notting to do.
        if (key.getTypeConverter() == null) {
            message.append(", parameter:null");
        }
        else
        {
            // Print the parameter type
            Class<?> clazz = key.getTypeConverter().getClassType();
            message.append(", parameter:'").append(clazz.getCanonicalName()).append("'");

            // If enum, print all the available values
            if (clazz.isEnum())
            {
                message.append(", values:[");

                // Iterate available values
                Object[] values = clazz.getEnumConstants();
                if (values != null)
                {
                    for (Object value : values)
                    {
                        // Add the original name of the enum object
                        if (!(value instanceof Enum<?>)) continue;
                        message.append(((Enum<?>) value).name()).append(',');
                    }
                }

                // Set the last ',' as ']'
                if (values != null && values.length > 0)
                    message.setCharAt(message.length() - 1, ']');

                else
                    message.append(']');
            }

            // Else, just print example
            else {

                final String canonicalName = clazz.getCanonicalName();

                if (canonicalName != null)
                {
                    // For primitive we get custom example
                    if (canonicalName.equals(Boolean.class.getCanonicalName()))
                        message.append(", example:\"true\"");

                    else if (canonicalName.equals(Integer.class.getCanonicalName()))
                        message.append(", example:\"1234\"");

                    else if (canonicalName.equals(Long.class.getCanonicalName()))
                        message.append(", example:\"12345678\"");

                    else if (canonicalName.equals(Double.class.getCanonicalName()))
                        message.append(", example:\"123.456\"");

                    else if (canonicalName.equals(String.class.getCanonicalName()))
                        message.append(", example:\"abcdef\"");

                    // For some general object, generate instance and call toString()
                    else {
                        try {
                            Object value = clazz.getDeclaredConstructor().newInstance();
                            message.append(", example:\"").append(value).append('\"');
                        }
                        catch (InvocationTargetException | IllegalAccessException |
                               InstantiationException | NoSuchMethodException ignored)
                        { }
                    }
                }

            }
        }

        commandServer.sendMessage(message.append('}').toString());
    }
}
