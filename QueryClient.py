import socket
import keyboard
import time

HOST = '10.0.0.5'
PORT_VIDEO = 9997



with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sCommand:

    sCommand.connect((HOST, PORT_VIDEO))


    while True:

        command = input("> ")
        sCommand.sendall(bytes(command + '\r\n', 'utf-8'))

        time.sleep(0.1)
        data = sCommand.recv(1000000, )
        if len(data) == 0:
            break

        print('Data size: ', len(data), 'bytes')
        print(data.decode("utf-8"))
