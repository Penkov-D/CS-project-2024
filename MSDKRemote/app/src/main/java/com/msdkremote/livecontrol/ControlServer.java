package com.msdkremote.livecontrol;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dji.sdk.keyvalue.key.FlightControllerKey;
import dji.sdk.keyvalue.key.KeyTools;
import dji.sdk.keyvalue.value.common.EmptyMsg;
import dji.v5.common.callback.CommonCallbacks;
import dji.v5.common.error.IDJIError;
import dji.v5.manager.KeyManager;
import dji.v5.manager.aircraft.virtualstick.IStick;
import dji.v5.manager.aircraft.virtualstick.VirtualStickManager;
import dji.v5.manager.interfaces.IKeyManager;
import dji.v5.manager.interfaces.IVirtualStickManager;

public class ControlServer
{
    private final String TAG = this.getClass().getSimpleName();

    public ControlServer (Context context, int port)
    {
        IVirtualStickManager stickManager = VirtualStickManager.getInstance();

        stickManager.enableVirtualStick(
                new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(context, "onSuccess", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        Toast.makeText(context, "onFailure - " + idjiError.errorCode(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        new Thread(
                new Runnable() {
                    @Override
                    public void run()
                    {
                        try {
                            Log.i(TAG, "Start Server");
                            ServerSocket serverSocket = new ServerSocket(port);
                            Log.i(TAG, "Server Started");
                            Socket clientSocket = serverSocket.accept();
                            Log.i(TAG, "Client Connected");
                            BufferedReader bufferedInput = new BufferedReader(
                                    new InputStreamReader(
                                            clientSocket.getInputStream()
                                    )
                            );

                            IStick leftStick = stickManager.getLeftStick();
                            IStick rightStick = stickManager.getRightStick();

                            String inputLine;
                            while ((inputLine = bufferedInput.readLine()) != null)
                            {
                                Log.i(TAG, inputLine);

                                if (inputLine.equals("takeoff"))
                                {
                                    IKeyManager keyManager = KeyManager.getInstance();

                                    keyManager.performAction(
                                            KeyTools.createKey(FlightControllerKey.KeyStartTakeoff),
                                            new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                                                @Override
                                                public void onSuccess(EmptyMsg emptyMsg) {

                                                }

                                                @Override
                                                public void onFailure(@NonNull IDJIError idjiError) {

                                                }
                                            }
                                    );
                                }
                                else if (inputLine.equals("land"))
                                {
                                    IKeyManager keyManager = KeyManager.getInstance();

                                    keyManager.performAction(
                                            KeyTools.createKey(FlightControllerKey.KeyStartAutoLanding),
                                            new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                                                @Override
                                                public void onSuccess(EmptyMsg emptyMsg) {

                                                }

                                                @Override
                                                public void onFailure(@NonNull IDJIError idjiError) {

                                                }
                                            }
                                    );
                                }
                                else
                                {
                                    Pattern p = Pattern.compile("(\\d+).(\\d+).(\\d+).(\\d+)");
                                    Matcher m = p.matcher(inputLine);

                                    if (m.matches())
                                    {
                                        try
                                        {
                                            int leftHorizon = Integer.parseInt(Objects.requireNonNull(m.group(1)));
                                            int leftVertical = Integer.parseInt(Objects.requireNonNull(m.group(2)));
                                            int rightHorizon = Integer.parseInt(Objects.requireNonNull(m.group(3)));
                                            int rightVertical = Integer.parseInt(Objects.requireNonNull(m.group(4)));

                                            leftStick.setHorizontalPosition(leftHorizon);
                                            leftStick.setVerticalPosition(leftVertical);
                                            rightStick.setHorizontalPosition(rightHorizon);
                                            rightStick.setVerticalPosition(rightVertical);
                                        }
                                        catch (NullPointerException e) {
                                            Log.w(TAG, e);
                                        }
                                    }
                                    else
                                    {
                                        leftStick.setHorizontalPosition(0);
                                        leftStick.setVerticalPosition(0);
                                        rightStick.setHorizontalPosition(0);
                                        rightStick.setVerticalPosition(0);
                                    }
                                }
                            }

                            serverSocket.close();
                        }
                        catch (IOException e) {
                            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        ).start();
    }
}
