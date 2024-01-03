package com.artformgames.injector.bungeeauthproxy;

import cc.carm.lib.configuration.EasyConfiguration;
import com.artformgames.injector.bungeeauthproxy.handler.ProxiedAuthHandler;
import com.artformgames.injector.bungeeauthproxy.transformer.ProxyHandlerTransformer;
import io.netty.channel.EventLoop;
import net.md_5.bungee.api.Callback;

import java.lang.instrument.Instrumentation;

import static com.artformgames.injector.bungeeauthproxy.Logging.log;

public class BungeeAuthProxyInjector {

    private BungeeAuthProxyInjector() {
    }

    private static Instrumentation instance = null;
    private static ProxiedAuthHandler handler;

    public static void premain(String args, Instrumentation instrumentation) {
        BungeeAuthProxyInjector.instance = instrumentation;
        log(Logging.Level.INFO, "Loading auth configurations...");
        String configFileName = args == null || args.isBlank() ? "auth.yml" : args;
        EasyConfiguration.from(configFileName).initialize(Config.class);

        log(Logging.Level.INFO, "Initializing auth handler...");
        handler = new ProxiedAuthHandler();

        log(Logging.Level.INFO, "Injecting InitialHandler...");
        instrumentation.addTransformer(new ProxyHandlerTransformer(), true);
    }

    public static void submitRequest(String url, EventLoop loop, Callback<String> callback) throws Exception {
        log(Logging.Level.DEBUG, "Submitting request [" + url + "]");
        getHandler().submit(url, loop, callback);
    }

    public static Instrumentation getInstance() {
        return instance;
    }

    public static ProxiedAuthHandler getHandler() {
        return handler;
    }


}
