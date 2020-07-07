package com.mashibing.nettystudy.s01;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class Client {
    public static void main(String[] args) {
    	//�¼�������̳߳أ�netty���̷߳�װ���������
    	//Event:�����ϵ�IO�¼�
    	//Loop:��Щ�¼�ѭ����ͣ�Ĵ���
    	//Group:�γ�һ���飬����һ������
    	//new NioEventLoopGroup(); Ĭ��ֵ��CPU����*2
    	//new NioEventLoopGroup(1); �ͻ��ˣ�Ĭ����һ���߳̾Ϳ�����
		EventLoopGroup group = new NioEventLoopGroup(1);
		//�����һ��socket��ȥ����Զ�̷������أ�netty���������һ����װ��Bootstrap�����������ࣩ
		Bootstrap b = new Bootstrap(); //��ѥ�Ӵ�
		try {
			ChannelFuture f = b.group(group) //����ʱָ���̳߳أ�group�����̳߳�
			.channel(NioSocketChannel.class) //ָ�������������ϵ�channel���͡�ͨ������ָ����ͬ��channel�Ϳ���ʵ��netty���������������汾
			 //��channel�����¼�����ʱ�򣬽����ĸ�handler����
			.handler(new ClientChannelInitializer())
			.connect("localhost", 8888);
			
			//�������sync()����Ҫ����д��
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
		System.out.println(ch);
	}
}
//����netty���¼�ģ�͵ģ���ֻҪ�����¼�����֮������Ҫ��ʲô���Ĵ�������ˣ�����System.out.print(ch)�ⲿ�ִ���
//netty�������еķ��������첽����








