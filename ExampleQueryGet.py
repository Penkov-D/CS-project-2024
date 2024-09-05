from OpenDJI import OpenDJI

import re

"""
In this example you will have simple demo on how to get information fron the drone.
This will show how the message is received and should parsed, in this example the 
battery information from the remote controller is used.
"""

# IP address of the connected android device
IP_ADDR = "10.0.0.6"


# Connect to the drone
with OpenDJI(IP_ADDR) as drone:
    
    # Get the battery info
    battery_text = drone.getValue(OpenDJI.MODULE_REMOTECONTROLLER, "BatteryInfo")
    print("Original result :", battery_text)

    # you need to manually check for errors, and pharse the returned string
    battery_pattern = re.compile(
        '{"enabled":(.+),"batteryPower":(\\d+),"batteryPercent":(\\d+)}')

    # If the result match the regex, parse it.
    battery_match : re.Match = battery_pattern.fullmatch(battery_text)
    if battery_match is not None:

        # first value if enabled
        print("Enabled :", battery_match.group(1))

        # Second value for the pawer
        print("Power :", battery_match.group(2), "mah")

        # Third value is the percentage
        print("Percent :", battery_match.group(3), "%")

    print()