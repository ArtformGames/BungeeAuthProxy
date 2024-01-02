package com.artformgames.injector.bungeeauthproxy;

import cc.carm.lib.configuration.core.Configuration;
import cc.carm.lib.configuration.core.annotation.HeaderComment;
import cc.carm.lib.configuration.core.value.type.ConfiguredValue;

@HeaderComment({
        "BungeeAuthProxy injector configurations",
        "See https://github.com/ArtformGames/BungeeAuthProxy for more information."
})
public interface Config extends Configuration {

    ConfiguredValue<Boolean> DEBUG = ConfiguredValue.of(false);

    @HeaderComment("MineCraft service settings")
    interface SERVICE extends Configuration {

        @HeaderComment("The authentication url for minecraft.net")
        ConfiguredValue<String> MOJANG_AUTH_URL = ConfiguredValue.of(
                "https://sessionserver.mojang.com/session/minecraft/hasJoined?username=%s&serverId=%s%s"
        );

        @HeaderComment("Timeout duration for single request in milliseconds.")
        ConfiguredValue<Long> TIME_OUT = ConfiguredValue.of(5000L);

        @HeaderComment({
                "Authentication url dns-cache expire duration in milliseconds",
                "If this value ≤0, will disable dns-cache."
        })
        ConfiguredValue<Long> DNS_CACHE_EXPIRE = ConfiguredValue.of(60000L);

    }

    @HeaderComment("Proxy server settings")
    interface PROXY extends Configuration {

        @HeaderComment("Whether to enable proxy to access the authentication services")
        ConfiguredValue<Boolean> ENABLE = ConfiguredValue.of(true);

        @HeaderComment("Proxy protocol, 0 = HTTP/HTTPS, 1 = SOCKS4, 2 = SOCKS5")
        ConfiguredValue<Integer> PROTOCOL = ConfiguredValue.of(1);

        @HeaderComment("Proxy host")
        ConfiguredValue<String> HOST = ConfiguredValue.of("127.0.0.1");

        @HeaderComment("Proxy port")
        ConfiguredValue<Integer> PORT = ConfiguredValue.of(7890);

        @HeaderComment("Proxy authentication settings")
        interface AUTH extends Configuration {

            @HeaderComment("Whether to enable proxy authentication")
            ConfiguredValue<Boolean> ENABLED = ConfiguredValue.of(false);

            ConfiguredValue<String> USERNAME = ConfiguredValue.of("proxy-username");

            ConfiguredValue<String> PASSWORD = ConfiguredValue.of("proxy-password");

        }

    }


}
