```text
   ___                         ___       __  __   ___                   
  / _ )__ _____  ___ ____ ___ / _ |__ __/ /_/ /  / _ \_______ __ ____ __
 / _  / // / _ \/ _ `/ -_) -_) __ / // / __/ _ \/ ___/ __/ _ \\ \ / // /
/____/\_,_/_//_/\_, /\__/\__/_/ |_\_,_/\__/_//_/_/  /_/  \___/_\_\\_, / 
               /___/                                             /___/  
```

README LANGUAGES [ [English](README.md) | [**中文**](README_CN.md)  ]

![CodeSize](https://img.shields.io/github/languages/code-size/ArtformGames/BungeeAuthProxy)
[![Download](https://img.shields.io/github/downloads/ArtformGames/BungeeAuthProxy/total)](https://github.com/ArtformGames/BungeeAuthProxy/releases)
[![Java CI with Maven](https://github.com/ArtformGames/BungeeAuthProxy/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/ArtformGames//actions/workflows/maven.yml)
![Support](https://img.shields.io/badge/Minecraft-Java%201.16--Latest-green)

# **BungeeAuthProxy**

BungeeCord 服务器的身份验证代理注入器，
以尝试解决某些区域无法访问 MineCraft 在线会话和身份验证服务的问题。

## 用法

1. [在这里](https://github.com/ArtformGames/BungeeAuthProxy/releases)下载最新版本的注入器包。
2. 将 jar 文件放入 BungeeCord 服务器的文件夹中（与服务器jar在同一个文件夹）。
3. 在开服命令的 `-jar <server-jar>.jar` 前添加 `-javaagent:BungeeAuthProxy.jar[=<CONFIG-FILE-NAME>]`。
    - 以最简单的开发指令为例 `java -javaagent:BungeeAuthProxy.jar -jar BungeeCord.jar`
    - 若想要使用其他名称的配置文件 `java -javaagent:BungeeAuthProxy.jar=auth-proxy.yml -jar BungeeCord.jar`
4. 打开BungeeCord服务器，然后在配置文件中修改代理配置 (默认为服务器文件夹中的 `auth.yml`)。

## 配置文件

首次运行将自动生成配置文件，默认为 `auth.yml` 。

可以通过 `-javaagent:BungeeAuthProxy.jar=<CONFIG-FILE-NAME>` 来指定配置文件名称。

```yaml
debug: false

# 访问服务设定
service:
  # 单个请求的超时时间（以毫秒为单位）。
  time-out: 5000
  # 身份验证 url 的 dns缓存过期时间（以毫秒为单位）
  # 如果此值≤0，将禁用内置的 dns 缓存。
  dns-cache-expire: 60000

# 代理服务器设置
proxy:
  # 代理协议， -1 = NO_PROXY ,0 = HTTP/HTTPS, 1 = SOCKS4, 2 = SOCKS5
  protocol: -1
  # 代理服务器的地址
  host: 127.0.0.1
  # 代理服务器的端口
  port: 7890
  # 代理验证设置
  auth:
    # 是否启用代理验证
    enabled: false
    username: proxy-username
    password: proxy-password
```

## 开源协议

本开源项目基于 [GNU General Public License v3.0](https://opensource.org/licenses/GPL-3.0) 协议。

## 支持

此项目由 [Artfrom Games](https://github.com/ArtformGames/) 主持开发与维护。

万分感谢 Jetbrains 为我们提供了从事此项目和其他开源项目的许可。
[![](https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg)](https://www.jetbrains.com/?from=https://github.com/ArtformGames/BungeeAuthProxy)

