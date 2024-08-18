package com.msdkremote.livequery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.msdkremote.commandserver.CommandHandler;
import com.msdkremote.commandserver.CommandServer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Locale;

import dji.sdk.keyvalue.converter.DJIValueConverter;
import dji.sdk.keyvalue.converter.IDJIValueConverter;
import dji.sdk.keyvalue.key.DJIActionKeyInfo;
import dji.sdk.keyvalue.key.DJIKey;
import dji.sdk.keyvalue.key.DJIKeyInfo;
import dji.sdk.keyvalue.key.FlightControllerKey;
import dji.sdk.keyvalue.key.KeyTools;
import dji.sdk.keyvalue.key.RemoteControllerKey;
import dji.sdk.keyvalue.value.common.EmptyMsg;
import dji.sdk.keyvalue.value.common.LocationCoordinate2D;
import dji.sdk.keyvalue.value.common.LocationCoordinate3D;
import dji.sdk.keyvalue.value.remotecontroller.BatteryInfo;
import dji.sdk.keyvalue.value.remotecontroller.RemoteControllerType;
import dji.sdk.kmz.value.base.DJIValue;
import dji.v5.common.callback.CommonCallbacks;
import dji.v5.common.error.IDJIError;
import dji.v5.manager.KeyManager;
import io.reactivex.internal.operators.observable.ObservableElementAt;

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
        String[] words = command.split(" ", 5);

        // Command should contain at least method and identifier.
        if (words.length < 2) {
            commandServer.sendMessage("Illegal syntax.");
            return;
        }

        // Command sub components.
        String commandMethod = words[0];
        String commandIdentifier = words[1] + " ";
        String moduleName = words.length >= 3 ? words[2] : "";
        String keyName = words.length >= 4 ? words[3] : "";
        String param = words.length == 5 ? words[4] : "";

        switch (commandMethod.toUpperCase(Locale.ENGLISH))
        {
            // Command - GET <identifier> <module> <key>
            case COMMAND_GET:
                commandGet(commandServer, commandIdentifier, moduleName, keyName);
                break;

            // Command - LISTEN <identifier> <module> <key>
            case COMMAND_LISTEN:
                commandListen(commandServer, commandIdentifier, moduleName, keyName);
                break;

            // Command - UNLISTEN <identifier> <module> <key>
            case COMMAND_CANCEL_LISTEN:
                commandUnListen(commandServer, commandIdentifier, moduleName, keyName);
                break;

            // Command - SET <identifier> <module> <key> <parameter>
            case COMMAND_SET:
                commandSet(commandServer, commandIdentifier, moduleName, keyName, param);
                break;

            // Command - ACTION <identifier> <module> <key>
            // Command - ACTION <identifier> <module> <key> <parameter>
            case COMMAND_ACTION:
                if (param.isEmpty())
                    commandAction(commandServer, commandIdentifier, moduleName, keyName);
                else
                    commandAction(commandServer, commandIdentifier, moduleName, keyName, param);
                break;

            // Command - HELP <identifier>
            // Command - HELP <identifier> <module>
            // Command - HELP <identifier> <module> <key>
            case COMMAND_HELP:
                if (moduleName.isEmpty())
                    commandHelp(commandServer, commandIdentifier);
                else if (keyName.isEmpty())
                    commandHelp(commandServer, commandIdentifier, moduleName);
                else
                    commandHelp(commandServer, commandIdentifier, moduleName, keyName);
                break;

            // Unknown command
            default:
                commandServer.sendMessage(commandIdentifier + "Unknown command: " + commandMethod);
                break;
        }
    }



        /*
        DJIKeyInfo<BatteryInfo> keyInfo = RemoteControllerKey.KeyBatteryInfo;

        keyInfo = new DJIKeyInfo<>(
                keyInfo.getComponentType(),
                keyInfo.getSubComponentType(),
                keyInfo.getIdentifier(),
                new DJIValueConverter<>(BatteryInfo.class)
        );

        KeyManager.getInstance().getValue(
                KeyTools.createKey(RemoteControllerKey.KeyBatteryInfo),
                new CommonCallbacks.CompletionCallbackWithParam<BatteryInfo>() {
                    @Override
                    public void onSuccess(BatteryInfo info) {
                        if (info != null)
                            commandServer.sendMessage("success : " + Arrays.toString(info.toBytes()));
                        else
                            commandServer.sendMessage("null ?");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        commandServer.sendMessage("fail - " + idjiError.toString());
                    }
                }
        );
         */

        /*
        StringBuilder stringBuilder = new StringBuilder();

        for (DJIKeyInfo<?> key : FlightControllerKey.getKeyList())
        {
            stringBuilder
                    .append('{')
                    .append(key.getIdentifier())
                    .append(',')
                    .append(" ")
                    .append(',')
                    .append("type:")
                    .append(key.getTypeConverter() != null ? key.getTypeConverter().getClassType().getCanonicalName() : "null")
                    .append('}');
        }
         */

        /*
        DJIKeyInfo<RemoteControllerType> key = RemoteControllerKey.KeyRemoteControllerType;
        RemoteControllerType type = RemoteControllerType.DJI_RC_PLUS;


        commandServer.sendMessage(key.getTypeConverter().fromStr(type.toString()).toString());

        try {
            Class<?> clazz = RemoteControllerKey.KeyRemoteControllerType.getTypeConverter().getClassType();
            Object object = clazz.getDeclaredConstructor().newInstance();
            commandServer.sendMessage(object.toString());
        }
        catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                 InstantiationException e) {
            commandServer.sendMessage(e.toString());
        }
         */

        /*
        KeyManager.getInstance().getValue(key,
                new CommonCallbacks.CompletionCallbackWithParam<BatteryInfo>() {
                    @Override
                    public void onSuccess(BatteryInfo obj) {
                        commandServer.sendMessage(obj == null ? "null" : obj.toString());
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        commandServer.sendMessage(idjiError.toString());
                    }
                }
        );
         */

    // commandServer.sendMessage(String.valueOf((Object) null));

    /**
     * Gets specific key, with common message when the key or module not found.
     *
     * @param commandServer the command server to send the result on.
     * @param commandIdentifier the command identifier of this command.
     * @param moduleName the name of the desired module.
     * @param keyName  the name of the desired key.
     * @return DJIKeyInfo if key found, null otherwise.
     */
    @Nullable
    private DJIKeyInfo<?> getKeyWithMessage(
            @NonNull CommandServer commandServer,
            @NonNull String commandIdentifier,
            @NonNull String moduleName,
            @NonNull String keyName)
    {
        DJIKeyInfo<?> key = null;

        // Get the key
        try {
            key = this.keysManager.getKeyInfo(moduleName, keyName);
        }
        // Module not found
        catch (UnknownModuleException ignored) {
            commandServer.sendMessage(commandIdentifier + "Unknown module name: " + moduleName);
        }
        // Key not found.
        catch (UnknownKeyException ignored) {
            commandServer.sendMessage(commandIdentifier + "Unknown key name: " + keyName);
        }

        return key;
    }

    private void commandGet(
            @NonNull CommandServer commandServer,
            @NonNull String commandIdentifier,
            @NonNull String moduleName,
            @NonNull String keyName)
    {
        DJIKeyInfo<?> key = getKeyWithMessage(commandServer, commandIdentifier, moduleName, keyName);

        if (key == null)
            return;

        if (!key.isCanGet()) {
            commandServer.sendMessage(commandIdentifier + "Command 'GET' not supported for: " + keyName);
            return;
        }

        KeyManager.getInstance().getValue(
                DJIKey.create(key),
                new CommonCallbacks.CompletionCallbackWithParam()
                {
                    @Override
                    public void onSuccess(Object o)
                    {
                        commandServer.sendMessage(commandIdentifier + o.toString());
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError)
                    {
                        commandServer.sendMessage(commandIdentifier + idjiError);
                    }
                }
        );
    }

    private void commandListen(
            @NonNull CommandServer commandServer,
            @NonNull String commandIdentifier,
            @NonNull String moduleName,
            @NonNull String keyName)
    {
        DJIKeyInfo<?> key = getKeyWithMessage(commandServer, commandIdentifier, moduleName, keyName);

        if (key == null)
            return;

        if (!key.isCanListen()) {
            commandServer.sendMessage(commandIdentifier + "Command 'LISTEN' not supported for: " + keyName);
            return;
        }

        KeyManager.getInstance().listen(
                DJIKey.create(key),
                this.keysManager,
                new CommonCallbacks.KeyListener()
                {
                    @Override
                    public void onValueChange(@Nullable Object oldValue, @Nullable Object newValue)
                    {
                        commandServer.sendMessage(commandIdentifier + newValue);
                    }
                }
        );
    }

    private void commandUnListen(
            @NonNull CommandServer commandServer,
            @NonNull String commandIdentifier,
            @NonNull String moduleName,
            @NonNull String keyName)
    {
        DJIKeyInfo<?> key = getKeyWithMessage(commandServer, commandIdentifier, moduleName, keyName);

        if (key == null)
            return;

        if (!key.isCanListen()) {
            commandServer.sendMessage(commandIdentifier + "Command 'LISTEN' not supported for: " + keyName);
            return;
        }

        KeyManager.getInstance().cancelListen(DJIKey.create(key));
    }

    private void commandSet(
            @NonNull CommandServer commandServer,
            @NonNull String commandIdentifier,
            @NonNull String moduleName,
            @NonNull String keyName,
            @NonNull String parameter)
    {

    }

    private void commandAction(
            @NonNull CommandServer commandServer,
            @NonNull String commandIdentifier,
            @NonNull String moduleName,
            @NonNull String keyName)
    {
        DJIKeyInfo<?> key = getKeyWithMessage(commandServer, commandIdentifier, moduleName, keyName);

        if (key == null)
            return;

        if (!key.isCanPerformAction()) {
            commandServer.sendMessage(commandIdentifier + "Command 'ACTION' not supported for: " + keyName);
            return;
        }

        /*
        KeyManager.getInstance().performAction(DJIKey.create(key),

                );

         */
    }

    private void commandAction(
            @NonNull CommandServer commandServer,
            @NonNull String commandIdentifier,
            @NonNull String moduleName,
            @NonNull String keyName,
            @NonNull String parameter)
    {
        // RemoteControllerType.values()[0].name();
    }

    /**
     * Send list of all available modules.
     *
     * @param commandServer the command server to send the result on.
     * @param commandIdentifier the command identifier of this command.
     */
    private void commandHelp(
            @NonNull CommandServer commandServer,
            @NonNull String commandIdentifier)
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
        commandServer.sendMessage(commandIdentifier + stringBuilder.toString());
    }

    /**
     * Send list of all available keys inside a module.
     *
     * @param commandServer the command server to send the result on.
     * @param commandIdentifier the command identifier of this command.
     * @param moduleName the name of the desired module.
     */
    private void commandHelp(
            @NonNull CommandServer commandServer,
            @NonNull String commandIdentifier,
            @NonNull String moduleName)
    {
        // Stores all available keys.
        String[] keys;

        try {
            keys = this.keysManager.getAvailableKeys(moduleName);
        }
        catch (UnknownModuleException ignore) {
            commandServer.sendMessage(commandIdentifier + "Unknown module name: " + moduleName);
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
        commandServer.sendMessage(commandIdentifier + stringBuilder.toString());
    }

    /**
     * Send information about specific key.
     *
     * @param commandServer the command server to send the result on.
     * @param commandIdentifier the command identifier of this command.
     * @param moduleName the name of the desired module.
     * @param keyName  the name of the desired key.
     */
    private void commandHelp(
            @NonNull CommandServer commandServer,
            @NonNull String commandIdentifier,
            @NonNull String moduleName,
            @NonNull String keyName)
    {
        // Get the desired key
        DJIKeyInfo<?> key = getKeyWithMessage(commandServer, commandIdentifier, moduleName, keyName);

        if (key == null)
            return;

        // Basic information
        StringBuilder message = new StringBuilder();
        message.append("{\"ModuleName\":")
                .append('\"').append(moduleName).append("\",")
                .append("\"KeyName\":")
                .append('\"').append(keyName).append("\",")
                .append("\"CanGet\":").append(key.isCanGet()).append(',')
                .append("\"CanSet\":").append(key.isCanSet()).append(',')
                .append("\"CanListen\":").append(key.isCanListen()).append(',')
                .append("\"CanAction\":").append(key.isCanPerformAction()).append(',');

        // Result type information
        if (key.getTypeConverter() != null)
        {
            Class<?> clazz = key.getTypeConverter().getClassType();
            message.append("\"ResultType\":\"").append(clazz.getCanonicalName()).append("\",");

            if (clazz.isEnum()) {
                message.append("\"Values\":[");

                try {
                    for (Object value : clazz.getEnumConstants())
                        message.append('\"').append(value.toString()).append("\",");
                }
                catch (NullPointerException ignored) { }

                message.append(']');
            }
            else {

                try {
                    Object value = clazz.getDeclaredConstructor().newInstance();
                    message.append("\"Example\":\"").append(value).append('\"');
                }
                catch (InvocationTargetException | IllegalAccessException |
                         InstantiationException | NoSuchMethodException ignored)
                {
                }
            }
        }
        else {
            message.append("\"ResultType\":null");
        }
        message.append(',');

        /*

            Class<?> clazz = RemoteControllerKey.KeyRemoteControllerType.getTypeConverter().getClassType();
            Object object = clazz.getDeclaredConstructor().newInstance();
            commandServer.sendMessage(object.toString());
         */

        commandServer.sendMessage(commandIdentifier + message.append('}').toString());
    }
}
