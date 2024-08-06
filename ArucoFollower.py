import socket
import av
import av.codec
import cv2
import time
import threading
import numpy as np

HOST = '10.0.0.6'
PORT_VIDEO = 9999
PORT_COMMAND = 9998

codec = av.codec.context.CodecContext.create('h264', 'r')

def parseData(data):

    img = None
    for packet in codec.parse(data):
        for frame in codec.decode(packet):
            img = frame.to_ndarray(format = 'bgr24')

    return img


def getPose(image):

    # Load the data from the 'npy' file
    with open('Calibration/Calib/Calibration.npy', 'rb') as f:
        # Load the data
        mtx = np.load(f)
        dist = np.load(f)

    aruco_dict = cv2.aruco.getPredefinedDictionary(cv2.aruco.DICT_ARUCO_ORIGINAL)
    aruco_detector = cv2.aruco.ArucoDetector(aruco_dict, cv2.aruco.DetectorParameters())

    markerCorners, markerIds, _ = aruco_detector.detectMarkers(image)

    board = np.array([
        [-5,  5, 0],
        [ 5,  5, 0],
        [ 5, -5, 0],
        [-5, -5, 0],
    ], dtype = np.float32)

    ret = False
    tvec = None
    rmat = None

    if markerIds is not None:
        for corners, id in zip(markerCorners, markerIds):
            if id != 500:
                continue

            # Convert corners to the correct format for solvePnP
            corners = np.array(corners[0], dtype=np.float32)

            ret, rvec, tvec = cv2.solvePnP(board, corners, mtx, dist)
            
            if ret:
                tvec = tvec[:,0]
                rmat, _ = cv2.Rodrigues(rvec)
                image = cv2.drawFrameAxes(image, mtx, dist, rvec, tvec, 5.0, 2)

    image = cv2.aruco.drawDetectedMarkers(image, markerCorners, markerIds)
    return image, tvec, rmat


with (
    socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sVideo,
    socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sCommand,
):

    sVideo.connect((HOST, PORT_VIDEO))
    sCommand.connect((HOST, PORT_COMMAND))
    
    '''
    # Start drone
    sCommand.sendall(bytes('takeoff' + '\r\n', 'utf-8'))
    time.sleep(2.0)

    sCommand.sendall(bytes('enable' + '\r\n', 'utf-8'))
    sCommand.sendall(bytes('rc 0.0 0.0 0.0 0.0' + '\r\n', 'utf-8'))
    '''

    while True:

        data = sVideo.recv(100000)
        if len(data) == 0:
            break

        img = parseData(data)
        if img is None:
            continue

        img, tvec, rmat = getPose(img)

        # Assume the camera is horizontal and positioned up front
        if tvec is not None:
            # print(tvec)

            zvec = np.array([0, 0, -1], np.float64)
            pvec = zvec @ rmat.T

            yaw = np.rad2deg(np.arctan2(pvec[0], pvec[2]))
            # print(yaw)

            lh = np.rad2deg(np.arctan2(tvec[0], tvec[2])) / 100
            lv = -tvec[1] / 1000
            rh = -yaw / 500
            rv = (np.sqrt(tvec[0] ** 2 + tvec[2] ** 2) - 200) / 1000

            lh = np.clip(lh,  -1.,  1.)
            lv = np.clip(lv, -.05, .05)
            rh = np.clip(rh, -.05, .05)
            rv = np.clip(rv, -.05, .05)

            command = f'rc {lh:.2f} {lv:.2f} {rh:.2f} {rv:.2f}'
            sCommand.sendall(bytes(command + '\r\n', 'utf-8'))
            print(command)

        else:
            print('No marker!')
            sCommand.sendall(bytes('rc 0.0 0.0 0.0 0.0' + '\r\n', 'utf-8'))

        cv2.imshow('stream', img)
        key =  cv2.waitKey(20)  # 50Hz
        if key == ord('q'):
            break
    
    '''
    # Shutdown drone
    sCommand.sendall(bytes('rc 0.0 0.0 0.0 0.0' + '\r\n', 'utf-8'))
    sCommand.sendall(bytes('disable' + '\r\n', 'utf-8'))

    sCommand.sendall(bytes('land' + '\r\n', 'utf-8'))
    time.sleep(2.0)

    sCommand.sendall(bytes('land' + '\r\n', 'utf-8'))
    time.sleep(2.0)
    '''

'''
# Change here
HOST = '10.0.0.6'
PORT_VIDEO = 9998

# Connect to the remote server
with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sCommand:
    sCommand.connect((HOST, PORT_VIDEO))

    while True:
        time.sleep(0.1)

        lh = 0.0
        lv = 0.0
        rh = 0.0
        rv = 0.0

        move_value = 0.03

        if keyboard.is_pressed('a'): lh = -move_value * 10
        if keyboard.is_pressed('d'): lh =  move_value * 10
        if keyboard.is_pressed('s'): lv = -move_value
        if keyboard.is_pressed('w'): lv =  move_value

        if keyboard.is_pressed('left'):  rh = -move_value
        if keyboard.is_pressed('right'): rh =  move_value
        if keyboard.is_pressed('down'):  rv = -move_value
        if keyboard.is_pressed('up'):    rv =  move_value
        
        command = f'rc {lh:.2f} {lv:.2f} {rh:.2f} {rv:.2f}'

        if keyboard.is_pressed('f'): command = 'takeoff'
        if keyboard.is_pressed('r'): command = 'land'
        if keyboard.is_pressed('e'): command = 'enable'
        if keyboard.is_pressed('q'): command = 'disable'

        sCommand.sendall(bytes(command + '\r\n', 'utf-8'))

        data = sCommand.recv(10000, )
        if len(data) == 0:
            break

        print('Data size: ', len(data), 'bytes')
        print(data)

'''