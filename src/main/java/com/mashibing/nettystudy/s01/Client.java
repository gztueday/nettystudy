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
    	//netty自动是多线程的，你想用单线程都不行，Netty的线程封装在EventLoopGroup里面
    	//EventLoopGroup:事件处理的线程池：netty的线程封装在这个里面
    	//Event:网络上的IO事件
    	//Loop:这些事件循环不停的处理
    	//Group:形成一个组，做成一个池子
    	//new NioEventLoopGroup(); 默认值，CPU核数*2
    	//new NioEventLoopGroup(1); 客户端，默认起一个线程就可以了
    	//如果你是客户端，起1个线程就够了
    	//但是如果你客户端写的非常频繁、读的也非常频繁，可以多起几个线程
		EventLoopGroup group = new NioEventLoopGroup(1);
		//如何起一个socket，去连接远程服务器呢？netty里面进行了一个封装，Bootstrap（辅助启动类）
		Bootstrap b = new Bootstrap(); //解靴子带
		try {
			/***** 如果远程有一个server，这一句话就连上了 start *****/
			ChannelFuture f = b.group(group) //启动时指定线程池，group就是线程池
				//指定将来连到服务器上的channel类型。通过这里指定不同的channel就可以实现netty的阻塞、非阻塞版本
				//NioSocketChannel.class 非阻塞
				//SocketChannel.class 就是阻塞版
				.channel(NioSocketChannel.class)
				 //当channel上有事件来的时候，交给哪个handler处理
				.handler(new ClientChannelInitializer())
				.connect("localhost", 8888);
			/***** 如果远程有一个server，这一句话就连上了 end *****/
			
			//ChannelFuture，就是client连接server这件事(即connect方法)成功与否，在ChannelFuture里面
			//因为netty里面所有方法都是异步的，所以执行完connect它就不管了，那么到底连接成功没成功啊？
			//这件事在ChannelFuture里面。连接成功了的话，怎么处理啊？也有ChannelFuture决定
			//connect这件事成功没成功，你得写个监听器去监听它
			//所以如果不加.sync()，需要这样写：
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
			//马老师因为没写这句话，调试了20分钟（16.3节 43:00）
			f.channel().closeFuture().sync();
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
		//往server端写数据
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
	
	//网络上都是通过比特流（01010101001），这样来写。
	//所以你任何东西，想放到网上来写的话，只有一个办法，就是转换成字节数组，就是二进制
	//在NIO里面，有一个ByteBuffer，但是特别难用
	//所以在netty里面，有一个可以用的字节数组：ByteBuf
	//在netty里面，写任何数据，最终都是由ByteBuf写出去的，而且ByteBuf效率特别高
	//在netty里面，写任何数据，归根结底都要搞成ByteBuf，因为Netty读数据，是ByteBuf
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		//buf直接指向系统内存，需要释放，因为跳过了GC
		//client往server写了个数据，hello
		ByteBuf buf = Unpooled.copiedBuffer("hello".getBytes());
		//writeAndFlush()方法内部自动释放内存
		ctx.writeAndFlush(buf);
	}
}

//所以netty是事件模型的，你只要处理事件发生之后，你需要做什么样的处理就行了，就是System.out.print(ch)这部分代码
//netty里面所有的方法都是异步方法

//解释一下为什么ByteBuf效率特别高？
//JVMg管理着自己的内存，JVM运行在操作系统上，
//操作系统管理自己更大的内存，
//如果一个网络数据，首先是写给操作系统，
//JVM想用它的话，得把这份数据copy到虚拟机的内存中来，
//少不了一个拷贝的过程。往外写也是一样的，
//而Netty的ByteBuf是在JVM里面直接访问操作系统内存。所以叫 Direct Memory，直接内存
//直接访问内存带来的问题？跳过了JVM的垃圾回收机制






