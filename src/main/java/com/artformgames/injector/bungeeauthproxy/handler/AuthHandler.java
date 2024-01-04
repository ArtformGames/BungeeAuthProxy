package com.artformgames.injector.bungeeauthproxy.handler;

import io.netty.channel.EventLoop;
import net.md_5.bungee.api.Callback;

public interface AuthHandler {

    void submit(String url, EventLoop loop, Callback<String> callback) throws Exception;

}
