package com.mashibing.nettystudy.s13;

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
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

public class Server {
	
	public static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	
	public void serverStart() {

		//大管家
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		//服务员
		EventLoopGroup workerGroup = new NioEventLoopGroup(2);
		
		try {
			ServerBootstrap b = new ServerBootstrap();
			
			ChannelFuture f = b.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline pl = ch.pipeline();
					pl.addLast(new TankMsgDecoder())
						.addLast(new ServerChildHandler());
				}
			})
			.bind(8888)
			.sync();
			
			ServerFrame.getInstance().updateServerMsg("Server Started!");
			
			//等着close的那个
			f.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
}

//server和client获得的channel肯定不是同一个，因为他们都不在同一台机器上
//server和client获得的是channel的两端，就像打电话中你的手机、我的手机

//服务端有很多的ChannelHandlerContext，一个ChannelHandlerContext只服务对应的client
//ChannelHandlerContext代表的是目前这个channel运行的整个的网络环境，上下文
//ChannelHandlerContext中有一些比较常用的方法
//ChannelHandlerContext-writeAndFlush()也是通过channel写
//ChannelHandlerContext也有.channel()方法，拿到对应的channel
//ChannelHandlerContext和channel之间是聚合关系，context中有channel
class ServerChildHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		//有clinet连接上来
		Server.clients.add(ctx.channel());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			
			//channel里的ByteBuf经过TankMsgDecoder直接转换为了TankMsg
			TankMsg tm = (TankMsg) msg;
			System.out.println(tm);
		} finally {
			//手动释放buf
			ReferenceCountUtil.release(msg);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		Server.clients.remove(ctx.channel());
		//通知client执行f.channel().closeFuture().sync();回调函数
		ctx.close();
	}
	
}
