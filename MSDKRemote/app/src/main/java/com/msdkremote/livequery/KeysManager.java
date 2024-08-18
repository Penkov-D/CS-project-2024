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
    // List of all the keys
    private List<DJIKeyInfo<?>> listKeysProduct = null;
    private List<DJIKeyInfo<?>> listKeysAirlink = null;
    private List<DJIKeyInfo<?>> listKeysCamera = null;
    private List<DJIKeyInfo<?>> listKeysGimbal = null;
    private List<DJIKeyInfo<?>> listKeysFlightController = null;
    private List<DJIKeyInfo<?>> listKeysRemoteController = null;
    private List<DJIKeyInfo<?>> listKeysBattery = null;

    // Map keys name to their relative info
    private Map<String, DJIKeyInfo<?>> mapKeysProduct = null;
    private Map<String, DJIKeyInfo<?>> mapKeysAirlink = null;
    private Map<String, DJIKeyInfo<?>> mapKeysCamera = null;
    private Map<String, DJIKeyInfo<?>> mapKeysGimbal = null;
    private Map<String, DJIKeyInfo<?>> mapKeysFlightController = null;
    private Map<String, DJIKeyInfo<?>> mapKeysRemoteController = null;
    private Map<String, DJIKeyInfo<?>> mapKeysBattery = null;

    // Map module name to a map of keys names to relative info
    private Map<String, Map<String, DJIKeyInfo<?>>> mapModuleName = null;


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
        this.listKeysProduct = ProductKey.getKeyList();
        this.listKeysAirlink = AirLinkKey.getKeyList();
        this.listKeysCamera = CameraKey.getKeyList();
        this.listKeysGimbal = GimbalKey.getKeyList();
        this.listKeysFlightController = FlightControllerKey.getKeyList();
        this.listKeysRemoteController = RemoteControllerKey.getKeyList();
        this.listKeysBattery = BatteryKey.getKeyList();

        mapKeysProduct = new HashMap<>();
        mapKeysAirlink = new HashMap<>();
        mapKeysCamera = new HashMap<>();
        mapKeysGimbal = new HashMap<>();
        mapKeysFlightController = new HashMap<>();
        mapKeysRemoteController = new HashMap<>();
        mapKeysBattery = new HashMap<>();

        for (DJIKeyInfo<?> key : listKeysProduct) mapKeysProduct.put(key.getIdentifier(), key);
        for (DJIKeyInfo<?> key : listKeysAirlink) mapKeysAirlink.put(key.getIdentifier(), key);
        for (DJIKeyInfo<?> key : listKeysCamera) mapKeysCamera.put(key.getIdentifier(), key);
        for (DJIKeyInfo<?> key : listKeysGimbal) mapKeysGimbal.put(key.getIdentifier(), key);
        for (DJIKeyInfo<?> key : listKeysFlightController) mapKeysFlightController.put(key.getIdentifier(), key);
        for (DJIKeyInfo<?> key : listKeysRemoteController) mapKeysRemoteController.put(key.getIdentifier(), key);
        for (DJIKeyInfo<?> key : listKeysBattery) mapKeysBattery.put(key.getIdentifier(), key);

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
    private Map<String, DJIKeyInfo<?>> getModuleMap(@NonNull String moduleName)
            throws UnknownModuleException
    {
        // Search the module map for the module name.
        Map<String, DJIKeyInfo<?>> keysMap = mapModuleName.get(moduleName);

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
    public DJIKeyInfo<?> getKeyInfo(@NonNull String moduleName, @NonNull String keyName)
            throws UnknownModuleException, UnknownKeyException
    {
        // Search the module map for the module name.
        DJIKeyInfo<?> keyInfo = getModuleMap(moduleName).get(keyName);

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
