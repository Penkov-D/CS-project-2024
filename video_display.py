import cv2
import av


container = av.open('video_2.h264', 'r')

for frame in container.decode():

    d_frame = frame.to_ndarray(format='bgr24')
    d_frame = cv2.resize(d_frame, dsize=None, fx=0.5, fy=0.5)

    cv2.imshow('frame', d_frame)
    cv2.waitKey(100)

cv2.destroyAllWindows()
exit()


cap = cv2.VideoCapture('video.h264')

while cv2.waitKey(30) != ord('q'):

    ret, frame = cap.read()

    if ret:
        cv2.imshow('frame', frame)
    else:
        break

cv2.waitKey(0)
cv2.destroyAllWindows()
exit()