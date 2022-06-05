# DroneFlight
## Introduction
This project is developed with Dji MSDK, which is able to access the remote controller of the DJI drones. And it allows users to start the live stream to their RTMP server that can see the video stream in real-time.
## Build
```
# for Windows
./gradlew :app:assembleDebug 
# for Linux or Mac OS
gradlew :app:assembleDebug
```
## Future
1. Add some object detecion algorithm on the App and detect in real-time of specific tasks.
2. Automatic zoom and forcus the specific target by controlling the gimbal.
