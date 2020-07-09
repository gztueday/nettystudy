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

		//��ܼ�
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		//����Ա
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
			
			//����close���Ǹ�
			f.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
}

//server��client��õ�channel�϶�����ͬһ������Ϊ���Ƕ�����ͬһ̨������
//server��client��õ���channel�����ˣ������绰������ֻ����ҵ��ֻ�

//������кܶ��ChannelHandlerContext��һ��ChannelHandlerContextֻ�����Ӧ��client
//ChannelHandlerContext�������Ŀǰ���channel���е����������绷����������
//ChannelHandlerContext����һЩ�Ƚϳ��õķ���
//ChannelHandlerContext-writeAndFlush()Ҳ��ͨ��channelд
//ChannelHandlerContextҲ��.channel()�������õ���Ӧ��channel
//ChannelHandlerContext��channel֮���ǾۺϹ�ϵ��context����channel
class ServerChildHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		//��clinet��������
		Server.clients.add(ctx.channel());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			
			//channel���ByteBuf����TankMsgDecoderֱ��ת��Ϊ��TankMsg
			TankMsg tm = (TankMsg) msg;
			System.out.println(tm);
		} finally {
			//�ֶ��ͷ�buf
			ReferenceCountUtil.release(msg);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		Server.clients.remove(ctx.channel());
		//֪ͨclientִ��f.channel().closeFuture().sync();�ص�����
		ctx.close();
	}
	
}
