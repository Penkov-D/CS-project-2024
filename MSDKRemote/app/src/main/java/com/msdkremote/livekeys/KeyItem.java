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

    protected void get(CommonCallbacks.CompletionCallbackWithParam<R> getCallback) {
        KeyManager.getInstance().getValue(createKey((DJIKeyInfo<R>)keyInfo), getCallback);
    }

    protected R syncGet() {
        return KeyManager.getInstance().getValue(createKey((DJIKeyInfo<R>)keyInfo));
    }

    protected void set(P param, CommonCallbacks.CompletionCallback setCallback) {
        KeyManager.getInstance().setValue(createKey((DJIKeyInfo<P>)keyInfo), param, setCallback);
    }

    protected void listen(Object listenHolder, CommonCallbacks.KeyListener<R> listenCallback) {
        this.listenHolder = listenHolder;
        KeyManager.getInstance().listen(createKey((DJIKeyInfo<R>)keyInfo), listenHolder, listenCallback);
    }

    protected void cancelListen(Object listenHolder) {
        KeyManager.getInstance().cancelListen(createKey((DJIKeyInfo<R>)keyInfo), listenHolder);
    }

    protected void action(P param, CommonCallbacks.CompletionCallbackWithParam<R> actonCallback) {
        DJIKey.ActionKey<P,R> key = createActionKey((DJIActionKeyInfo<P, R>)keyInfo);
        KeyManager.getInstance().performAction(key, param, actonCallback);
    }

    public void get(@NonNull CommandServer commandServer, @NonNull String command) {
        try {
            get(new CommonCallbacks.CompletionCallbackWithParam<R>() {
                @Override
                public void onSuccess(R value) {
                    commandServer.sendMessage(String.format("Success: %s %s", command, value.toString()));
                }

                @Override
                public void onFailure(@NonNull IDJIError idjiError) {
                    commandServer.sendMessage(idjiError.toString());
                }
            });
        } catch (Exception e){
            commandServer.sendMessage(String.format("Error: %s %s", command, e));
        }
    }

    public void set(P param, @NonNull CommandServer commandServer, @NonNull String command) {
        try {
            set(param, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onSuccess() {
                    commandServer.sendMessage(String.format("Success: %s", command));
                }

                @Override
                public void onFailure(@NonNull IDJIError idjiError) {
                    commandServer.sendMessage(idjiError.toString());
                }
            });


        } catch (Exception e) {
            commandServer.sendMessage(String.format("Error: %s %s", command, e));
        }
    }

    public void listen(@NonNull Object listenHolder, @NonNull CommandServer commandServer, @NonNull String command) {
        try{
            listen(listenHolder, (oldValue, newValue) -> {
                if (newValue != null)
                    commandServer.sendMessage(String.format("NewValue: %s %s", command, newValue));
            });
        } catch (Exception e) {
            commandServer.sendMessage(String.format("Error: %s %s", command, e));
        }
    }

    public DJIKeyInfo<?> getKeyInfo() {
        return keyInfo;
    }

    public Object getListenHolder() {
        return listenHolder;
    }

    public boolean canGet() {
        return keyInfo.isCanGet();
    }

    public boolean canSet() {
        return keyInfo.isCanSet();
    }

    public boolean canListen() {
        return keyInfo.isCanListen();
    }

    public boolean canAction() {
        return keyInfo.isCanPerformAction();
    }
}