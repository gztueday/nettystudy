package com.mashibing.nettystudy.s01;

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

//Netty��client��server��������
//bossGroup�Ǵ�ܼң������ſ�ӭ��
//�������ˣ���ܼҸ��𽫿���ӭ������
//����ֱ�������������
//�ɷ���Ա����
//����Ա�����֮�佨����channel��Socket ch��
//�����������������ˣ�client�˵�channelActive��������������һ��ͨ��ByteBuf����й¶һ������
//д��ȥ֮���أ�����Netty���ƾ��յ��ˡ�
//����Netty���¼������ͻ��Զ�����ChannelRead
//����ChannelRead��ʱ�򣬻����������ת����һ��ByteBuf�����ݸ�msg

//�������Ҫ����ServerChildHandler����
//ServerChildHandler��SocketChannel�ڲ����ڴ���ͽػ�ͨ�����պͷ��͵����ݵ�ChannelPipeline��������ģʽ��������
public class Server {
	
	//����һ���������пͻ������ӵ�list
	//��Ĭ���߳�������ͨ�����ϵ��¼�
	public static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	
	public static void main(String[] args) throws Exception {
		//��ܼ�
		//ֻ����ͻ��˵����ӣ���������֮���һ����socket���������¼�����workerGroup����
		//��������server��Ҫacceptһ�£��ͻ��˲��������ϣ�bossGroup������accept��
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		//������ķ���Ա����ܼҰ�client�ŵ���������֮�󣬷���Ա����client
		//����������������֮�����Щ�¼�
		EventLoopGroup workerGroup = new NioEventLoopGroup(2);
		
		try {
			ServerBootstrap b = new ServerBootstrap();
			ChannelFuture f = b.group(bossGroup, workerGroup)
					//ָ��channel����
					.channel(NioServerSocketChannel.class)
					//.handler(handler)
					//��clientҪ��������ʱ���п���Ҫ������ʱ��
					//��ô������˽���֮���أ�����Ҫ��ʼ����client��channel
					//��ʼ�����֮�󣬵��ûص�����initChannel����ӡ�������ͻ������ӵ�SocketChannel
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							//�����Ѿ��Ƿ���Ա�ڴ���ͻ���������
							//SocketChannel���Ƿ���Ա��client����������
							//ChannelPipeline���ڴ���ͽػ�ͨ�����պͷ��͵�����
							ChannelPipeline pl = ch.pipeline();
							pl.addLast(new ServerChildHandler());
						}
					}) //������client��socket
					//����8888�˿�
					.bind(8888) 
					//sync֮�󣬲���һ��ChannelFuture
					//sync������������ǿ���bind��û�гɹ���bind�ɹ��ˣ��Ż��������ִ��
					//�����д���sync���Ͳ�֪��bind�Ƿ�ɹ���
					//��ס����������첽�ģ���Ҫ��õ�������͵ü�sync����ͬ��
					.sync(); 
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

//��������������˽���֮�󣬶���Ҫ��Ĵ���
class ServerChildHandler extends ChannelInboundHandlerAdapter {
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		//֪ͨclient
		//clientִ��f.channel().closeFuture().sync();Ȼ��client���߳̽���
		ctx.close();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		Server.clients.add(ctx.channel());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		
		ByteBuf buf = null;
		try {
			//�˷������ڽ��տͻ������ӣ���ȡ�ͻ������ݵ�msg
			buf = (ByteBuf) msg;
			System.out.println(buf);
			//�ж�����������buf
			//�������try...finally...����������ͷ�buf����ֵ>0
			System.out.println(buf.refCnt());
			//�����յ�������ת����String
			byte[] bytes = new byte[buf.readableBytes()];
			buf.getBytes(buf.readerIndex(), bytes);
			System.out.println(new String(bytes));
			//�˷����Զ��ͷ�buf�Բ���ϵͳ�����ã���Ҫ��ReferenceCountUtil.release(buf);ע�͵�
			//ctx.writeAndFlush(msg);
			
			//�ó�ͨ�����е�ÿ��ͨ��д����
			Server.clients.writeAndFlush(msg);
		} finally {
			//�ͷ�buf�Բ���ϵͳ�ڴ�����ã����ͷ����������ڴ�й¶
			//if(buf != null) ReferenceCountUtil.release(buf);
			//System.out.println(buf.refCnt());
		}
	}
}

//ChannelInboundHandlerAdapter��ChannelOutboundHandlerAdapter������
//Ϊʲô����˺Ϳͻ��˶����õ�ChannelInboundHandlerAdapter��
//��Ϊվ��client�ĽǶȣ�server�˷����������ݾ������룬������ChannelInboundHandlerAdapter
//��վ��server�ĽǶȣ�client�˷�����������Ҳ�����룬����Ҳ��ChannelInboundHandlerAdapter
//����client�˽��յ����������ݣ���server�Ǳ߿�������ChannelOutboundHandlerAdapter
//ͬ��server�˽��յ����������ݣ�վ��client�˿�������ChannelOutboundHandlerAdapter
//ChannelOutboundHandlerAdapterһ���ò�̫��

