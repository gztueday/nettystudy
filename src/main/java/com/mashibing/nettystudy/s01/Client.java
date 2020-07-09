package com.mashibing.nettystudy.s01;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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
    public static void main(String[] args) {
    	//netty�Զ��Ƕ��̵߳ģ������õ��̶߳����У�Netty���̷߳�װ��EventLoopGroup����
    	//EventLoopGroup:�¼�������̳߳أ�netty���̷߳�װ���������
    	//Event:�����ϵ�IO�¼�
    	//Loop:��Щ�¼�ѭ����ͣ�Ĵ���
    	//Group:�γ�һ���飬����һ������
    	//new NioEventLoopGroup(); Ĭ��ֵ��CPU����*2
    	//new NioEventLoopGroup(1); �ͻ��ˣ�Ĭ����һ���߳̾Ϳ�����
    	//������ǿͻ��ˣ���1���߳̾͹���
    	//���������ͻ���д�ķǳ�Ƶ��������Ҳ�ǳ�Ƶ�������Զ��𼸸��߳�
		EventLoopGroup group = new NioEventLoopGroup(1);
		//�����һ��socket��ȥ����Զ�̷������أ�netty���������һ����װ��Bootstrap�����������ࣩ
		Bootstrap b = new Bootstrap(); //��ѥ�Ӵ�
		try {
			/***** ���Զ����һ��server����һ�仰�������� start *****/
			ChannelFuture f = b.group(group) //����ʱָ���̳߳أ�group�����̳߳�
				//ָ�����������������ϵ�channel���͡�ͨ������ָ����ͬ��channel�Ϳ���ʵ��netty���������������汾
				//NioSocketChannel.class ������
				//SocketChannel.class ����������
				.channel(NioSocketChannel.class)
				 //��channel�����¼�����ʱ�򣬽����ĸ�handler����
				.handler(new ClientChannelInitializer())
				.connect("localhost", 8888);
			/***** ���Զ����һ��server����һ�仰�������� end *****/
			
			//ChannelFuture������client����server�����(��connect����)�ɹ������ChannelFuture����
			//��Ϊnetty�������з��������첽�ģ�����ִ����connect���Ͳ����ˣ���ô�������ӳɹ�û�ɹ�����
			//�������ChannelFuture���档���ӳɹ��˵Ļ�����ô������Ҳ��ChannelFuture����
			//connect����³ɹ�û�ɹ������д��������ȥ������
			//�����������.sync()����Ҫ����д��
			f.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if(!future.isSuccess()) {
						System.out.println("not connected!");
					} else {
						System.out.println("connected!");
					}
				}
			});
			
			//.sync();//���ˣ����������ˡ�sync()��������ȷ��connect()�����ɹ��˲�������
			f.sync(); //����ס��ֱ�����˽��Ϊֹ
			System.out.println("...");
			//����ʦ��Ϊûд��仰��������20���ӣ�16.3�� 43:00��
			f.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			group.shutdownGracefully(); // ��group���ŵĽ���
		}
	}
}
//interface EventLoopGroup extends EventExecutorGroup extends java.util.concurrent.ScheduledExecutorService
//EventLoopGroup: �¼�������̳߳أ�������������channel�ϵ������¼���ʲô�¼�������:connect�¼������̳߳������һ���̴߳���ģ�
//EventExecutorGroup: ���¼�������߳���ɵ�һ��Group
//java.util.concurrent.ScheduledExecutorService: java��ִ���̵߳�һ������

//abstract class ChannelInitializer<C extends Channel> extends ChannelInboundHandlerAdapter
//class ChannelInboundHandlerAdapter extends ChannelHandlerAdapter implements ChannelInboundHandler
//abstract ChannelHandlerAdapter implements ChannelHandler
//����ChannelInitializer����һ��ChannelHandler�����Կ��Դ���handler()������

//ChannelInitializer����ָ��channel�����ͣ�����ָ������SocketChannel
class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		//ChannelInitializer����channel��ʼ���õ�
		//��channel�������������Ժ󣬵���initChannel����
		//��server��д����
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
			System.out.println(new String(bytes));
		} finally {
			if(buf != null) ReferenceCountUtil.release(buf);
		}
	}
	
	//�����϶���ͨ����������01010101001����������д��
	//�������κζ�������ŵ�������д�Ļ���ֻ��һ���취������ת�����ֽ����飬���Ƕ�����
	//��NIO���棬��һ��ByteBuffer�������ر�����
	//������netty���棬��һ�������õ��ֽ����飺ByteBuf
	//��netty���棬д�κ����ݣ����ն�����ByteBufд��ȥ�ģ�����ByteBufЧ���ر��
	//��netty���棬д�κ����ݣ������׶�Ҫ���ByteBuf����ΪNetty�����ݣ���ByteBuf
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		//bufֱ��ָ��ϵͳ�ڴ棬��Ҫ�ͷţ���Ϊ������GC
		//client��serverд�˸����ݣ�hello
		ByteBuf buf = Unpooled.copiedBuffer("hello".getBytes());
		//writeAndFlush()�����ڲ��Զ��ͷ��ڴ�
		ctx.writeAndFlush(buf);
	}
}

//����netty���¼�ģ�͵ģ���ֻҪ�����¼�����֮������Ҫ��ʲô���Ĵ�������ˣ�����System.out.print(ch)�ⲿ�ִ���
//netty�������еķ��������첽����

//����һ��ΪʲôByteBufЧ���ر�ߣ�
//JVMg�������Լ����ڴ棬JVM�����ڲ���ϵͳ�ϣ�
//����ϵͳ�����Լ�������ڴ棬
//���һ���������ݣ�������д������ϵͳ��
//JVM�������Ļ����ð��������copy����������ڴ�������
//�ٲ���һ�������Ĺ��̡�����дҲ��һ���ģ�
//��Netty��ByteBuf����JVM����ֱ�ӷ��ʲ���ϵͳ�ڴ档���Խ� Direct Memory��ֱ���ڴ�
//ֱ�ӷ����ڴ���������⣿������JVM���������ջ���






