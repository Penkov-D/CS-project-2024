
import subprocess
import sys
import io
import av
import av.codec
import cv2
import tempfile

adbCmd = ['adb', 'shell', 'screenrecord --size=720x1280 --time-limit=180 --output-format=h264 -', ]
stream = subprocess.Popen(adbCmd, stdin = subprocess.PIPE, stdout = subprocess.PIPE)

codec = av.codec.context.CodecContext.create('h264', 'r')

while cv2.waitKey(1) != ord('q'):

    data = stream.stdout.read(1000)
    if len(data) == 0: break

    for packet in codec.parse(data):
        for frame in codec.decode(packet):
            img = frame.to_ndarray(format='bgr24')
            img = cv2.resize(img, None, fx = 0.5, fy = 0.5)
            cv2.imshow('frame', img)

cv2.destroyAllWindows()
stream.kill()
exit()