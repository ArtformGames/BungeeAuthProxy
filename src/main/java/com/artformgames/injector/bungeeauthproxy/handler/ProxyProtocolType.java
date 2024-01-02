package com.artformgames.injector.bungeeauthproxy.handler;

import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import jline.internal.Nullable;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.function.Supplier;

import static com.artformgames.injector.bungeeauthproxy.Config.PROXY.*;

public enum ProxyProtocolType {

    HTTP(() -> {
        if (AUTH.ENABLED.getNotNull()) {
            return new HttpProxyHandler(getProxyAddress(), AUTH.USERNAME.getNotNull(), AUTH.PASSWORD.getNotNull());
        } else {
            return new HttpProxyHandler(getProxyAddress());
        }
    }),
    SOCKS4(() -> new Socks4ProxyHandler(getProxyAddress(), getProxyUsername())),
    SOCKS5(() -> new Socks5ProxyHandler(getProxyAddress(), getProxyUsername(), getProxyPassword()));

    private final Supplier<ProxyHandler> handlerSupplier;

    ProxyProtocolType(Supplier<ProxyHandler> handlerSupplier) {
        this.handlerSupplier = handlerSupplier;
    }

    public ProxyHandler createHandler() {
        return handlerSupplier.get();
    }

    public static InetSocketAddress getProxyAddress() {
        return new InetSocketAddress(HOST.getNotNull(), PORT.getNotNull());
    }

    public static @Nullable String getProxyUsername() {
        return AUTH.ENABLED.getNotNull() ? AUTH.USERNAME.getNotNull() : null;
    }

    public static @Nullable String getProxyPassword() {
        return AUTH.ENABLED.getNotNull() ? AUTH.PASSWORD.getNotNull() : null;
    }

    public static @Nullable ProxyProtocolType parse(int id) {
        return Arrays.stream(values()).filter(type -> type.ordinal() == id).findFirst().orElse(null);
    }

    public static @Nullable ProxyProtocolType parse(String name) {
        return Arrays.stream(values()).filter(type -> type.name().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

}
