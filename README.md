# Legacy-Ascendancy-Parent
Unofficial (legacy) implementation of the Ascendancy game mode using the [Sponge API](www.spongepowered.org).
<br>
This project is not affiliated with the official Ascendancy Game mode. 
<br>

# Setup
The project utilizes git submodules for individual components of implementation. 
- The **AscendancyServerPlugin** module represents the server-side projects
- The **AscendancyLib** module represents common classes (such as network protocol) for both the client and server. 
- The **AscnednacyCustomMod** module represents the client side mod needed for the core game logic. 

# Compiling
This project uses [Gradle](www.gradle.org) 4.10.3 and requires Java 8. 
<br> 
**NOTE**: The project will not compile on anything new than Java 11 due to the version of [ForgeGradle](https://github.com/MinecraftForge/ForgeGradle) used. 
<br>
To compile the project: 
<br>
Unix-like: 
`./gradlew build`
<br>
Windows: 
`gradlew build`
<br>

# Misc
- This project targets [Minecraft](www.minecraft.net) version *1.12.2*. 

# Copyright
