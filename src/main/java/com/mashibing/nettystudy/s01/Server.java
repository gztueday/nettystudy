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
		//��ܼ�
		//ֻ����ͻ��˵����ӣ���������֮���һ����socket���������¼�����workerGroup����
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		//������ķ���Ա
		EventLoopGroup workerGroup = new NioEventLoopGroup(2);
		
		try {
			ServerBootstrap b = new ServerBootstrap();
			ChannelFuture f = b.group(bossGroup, workerGroup)
					//ָ��channel����
					.channel(NioServerSocketChannel.class)
					//.handler(handler)
					//ÿһ���������˷�����֮�󣬵��ûص�����initChannel����ӡ�������ͻ������ӵ�SocketChannel
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							System.out.println(ch);
						}
					}) //������client��socket
					.bind(8888) //����8888�˿�
					.sync(); // sync֮�󣬲���һ��ChannelFuture
			System.out.println("server started!");
			//���û����仰�������������������ִ�е�main����
			//closeFuture�ǲ������Ź��ŵ��Ǹ��ˣ�ʲôʱ�����close����仰�Ż����ִ��
			f.channel().closeFuture().sync();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}
}
