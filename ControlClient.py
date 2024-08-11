import socket
import keyboard
import time

HOST = '10.0.0.6'
PORT_VIDEO = 9998


with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sCommand:

    sCommand.connect((HOST, PORT_VIDEO))


    while True:

        time.sleep(0.1)

        lh = 0.0
        lv = 0.0
        rh = 0.0
        rv = 0.0

        move_value = 0.03

        if keyboard.is_pressed('a'):
            lh = -move_value * 10
        if keyboard.is_pressed('d'):
            lh =  move_value * 10
        if keyboard.is_pressed('s'):
            lv = -move_value
        if keyboard.is_pressed('w'):
            lv =  move_value

        if keyboard.is_pressed('left'):
            rh = -move_value
        if keyboard.is_pressed('right'):
            rh =  move_value
        if keyboard.is_pressed('down'):
            rv = -move_value
        if keyboard.is_pressed('up'):
            rv =  move_value
        
        command = f'rc {lh:.2f} {lv:.2f} {rh:.2f} {rv:.2f}'

        if keyboard.is_pressed('f'):
            command = 'takeoff'

        if keyboard.is_pressed('r'):
            command = 'land'

        if keyboard.is_pressed('e'):
            command = 'enable'

        if keyboard.is_pressed('q'):
            command = 'disable'

        sCommand.sendall(bytes(command + '\r\n', 'utf-8'))

        data = sCommand.recv(10000, )
        if len(data) == 0:
            break

        print('Data size: ', len(data), 'bytes')

        print(data)
