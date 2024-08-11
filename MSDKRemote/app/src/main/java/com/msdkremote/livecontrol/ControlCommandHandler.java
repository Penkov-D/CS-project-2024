package com.msdkremote.livecontrol;

import androidx.annotation.NonNull;

import com.msdkremote.commandserver.CommandHandler;
import com.msdkremote.commandserver.CommandServer;

import dji.v5.common.error.IDJIError;

class ControlCommandHandler implements CommandHandler
{
    final private StickManager stickManager;

    public ControlCommandHandler(@NonNull StickManager stickManager)
    {
        this.stickManager = stickManager;
    }

    @Override
    public void onCommand(@NonNull CommandServer commandServer, @NonNull String command)
    {
        // Remove irrelevant spaces
        command = command.toLowerCase().strip();

        // Get first word of command
        String commandWord;
        if (command.indexOf(' ') >= 0)
                commandWord = command.substring(0, command.indexOf(' '));
        else
                commandWord = command;

        // Divide the command for its types
        switch (commandWord)
        {
            case "enable":
                stickManager.startStickManagement(new ActionCallback() {
                    @Override
                    public void onSuccess() {
                        commandServer.sendMessage("success");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {
                        commandServer.sendMessage(error.toString());
                    }
                });
                break;

            case "disable":
                stickManager.stopStickManagement(new ActionCallback() {
                    @Override
                    public void onSuccess() {
                        commandServer.sendMessage("success");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {
                        commandServer.sendMessage(error.toString());
                    }
                });
                break;

            case "takeoff":
                stickManager.takeoff(new ActionCallback() {
                    @Override
                    public void onSuccess() {
                        commandServer.sendMessage("success");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {
                        commandServer.sendMessage(error.toString());
                    }
                });
                break;

            case "land":
                stickManager.land(new ActionCallback() {
                    @Override
                    public void onSuccess() {
                        commandServer.sendMessage("success");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {
                        commandServer.sendMessage(error.toString());
                    }
                });

            case "rc":

                final String[] values = command.split(" ");

                if (values.length != 5) {
                    commandServer.sendMessage("Illegal arguments: " + command);
                    break;
                }

                try {
                    float lh = Float.parseFloat(values[1].trim());
                    float lv = Float.parseFloat(values[2].trim());
                    float rh = Float.parseFloat(values[3].trim());
                    float rv = Float.parseFloat(values[4].trim());

                    stickManager.setSticks(lh, lv, rh, rv);
                    commandServer.sendMessage("success");
                    break;
                }
                catch (NumberFormatException ignored) {
                    commandServer.sendMessage("Illegal arguments: " + command);
                    break;
                }

            default:
                commandServer.sendMessage("Unknown command: " + commandWord);
                break;
        }
    }
}
