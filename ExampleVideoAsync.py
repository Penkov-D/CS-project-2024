from OpenDJI import OpenDJI
from OpenDJI import EventListener

import cv2
import numpy as np

"""
In this example you will see how the retrieve the drone images asynchronously,
using a listener, with the OpenDJI class.

    press Q - to close the problam
"""

# IP address of the connected android device
IP_ADDR = "10.0.0.6"

# The image from the drone can be quit big,
#  use this to scale down the image:
SCALE_FACTOR = 0.25

# Initiate frame as blank frame
frame = np.zeros((1080, 1920, 3))
frame = cv2.putText(frame, "No Image", (200, 300),
                    cv2.FONT_HERSHEY_PLAIN, 10,
                    (255, 255, 255), 10)
frame = cv2.resize(frame, dsize = None,
                   fx = SCALE_FACTOR, fy = SCALE_FACTOR)


# Create background listener
class frameListener(EventListener):

    def onValue(self, _frame):
        """ Called when new frame available """
        global frame
        frame = cv2.resize(_frame, dsize = None,
                           fx = SCALE_FACTOR, fy = SCALE_FACTOR)

    def onError(self, ):
        # TODO : change parameters of onError
        pass


# Connect to the drone
with OpenDJI(IP_ADDR) as drone:
    
    # Register the frame background listener
    drone.frameListener(frameListener())
    # After doing so, the frame will be updated in the background

    # Press 'q' to close the program
    print("Press 'q' to close the program")
    while cv2.waitKey(20) != ord('q'):

        # Show frame
        cv2.imshow("Live video", frame)