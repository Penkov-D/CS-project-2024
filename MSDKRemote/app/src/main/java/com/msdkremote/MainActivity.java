package com.msdkremote;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.msdkremote.commandserver.CommandHandler;
import com.msdkremote.commandserver.CommandServer;
import com.msdkremote.livecontrol.ControlServer;
import com.msdkremote.livecontrol.smartstick.RegularStickManager;
import com.msdkremote.livecontrol.smartstick.StickManager;
import com.msdkremote.livevideo.VideoServerManager;
import com.msdkremote.networkstate.NetworkChangeListener;
import com.msdkremote.networkstate.NetworkMonitor;
import com.msdkremote.networkstate.NetworkType;

import java.net.InetAddress;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dji.sdk.keyvalue.key.FlightControllerKey;
import dji.sdk.keyvalue.key.KeyTools;
import dji.sdk.keyvalue.value.common.EmptyMsg;
import dji.v5.common.callback.CommonCallbacks;
import dji.v5.common.error.IDJIError;
import dji.v5.manager.KeyManager;
import dji.v5.manager.SDKManager;
import dji.v5.manager.aircraft.virtualstick.VirtualStickManager;
import dji.v5.manager.datacenter.camera.CameraStreamManager;
import dji.v5.manager.interfaces.ICameraStreamManager;
import dji.v5.manager.interfaces.IVirtualStickManager;

public class MainActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();

    // Hold whether the onRegister method was called
    private boolean wasRegistered = false;

    private CommandServer commandServer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register the MSDK once it was opened
        if (!SDKManager.getInstance().isRegistered()) {
            Log.i(TAG, "onCreate(): Registering the MSDK.");
            Log.i(TAG, "onCreate(): Moving to loading screen.");

            // Start the loading screen
            this.startActivity(
                    new Intent(this, LoadingActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            );
        }
        else {
            Log.i(TAG, "OnCreate(): App is registered!");
        }

        NetworkMonitor monitor = new NetworkMonitor(this);
        monitor.registerListener((networkType, address) -> {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView textView = (TextView) findViewById(R.id.networkStateTextView);
                    textView.setText("Network type : " + networkType + ", address : " + address.getHostAddress());
                }
            });
        });
    }


    @Override
    protected synchronized void onResume() {
        super.onResume();

        // Calling onRegistered after MSDK registered and only once per launch.
        if (!wasRegistered && SDKManager.getInstance().isRegistered()) {
            wasRegistered = true;
            onRegistered();
        }

        // Start video broadcast on phone
        if (SDKManager.getInstance().isRegistered())
            setVideoSurface();
    }


    @Override
    protected synchronized void onDestroy() {
        super.onDestroy();

        // Calling onUnregistered if app closes and onRegistered was called.
        if (wasRegistered) {
            onUnregistered();
        }
    }


    /**
     * This method will be called when the MSDK is activated.
     * Put here all the MSDK functions that run on start.
     * Note, this from the UI thread, so no blocking functions.
     */
    private void onRegistered()
    {
        Log.i(TAG, "onRegistered(): starting default services.");

        // Start video server
        VideoServerManager.getInstance().startServer(9999);

        // Start command server
        StickManager stickManager = new RegularStickManager();
        stickManager.startStickManagement();


        commandServer = new CommandServer(10000);
        commandServer.startServer(new CommandHandler() {
            @Override
            public void onClientConnected(InetAddress address) {
                MainActivity.this.runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,
                                        "Client connected " + address,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            }

            @Override
            public String onCommand(@NonNull String command) {
                if (command.equals("land")) {
                    Log.i(MainActivity.this.TAG, "land");
                    KeyManager.getInstance().performAction(
                            KeyTools.createKey(FlightControllerKey.KeyStartAutoLanding),
                            new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                                @Override
                                public void onSuccess(EmptyMsg emptyMsg) {
                                }

                                @Override
                                public void onFailure(@NonNull IDJIError idjiError) {
                                    Toast.makeText(MainActivity.this, idjiError.errorCode(), Toast.LENGTH_SHORT).show();
                                }
                            }
                    );

                    KeyManager.getInstance().performAction(
                            KeyTools.createKey(FlightControllerKey.KeyConfirmLanding),
                            new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                                @Override
                                public void onSuccess(EmptyMsg emptyMsg) {
                                }

                                @Override
                                public void onFailure(@NonNull IDJIError idjiError) {
                                    Toast.makeText(MainActivity.this, idjiError.errorCode(), Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                } else if (command.equals("takeoff")) {
                    Log.i(MainActivity.this.TAG, "takeoff");
                    KeyManager.getInstance().performAction(
                            KeyTools.createKey(FlightControllerKey.KeyStartTakeoff),
                            new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                                @Override
                                public void onSuccess(EmptyMsg emptyMsg) {
                                }

                                @Override
                                public void onFailure(@NonNull IDJIError idjiError) {
                                    Toast.makeText(MainActivity.this, idjiError.errorCode(), Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                } else {
                    String patternString = "rc\\s+(-?\\d+\\.\\d+)\\s+(-?\\d+\\.\\d+)\\s+(-?\\d+\\.\\d+)\\s+(-?\\d+\\.\\d+)";

                    // Compile the pattern
                    Pattern pattern = Pattern.compile(patternString);

                    // Match the pattern against the input string
                    Matcher matcher = pattern.matcher(command);

                    // Check if the pattern matches
                    if (matcher.matches()) {
                        // Extract and parse the floating-point numbers
                        try {
                            float value1 = Float.parseFloat(Objects.requireNonNull(matcher.group(1)));
                            float value2 = Float.parseFloat(Objects.requireNonNull(matcher.group(2)));
                            float value3 = Float.parseFloat(Objects.requireNonNull(matcher.group(3)));
                            float value4 = Float.parseFloat(Objects.requireNonNull(matcher.group(4)));

                            // Output the parsed values
                            Log.i(MainActivity.this.TAG, "rc " + value1 + ", " + value2 + ", " + value3 + ", " + value4);
                            stickManager.setSticks(value1, value2, value3, value4);
                        } catch (NumberFormatException e) {
                            Log.w(MainActivity.this.TAG, e);
                        }
                    } else {
                        Log.w(MainActivity.this.TAG, "Not in format");
                    }
                }

                return command;
            }

            @Override
            public void onClientDisconnected() {
                MainActivity.this.runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,
                                        "Client disconnected",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            }
        });
    }


    private void setVideoSurface()
    {
        ICameraStreamManager cameraStreamManager = CameraStreamManager.getInstance();

        cameraStreamManager.addAvailableCameraUpdatedListener(
            availableCameraList -> {
                if (availableCameraList.isEmpty())
                    return;

                SurfaceView surfaceView = findViewById(R.id.mainVideo);
                Surface surface = surfaceView.getHolder().getSurface();

                cameraStreamManager.putCameraStreamSurface(
                        availableCameraList.get(0),
                        surface,
                        surfaceView.getWidth(),
                        surfaceView.getHeight(),
                        ICameraStreamManager.ScaleType.CENTER_INSIDE
                );
            }
        );
    }


    /**
     * This method will be called when the app is closed.
     * Put here all the "cleanup" methods from the MSDK.
     * Note, this method will be called only if onRegistered method was called.
     */
    private void onUnregistered()
    {
        try {
            // Close video server broadcast
            VideoServerManager.getInstance().killServer();

            // Stop command server
            commandServer.stopServer();
        }
        catch (InterruptedException e) {
            Log.e(TAG, "onUnregistered: Interrupted Exception occurred on UI thread");
        }
    }


    // Start Server button action
    public void StartVideoServer (View view)
    {
        VideoServerManager.getInstance().startServer(9999);

        IVirtualStickManager stickManager = VirtualStickManager.getInstance();

        stickManager.enableVirtualStick(
                new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this, "onSuccess", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        Toast.makeText(MainActivity.this, "onFailure - " + idjiError.errorCode(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }


    // Stop Server button action
    public void StopVideoServer (View view)
    {
        try {
            VideoServerManager.getInstance().killServer();
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted Exception occurred on UI thread");
        }
    }


    public void StartControlActivity(View view) {
        startActivity(new Intent(this, ControlActivity.class));
    }


    @Override
    public void onBackPressed() {
        // Do noting when back pressed (e.g. don't close activity)
        // super.onBackPressed();
    }


}
