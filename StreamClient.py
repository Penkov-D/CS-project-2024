import socket
import io
import av
import av.codec
import cv2

HOST = '10.0.0.6'
PORT = 9999

codec = av.codec.context.CodecContext.create('h264', 'r')

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:

    s.connect((HOST, PORT))

    while True:

        data = s.recv(10000)
        if len(data) == 0:
            break

        print('Data size: ', len(data), 'bytes')

        for packet in codec.parse(data):
            for frame in codec.decode(packet):
                img = frame.to_ndarray(format = 'bgr24')
                img = cv2.resize(img, None, fx = 0.5, fy = 0.5)
                cv2.imshow('stream', img)
                if cv2.waitKey(1) == ord('q'):
                    exit()
                
