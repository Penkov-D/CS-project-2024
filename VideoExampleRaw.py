import socket
import av
import av.codec
import cv2

# Set IP and port
HOST = '10.0.0.6'
PORT_VIDEO = 9999

# Set the codec for raw H264 data
codec = av.codec.context.CodecContext.create('h264', 'r')

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sVideo:

    # Connect the video module
    sVideo.connect((HOST, PORT_VIDEO))

    # The 20ms delay equal roughly 50 frames per second,
    # the drone transmit in 30 frames per seconds.
    # Press 'q' to close the program.
    while cv2.waitKey(20) != ord('q'):

        # Receive raw data
        data = sVideo.recv(100000)
        if len(data) == 0:
            break

        print('Data size: ', len(data), 'bytes')

        # Decode the data to packets
        for packet in codec.parse(data):
            # and then decode the data to frames
            for frame in codec.decode(packet):

                # Convert frame object to numpy array (for openCV)
                img = frame.to_ndarray(format = 'bgr24')
                # Resize the image a bit
                img = cv2.resize(img, None, fx = 0.5, fy = 0.5)
                cv2.imshow('stream', img)
                