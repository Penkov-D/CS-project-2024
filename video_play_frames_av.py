import os
import glob
import av
import av.codec
import av.error
import cv2
import numpy as np
import io

frame_list = glob.glob('frames/*')
frame_list.sort(key=lambda name: int(name[12:]), reverse=True)


codec = av.codec.context.CodecContext.create('h264', 'r')
errors = 0

for frame_name in frame_list[:]:

    data = None
    with open(frame_name, 'rb') as f:
        data = f.read()

    for packet in codec.parse(data):
        try:
            for frame in codec.decode(packet):
                print('frame decoded')
                
                img = frame.to_ndarray(format='bgr24')
                img = cv2.resize(img, None, fx = 0.5, fy = 0.5)
                cv2.imshow('frame', img)
                cv2.waitKey(1)

        except av.error.InvalidDataError:
            errors += 1

cv2.destroyAllWindows()
print('number of errors: ', errors)