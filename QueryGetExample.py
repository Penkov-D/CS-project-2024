from OpenDJI import OpenDJI

import re

# IP address of the connected android device
IP_ADDR = "10.0.0.6"


# Connect to the drone
with OpenDJI(IP_ADDR) as drone:
    
    # Get the battery info
    battery_text = drone.getValue("RemoteController", "BatteryInfo")
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