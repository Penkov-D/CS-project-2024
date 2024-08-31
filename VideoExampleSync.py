from OpenDJI import OpenDJI

import cv2
import numpy as np

# IP address of the connected android device
IP_ADDR = "10.0.0.6"

# The image from the drone can be quit big,
#  use this to scale down the image:
SCALE_FACTOR = 0.25

# Create blank frame
BLANK_FRAME = np.zeros((1080, 1920, 3))
BLNAK_FRAME = cv2.putText(BLANK_FRAME, "No Image", (200, 300),
                          cv2.FONT_HERSHEY_PLAIN, 10,
                          (255, 255, 255), 10)


# Connect to the drone
with OpenDJI(IP_ADDR) as drone:
    
    # Press 'q' to close the program
    print("Press 'q' to close the program")
    while cv2.waitKey(20) != ord('q'):

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