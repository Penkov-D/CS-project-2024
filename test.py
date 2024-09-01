from OpenDJI import OpenDJI
from OpenDJI import EventListener

import keyboard

import cv2
import numpy as np


with OpenDJI("10.0.0.6") as drone:

    # drone.frameListener(frameListener())
    
    while not keyboard.is_pressed('x'):

        lh = 0.0
        lv = 0.0
        rh = 0.0
        rv = 0.0
        move_value = 0.03

        if keyboard.is_pressed('a'): lh = -move_value * 10
        if keyboard.is_pressed('d'): lh =  move_value * 10
        if keyboard.is_pressed('s'): lv = -move_value
        if keyboard.is_pressed('w'): lv =  move_value

        if keyboard.is_pressed('left'): rh = -move_value
        if keyboard.is_pressed('right'): rh =  move_value
        if keyboard.is_pressed('down'): rv = -move_value
        if keyboard.is_pressed('up'): rv =  move_value
        
        print(drone.move(lh, lv, rh, rv, True))

        if keyboard.is_pressed('f'): print(drone.takeoff(True))
        if keyboard.is_pressed('r'): print(drone.land(True))
        if keyboard.is_pressed('e'): print(drone.enableControl(True))
        if keyboard.is_pressed('q'): print(drone.disableControl(True))


        frame = drone.getFrame()
        if frame is None:
            frame = np.zeros((200, 200, 3))
        
        frame = cv2.resize(frame, dsize = None, fx = 0.25, fy = 0.25)
        cv2.imshow("window", frame)
        cv2.waitKey(100)