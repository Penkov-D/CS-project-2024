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

        // TODO key from command
        DJIKeyInfo<?> currentKey = BatteryKey.KeyChargeRemainingInPercent;

        switch (commandWords[0])
        {
            case "get":
                if (!currentKey.isCanGet()){
                    commandServer.sendMessage("Illegal command: " + command);
                    break;
                }
                keyManager.getValue(
                        KeyTools.createKey(currentKey),
                        new CommonCallbacks.CompletionCallbackWithParam<>() {
                            @Override
                            public void onSuccess(Integer value) {


                            }
                            @Override
                            public void onFailure(@NonNull IDJIError idjiError) {


                            }
                        });
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
}
