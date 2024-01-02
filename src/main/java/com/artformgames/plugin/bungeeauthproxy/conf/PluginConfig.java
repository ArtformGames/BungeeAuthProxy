package com.artformgames.plugin.bungeeauthproxy.conf;

import cc.carm.lib.configuration.core.Configuration;
import cc.carm.lib.configuration.core.annotation.HeaderComment;
import cc.carm.lib.configuration.core.value.type.ConfiguredValue;

public interface PluginConfig extends Configuration {

    ConfiguredValue<Boolean> DEBUG = ConfiguredValue.of(false);

    @HeaderComment({
            "Check update settings",
            "This option is used by the plug-in to determine whether to check for updates.",
            "If you do not want the plug-in to check for updates and prompt you, you can choose to close.",
            "Checking for updates is an asynchronous operation that will never affect performance and user experience."
    })
    ConfiguredValue<Boolean> CHECK_UPDATE = ConfiguredValue.of(true);

    @HeaderComment("MineCraft service settings")
    interface SERVICE extends Configuration {

        @HeaderComment("The authentication url for minecraft.net")
        ConfiguredValue<String> MOJANG_AUTH_URL = ConfiguredValue.of(
                "https://sessionserver.mojang.com/session/minecraft/hasJoined?username=%s&serverId=%s%s"
        );

    }

    @HeaderComment("Proxy server settings")
    interface PROXY extends Configuration {

        @HeaderComment("Whether to enable proxy to access the authentication services")
        ConfiguredValue<Boolean> ENABLE = ConfiguredValue.of(true);

        @HeaderComment("Proxy protocol, 0 = HTTP/HTTPS, 1 = SOCKS5")
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
