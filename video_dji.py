import numpy as np
import cv2

USER = 'user'
PASS = '123456'
ADDR = '192.168.1.127'
PORT = '5555'

cam = cv2.VideoCapture(f'rtsp://{USER}:{PASS}@{ADDR}:{PORT}/streaming/live/1')


while cv2.waitKey(1) != ord('q'):

    # Read frame
    ret, frame = cam.read()
    
    # In case of error, stop
    if not ret:
        break

    # Show frame
    if frame is not None:
        cv2.imshow('VIDEO', frame)
        

cv2.destroyAllWindows()
