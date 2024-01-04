```text
   ___                         ___       __  __   ___                   
  / _ )__ _____  ___ ____ ___ / _ |__ __/ /_/ /  / _ \_______ __ ____ __
 / _  / // / _ \/ _ `/ -_) -_) __ / // / __/ _ \/ ___/ __/ _ \\ \ / // /
/____/\_,_/_//_/\_, /\__/\__/_/ |_\_,_/\__/_//_/_/  /_/  \___/_\_\\_, / 
               /___/                                             /___/  
```

README LANGUAGES [ [**English**](README.md) | [中文](README_CN.md)  ]

![CodeSize](https://img.shields.io/github/languages/code-size/ArtformGames/BungeeAuthProxy)
[![Download](https://img.shields.io/github/downloads/ArtformGames/BungeeAuthProxy/total)](https://github.com/ArtformGames/BungeeAuthProxy/releases)
[![Java CI with Maven](https://github.com/ArtformGames/BungeeAuthProxy/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/ArtformGames//actions/workflows/maven.yml)
![Support](https://img.shields.io/badge/Minecraft-Java%201.16--Latest-green)

# **BungeeAuthProxy**

Authentication proxy injector for BungeeCord servers,
which is trying to solve the problem of inability to access the MineCraft online session and auth service in some areas.

## Usage

**Before using this injector, make sure that your Java version is 11 or above.**

1. Download latest release from [here](https://github.com/ArtformGames/BungeeAuthProxy/releases) .
2. Put the jar file into the folder of your BungeeCord server (same path with server jar).
3. Add the `-javaagent:BungeeAuthProxy.jar[=<CONFIG-FILE-NAME>]` before `-jar <server-jar>.jar` to the start command of
   your BungeeCord server.
    - For example: `java -javaagent:BungeeAuthProxy.jar -jar BungeeCord.jar`
    - Using custom config file name: `java -javaagent:BungeeAuthProxy.jar=auth-proxy.yml -jar BungeeCord.jar`
4. Start your bungeecord server, and configured the proxy (Default is `auth.yml` in server folder).

## Configurations

Will be generated on the first boot up.

```yaml
debug: false

# MineCraft service settings
service:
  # Timeout duration for single request in milliseconds.
  time-out: 5000
  # Authentication url dns-cache expire duration in milliseconds
  # If this value ≤0, will disable dns-cache.
  dns-cache-expire: 60000

# Proxy server settings
proxy:
  # Proxy protocol, -1 = NO_PROXY ,0 = HTTP/HTTPS, 1 = SOCKS4, 2 = SOCKS5
  protocol: -1
  # Proxy host
  host: 127.0.0.1
  # Proxy port
  port: 7890
  # Proxy authentication settings
  auth:
    # Whether to enable proxy authentication
    enabled: false
    username: proxy-username
    password: proxy-password

advance:
   # Remove unused field after injection.
   # If any 'NoSuchFieldException' or 'IllegalAccessException' occurred, try to set this to false.
   remove-unused-field: true
```

## Open Source Licence

The source code of this project adopts the [GNU General Public License v3.0](https://opensource.org/licenses/GPL-3.0).

## Supports

This project is mainly developed by the [Artfrom Games](https://github.com/ArtformGames/) .

Many thanks to Jetbrains for kindly providing a license for us to work on this and other open-source projects.  
[![](https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg)](https://www.jetbrains.com/?from=https://github.com/ArtformGames/BungeeAuthProxy)

