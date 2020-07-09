package com.mashibing.nettystudy.s02;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;

public class Client {
	
	//channel�൱�����������socket������Ҫ��channel����Ϣ
	//Ҳ������ChannelHandlerContext������Ϣ����Ϊ��ChannelHandlerContext����Ҳ�ǵ����Լ���channel()������
	//�õ��Ǹ�channel���ٷ���Ϣ
	//һ���ͻ������ӵ��������ϣ�����һ��channel����
	//channelʲôʱ���ʼ���أ�Ӧ���������Ϸ�������ʱ��
	private Channel channel = null;
	
	public void connect() {
		//��ܼ�
		EventLoopGroup group = new NioEventLoopGroup(1);
		//ѥ�Ӵ�����һ��socket��ȥ����Զ�̷�����
		Bootstrap b = new Bootstrap();
		
		try {
			ChannelFuture f = b.group(group) //����ʱָ���̳߳�
					.channel(NioSocketChannel.class) //ָ�����ӵ���������channel����
					.handler(new ClientChannelInitializer()) //��channel�����¼���ʱ�򣬽����ĸ�handler����
					.connect("localhost", 8888);
					
			f.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if(future.isSuccess()) {
						System.out.println("connected!");
						//channel��ȷ��client����server�ɹ�֮�󣬳�ʼ��
						channel = future.channel();
					} else {
						System.out.println("not connected!");
					}
				}
			});
			
			f.sync(); //����ס
			//����close
			f.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			group.shutdownGracefully(); //���ŵĹر�
		}
	}
	
	public void send(String msg) {
		ByteBuf buf = Unpooled.copiedBuffer(msg.getBytes());
		channel.writeAndFlush(buf);
	}
	
	public void closeConnect() {
		this.send("_bye_");
	}
	
}

class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		//ChannelInitializer����channel��ʼ����
		//��client���ӵ��������Ϻ󣬵���initChannel��������Server��д����
		ch.pipeline().addLast(new ClientHandler());
	}
}

class ClientHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buf = null;
		try {
			buf = (ByteBuf) msg;
			byte[] bytes = new byte[buf.readableBytes()];
			buf.getBytes(buf.readerIndex(), bytes);
			String msgAccepted = new String(bytes);
			System.out.println("channelRead : " + msgAccepted);
			ClientFrame.getInstance().updateText(msgAccepted);
//			buf.writeBytes(bytes);
//			System.out.println(new String(bytes));
		} finally {
			if(buf != null) ReferenceCountUtil.release(buf);
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		//���綼��ͨ��������01000101110����������
		//�����κζ�����ŵ�������д�Ļ���ֻ��һ���취������ת�����ֽ����飬���Ƕ�����
		//��NIO������һ��ByteBuffer���ر�����
		//������Netty���棬��һ��ByteBuf
		//��Netty���棬д�κ����ݣ����ն�����ByteBufд��ȥ�ģ�����Ч���ر��
		//��Netty���棬������Ҳ��ByteBuf
		//client��serverд�˸����ݣ�hello
		ByteBuf buf = Unpooled.copiedBuffer("hello���������ˣ�".getBytes());
		ctx.writeAndFlush(buf);
	}
}
