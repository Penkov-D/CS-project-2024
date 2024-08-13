package com.msdkremote.livekeys;

import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.msdkremote.R;
import com.msdkremote.commandserver.CommandHandler;
import com.msdkremote.commandserver.CommandServer;

import dji.sdk.keyvalue.key.KeyTools;
import dji.v5.common.callback.CommonCallbacks;
import dji.v5.common.error.IDJIError;
import dji.sdk.keyvalue.key.DJIKeyInfo;
import dji.v5.manager.KeyManager;
import dji.v5.manager.interfaces.IKeyManager;

class KeyCommandHandler implements CommandHandler
{
    private final IKeyManager keyManager = KeyManager.getInstance();

    @Override
    public void onCommand(@NonNull CommandServer commandServer, @NonNull String command) {
        final String[] commandWords = command.toLowerCase().strip().split(" ");
        if (commandWords.length < 3) {
            commandServer.sendMessage("Illegal arguments: " + command);
            return;
        }

        KeyFromCommand keyFromCommand = new KeyFromCommand(command);
        KeyItem<?, ?> currentKey = keyFromCommand.getKeyItem();

        if (currentKey == null) {
            commandServer.sendMessage("Illegal arguments: " + command);
            return;
        }

        switch (keyFromCommand.commandWord)
        {
            case "get":
                currentKey.get(commandServer, command);
                break;

            case "set":
                currentKey.set(keyFromCommand.param, commandServer, command);
                break;

            case "listen":
                currentKey.listen(this, commandServer, command);
                break;

            case "cancel_listen":
                currentKey.cancelListen(this, commandServer, command);
                break;

            case "action":

                break;

            default:
                break;
        }
    }

}
