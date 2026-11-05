package com.websockets.plugin.websocket;

import com.eu.habbo.Emulator;
import com.eu.habbo.networking.Server;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;

public class WebSocketServer extends Server {
    public static WebSocketServer instance;

    public WebSocketServer() throws Exception {
        super("WEBSOCKETS BY SRTHRONE", Emulator.getConfig().getValue("websockets_srthrone.ws.host"),
                Emulator.getConfig().getInt("websockets_srthrone.ws.port"), 1, 2);
    }

    @Override
    public void initializePipeline() {
        super.initializePipeline();
        this.serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {

                ch.pipeline().addLast(new HttpServerCodec());
                ch.pipeline().addLast(new HttpObjectAggregator(65536));
                ch.pipeline().addLast(new WebSocketServerProtocolHandler("/", true));
                ch.pipeline().addLast(new IdleStateHandler(60, 30, 0));
                ch.pipeline().addLast("MessageHandler", new MessageHandler());

            }
        });
    }

}
