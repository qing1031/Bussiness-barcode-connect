Connect Mobile
===============================

Android App to Access FLQ Connect


If developing for the Motorola TC55, some extra steps are required to get it running with ADB.
In your android sdk folder, there should be a file called adb_usb.ini, which contains the vendor
numbers for the phones connected by usb. The TC55 doesn't generate these automatically, so they must
be added manually. The two device id's are:
- 0x0451
- 0x05E0

To enable both the soft keyboard and the scanner device, it may be necessary to change your Android
Language and Input settings. To do this:
1) Go to Settings
2) Select "Language & input"
3) Click on "Default"
4) If Hardware (Physical Keyboard) is set to "On" change it to "Off"
Note that some Samsung devices (Samsung Galaxy 3, for example) do not have this option.

If using GenyMotion, you need to set your Customer Connect Mobile Host to http://192.168.56.1:PORT

Reference:
https://developer.motorolasolutions.com/docs/DOC-1880

# FLQAssets
When first pulling the project, it will be necessary to also pull in the FLQAssets module. This is done
using docker.
From the root directory of the project, run

`docker-compose build`

which will pull the flqAssets project
into the container. Following this run

`docker-compose up`

which will copy the flqAssets project from
the tmp directory into the project. It is recommended to update flqAssets in the project as soon as 
it is changed in the upstream.

@djak250: I've found it's easiest to pull the flqAssets project (https://github.com/FoodLogiQ/flq-android-assets)
into my android dev environment and copy the files over directly in instances where I'm updating flqAssets
often. Like so: 

`cp -R flq-android-assets/flqassets/ connect-mobile/flqassets/`

from the parent directory.
Resync your connect-mobile project, and you should be good to go.

# Application Signing    
In order to build the app for install correctly, the keystore needs to be downloaded and used to 
sign the application files. Contact the tech lead for access.
The build release exposes a settings cog on the login page to change which host the application
uses. This can be used to redirect requests to a dev environment when developing. PS: If using docker,
the developer will need to expose the port that connect is running on to the host machine. Then change
the connect-mobile host to point to the host machine's ip:port. Again, contact the tech lead, if more
information is needed.

# PRIVATE AND CONFIDENTIAL

Tech Lead: https://github.com/djak250