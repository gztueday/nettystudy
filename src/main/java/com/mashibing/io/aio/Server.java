package com.mashibing.io.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class Server {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		final AsynchronousServerSocketChannel serverChannel = AsynchronousServerSocketChannel.open()
				.bind(new InetSocketAddress(8888));
		//BIO�У�new ServerSocket().accept() �������û�пͻ���������������һֱ������ɵ����
		//NIO�У�((ServerScoketChannel) key.channel()).accept() ����Ҳ��������ɵ�ȣ�����NIO�Ƿ����пͻ���
		//Ҫ��������ʱ�򣬲ŵ���accpet()������������һ���ܺܿ�ķ��ء�
		//AIO�У�accept���������ġ�������������˼��˵����acceptһ�£�����û�ɹ�����Ҳ���أ������������С�
		//��������Ļ������治дwhile(true) {Thread.sleep(1000);}; �����ֱ�ӽ�����
		//�ǻ���ô���տͻ������ӣ��ǻ���ô����ӭ���أ�AIO�൱�������˸������������￪��ӭ�͡�
		//��ô�����أ�
		//д��һ�����Ӻ�����new CompletionHandler<AsynchronousSocketChannel, Object>()
		//accept�������ֺ�CompletionHandler���е�completed�������Զ�����
		//���ԣ�ʵ����AIO�е�accept�������������ſ�����д��һ��Observer��
		//�������ˣ��������ô������أ�
		//BIO:ֻ��һ���˽Ӵ����ˣ���һ��client���ˣ����͸ɱ����ȥ�ˣ�������˽�����
		//NIO:��һ����ܼң������ܣ��ܹ��ӽ������ӽ���֮��client�ܲ��ܵ�˾Ͳ�����
		//AIO:��һ�������ˣ�clientһ�����������˾͵���completed����������accpet��������ֱ�ӷ��ص�ԭ���ǣ�
		//accept����һ�����ã������˾Ͷ��������ˡ����ԾͿ������ˣ���������ȥ��
		serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
			@Override
			public void completed(AsynchronousSocketChannel client, Object attachment) {
				serverChannel.accept(null, this);
				try {
					System.out.println(client.getRemoteAddress());
					ByteBuffer buffer = ByteBuffer.allocate(1024);
					//readҲ���첽��
					client.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
						@Override
						public void completed(Integer result, ByteBuffer attachment) {
							attachment.flip();
							System.out.println(new String(attachment.array(), 0, result));
							client.write(ByteBuffer.wrap("HelloClient".getBytes()));
						}
						@Override
						public void failed(Throwable exc, ByteBuffer attachment) {
							exc.printStackTrace();
						}
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			@Override
			public void failed(Throwable exc, Object attachment) {
				
			}
		});
		while(true) {
			Thread.sleep(1000);
		}
	}
	
}
