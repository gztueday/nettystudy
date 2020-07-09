package com.mashibing.nettystudy.s11;

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
	
	public void serverStart() {

		//��ܼ�
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		//����Ա
		EventLoopGroup workerGroup = new NioEventLoopGroup(2);
		
		try {
			ServerBootstrap b = new ServerBootstrap();
			
			//ChannelFuture�����ж�connect����³ɹ�û�ɹ�
			ChannelFuture f = b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline pl = ch.pipeline();
							pl.addLast(new ServerChildHandler());
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
		ByteBuf buf = null;
		try {
			buf = (ByteBuf) msg;
			byte[] bytes = new byte[buf.readableBytes()];
			//buf.readBytes(bytes); ������Ϊ���ô��˷��������Ҵ�����Сʱ
			//getbytes�����û������дӸ���������ʼ�����ݴ��͵�ָ����Ŀ�ĵ�
			buf.getBytes(buf.readerIndex(), bytes);
			String s = new String(bytes);
			ServerFrame.getInstance().updateClientMsg(s);
			
			if(s.equals("_bye_")) {
				ServerFrame.getInstance().updateClientMsg("�ͻ���Ҫ���˳�...........");
				Server.clients.remove(ctx.channel());
				ctx.close();
			} else {
				//�˷����Զ��ͷ�buf
				Server.clients.writeAndFlush(msg);
			}
		} finally {
			//�ֶ��ͷ�buf
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
