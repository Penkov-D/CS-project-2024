import socket
import keyboard
import time

# Set IP and port
HOST = '10.0.0.6'
PORT_CONTROL = 9998

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sCommand:

    # Connect the control module
    sCommand.connect((HOST, PORT_CONTROL))

    # Press 'x' to close the program.
    while not keyboard.is_pressed('x'):

        # Delay a bit the command update, to not spam the server.
        # 100ms, means 10Hz, 10 command per second.
        time.sleep(0.1)

        # Core movement variables
        yaw = 0.0
        ascent = 0.0
        roll = 0.0
        pitch = 0.0

        # Coefficients for movement
        rotate_value = 0.1
        move_value = 0.03

        # Change the movement variables according to key presses
        if keyboard.is_pressed('a'): yaw = -rotate_value
        if keyboard.is_pressed('d'): yaw =  rotate_value
        if keyboard.is_pressed('s'): ascent = -move_value
        if keyboard.is_pressed('w'): ascent =  move_value

        if keyboard.is_pressed('left'): roll = -move_value
        if keyboard.is_pressed('right'): roll =  move_value
        if keyboard.is_pressed('down'): pitch = -move_value
        if keyboard.is_pressed('up'): pitch =  move_value
        
        # Command syntax example : "rc -0.100 0.231 0.000 -0.009"
        command = f'rc {yaw:.2f} {ascent:.2f} {roll:.2f} {pitch:.2f}'

        # Special commands
        if keyboard.is_pressed('f'): command = 'takeoff'
        if keyboard.is_pressed('r'): command = 'land'
        if keyboard.is_pressed('e'): command = 'enable'
        if keyboard.is_pressed('q'): command = 'disable'

        # Send the command
        sCommand.sendall(bytes(command + '\r\n', 'utf-8'))

        # Wait for return message
        data = sCommand.recv(10000, )
        if len(data) == 0:
            break

        print('Data size: ', len(data), 'bytes')
        print(data)
