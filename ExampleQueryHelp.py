from OpenDJI import OpenDJI

"""
In this example you will see how to use the 'help' functions.
The help function help the user to find and understand the available
commands that the MSDK offer.
"""

# IP address of the connected android device
IP_ADDR = "10.0.0.6"


# Connect to the drone
with OpenDJI(IP_ADDR) as drone:
    
    # Get list of available modules
    list_modules = drone.getModules()[1:-1].replace('"', '').split(",")
    print("Modules :", list_modules)
    print()

    # Get list of available keys inside a module
    list_keys = drone.getModuleKeys(OpenDJI.MODULE_BATTERY)[1:-1].replace('"', '').split(",")
    print("Module Keys :", sorted(list_keys))
    print()

    # Get information on specific key
    key_info = drone.getKeyInfo(OpenDJI.MODULE_FLIGHTCONTROLLER, "AircraftLocation3D")
    print("Key Info :")
    print(key_info)

    print()