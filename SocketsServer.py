import socket
import io
import av
import cv2

HOST = '0.0.0.0'
PORT = 8888


def decode_h264_frame_from_bytes(h264_data):
    # Decode the frame using OpenCV
    container = av.open(io.BytesIO(h264_data), format='h264')
    frames = []
    try:
        for frame in container.decode(video=0):
            # Convert the frame to an OpenCV image (numpy array)
            img = frame.to_ndarray(format='bgr24')
            frames.append(img)
            break  # Only read the first frame
    except:
        return None

    if frames:
        return frames[0]


with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    s.bind((HOST, PORT))
    s.listen()

    while True:
        conn, addr = s.accept()

        with conn:
            print(f'Connected by {addr}')

            data = conn.recv(100000)
            print(len(data))

            frame = decode_h264_frame_from_bytes(data)

            # print(frame.shape)

            if frame is not None:
                frame2 = cv2.resize(frame, None, fx = 0.5, fy = 0.5)
                print(frame2.shape)
                cv2.imshow('frame1', frame2)
                cv2.waitKey(1)