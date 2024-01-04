package com.artformgames.injector.bungeeauthproxy;

import cc.carm.lib.configuration.core.Configuration;
import cc.carm.lib.configuration.core.annotation.HeaderComment;
import cc.carm.lib.configuration.core.value.type.ConfiguredValue;
import com.artformgames.injector.bungeeauthproxy.channel.ProxyProtocolType;

@HeaderComment({
        "BungeeAuthProxy injector configurations",
        "See https://github.com/ArtformGames/BungeeAuthProxy for more information."
})
public interface Config extends Configuration {

    static ProxyProtocolType getProxyProtocol() {
        return ProxyProtocolType.parse(PROXY.PROTOCOL.getNotNull());
    }

    static int getTimeoutDuration() {
        return Math.max(SERVICE.TIME_OUT.getNotNull().intValue(), 100);
    }

    static long getDNSCacheDuration() {
        return Math.max(SERVICE.DNS_CACHE_EXPIRE.getNotNull(), -1L);
    }

    @HeaderComment("Debug mode for developers, with more detailed logs.")
    ConfiguredValue<Boolean> DEBUG = ConfiguredValue.of(false);

    @HeaderComment("MineCraft service settings")
    interface SERVICE extends Configuration {

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

        @HeaderComment("Proxy protocol, -1 = NO_PROXY, 0 = HTTP/HTTPS, 1 = SOCKS4, 2 = SOCKS5")
        ConfiguredValue<Integer> PROTOCOL = ConfiguredValue.of(-1);

        @HeaderComment("Proxy host")
        ConfiguredValue<String> HOST = ConfiguredValue.of("127.0.0.1");

        @HeaderComment("Proxy port")
        ConfiguredValue<Integer> PORT = ConfiguredValue.of(7890);

        @HeaderComment({
                "Proxy authentication settings",
                "If proxy authentication is not required, set 'enabled' to false."
        })
        interface AUTH extends Configuration {

            ConfiguredValue<Boolean> ENABLED = ConfiguredValue.of(false);

            ConfiguredValue<String> USERNAME = ConfiguredValue.of("proxy-username");

            ConfiguredValue<String> PASSWORD = ConfiguredValue.of("proxy-password");

        }

    }

    interface ADVANCE extends Configuration {

        @HeaderComment({
                "Remove unused field after injection.",
                "If any 'NoSuchFieldException' or 'IllegalAccessException' occurred, try to set this to false."
        })
        ConfiguredValue<Boolean> REMOVE_UNUSED_FILED = ConfiguredValue.of(true);

    }


}
