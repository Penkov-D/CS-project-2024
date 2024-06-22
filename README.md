# CS-project-2024
This project seek to give convenient way to control the `DJI mini 3 pro` drone (and similars) without the need to make android app each time!

The project consist of a made app the you run on android phone, and controll the drone from your PC using *python* or *c++* with ease.

## How to use
For now, the project doesn't consist of release version and is under development.
The video-server is set (sending live video from the drone to PC) and when the control-server is done (controlling the drone from PC),
there will be update on the main branch that will give this core functions.
If one wish, he can see the updates and the work done so far in the `live-video-server` branch.


## How to compile - drone side
### Android studio
To complie / build the project you will need to install android studio: https://developer.android.com/studio/install

### Registering as DJI Developer
To use the MSDK with the drone, one need to obtain a developer key <br>
For that register to DJI Developer : https://account.dji.com/register <br>
We suggest using the school / university email address for that.

Once registered, go to the developer center : https://developer.dji.com/user/apps <br>
And register an app with the following parameters:
* App Type : Mobile SDK
* App Name : MSDKRemote
* Software Platform : Android
* Package Name : com.msdkremote
* Category : *as you want*
* Description : *as you want*

Where ever confirmation is needed just follow the sites quests, usually email confirmation. <br>
Now if you go again to developer center, you should see the app listed, and by clicking in the app-key should appear,
a 24 long hexdecimal string. It will be used in the next step.

### Setting the Project 
Open Android Studio, when promoted to create or open a project, click on open and open the `/MSDKRemote` folder as the root of the project.
> Do not open the whole git repository!

When the project is all loaded (give it some time for the gardle to sync) in the right side in the project tree, go to `app/res/values`,
right click on it and create `value resource file`. Name him `secrets`, and leave the other options alone. Now, inside `app/res/values`
should be file named `secrets.xml`. Open him and set him as following:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="API_KEY">xxxxxxxxxxxxxxxxxxxxxxxx</string>
</resources>
```
Replace the x's with your app key you obtained the previous step. 

### Running the Project
That's it! you now can build the app (the hummer icon upper right side) or even run it on your device! (green arrow icon top side). <br>
First build may take time as the Android Studio download dependencies that needed to build the app. The next builds are instant.

## How to compile - user side
Most of the scripts provided in pure python, so it souldn't be a problem to just run them, once all the relevant packages are installed. <br>
When the project will become more complex, this section will be extended.

NOTE : some scripts uses both *OpenCV* and [*AV*](https://pypi.org/project/av/) (ffmpeg python wrap), and becouse both use ffmpeg library underhood may create [unexpected behavior](https://stackoverflow.com/a/78482628). <br>
To address this issue, install *av* with the following as following:
```bash
sudo apt-get install libavformat-dev libavdevice-dev
pip install av --no-binary av
```

### Reported hours on the project
https://docs.google.com/spreadsheets/d/13MvI2DrXtzP0ggId6Q-3oknLaDBWd-IZYB2vUNI7Uto/edit?usp=sharing
