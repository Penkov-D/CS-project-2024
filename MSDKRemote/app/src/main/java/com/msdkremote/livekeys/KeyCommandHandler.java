package com.msdkremote.livekeys;

import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.msdkremote.R;
import com.msdkremote.commandserver.CommandHandler;
import com.msdkremote.commandserver.CommandServer;

import dji.sdk.keyvalue.key.BatteryKey;
import dji.sdk.keyvalue.key.KeyTools;
import dji.v5.common.callback.CommonCallbacks;
import dji.v5.common.error.IDJIError;
import dji.sdk.keyvalue.key.DJIKeyInfo;
import dji.v5.manager.KeyManager;
import dji.v5.manager.interfaces.IKeyManager;

class KeyCommandHandler implements CommandHandler
{
    private final IKeyManager keyManager = KeyManager.getInstance();

    public KeyCommandHandler (){

    }

    @Override
    public void onCommand(@NonNull CommandServer commandServer, @NonNull String command) {
        final String[] commandWords = command.toLowerCase().strip().split(" ");
        if (commandWords.length < 3) {
            commandServer.sendMessage("Illegal arguments: " + command);
            return;
        }

        DJIKeyInfo<?> currentKey = KeyFromCommand(commandWords[1], commandWords[2]);

        switch (commandWords[0])
        {
            case "get":
                if (!currentKey.isCanGet()){
                    commandServer.sendMessage("Illegal command: " + command);
                    break;
                }
//                keyManager.getValue(
//                        KeyTools.createKey(currentKey),
//                        new CommonCallbacks.CompletionCallbackWithParam<T>() {
//                            @Override
//                            public void onSuccess(T value) {
//                                commandServer.sendMessage(String.format("Success: %s %s", command, value.toString()));
//                            }
//                            @Override
//                            public void onFailure(@NonNull IDJIError idjiError) {
//                                commandServer.sendMessage(idjiError.toString());
//                            }
//                        });
                break;

            case "set":
                if (!currentKey.isCanSet()){
                    commandServer.sendMessage("Illegal command: " + command);
                    break;
                }
                break;

            case "listen":
                if (!currentKey.isCanListen()){
                    commandServer.sendMessage("Illegal command: " + command);
                    break;
                }
                break;

            case "cancel_listen":
                if (!currentKey.isCanListen()){
                    commandServer.sendMessage("Illegal command: " + command);
                    break;
                }
                break;

            case "action":
                if (!currentKey.isCanPerformAction()){
                    commandServer.sendMessage("Illegal command: " + command);
                    break;
                }
                break;

            default:
                break;
        }
    }

    DJIKeyInfo<?> KeyFromCommand (String keyClass, String keyName){
        return BatteryKey.KeyChargeRemainingInPercent;
    }
}
