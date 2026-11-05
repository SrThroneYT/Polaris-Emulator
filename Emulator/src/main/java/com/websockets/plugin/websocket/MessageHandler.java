package com.websockets.plugin.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.nio.charset.StandardCharsets;

import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.incoming.Incoming;

public class MessageHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Nothing
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        HabboClientManager.killClient(ctx.channel().id());
        ctx.channel().close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, BinaryWebSocketFrame textWebSocketFrame)
            throws Exception {
        new Incoming(textWebSocketFrame.content().toString(StandardCharsets.UTF_8), channelHandlerContext.channel());
    }

}
