import socket
import threading
import time

HOST = '10.0.0.5'
PORT_VIDEO = 9997


info = """
      <<< COMMANDS >>>

    Type '?' to show this helping message.

    Type 'help' to view all available modules.
    Type 'help <module>' to view all available keys inside the module <module>.
    Type 'help <module> <key>' to view information about the key <key> inside the module <module>.

    Type 'get <module> <key>' to get value about key <key> from module <module>.
    Type 'listen <module> <key>' to listen on changes of key <key> from module <module>.
    Type 'unlisten <module> <key>' to remove the listener from key <key> from module <module>.
    Type 'set <module> <key> <param>' to set the parameter <param> in key <key> from module <module>.
    Type 'action <module> <key>' to perform action on key <key> from module <module>.
    Type 'action <module> <key> <param>' to perform action with parameter<param> on key <key> from module <module>.

    Type 'quit' or 'exit' to close the program.

    
      <<< EXAMPLES >>>
  
> help
{"Gimbal","RemoteController","FlightController","Battery","AirLink","Product",
"Camera"}

> help Battery
{"StopCommonFileUpdate","HeatingState","BatteryBreakCellIndex",...

> help Battery Connection
{module:'Battery', key:'Connection', CanGet:true, CanSet:false, CanListen:true,
CanAction:false, parameter:'java.lang.Boolean', example:"true"}

> help Battery HeatingState
{module:'Battery', key:'HeatingState', CanGet:true, CanSet:false,
CanListen:true, CanAction:false, parameter:'dji.sdk.keyvalue.value.battery.
BatteryHeatingState', values:[IDLE,HEATING,INSULATION,UNKNOWN]}

> get FlightController AircraftName
FlightController AircraftName DJI MINI 3 PRO

> set Gimbal GimbalVerticalShotEnabled true
Gimbal GimbalVerticalShotEnabled success

> set FlightController LEDsSettings {"frontLEDsOn":false,"statusIndicatorLEDsOn":false,"rearLEDsOn":false,"navigationLEDsOn":true}
FlightController LEDsSettings success

"""


def read_message(sock):
    
    while True:
        try:
            data = sCommand.recv(1000000, )
            if not data:
                break
            print(data.decode("utf-8"))

        except Exception as e:
            print('Error -', e)
            break


print()
print("  Make sure the application is open, and you enther the correct IP address!")
print()
print("  Type '?' to show helping message.")
print()

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sCommand:

    sCommand.connect((HOST, PORT_VIDEO))

    # Retrive the output in the background - mainly for 'listen' commands.
    thread = threading.Thread(target = read_message, args = (sCommand,))
    thread.daemon = True
    thread.start()

    while True:

        command = input("> ")

        if command == "": continue
        if command.isspace(): continue
        if command == "quit": break
        if command == "exit": break

        if command == "?":
            print(info)
            continue

        sCommand.sendall(bytes(command + '\r\n', 'utf-8'))
        # Give a moment to read the output
        time.sleep(0.5)

    sCommand.close()

print('bye!')

