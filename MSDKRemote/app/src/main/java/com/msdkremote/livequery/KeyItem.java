package com.msdkremote.livequery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.msdkremote.commandserver.CommandServer;

import dji.sdk.keyvalue.key.DJIActionKeyInfo;
import dji.sdk.keyvalue.key.DJIKey;
import dji.sdk.keyvalue.key.DJIKeyInfo;
import dji.v5.common.callback.CommonCallbacks;
import dji.v5.common.error.IDJIError;
import dji.v5.manager.KeyManager;

public class KeyItem<Param, Result>
{
    private final String moduleName;
    private final String keyName;

    private final DJIKeyInfo<Param> keyInfo;
    private final DJIActionKeyInfo<Param, Result> ActionKeyInfo;

    public KeyItem(DJIKeyInfo<Param> keyInfo, String moduleName)
    {
        this.keyInfo = keyInfo;
        this.moduleName = keyInfo.getIdentifier();
        this.keyName = keyInfo.getInnerIdentifier();

        if (keyInfo instanceof DJIActionKeyInfo)
            this.ActionKeyInfo = (DJIActionKeyInfo<Param, Result>) keyInfo;

        else
            this.ActionKeyInfo = null;
    }

    public String getModuleName() {
        return this.moduleName;
    }

    public String getKeyName() {
        return this.keyName;
    }

    public String getPresentingName() {
        return this.moduleName + " " + this.keyName;
    }

    public DJIKeyInfo<Param> getRawKeyInfo() {
        return this.keyInfo;
    }

    public DJIActionKeyInfo<Param, Result> getRawActionKeyInfo() {
        return this.ActionKeyInfo;
    }

    private void sendCommand(CommandServer server, String command) {
        server.sendMessage(this.getPresentingName() + " " + command);
    }

    private void sendCommand(CommandServer server, Object command)
    {
        if (command == null)
            sendCommand(server, "null");
        else
            sendCommand(server, command.toString());
    }

    public void commandGet(CommandServer server)
    {
        if (!keyInfo.isCanGet())
        {
            sendCommand(server, "Cannot command 'GET' on key.");
            return;
        }

        KeyManager.getInstance().getValue(
                DJIKey.create(keyInfo),
                new CommonCallbacks.CompletionCallbackWithParam<Param>()
                {
                    @Override
                    public void onSuccess(Param param)
                    {
                        sendCommand(server, param);
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError)
                    {
                        sendCommand(server, idjiError);
                    }
                }
        );
    }

    public void commandListen(CommandServer server)
    {
        if (!keyInfo.isCanListen())
        {
            sendCommand(server, "Cannot command 'LISTEN' on key.");
            return;
        }

        KeyManager.getInstance().listen(
                DJIKey.create(keyInfo),
                KeysManager.getInstance(),
                new CommonCallbacks.KeyListener<Param>() {
                    @Override
                    public void onValueChange(@Nullable Param oldValue, @Nullable Param newValue) {
                        sendCommand(server, newValue);
                    }
                }
        );
    }

    public void commandUnlisten(CommandServer server)
    {
        if (!keyInfo.isCanListen())
        {
            sendCommand(server, "Cannot command 'LISTEN' on key.");
            return;
        }

        KeyManager.getInstance().cancelListen(DJIKey.create(keyInfo));
        sendCommand(server, "success");
    }

    @NonNull
    @Override
    public String toString() {
        return "KeyItem{" +
                "moduleName='" + moduleName + '\'' +
                ", keyName='" + keyName + '\'' +
                ", keyInfo=" + keyInfo +
                ", ActionKeyInfo=" + ActionKeyInfo +
                '}';
    }
}
