package com.artformgames.injector.bungeeauthproxy.handler;

import com.artformgames.injector.bungeeauthproxy.Config;
import com.artformgames.injector.bungeeauthproxy.channel.ProxiedHttpInitializer;
import com.artformgames.injector.bungeeauthproxy.channel.ProxyProtocolType;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.handler.codec.http.*;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.http.HttpInitializer;
import net.md_5.bungee.netty.PipelineUtils;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.artformgames.injector.bungeeauthproxy.Logging.debug;

public class ProxiedAuthHandler implements AuthHandler {

    protected Cache<String, InetAddress> addressCache;

    public ProxiedAuthHandler() {
        if (Config.getDNSCacheDuration() > 0) {
            addressCache = CacheBuilder.newBuilder()
                    .expireAfterWrite(Config.getDNSCacheDuration(), TimeUnit.MILLISECONDS)
                    .maximumSize(5).build();
        }
    }

    @Override
    public void submit(String authURL, EventLoop loop, Callback<String> callback) throws Exception {
        URI uri = new URI(authURL);
        Preconditions.checkNotNull((Object) uri.getScheme(), "scheme");
        Preconditions.checkNotNull((Object) uri.getHost(), "host");
        boolean ssl = uri.getScheme().equals("https");
        int port = getPort(uri);

        Bootstrap bootstrap = new Bootstrap().channel(PipelineUtils.getChannel(null)).group(loop);

        InetAddress address = resolveAddress(uri.getHost());
        ProxyProtocolType proxyProtocol = Config.getProxyProtocol();
        if (proxyProtocol != null) {
            debug("Using proxy protocol [" + proxyProtocol.name() + "] for " + uri.getHost() + ":" + port);
            bootstrap.handler(new ProxiedHttpInitializer(proxyProtocol, callback, ssl, uri.getHost(), port));
        } else {
            bootstrap.handler(new HttpInitializer(callback, ssl, uri.getHost(), port));
        }
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Config.getTimeoutDuration())
                .remoteAddress(address, port).connect().addListener((ChannelFutureListener) channel -> {
                    if (channel.isSuccess()) {
                        String path = uri.getRawPath() + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
                        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, path);
                        request.headers().set(HttpHeaderNames.HOST, uri.getHost());
                        channel.channel().writeAndFlush(request);
                    } else {
                        if (addressCache != null) addressCache.invalidate(uri.getHost());
                        callback.done(null, channel.cause());
                    }
                });
    }

    private InetAddress resolveAddress(String host) throws UnknownHostException, ExecutionException {
        if (this.addressCache == null) return InetAddress.getByName(host);
        else return addressCache.get(host, () -> InetAddress.getByName(host));
    }

    private static int getPort(URI uri) {
        if (uri.getPort() != -1) return uri.getPort();
        else if (uri.getScheme().equals("https")) return 443;
        else if (uri.getScheme().equals("http")) return 80;
        throw new IllegalArgumentException("Unknown scheme " + uri.getScheme());
    }

}
