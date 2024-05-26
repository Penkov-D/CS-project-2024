import os
import glob
import av
import cv2
import numpy as np
import io

frame_list = glob.glob('frames/*')
frame_list.sort(key=lambda name: int(name[12:]), reverse=True)


with open('video_2.h264', 'wb') as of:
    for frame_file in frame_list[3000::]:
        with open(frame_file, 'rb') as f:
            data = f.read()
            of.write(data)
            of.flush()

cap = cv2.VideoCapture('video_2.h264')

while cv2.waitKey(33) != ord('q'):
    ret, frame = cap.read()

    if not ret:
        # File ended
        break

    frame = cv2.resize(frame, None, fx = 0.5, fy = 0.5)
    cv2.imshow('frame', frame)
    
cv2.destroyAllWindows()
exit()
