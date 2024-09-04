from OpenDJI import OpenDJI
from OpenDJI import EventListener

import keyboard
import time

import cv2
import numpy as np


class PrintListener(EventListener):
    def __init__(self, identifier = ""):
        self._id = identifier

    def onValue(self, value):
        print(self._id, value)


with OpenDJI("10.0.0.6") as drone:

    # drone.frameListener(frameListener())
    
    drone.listen("RemoteController", "StickLeftVertical", PrintListener("Left Vertical"))
    drone.listen("RemoteController", "StickLeftHorizontal", PrintListener("Left Horizontal"))
    drone.listen("RemoteController", "StickRightVertical", PrintListener("Right Vertical"))
    drone.listen("RemoteController", "StickRightHorizontal", PrintListener("Right Horizontal"))

    
    while not keyboard.is_pressed("space"):
        pass

    time.sleep(0.1)

    drone.unlisten("RemoteController", "StickRightVertical")
    drone.unlisten("RemoteController", "StickRightHorizontal")

    while not keyboard.is_pressed("space"):
        pass

    drone.unlisten("RemoteController", "StickLeftVertical")
    drone.unlisten("RemoteController", "StickLeftHorizontal")
    drone.unlisten("RemoteController", "StickRightVertical")
    drone.unlisten("RemoteController", "StickRightHorizontal")