package com.msdkremote.livekeys;

import androidx.annotation.NonNull;
import com.msdkremote.commandserver.CommandHandler;
import com.msdkremote.commandserver.CommandServer;

class KeyCommandHandler implements CommandHandler
{
    @Override
    public void onCommand(@NonNull CommandServer commandServer, @NonNull String command) {
        commandServer.sendMessage("Command received: " + command);
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
                currentKey.action(keyFromCommand.param, commandServer, command);
                break;

            default:
                commandServer.sendMessage("Unknown Command: " + command);
                break;
        }
    }

}
