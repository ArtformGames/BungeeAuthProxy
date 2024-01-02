package com.artformgames.injector.bungeeauthproxy.handler;

import com.artformgames.injector.bungeeauthproxy.conf.Config;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.handler.codec.http.*;
import jline.internal.Nullable;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.http.HttpClient;
import net.md_5.bungee.netty.PipelineUtils;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class ProxiedAuthHandler {

    protected Cache<String, InetAddress> addressCache;

    public ProxiedAuthHandler() {
        if (Config.SERVICE.DNS_CACHE_EXPIRE.getNotNull() > 0) {
            addressCache = CacheBuilder.newBuilder()
                    .expireAfterWrite(Config.SERVICE.DNS_CACHE_EXPIRE.getNotNull(), TimeUnit.MILLISECONDS)
                    .build();
        }
    }

    public void submit(InitialHandler handler, String encodedHash, EventLoop loop, Callback<String> callback) throws Exception {
        submit(encodeName(handler.getName()), encodedHash, getPreventProxyParams(handler.getAddress()), loop, callback);
    }

    public void submit(String encodedName, String encodedHash, String preventProxy,
                       EventLoop loop, Callback<String> callback)
            throws Exception {
        submit(getAuthURL(encodedName, encodedHash, preventProxy), loop, callback);
    }

    public void submit(String authURL, EventLoop loop, Callback<String> callback) throws Exception {
        if (!Config.PROXY.ENABLE.getNotNull()) { // Proxy disabled
            HttpClient.get(authURL, loop, callback);
            return;
        }

        URI uri = new URI(authURL);
        Preconditions.checkNotNull((Object) uri.getScheme(), "scheme");
        Preconditions.checkNotNull((Object) uri.getHost(), "host");
        boolean ssl = uri.getScheme().equals("https");
        int port = getPort(uri);

        ProxyProtocolType proxyProtocol = getProxyProtocol();
        InetAddress address = resolveAddress(proxyProtocol, uri.getHost());

        ChannelFutureListener listener = channel -> {
            if (channel.isSuccess()) {
                String path = uri.getRawPath() + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
                HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, path);
                request.headers().set(HttpHeaderNames.HOST, uri.getHost());
                channel.channel().writeAndFlush(request);
            } else {
                addressCache.invalidate(uri.getHost());
                callback.done(null, channel.cause());
            }
        };

        new Bootstrap().channel(PipelineUtils.getChannel(null)).group(loop)
                .handler(new ProxiedHttpInitializer(proxyProtocol, callback, ssl, uri.getHost(), port))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.max(Config.SERVICE.TIME_OUT.getNotNull().intValue(), 100))
                .remoteAddress(address, port)
                .connect().addListener(listener);
    }

    private InetAddress resolveAddress(@Nullable ProxyProtocolType protocol, String host) throws UnknownHostException {
        if (protocol == null || this.addressCache == null) return InetAddress.getByName(host);

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

    public String getAuthURL(String encodedName, String encodedHash, String preventProxy) {
        try {
            return String.format(Config.SERVICE.MOJANG_AUTH_URL.getNotNull(), encodedName, encodedHash, preventProxy);
        } catch (Exception e) {
            e.printStackTrace();
            return "https://sessionserver.mojang.com/session/minecraft/hasJoined?username=" + encodedName + "&serverId=" + encodedHash + preventProxy;
        }
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

    protected static String encodeName(String name) {
        return URLEncoder.encode(name, StandardCharsets.UTF_8);
    }

    protected static String getPreventProxyParams(SocketAddress address) {
        if (!BungeeCord.getInstance().config.isPreventProxyConnections() || !(address instanceof InetSocketAddress)) {
            return "";
        }
        return URLEncoder.encode(((InetSocketAddress) address).getAddress().getHostAddress(), StandardCharsets.UTF_8);
    }

}
