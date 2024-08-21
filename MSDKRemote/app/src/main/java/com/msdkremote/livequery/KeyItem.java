package com.msdkremote.livequery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.msdkremote.commandserver.CommandServer;

import dji.sdk.keyvalue.converter.DJIValueConverter;
import dji.sdk.keyvalue.converter.IDJIValueConverter;
import dji.sdk.keyvalue.key.DJIActionKeyInfo;
import dji.sdk.keyvalue.key.DJIKey;
import dji.sdk.keyvalue.key.DJIKeyInfo;
import dji.sdk.keyvalue.key.FlightControllerKey;
import dji.sdk.keyvalue.key.ProductKey;
import dji.sdk.keyvalue.key.RemoteControllerKey;
import dji.sdk.keyvalue.value.common.EmptyMsg;
import dji.v5.common.callback.CommonCallbacks;
import dji.v5.common.error.IDJIError;
import dji.v5.manager.KeyManager;

public class KeyItem<Param, Result>
{
    // Whenever the operation is successful without specific output,
    // return this string to signal success.
    public static final String SUCCESS_MESSAGE = "success";

    // Whenever the operator needed a parameter, and the cast didn't succeed,
    // return this string to show the operation wasn't made.
    public static final String UNSUCCESSFUL_CAST = "could not cast parameter";

    @NonNull private final String moduleName;
    @NonNull private final String keyName;

    @NonNull private final DJIKeyInfo<Param> keyInfo;
    @Nullable private final DJIActionKeyInfo<Param, Result> ActionKeyInfo;


    /**
     * Construct new KeyItem.
     *
     * @param keyInfo the DJIKeyInfo this item represents.
     * @param moduleName the module associated with this key.
     */
    public KeyItem(@NonNull DJIKeyInfo<Param> keyInfo, @NonNull String moduleName)
    {
        this.keyInfo = keyInfo;
        this.moduleName = moduleName;
        this.keyName = keyInfo.getIdentifier();

        if (keyInfo instanceof DJIActionKeyInfo)
            this.ActionKeyInfo = (DJIActionKeyInfo<Param, Result>) keyInfo;

        else
            this.ActionKeyInfo = null;
    }


    /**
     * Get the associated module name for this key.
     *
     * @return string representing the module name.
     */
    @NonNull
    public String getModuleName() {
        return this.moduleName;
    }


    /**
     * Get the name of this key.
     *
     * @return string representing the key.
     */
    @NonNull
    public String getKeyName() {
        return this.keyName;
    }


    /**
     * Get the representing name of this key, used for notating the key.
     * Mainly used internally to use with the CommandServer.
     * <br/>
     * {@code moduleName} + " " + {@code keyName}
     *
     * @return unique string representing this {@code KeyInfo}.
     */
    @NonNull
    public String getPresentingName() {
        return this.moduleName + " " + this.keyName;
    }


    /**
     * Get the raw {@code DJIKeyInfo} of this {@code KeyInfo}.
     *
     * @return {@code DJIKeyInfo} making this {@code KeyInfo}
     */
    @NonNull
    public DJIKeyInfo<Param> getRawKeyInfo() {
        return this.keyInfo;
    }


    /**
     * Get the raw {@code DJIActionKeyInfo} of this {@code KeyInfo},
     * if this {@code KeyInfo} is created by action key,
     * else this method will return {@code null}.
     *
     * @return {@code DJIActionKeyInfo} of this InfoKey, or null.
     */
    @Nullable
    public DJIActionKeyInfo<Param, Result> getRawActionKeyInfo() {
        return this.ActionKeyInfo;
    }


    /**
     * Send message on associated CommandServer, with this KeyInfo identifier.
     *
     * @param server server of the communication.
     * @param message the string to send.
     */
    private void sendMessage(@NonNull CommandServer server, @NonNull String message) {
        server.sendMessage(this.getPresentingName() + " " + message);
    }


    /**
     * Send message on associated CommandServer, represented as object.
     *
     * @param server server of the communication.
     * @param objectMessage the object to send.
     */
    private void sendMessage(@NonNull CommandServer server, @Nullable Object objectMessage)
    {
        if (objectMessage == null)
            sendMessage(server, "null");
        else
            sendMessage(server, objectMessage.toString());
    }


    /**
     * Convert string to parameter. <br>
     * Supports classes, enums and Java types.
     *
     * @param parameter the parameter, in string format.
     * @return return the parameter in its raw format, or null if cast failed.
     */
    @Nullable
    private Param getParameter(@NonNull String parameter)
    {
        // If no TypeConverter is presented, notting to do.
        if (getRawKeyInfo().getTypeConverter() == null)
            return null;

        // Get class of parameter
        Class<?> clazz = getRawKeyInfo().getTypeConverter().getClassType();

        // If no Class<Param>, notting to do.
        if (clazz == null)
            return null;

        // If enum, find value by key.
        if (clazz.isEnum())
        {
            // Get all enums in this class
            Object[] enums = clazz.getEnumConstants();
            if (enums == null)
                return null;

            // Check if one of them match its name.
            for (Object enumObj : enums)
            {
                // If this is not enum type, notting to do.
                if (!(enumObj instanceof Enum))
                    continue;

                // Check if name matching
                if (((Enum<?>) enumObj).name().equals(parameter))
                    return (Param) enumObj;
            }
        }

        // If Java type, cast by string.
        // If general class, just cast.
        return (Param) getRawKeyInfo().getTypeConverter().fromStr(parameter);

        /*
         * Sadly, the casting is unchecked, e.g. (Param) obj is valid,
         * even if obj is not instance of Param. However, this should not be limitation,
         * as in Java, the generic type is only for convenience, and no casting error
         * will be thrown. Also, we at most call the toString() method, which present
         * in any object. Sadly, I have no clue how th DJI package handle the objects,
         * as it is obfuscated. Hopefully they didn't mismatch any type, and with all
         * my tests and examples, I didn't find one.
         */
    }


    /**
     * Command 'GET' on this KeyInfo, and return the message over CommandServer.
     * This command is asynchronous.
     *
     * @param server the server to return the command output over.
     */
    public void commandGet(@NonNull CommandServer server)
    {
        // Check if 'GET' is permitted for this KeyInfo.
        if (!keyInfo.isCanGet())
        {
            sendMessage(server, "Cannot command 'GET' on key.");
            return;
        }

        // Register getValue to KeyManager, and return the answer over the CommandServer.
        KeyManager.getInstance().getValue(
                DJIKey.create(keyInfo),
                new CommonCallbacks.CompletionCallbackWithParam<Param>()
                {
                    @Override
                    public void onSuccess(Param param)
                    {
                        sendMessage(server, param);
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError)
                    {
                        sendMessage(server, idjiError);
                    }
                }
        );
    }


    /**
     * Command 'LISTEN' on this KeyInfo, and return the message over CommandServer.
     * This command is asynchronous, and continuous while unlisten wasn't called.
     *
     * @param server the server to return the command output over.
     */
    public void commandListen(@NonNull CommandServer server)
    {
        // Check if 'LISTEN' is permitted for this KeyInfo.
        if (!keyInfo.isCanListen())
        {
            sendMessage(server, "Cannot command 'LISTEN' on key.");
            return;
        }

        // Register listen to KeyManager, and return the answer over the CommandServer.
        KeyManager.getInstance().listen(
                DJIKey.create(keyInfo),
                KeysManager.getInstance(),
                new CommonCallbacks.KeyListener<Param>() {
                    @Override
                    public void onValueChange(@Nullable Param oldValue, @Nullable Param newValue) {
                        sendMessage(server, newValue);
                    }
                }
        );
    }


    /**
     * Command 'UNLISTEN' on this KeyInfo, and return the message over CommandServer.
     * This command is synchronous, remove all listeners for this key.
     *
     * @param server the server to return the command output over.
     */
    public void commandUnlisten(@NonNull CommandServer server)
    {
        // Check if 'LISTEN' is permitted for this KeyInfo.
        if (!keyInfo.isCanListen())
        {
            sendMessage(server, "Cannot command 'LISTEN' on key.");
            return;
        }

        // Removes all the listeners over this KeyInfo,
        // and return the answer over the CommandServer.
        KeyManager.getInstance().cancelListen(DJIKey.create(keyInfo));
        sendMessage(server, SUCCESS_MESSAGE);
    }


    /**
     * Command 'SET' on this KeyInfo, and return the message over CommandServer.
     * This command is asynchronous.
     *
     * @param server the server to return the answer on.
     * @param parameter the parameter to set, in textual format.
     */
    public void commandSet(@NonNull CommandServer server, @NonNull String parameter)
    {
        // Check if 'SET' is permitted for this KeyInfo.
        if (!keyInfo.isCanSet())
        {
            sendMessage(server, "Cannot command 'SET' on key.");
            return;
        }

        // Get the parameter.
        Param param = getParameter(parameter);

        if (param == null) {
            sendMessage(server, UNSUCCESSFUL_CAST);
            return;
        }

        // Register the set, and return the answer over the CommandServer.
        KeyManager.getInstance().setValue(
                DJIKey.create(keyInfo),
                param,
                new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        sendMessage(server, SUCCESS_MESSAGE);
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        sendMessage(server, idjiError);
                    }
                }
        );
    }


    /**
     * Command 'ACTION' on this KeyInfo, without parameters,
     * and return the message over CommandServer. <br>
     * This command is synchronous.
     *
     * @param server the server to return the command output over.
     */
    public void commandAction(@NonNull CommandServer server)
    {
        // Check if 'ACTION' is permitted for this KeyInfo.
        if (!keyInfo.isCanPerformAction() || ActionKeyInfo == null)
        {
            sendMessage(server, "Cannot command 'ACTION' on key.");
            return;
        }

        // Register the action, and return the answer over the CommandServer.
        KeyManager.getInstance().performAction(
                DJIKey.create(ActionKeyInfo),
                new CommonCallbacks.CompletionCallbackWithParam<Result>() {
                    @Override
                    public void onSuccess(Result result) {
                        // If the result is empty message, signal success
                        if (result instanceof EmptyMsg) {
                            sendMessage(server, SUCCESS_MESSAGE);
                        }
                        // Else, return the original result
                        else {
                            sendMessage(server, result);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        sendMessage(server, idjiError);
                    }
                }
        );
    }


    /**
     * Command 'ACTION' on this KeyInfo, with parameters,
     * and return the message over CommandServer. <br>
     * This command is synchronous.
     *
     * @param server the server to return the command output over.
     * @param parameter the parameter to action with, in textual format.
     */
    public void commandAction(@NonNull CommandServer server, @NonNull String parameter)
    {
        // Check if 'ACTION' is permitted for this KeyInfo.
        if (!keyInfo.isCanPerformAction() || ActionKeyInfo == null)
        {
            sendMessage(server, "Cannot command 'ACTION' on key.");
            return;
        }

        // Get the parameter.
        Param param = getParameter(parameter);

        if (param == null) {
            sendMessage(server, UNSUCCESSFUL_CAST);
            return;
        }

        // Register the action, and return the answer over the CommandServer.
        KeyManager.getInstance().performAction(
                DJIKey.create(ActionKeyInfo),
                param,
                new CommonCallbacks.CompletionCallbackWithParam<Result>() {
                    @Override
                    public void onSuccess(Result result) {
                        // If the result is empty message, signal success
                        if (result instanceof EmptyMsg) {
                            sendMessage(server, SUCCESS_MESSAGE);
                        }
                        // Else, return the original result
                        else {
                            sendMessage(server, result);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        sendMessage(server, idjiError);
                    }
                }
        );
    }


    /**
     * Get string representing this KeyItem object.
     *
     * @return string with the module name, key name,
     *         and the string representing DJIKeyInfo.
     */
    @NonNull
    @Override
    public String toString() {
        return "KeyItem{" +
                "moduleName='" + moduleName + '\'' +
                ", keyName='" + keyName + '\'' +
                ", keyInfo=" + keyInfo +
                '}';
    }
}
