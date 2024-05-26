import socket
import io
import av
import av.codec
import cv2

HOST = '0.0.0.0'
PORT = 8888

codec = av.codec.context.CodecContext.create('h264', 'r')

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:

    s.bind((HOST, PORT))
    s.listen()

    conn, addr = s.accept()

    with conn:
        print(f'Connected by {addr}')

        while True:

            data = conn.recv(10000)
            if len(data) == 0:
                break

            print('Data size: ', len(data), 'bytes')

            for packet in codec.parse(data):
                for frame in codec.decode(packet):
                    img = frame.to_ndarray(format = 'bgr24')
                    img = cv2.resize(img, None, fx = 0.5, fy = 0.5)
                    cv2.imshow('stream', img)
                    cv2.waitKey(1)