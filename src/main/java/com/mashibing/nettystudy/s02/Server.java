package com.mashibing.nettystudy.s02;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;

public class Server {
	
	public static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	
	public static void main(String[] args) throws Exception {
		//大管家
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		//服务员
		EventLoopGroup workerGroup = new NioEventLoopGroup(2);
		
		ServerBootstrap b = new ServerBootstrap();
		
		ChannelFuture f = b.group(bossGroup, workerGroup)
		.channel(NioServerSocketChannel.class)
		.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pl = ch.pipeline();
				pl.addLast(new ServerChildHandler());
			}
		})
		.bind(8888).sync();
		System.out.println("Server Started!");
		//等着close的那个
		f.channel().closeFuture().sync();
	}
	
}

class ServerChildHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		//有clinet连接上来
		Server.clients.add(ctx.channel());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buf = null;
		try {
			buf = (ByteBuf) msg;
			System.out.println("Server 接受：" + buf);
			byte[] bytes = new byte[buf.readableBytes()];
			buf.readBytes(bytes);
			System.out.println(new String(bytes));
			
			//自动释放buf
			Server.clients.writeAndFlush(msg);
		} finally {
			//手动释放buf
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		//通知client执行f.channel().closeFuture().sync();回调函数
		ctx.close();
	}
	
}
