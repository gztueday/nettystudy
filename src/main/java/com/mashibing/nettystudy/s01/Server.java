package com.mashibing.nettystudy.s01;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Server {
	public static void main(String[] args) throws Exception {
		//大管家
		//只负责客户端的连接，连接上了之后的一个个socket所产生的事件交给workerGroup处理
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		//餐厅里的服务员
		EventLoopGroup workerGroup = new NioEventLoopGroup(2);
		
		try {
			ServerBootstrap b = new ServerBootstrap();
			ChannelFuture f = b.group(bossGroup, workerGroup)
					//指定channel类型
					.channel(NioServerSocketChannel.class)
					//.handler(handler)
					//每一桌人连上了服务器之后，调用回调方法initChannel，打印出来跟客户端连接的SocketChannel
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							System.out.println(ch);
						}
					}) //加在了client的socket
					.bind(8888) //监听8888端口
					.sync(); // sync之后，产生一个ChannelFuture
			System.out.println("server started!");
			//如果没有这句话，不能起到阻塞，程序会执行到main结束
			//closeFuture是餐厅等着关门的那个人，什么时候调用close，这句话才会继续执行
			f.channel().closeFuture().sync();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}
}
