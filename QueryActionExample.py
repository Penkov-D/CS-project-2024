from OpenDJI import OpenDJI

import time

# IP address of the connected android device
IP_ADDR = "10.0.0.6"


# Connect to the drone
with OpenDJI(IP_ADDR) as drone:
    
    # Reboot the controller
    print(drone.action("RemoteController", "RebootDevice"))


    # Uncomment the following secion at your own risk

    # # Takeoff
    # print(drone.action("FlightController", "StartTakeoff"))
    #
    # time.sleep(10.0)
    #
    # # land
    # print(drone.action("FlightController", "StartAutoLanding"))

    print()