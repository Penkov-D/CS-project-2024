package com.msdkremote.livequery;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dji.sdk.keyvalue.key.AirLinkKey;
import dji.sdk.keyvalue.key.BatteryKey;
import dji.sdk.keyvalue.key.CameraKey;
import dji.sdk.keyvalue.key.DJIKey;
import dji.sdk.keyvalue.key.DJIKeyInfo;
import dji.sdk.keyvalue.key.FlightControllerKey;
import dji.sdk.keyvalue.key.GimbalKey;
import dji.sdk.keyvalue.key.KeyTools;
import dji.sdk.keyvalue.key.ProductKey;
import dji.sdk.keyvalue.key.RemoteControllerKey;
import dji.v5.manager.KeyManager;

public class KeysManager
{

    // Map keys name to their relative info
    private Map<String, KeyItem<?,?>> mapKeysProduct = null;
    private Map<String, KeyItem<?,?>> mapKeysAirlink = null;
    private Map<String, KeyItem<?,?>> mapKeysCamera = null;
    private Map<String, KeyItem<?,?>> mapKeysGimbal = null;
    private Map<String, KeyItem<?,?>> mapKeysFlightController = null;
    private Map<String, KeyItem<?,?>> mapKeysRemoteController = null;
    private Map<String, KeyItem<?,?>> mapKeysBattery = null;

    // Map module name to a map of keys names to relative info
    private Map<String, Map<String, KeyItem<?,?>>> mapModuleName = null;


    private static KeysManager keysManager = null;

    private KeysManager() {
        reset();
    }

    /**
     * Get instance of KeysManager. <br>
     * Could be regular class, but creation of KeysManager is costly.
     *
     * @return instance of KeysManager.
     */
    @NonNull
    public static KeysManager getInstance()
    {
        if (keysManager == null)
            keysManager = new KeysManager();

        return keysManager;
    }

    /**
     * Resets all the mapping of the keys.
     * <p>
     * Must be called inside the constructor.
     */
    public void reset()
    {
        // Maps of all the keys inside their module
        mapKeysProduct = new HashMap<>();
        mapKeysAirlink = new HashMap<>();
        mapKeysCamera = new HashMap<>();
        mapKeysGimbal = new HashMap<>();
        mapKeysFlightController = new HashMap<>();
        mapKeysRemoteController = new HashMap<>();
        mapKeysBattery = new HashMap<>();

        // Add all the keys to the map
        for (DJIKeyInfo<?> key : ProductKey.getKeyList())
            mapKeysProduct.put(key.getIdentifier(), new KeyItem<>(key, "Product"));

        for (DJIKeyInfo<?> key : AirLinkKey.getKeyList())
            mapKeysAirlink.put(key.getIdentifier(), new KeyItem<>(key, "AirLink"));

        for (DJIKeyInfo<?> key : CameraKey.getKeyList())
            mapKeysCamera.put(key.getIdentifier(), new KeyItem<>(key, "Camera"));

        for (DJIKeyInfo<?> key : GimbalKey.getKeyList())
            mapKeysGimbal.put(key.getIdentifier(), new KeyItem<>(key, "Gimbal"));

        for (DJIKeyInfo<?> key : FlightControllerKey.getKeyList())
            mapKeysFlightController.put(key.getIdentifier(), new KeyItem<>(key, "FlightController"));

        for (DJIKeyInfo<?> key : RemoteControllerKey.getKeyList())
            mapKeysRemoteController.put(key.getIdentifier(), new KeyItem<>(key, "RemoteController"));

        for (DJIKeyInfo<?> key : BatteryKey.getKeyList())
            mapKeysBattery.put(key.getIdentifier(), new KeyItem<>(key, "Battery"));

        // Create Global map for modules
        mapModuleName = new HashMap<>();
        mapModuleName.put("Product", mapKeysProduct);
        mapModuleName.put("AirLink", mapKeysAirlink);
        mapModuleName.put("Camera", mapKeysCamera);
        mapModuleName.put("Gimbal", mapKeysGimbal);
        mapModuleName.put("FlightController", mapKeysFlightController);
        mapModuleName.put("RemoteController", mapKeysRemoteController);
        mapModuleName.put("Battery", mapKeysBattery);
    }

    /**
     * Get module map by string representing its name.
     *
     * @param moduleName the name of the module.
     * @return map of string to key inside this module.
     * @throws UnknownModuleException if no module named moduleName.
     */
    @NonNull
    private Map<String, KeyItem<?,?>> getModuleMap(@NonNull String moduleName)
            throws UnknownModuleException
    {
        // Search the module map for the module name.
        Map<String, KeyItem<?,?>> keysMap = mapModuleName.get(moduleName);

        // get() return null if no such key.
        if (keysMap == null)
            throw new UnknownModuleException("No module named: " + moduleName);

        return keysMap;
    }

    /**
     * Get key info (DJIKeyInfo) by module name and key name.
     *
     * @param moduleName module to search the key for.
     * @param keyName string representing the key.
     * @return DJIKeyInfo of the specified key.
     * @throws UnknownModuleException if no module named moduleName.
     * @throws UnknownKeyException if no key inside the module named keyName.
     */
    @NonNull
    public KeyItem<?,?> getKeyInfo(@NonNull String moduleName, @NonNull String keyName)
            throws UnknownModuleException, UnknownKeyException
    {
        // Search the module map for the module name.
        KeyItem<?,?> keyInfo = getModuleMap(moduleName).get(keyName);

        // get() return null if no such key.
        if (keyInfo == null)
            throw new UnknownKeyException("No key name: " + keyName);

        return keyInfo;
    }

    /**
     * Get list of all the available modules, by their representing name.
     *
     * @return array of all modules names.
     */
    @NonNull
    public String[] getAvailableModules() {
        return mapModuleName.keySet().toArray(new String[0]);
    }

    /**
     * Get list of all available keys inside a module, by their representing name.
     *
     * @param moduleName module to seek the keys from.
     * @return array of all string mapped to available keys.
     * @throws UnknownModuleException if no such module name.
     */
    @NonNull
    public String[] getAvailableKeys (String moduleName)
            throws UnknownModuleException
    {
        return getModuleMap(moduleName).keySet().toArray(new String[0]);
    }
}
