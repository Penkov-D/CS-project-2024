from OpenDJI import OpenDJI
from OpenDJI import EventListener

import cv2
import numpy as np


class frameListener(EventListener):

    def onValue(self, frame):
        
        frame = cv2.resize(frame, dsize = None, fx = 0.25, fy = 0.25)
        cv2.imshow("window", frame)

    def onError(self, ):
        pass

with OpenDJI("10.0.0.5") as drone:

    # drone.frameListener(frameListener())
    
    while cv2.waitKey(20) != ord('q'):

        frame = drone.getFrame()

        if frame is None:
            frame = np.zeros((200, 200, 3))
        
        frame = cv2.resize(frame, dsize = None, fx = 0.25, fy = 0.25)
        cv2.imshow("window", frame)