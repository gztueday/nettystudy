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
		//BIO中：new ServerSocket().accept() 方法如果没有客户端连接上来，就一直在这里傻等着
		//NIO中：((ServerScoketChannel) key.channel()).accept() 方法也会在这里傻等，但是NIO是发现有客户端
		//要连上来的时候，才调用accpet()方法，所以它一定能很快的返回。
		//AIO中：accept不是阻塞的。不是阻塞的意思是说，你accept一下，就算没成功，它也返回，继续向下运行。
		//如果这样的话，下面不写while(true) {Thread.sleep(1000);}; 程序就直接结束了
		//那还怎么接收客户端连接？那还怎么开门迎客呢？AIO相当于是做了个机器人在那里开门迎客。
		//怎么做的呢？
		//写了一个钩子函数：new CompletionHandler<AsynchronousSocketChannel, Object>()
		//accept结束了字后，CompletionHandler类中的completed方法会自动调用
		//所以，实际上AIO中的accept方法就是网大门口那里写了一个Observer，
		//客人来了，大家是怎么处理的呢？
		//BIO:只有一个人接待客人，有一个client来了，它就干别的事去了，后面的人进不来
		//NIO:有一个大管家，来回跑，能够接进来，接进来之后client能不能点菜就不管了
		//AIO:有一个机器人，client一进来，机器人就调用completed方法。所以accpet方法可以直接返回的原因是：
		//accept方法一旦调用，机器人就丢在这里了。所以就可以走了，想干嘛干嘛去了
		serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
			@Override
			public void completed(AsynchronousSocketChannel client, Object attachment) {
				serverChannel.accept(null, this);
				try {
					System.out.println(client.getRemoteAddress());
					ByteBuffer buffer = ByteBuffer.allocate(1024);
					//read也是异步的
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
