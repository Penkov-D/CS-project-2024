import numpy as np
import cv2 as cv
import glob
import yaml

######################## Input parameters ########################

# Chess board crosses (number of squares in each side, minus one. (e.g. 8x8 board (standard), means (7, 7)))
cross = (7, 7)  # <--- CHANGE HERE

# One square length in centimeters
size = 1.9      # <--- CHANGE HERE

# Folder to find all the images to process
path = 'Calibration/Calib'      # <--- CHANGE HERE

# Do you want some fancy output ?
debug = True


######################## Do not change ########################

# termination criteria (Magic numbers are from the OpenCV tutorial)
criteria = (cv.TERM_CRITERIA_EPS + cv.TERM_CRITERIA_MAX_ITER, 30, 0.001)

# prepare object points ()
objp = np.zeros((np.prod(cross), 3), np.float32)
objp[:,:2] = size * np.mgrid[0:cross[0], 0:cross[1]].T.reshape(-1, 2)

# Arrays to store object points and image points from all the images.
objpoints = [] # 3d point in real world space
imgpoints = [] # 2d points in image plane.

# Open the images folder
images = glob.glob(path + "/*.jpg")

# counters
count_opened = 0
count_processed = 0

# For each image in the folder proceed:
for fname in images:

    # Increase counter by one
    count_opened += 1

    if debug:
        # Print file name
        print(fname)

    # Read the image and convert to gray scale
    img = cv.imread(fname)
    gray = cv.cvtColor(img, cv.COLOR_BGR2GRAY)

    # Find the chess board corners
    ret, corners = cv.findChessboardCorners(gray, cross, None)

    # If found, add object points, image points (after refining them)
    if ret:
        # Increase counter by one
        count_processed += 1

        # Save the found points to process on leter
        objpoints.append(objp)
        corners2 = cv.cornerSubPix(gray,corners, (11,11), (-1,-1), criteria)
        imgpoints.append(corners2)

        if debug:
            # Draw and display the corners
            cv.drawChessboardCorners(img, cross, corners2, ret)

            # Resize the image (to fit for my screen)
            img = cv.resize(img, (720, 480))
            cv.imshow('img', img)
            cv.waitKey(300)     # Delay for each image in milis (adjustable)

if debug:
    # If showed the images, then cloase them
    cv.destroyAllWindows()


if count_opened == 0:
    # If no photo opened, could be incorrect path
    print ("No photos opened, did you enter the right path ?")

elif count_processed == 0:
    # If no photo proccesed, could be incorrect crosses parameter
    print ("Couldn't find chessboard pattern in the images. Did you put the right chessboard dimantion ?")

# Calibrate the camera.
ret, mtx, dist, rvecs, tvecs = cv.calibrateCamera(objpoints, imgpoints, gray.shape[::-1], None, None)

if not ret:
    # Check the 'ret' value
    print ("Calibration did not successed")

if debug:
    # Print the calibration matrix
    print (mtx)
    # Print the distortion parameters
    print (dist)



######################## Save the data ########################

# Store the output in yaml 
with open(path + '/Calibration.yaml', 'w') as f:
    # Write the calibration data
    yaml.dump([mtx.tolist()], f)
    yaml.dump([dist.tolist()], f)

# Store the output in npy
with open(path + '/Calibration.npy', 'wb') as f:
    # Write the calibration data
    np.save(f, mtx)
    np.save(f, dist)


# Source : https://docs.opencv.org/4.x/dc/dbb/tutorial_py_calibration.html