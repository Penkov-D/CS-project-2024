from OpenDJI import OpenDJI

import keyboard
import cv2
import numpy as np

"""
In this example you can fly and see video from the drone in live!
Like a computer game, move the drone with the keyboard and see its image
on your computer screen!

    press F - to takeoff the drone.
    press R - to land the drone.
    press E - to enable control from keyboard (joystick disabled)
    press Q - to disable control from keyboard (joystick enabled)
    press X - to close the problam

    press W/S - to move up/down (ascent)
    press A/D - to rotate left/right (yaw control)
    press ↑/↓ - to move forward/backward (pitch)
    press ←/→ - to move left/right (roll)
"""

# IP address of the connected android device
IP_ADDR = "10.0.0.4"

# The image from the drone can be quit big,
#  use this to scale down the image:
SCALE_FACTOR = 0.5

# Movement factors
MOVE_VALUE = 0.015
ROTATE_VALUE = 0.15

# Create blank frame
BLANK_FRAME = np.zeros((1080, 1920, 3))
BLNAK_FRAME = cv2.putText(BLANK_FRAME, "No Image", (200, 300),
                          cv2.FONT_HERSHEY_DUPLEX, 10,
                          (255, 255, 255), 15)

# Connect to the drone
with OpenDJI(IP_ADDR) as drone:

    # Press 'x' to close the program
    print("Press 'x' to close the program")
    while not keyboard.is_pressed('x'):

        # Show image from the drone
        # Get frame
        frame = drone.getFrame()

        # What to do when no frame available
        if frame is None:
            frame = BLANK_FRAME
    
        # Resize frame - optional
        frame = cv2.resize(frame, dsize = None,
                           fx = SCALE_FACTOR,
                           fy = SCALE_FACTOR)
        
        # Show frame
        cv2.imshow("Live video", frame)
        cv2.waitKey(20)
        
        # Move the drone with the keyboard
        # Core variables
        yaw = 0.0       # Rotate, left horizontal stick
        ascent = 0.0    # Ascent, left vertical stick
        roll = 0.0      # Side movement, right horizontal stick
        pitch = 0.0     # Forward movement, right vertical stick

        # Set core variables based on key presses
        if keyboard.is_pressed('a'): yaw = -ROTATE_VALUE
        if keyboard.is_pressed('d'): yaw =  ROTATE_VALUE
        if keyboard.is_pressed('s'): ascent  = -MOVE_VALUE
        if keyboard.is_pressed('w'): ascent  =  MOVE_VALUE

        if keyboard.is_pressed('left'):  roll = -MOVE_VALUE
        if keyboard.is_pressed('right'): roll =  MOVE_VALUE
        if keyboard.is_pressed('down'):  pitch = -MOVE_VALUE
        if keyboard.is_pressed('up'):    pitch =  MOVE_VALUE

        # Send the movement command
        drone.move(yaw, ascent, roll, pitch)

        # Special commands
        if keyboard.is_pressed('f'): print(drone.takeoff(True))
        if keyboard.is_pressed('r'): print(drone.land(True))
        if keyboard.is_pressed('e'): print(drone.enableControl(True))
        if keyboard.is_pressed('q'): print(drone.disableControl(True))
