package com.msdkremote.livekeys;


import androidx.annotation.NonNull;

import com.msdkremote.commandserver.CommandServer;

import dji.sdk.keyvalue.key.DJIActionKeyInfo;
import dji.sdk.keyvalue.key.DJIKeyInfo;
import dji.sdk.keyvalue.key.DJIKey;
import dji.sdk.keyvalue.key.KeyTools;
import dji.v5.common.error.IDJIError;
import dji.v5.manager.KeyManager;
import dji.v5.common.callback.CommonCallbacks;

public class KeyItem<P, R> {
    protected DJIKeyInfo<?> keyInfo;
    protected Object listenHolder;

    public KeyItem(@NonNull DJIKeyInfo<?> keyInfo) {
        this.keyInfo = keyInfo;
    }

    protected<Param> DJIKey<Param> createKey(DJIKeyInfo<Param> keyInfo){
        return KeyTools.createKey(keyInfo);
    }

    protected DJIKey.ActionKey<P,R> createActionKey(DJIActionKeyInfo<P,R> keyInfo) {
        return KeyTools.createKey(keyInfo);
    }

    protected R syncGet() {
        return KeyManager.getInstance().getValue(createKey((DJIKeyInfo<R>)keyInfo));
    }

    public void get(@NonNull CommandServer commandServer, @NonNull String command) {
        try {
            if (!keyInfo.isCanGet()){
                commandServer.sendMessage("Illegal command: " + command);
                return;
            }
            KeyManager.getInstance().getValue(
                    createKey((DJIKeyInfo<R>)keyInfo),
                    new CommonCallbacks.CompletionCallbackWithParam<R>() {
                        @Override
                        public void onSuccess(R value) {
                            commandServer.sendMessage(String.format("Success: %s %s", command, value.toString()));
                        }

                        @Override
                        public void onFailure(@NonNull IDJIError idjiError) {
                            commandServer.sendMessage(idjiError.toString());
                        }
                    }
            );
        } catch (Exception e){
            commandServer.sendMessage(String.format("Error: %s %s", command, e));
        }
    }

    public void set(String paramStr, @NonNull CommandServer commandServer, @NonNull String command) {
        try {
            if (!keyInfo.isCanSet()){
                commandServer.sendMessage("Illegal command: " + command);
                return;
            }
            KeyManager.getInstance().setValue(
                    createKey((DJIKeyInfo<P>)keyInfo),
                    getParam(paramStr),
                    new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onSuccess() {
                            commandServer.sendMessage(String.format("Success: %s", command));
                        }

                        @Override
                        public void onFailure(@NonNull IDJIError idjiError) {
                            commandServer.sendMessage(idjiError.toString());
                        }
                    }
            );
        } catch (Exception e) {
            commandServer.sendMessage(String.format("Error: %s %s", command, e));
        }
    }

    public void listen(@NonNull Object listenHolder, @NonNull CommandServer commandServer, @NonNull String command) {
        try{
            if (!keyInfo.isCanListen()){
                commandServer.sendMessage("Illegal command: " + command);
                return;
            }
            this.listenHolder = listenHolder;
            KeyManager.getInstance().listen(
                    createKey((DJIKeyInfo<R>)keyInfo),
                    listenHolder,
                    (oldValue, newValue) -> {
                        if (newValue != null)
                            commandServer.sendMessage(String.format("NewValue: %s %s", command, newValue));
                    }
            );
        } catch (Exception e) {
            commandServer.sendMessage(String.format("Error: %s %s", command, e));
        }
    }

    public void cancelListen(@NonNull Object listenHolder, @NonNull CommandServer commandServer, @NonNull String command) {
        try{
            if (!keyInfo.isCanListen()){
                commandServer.sendMessage("Illegal command: " + command);
                return;
            }
            KeyManager.getInstance().cancelListen(createKey((DJIKeyInfo<R>)keyInfo), listenHolder);
        } catch (Exception e) {
            commandServer.sendMessage(String.format("Error: %s %s", command, e));
        }
    }

    public void action(String paramStr, @NonNull CommandServer commandServer, @NonNull String command) {
        try {
            if (!keyInfo.isCanPerformAction()){
                commandServer.sendMessage("Illegal command: " + command);
                return;
            }
            KeyManager.getInstance().performAction(
                    createActionKey((DJIActionKeyInfo<P, R>)keyInfo),
                    getParam(paramStr),
                    new CommonCallbacks.CompletionCallbackWithParam<R>() {
                        @Override
                        public void onSuccess(Object r) {
                            if (r != null) {
                                commandServer.sendMessage(String.format("Success: %s %s", command, r));
                            } else {
                                commandServer.sendMessage(String.format("Success: %s", command));
                            }
                        }

                        @Override
                        public void onFailure(@NonNull IDJIError idjiError) {
                            commandServer.sendMessage(idjiError.toString());
                        }
                    }
            );
        } catch (Exception e) {
            commandServer.sendMessage(String.format("Error: %s %s", command, e));
        }
    }

    public P getParam (String paramStr){
        return (P) keyInfo.getTypeConverter().fromStr(paramStr);
    }

    public DJIKeyInfo<?> getKeyInfo() {
        return keyInfo;
    }

    public Object getListenHolder() { return listenHolder; }
}