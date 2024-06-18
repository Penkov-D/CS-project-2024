import socket
import io
import av
import av.codec
import cv2

HOST = '10.0.0.6'
PORT_VIDEO = 9999
PORT_CONTROL = 9998

codec = av.codec.context.CodecContext.create('h264', 'r')

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sVideo:
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sControl:

        sVideo.connect((HOST, PORT_VIDEO))
        sControl.connect((HOST, PORT_CONTROL))

        while True:

            data = sVideo.recv(1000000)
            if len(data) == 0:
                break

            print('Data size: ', len(data), 'bytes')

            for packet in codec.parse(data):
                for frame in codec.decode(packet):
                    img = frame.to_ndarray(format = 'bgr24')
                    img = cv2.resize(img, None, fx = 0.5, fy = 0.5)
                    cv2.imshow('stream', img)
                    
            key =  cv2.waitKey(50)
            
            if key == ord('q'):
                sControl.send('0.0.0.0\n'.encode('utf-8'))
                exit()
            elif key == ord('w'):
                sControl.send('0.100.0.0\n'.encode('utf-8'))
            elif key == ord('s'):
                sControl.send('0.-100.0.0\n'.encode('utf-8'))
            elif key == ord('a'):
                sControl.send('-100.0.0.0\n'.encode('utf-8'))
            elif key == ord('d'):
                sControl.send('100.0.0.0\n'.encode('utf-8'))

            elif key == ord('y'):
                sControl.send('0.0.0.100\n'.encode('utf-8'))
            elif key == ord('h'):
                sControl.send('0.0.0.-100\n'.encode('utf-8'))
            elif key == ord('j'):
                sControl.send('0.0.100.0\n'.encode('utf-8'))
            elif key == ord('g'):
                sControl.send('0.0.-100.0\n'.encode('utf-8'))

            elif key == ord('r'):
                sControl.send('takeoff\n'.encode('utf-8'))
            elif key == ord('f'):
                sControl.send('land\n'.encode('utf-8'))
            
            else:
                sControl.send('0.0.0.0\n'.encode('utf-8'))

