package com.example.tcp_server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

public class SimpleTCPChannelInitializer extends ChannelInitializer<SocketChannel> {

	protected void initChannel(SocketChannel socketChannel) throws Exception {
		socketChannel.pipeline().addLast(new ByteArrayEncoder());
		socketChannel.pipeline().addLast(new ByteArrayDecoder());
		socketChannel.pipeline().addLast(new SimpleTCPChannelHandler());
	}
}