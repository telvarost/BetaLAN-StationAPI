# BetaLAN StationAPI Edition for Minecraft Beta 1.7.3

A StationAPI mod for Minecraft Beta 1.7.3 that allows easily launching a server using the currently loaded single-player world and currently in use mod-set/configuration.
* Configure server settings and java version using GlassConfigAPI 3 (GCAPI3).
* In v1.1.0 and above a backup world zip file is created on server launch (if the config setting is turned on).
  * The backup file's name begins with an underscore (`_`) and ends with `.zip`
  * The backup file is recreated everytime the server is launched using the most recent world files

## Troubleshooting

* I strongly recommend launching the server jar from your .minecraft directory if the loading bar gets stuck at "Preparing World"
  * Most likely it crashed due to a mod incompatibility
* Sometimes the client will crash when it joins the world, this seems to be a common bug even when joining normal servers
  * Simply close the client, relaunch, and try again usually it goes away when trying to join a server the second time

## Installation using Prism Launcher

1. Download an instance of Babric for Prism Launcher: https://github.com/babric/prism-instance
2. Install Java 17 and set the instance to use it: https://adoptium.net/temurin/releases/
3. Add GlassConfigAPI 3.0.2+ to the mod folder for the instance: https://modrinth.com/mod/glass-config-api
4. Add Glass Networking to the mod folder for the instance: https://modrinth.com/mod/glass-networking
5. Add StationAPI to the mod folder for the instance: https://modrinth.com/mod/stationapi
6. (Optional) Add Mod Menu to the mod folder for the instance: https://modrinth.com/mod/modmenu-beta
7. Add this mod to the mod folder for the instance: https://github.com/telvarost/BetaLAN-StationAPI/releases
8. Run and enjoy! 👍

## Feedback

Got any suggestions on what should be added next? Feel free to share it by [creating an issue](https://github.com/telvarost/BetaLAN-StationAPI/issues/new). Know how to code and want to do it yourself? Then look below on how to get started.

## Contributing

Thanks for considering contributing! To get started fork this repository, make your changes, and create a PR. 

If you are new to StationAPI consider watching the following videos on Babric/StationAPI Minecraft modding: https://www.youtube.com/watch?v=9-sVGjnGJ5s&list=PLa2JWzyvH63wGcj5-i0P12VkJG7PDyo9T
