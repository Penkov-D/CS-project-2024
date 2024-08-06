import socket
import av
import av.codec
import cv2
import time
import threading
import numpy as np
import datetime

HOST = '10.0.0.6'
PORT_VIDEO = 9999
PORT_COMMAND = 9998

folder = 'Calibration/Calib'
cross = (7, 7)

codec = av.codec.context.CodecContext.create('h264', 'r')

def parseData(data):

    img = None
    for packet in codec.parse(data):
        for frame in codec.decode(packet):
            img = frame.to_ndarray(format = 'bgr24')

    return img


# Count number of pictures
count = 0
# termination criteria
criteria = (cv2.TERM_CRITERIA_EPS + cv2.TERM_CRITERIA_MAX_ITER, 30, 0.001)
# Last time a photo taken
last_taken_pic = datetime.datetime.now()


with (
    socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sVideo,
    socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sCommand,
):

    sVideo.connect((HOST, PORT_VIDEO))
    sCommand.connect((HOST, PORT_COMMAND))

    while True:

        data = sVideo.recv(100000)
        if len(data) == 0:
            break

        frame = parseData(data)
        if frame is None:
            continue

        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

        # Find the chess board corners
        ret, corners = cv2.findChessboardCorners(cv2.resize(gray, dsize = None, fx = 0.20, fy = 0.20), cross, None)


        # If found, add object points, image points (after refining them)
        if ret:
            ret, corners = cv2.findChessboardCorners(gray, cross, None)


        if ret:
            corners2 = cv2.cornerSubPix(gray,corners, (11,11), (-1,-1), criteria)
            cv2.drawChessboardCorners(gray, cross, corners2, ret)
            
        if ret and last_taken_pic + datetime.timedelta(milliseconds=1000) < datetime.datetime.now():
            count = count + 1
            last_taken_pic = datetime.datetime.now()
            current_time = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
            cv2.imwrite(folder + '/' + str(current_time) + '.jpg', frame)
            print ("saved - " + str(current_time) + " - " + str(count))
        
        img = cv2.resize(gray, (720, 480))
        cv2.imshow("Match", img)

        key =  cv2.waitKey(20)  # 50Hz
        if key == ord('q'):
            break
    
    