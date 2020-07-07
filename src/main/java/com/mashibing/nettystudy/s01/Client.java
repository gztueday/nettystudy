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
    	//事件处理的线程池：netty的线程封装在这个里面
    	//Event:网络上的IO事件
    	//Loop:这些事件循环不停的处理
    	//Group:形成一个组，做成一个池子
    	//new NioEventLoopGroup(); 默认值，CPU核数*2
    	//new NioEventLoopGroup(1); 客户端，默认起一个线程就可以了
		EventLoopGroup group = new NioEventLoopGroup(1);
		//如何起一个socket，去连接远程服务器呢？netty里面进行了一个封装，Bootstrap（辅助启动类）
		Bootstrap b = new Bootstrap(); //解靴子带
		try {
			ChannelFuture f = b.group(group) //启动时指定线程池，group就是线程池
			.channel(NioSocketChannel.class) //指定连到服务器上的channel类型。通过这里指定不同的channel就可以实现netty的阻塞、非阻塞版本
			 //当channel上有事件来的时候，交给哪个handler处理
			.handler(new ClientChannelInitializer())
			.connect("localhost", 8888);
			
			//如果不加sync()，需要这样写：
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
			
			//.sync();//好了，就连接上了。sync()方法用于确保connect()方法成功了才往下走
			f.sync(); //阻塞住，直到出了结果为止
			System.out.println("...");
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			group.shutdownGracefully(); // 让group优雅的结束
		}
	}
}
//interface EventLoopGroup extends EventExecutorGroup extends java.util.concurrent.ScheduledExecutorService
//EventLoopGroup: 事件处理的线程池，用来处理整个channel上的所有事件，什么事件？比如:connect事件就是线程池里出了一个线程处理的，
//EventExecutorGroup: 对事件处理的线程组成的一个Group
//java.util.concurrent.ScheduledExecutorService: java中执行线程的一个服务

//abstract class ChannelInitializer<C extends Channel> extends ChannelInboundHandlerAdapter
//class ChannelInboundHandlerAdapter extends ChannelHandlerAdapter implements ChannelInboundHandler
//abstract ChannelHandlerAdapter implements ChannelHandler
//所以ChannelInitializer就是一个ChannelHandler，所以可以穿到handler()方法中

//ChannelInitializer可以指定channel的类型，我们指定的是SocketChannel
class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		//ChannelInitializer是做channel初始化用的
		//当channel连到服务器上以后，调用initChannel方法
		System.out.println(ch);
	}
}
//所以netty是事件模型的，你只要处理事件发生之后，你需要做什么样的处理就行了，就是System.out.print(ch)这部分代码
//netty里面所有的方法都是异步方法








