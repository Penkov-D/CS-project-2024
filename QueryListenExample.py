from OpenDJI import OpenDJI
from OpenDJI import EventListener

import keyboard
import time

# IP address of the connected android device
IP_ADDR = "10.0.0.6"

# Map to hold the joystick position
joystick_position = {
    "LH" : 0, "LV" : 0,
    "RH" : 0, "RV" : 0,
}

# Demo implementation of EventListener,
#  with additional constractor
class MapUpdateListener(EventListener):

    def __init__(self, identifier : str = ""):
        """ Set this class updates to store in key 'identifier' """
        self._id = identifier

    def onValue(self, value):
        """ On new value, update 'joystick_position' """
        joystick_position[self._id] = int(value)

# Connect to the drone
with OpenDJI(IP_ADDR) as drone:
    
    # Register some listeners, for geting joystick position updates
    drone.listen("RemoteController", "StickLeftVertical", MapUpdateListener("LV"))
    drone.listen("RemoteController", "StickLeftHorizontal", MapUpdateListener("LH"))
    drone.listen("RemoteController", "StickRightVertical", MapUpdateListener("RV"))
    drone.listen("RemoteController", "StickRightHorizontal", MapUpdateListener("RH"))

    # Press 'x' to exit
    print("Press 'x' to exit!")
    while not keyboard.is_pressed("x"):

        # Print the joystick position
        print(
            f"LH : {joystick_position['LH']:4} " +
            f"LV : {joystick_position['LV']:4} " +
            f"RH : {joystick_position['RH']:4} " +
            f"RV : {joystick_position['RV']:4} ",
            end = '\t\t\r'
        )
        # Sleep for a little, too frequent updates are prone to difficalt errors.
        time.sleep(0.1)     # 100 ms -> 10 Hz

    # Remeber to clean the listeners registered,
    #  or the controller will be overflowed eventually.
    drone.unlisten("RemoteController", "StickLeftVertical")
    drone.unlisten("RemoteController", "StickLeftHorizontal")
    drone.unlisten("RemoteController", "StickRightVertical")
    drone.unlisten("RemoteController", "StickRightHorizontal")

    print()