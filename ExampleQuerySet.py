from OpenDJI import OpenDJI

import time

"""
In this example you will see how to use the set function.
The set function need a specific text, which is different for each key,
and should be obtained with the help of the 'help' function (e.g. keyInfo).
In this example we will set the leds behavior to be turned off for 10 seconds,
and then return them to the original state.
"""

# IP address of the connected android device
IP_ADDR = "10.0.0.6"


# Connect to the drone
with OpenDJI(IP_ADDR) as drone:
    
    # Get the LEDs information
    LEDs_settings_original = drone.getValue("FlightController", "LEDsSettings")
    print("Original result :", LEDs_settings_original)

    # The command to set the LEDs,
    # How I know the command protocol ? I used this command:
    #   print(drone.getKeyInfo("FlightController", "LEDsSettings"))
    LEDs_settings = \
    ('{'
        '"frontLEDsOn":false,'
        '"statusIndicatorLEDsOn":false,'
        '"rearLEDsOn":false,'
        '"navigationLEDsOn":false'
    '}')

    # Try to set the LEDs
    print(drone.setValue("FlightController", "LEDsSettings", LEDs_settings))

    # Get the LEDs information once again
    LEDs_settings_modified = drone.getValue("FlightController", "LEDsSettings")
    print("Modified result :", LEDs_settings_modified)

    time.sleep(10.0)

    # Finally, to not hurt any body, set back the original setting
    print(drone.setValue("FlightController", "LEDsSettings", LEDs_settings_original))
    print()
