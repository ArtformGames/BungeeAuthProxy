package com.artformgames.injector.bungeeauthproxy.channel;

import com.artformgames.injector.bungeeauthproxy.Config;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.http.HttpHandler;

import javax.net.ssl.SSLEngine;
import java.util.concurrent.TimeUnit;

public class ProxiedHttpInitializer extends ChannelInitializer<Channel> {

    private final ProxyProtocolType type;
    private final Callback<String> callback;
    private final boolean ssl;
    private final String host;
    private final int port;

    public ProxiedHttpInitializer(ProxyProtocolType type, Callback<String> callback,
                                  boolean ssl, String host, int port) {
        this.type = type;
        this.callback = callback;
        this.ssl = ssl;
        this.host = host;
        this.port = port;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addFirst(type.createHandler());
        if (ssl) {
            SslContextBuilder builder = SslContextBuilder.forClient();
            if (Config.ADVANCE.DISABLE_SSL_VERIFY.getNotNull()) { // Trust all certificates;
                builder.trustManager(InsecureTrustManagerFactory.INSTANCE);
            }
            ch.pipeline().addLast("ssl", new SslHandler(builder.build().newEngine(ch.alloc(), host, port)));
        }
        ch.pipeline().addLast("timeout", new ReadTimeoutHandler(Config.SERVICE.TIME_OUT.getNotNull(), TimeUnit.MILLISECONDS));
        ch.pipeline().addLast("http", new HttpClientCodec());
        ch.pipeline().addLast("handler", new HttpHandler(callback));
    }
}