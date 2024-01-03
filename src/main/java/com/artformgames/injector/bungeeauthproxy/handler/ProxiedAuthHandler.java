package com.artformgames.injector.bungeeauthproxy.handler;

import com.artformgames.injector.bungeeauthproxy.Config;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.handler.codec.http.*;
import jline.internal.Nullable;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.http.HttpInitializer;
import net.md_5.bungee.netty.PipelineUtils;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import static com.artformgames.injector.bungeeauthproxy.Logging.debug;

public class ProxiedAuthHandler {

    protected Cache<String, InetAddress> addressCache;

    public ProxiedAuthHandler() {
        if (Config.SERVICE.DNS_CACHE_EXPIRE.getNotNull() > 0) {
            addressCache = CacheBuilder.newBuilder()
                    .expireAfterWrite(Config.SERVICE.DNS_CACHE_EXPIRE.getNotNull(), TimeUnit.MILLISECONDS)
                    .build();
        }
    }

    public void submit(String authURL, EventLoop loop, Callback<String> callback) throws Exception {
        URI uri = new URI(authURL);
        Preconditions.checkNotNull((Object) uri.getScheme(), "scheme");
        Preconditions.checkNotNull((Object) uri.getHost(), "host");
        boolean ssl = uri.getScheme().equals("https");
        int port = getPort(uri);

        Bootstrap bootstrap = new Bootstrap().channel(PipelineUtils.getChannel(null)).group(loop);

        InetAddress address = resolveAddress(uri.getHost());
        ProxyProtocolType proxyProtocol = getProxyProtocol();
        if (proxyProtocol != null) {
            debug("Using proxy protocol [" + proxyProtocol.name() + "] for " + uri.getHost() + ":" + port);
            bootstrap.handler(new ProxiedHttpInitializer(proxyProtocol, callback, ssl, uri.getHost(), port));
        } else {
            bootstrap.handler(new HttpInitializer(callback, ssl, uri.getHost(), port));
        }
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.max(Config.SERVICE.TIME_OUT.getNotNull().intValue(), 100))
                .remoteAddress(address, port).connect().addListener((ChannelFutureListener) channel -> {
                    if (channel.isSuccess()) {
                        String path = uri.getRawPath() + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
                        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, path);
                        request.headers().set(HttpHeaderNames.HOST, uri.getHost());
                        channel.channel().writeAndFlush(request);
                    } else {
                        addressCache.invalidate(uri.getHost());
                        callback.done(null, channel.cause());
                    }
                });
    }

    private InetAddress resolveAddress(String host) throws UnknownHostException {
        if (this.addressCache == null) return InetAddress.getByName(host);

        InetAddress inetHost = addressCache.getIfPresent(host);
        if (inetHost == null) {
            inetHost = InetAddress.getByName(host);
            addressCache.put(host, inetHost);
        }
        return inetHost;
    }

    public @Nullable ProxyProtocolType getProxyProtocol() {
        return ProxyProtocolType.parse(Config.PROXY.PROTOCOL.getNotNull());
    }

    private static int getPort(URI uri) {
        int port = uri.getPort();
        if (port != -1) return port;
        switch (uri.getScheme()) {
            case "http": {
                return 80;
            }
            case "https": {
                return 443;
            }
            default: {
                throw new IllegalArgumentException("Unknown scheme " + uri.getScheme());
            }
        }
    }

}
