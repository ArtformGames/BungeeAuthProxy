package com.artformgames.injector.bungeeauthproxy;

import cc.carm.lib.configuration.EasyConfiguration;
import com.artformgames.injector.bungeeauthproxy.handler.ProxiedAuthHandler;
import com.artformgames.injector.bungeeauthproxy.transformer.ProxyHandlerTransformer;
import io.netty.channel.EventLoop;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.connection.InitialHandler;

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
        EasyConfiguration.from("auth.yml").initialize(Config.class);

        log(Logging.Level.INFO, "Initializing auth handler...");
        handler = new ProxiedAuthHandler();

        log(Logging.Level.INFO, "Injecting InitialHandler...");
        instrumentation.addTransformer(new ProxyHandlerTransformer());

        
        try {
            ClassPool pool = ClassPool.getDefault();
            pool.importPackage("com.artformgames.injector.bungeeauthproxy");

            CtClass handlerClass = pool.getCtClass("net.md_5.bungee.connection.InitialHandler");
            CtClass responseClass = pool.getCtClass("net.md_5.bungee.protocol.packet.EncryptionResponse");

            CtMethod handleMethod = handlerClass.getDeclaredMethod("handle", new CtClass[]{responseClass});
            log(Logging.Level.DEBUG, "Found target method: " + handleMethod.getLongName());
        } catch (Exception ex) {
            log(Logging.Level.ERROR, "Failed to inject handlers, are you really using BungeeCord?");
            ex.printStackTrace();
        }

    }

    public static void submitRequest(InitialHandler handler, String encodedHash,
                                     EventLoop loop, Callback<String> callback) throws Exception {
        log(Logging.Level.DEBUG, "Submitting request [" + handler.getName() + "] " + encodedHash);
        getHandler().submit(handler, encodedHash, loop, callback);
    }

    public static Instrumentation getInstance() {
        return instance;
    }

    public static ProxiedAuthHandler getHandler() {
        return handler;
    }


}
