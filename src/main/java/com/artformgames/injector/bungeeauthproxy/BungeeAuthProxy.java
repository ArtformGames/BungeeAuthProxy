package com.artformgames.injector.bungeeauthproxy;

import cc.carm.lib.configuration.EasyConfiguration;
import com.artformgames.injector.bungeeauthproxy.handler.AuthHandler;
import com.artformgames.injector.bungeeauthproxy.handler.ProxiedAuthHandler;
import com.artformgames.injector.bungeeauthproxy.transformer.HttpClientTransformer;
import io.netty.channel.EventLoop;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.http.HttpClient;

import java.lang.instrument.Instrumentation;

import static com.artformgames.injector.bungeeauthproxy.Logging.debug;
import static com.artformgames.injector.bungeeauthproxy.Logging.log;

public class BungeeAuthProxy {

    private BungeeAuthProxy() {
    }

    private static AuthHandler handler;

    public static void premain(String args, Instrumentation instrumentation) {
        log(Logging.Level.INFO, "Loading auth configurations...");
        String configFileName = args == null || args.isBlank() ? "auth.yml" : args;
        EasyConfiguration.from(configFileName).initialize(Config.class);

        log(Logging.Level.INFO, "Initializing auth handler...");
        setHandler(new ProxiedAuthHandler()); // Use ProxiedAuthHandler by default

        log(Logging.Level.INFO, "Registering transformer...");
        instrumentation.addTransformer(new HttpClientTransformer(), true);

        log(Logging.Level.INFO, "Preload target class...");
        debug(" -> Target class: " + HttpClient.class.getName());

        log(Logging.Level.INFO, "Initialization complete!");
    }

    public static void submitRequest(String url, EventLoop loop, Callback<String> callback) throws Exception {
        log(Logging.Level.DEBUG, "Submitting request [" + url + "]");
        handler.submit(url, loop, callback);
    }

    public static AuthHandler getHandler() {
        return handler;
    }

    public static void setHandler(AuthHandler handler) {
        BungeeAuthProxy.handler = handler;
    }

}
