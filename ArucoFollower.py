import cv2
import numpy as np
from OpenDJI import OpenDJI


# IP address of the connected android device
IP_ADDR = "10.0.0.4"

# The image from the drone can be quit big,
#  use this to scale down the image:
SCALE_FACTOR = 0.25

# Movement factors
MOVE_VALUE = 0.015
ROTATE_VALUE = 0.15

# Create blank frame
BLANK_FRAME = np.zeros((1080, 1920, 3))
BLNAK_FRAME = cv2.putText(BLANK_FRAME, "No Image", (200, 300),
                          cv2.FONT_HERSHEY_DUPLEX, 10,
                          (255, 255, 255), 15)

ARUCO_ID = 590

def showFrame(frame, scaleFactor):
    if frame is None:
        frame = BLANK_FRAME

    # Resize frame
    frame = cv2.resize(frame, dsize = None,
                    fx = scaleFactor,
                    fy = scaleFactor)
    
    # Show frame
    cv2.imshow("Live video", frame)

def getPose(image, mtx, dist):

    aruco_dict = cv2.aruco.getPredefinedDictionary(cv2.aruco.DICT_ARUCO_ORIGINAL)
    aruco_detector = cv2.aruco.ArucoDetector(aruco_dict, cv2.aruco.DetectorParameters())

    markerCorners, markerIds, _ = aruco_detector.detectMarkers(image)

    board = np.array([
        [-8,  8, 0],
        [ 8,  8, 0],
        [ 8, -8, 0],
        [-8, -8, 0],
    ], dtype = np.float32)

    ret = False
    tvec = None
    rmat = None

    if markerIds is not None:
        for corners, id in zip(markerCorners, markerIds):
            if id != ARUCO_ID:
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


# Load the data from the 'npy' file
with open('Calibration/Calib/Calibration.npy', 'rb') as f:
    # Load the data
    mtx = np.load(f)
    dist = np.load(f)

# Connect to the drone
with OpenDJI(IP_ADDR) as drone:
    # Start drone
    inAir = ""
    while inAir != "success":
        inAir = drone.takeoff(True)
        print(inAir)
        frame = drone.getFrame()
        showFrame(frame, SCALE_FACTOR)
        key = cv2.waitKey(200)
        if key == ord('q'):
            break
    
    print(drone.enableControl(True))

    while True:
        frame = drone.getFrame()
        if frame is None:
            continue

        frame, tvec, rmat = getPose(frame, mtx, dist)

        yawCmd, AscentCmd, rollCmd, pitchCmd = 0,0,0,0
        
        if tvec is None:
            print("no marker!")
            
        # Assume the camera is horizontal and positioned up front
        else:
            # print(tvec)

            zvec = np.array([0, 0, -1], np.float64)
            pvec = zvec @ rmat.T

            curr_yaw = np.rad2deg(np.arctan2(pvec[0], pvec[2]))
            # print(yaw)

            yawCmd = np.rad2deg(np.arctan2(tvec[0], tvec[2])) / 100
            AscentCmd = -tvec[1] / 1000
            rollCmd = -curr_yaw / 1000
            rollCmd = rollCmd - np.sign(rollCmd) * np.min([np.abs(rollCmd), 0.01])
            pitchCmd = (np.sqrt(tvec[0] ** 2 + tvec[2] ** 2) - 150) / 1000
            pitchCmd = pitchCmd - np.sign(pitchCmd) * np.min([np.abs(pitchCmd), 0.01])

            yawCmd = np.clip(yawCmd,  -ROTATE_VALUE,  ROTATE_VALUE)
            AscentCmd = np.clip(AscentCmd, -MOVE_VALUE, MOVE_VALUE)
            rollCmd = np.clip(rollCmd, -MOVE_VALUE, MOVE_VALUE)
            pitchCmd = np.clip(pitchCmd, -MOVE_VALUE, MOVE_VALUE)
                
            # Send the movement command
        print(drone.move(yawCmd, AscentCmd, rollCmd, pitchCmd, True))
        

        showFrame(frame, SCALE_FACTOR)
        key =  cv2.waitKey(20)  # 50Hz
        if key == ord('x'):
            break
    
    # Shutdown drone
    drone.move(0, 0, 0, 0)
    print(drone.disableControl(True))
    print(drone.land(True))
    