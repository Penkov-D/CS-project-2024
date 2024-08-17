import socket

HOST = '10.100.102.49'
PORT_KEY = 9997

def send_command(command, server):
    server.sendall(bytes(command + '\r\n', 'utf-8'))
    print(server.recv(10000, ) , '\n')


with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sCommand:
    sCommand.connect((HOST, PORT_KEY))

    send_command('set FlightController DistanceLimit 200', sCommand)
    send_command('get FlightController DistanceLimit', sCommand)
    send_command('action FlightController DistanceLimit', sCommand)
    send_command('blah blah blah', sCommand)
