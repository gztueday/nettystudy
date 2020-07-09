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

//Netty的client、server交互过程
//bossGroup是大管家，负责门口迎客
//客人来了，大管家负责将客人迎进来，
//客人直接做到桌子那里，
//由服务员管理
//服务员与客人之间建立了channel（Socket ch）
//客人有了这张桌子了，client端的channelActive就立即“棒”的一下通过ByteBuf往外泄露一条数据
//写过去之后呢，整个Netty机制就收到了。
//由于Netty是事件处理，就会自动调用ChannelRead
//调用ChannelRead的时候，会把这条数据转换成一个ByteBuf，传递给msg

//客人提出要求，由ServerChildHandler处理，
//ServerChildHandler是SocketChannel内部用于处理和截获通道接收和发送的数据的ChannelPipeline（责任链模式）的子类
public class Server {
	
	//定义一个保存所有客户端连接的list
	//用默认线程来处理通道组上的事件
	public static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	
	public static void main(String[] args) throws Exception {
		//大管家
		//只负责客户端的连接，连接上了之后的一个个socket所产生的事件交给workerGroup处理
		//网络编程中server需要accept一下，客户端才能连接上，bossGroup是用来accept的
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		//餐厅里的服务员，大管家把client放到桌子那里之后，服务员处理client
		//用来处理连接上了之后的那些事件
		EventLoopGroup workerGroup = new NioEventLoopGroup(2);
		
		try {
			ServerBootstrap b = new ServerBootstrap();
			ChannelFuture f = b.group(bossGroup, workerGroup)
					//指定channel类型
					.channel(NioServerSocketChannel.class)
					//.handler(handler)
					//有client要连上来的时候，有客人要进来的时候，
					//那么这个客人进来之后呢，必须要初始化好client的channel
					//初始化完成之后，调用回调方法initChannel，打印出来跟客户端连接的SocketChannel
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							//这里已经是服务员在处理客户端连接了
							//SocketChannel就是服务员与client建立的连接
							//ChannelPipeline用于处理和截获通道接收和发送的数据
							ChannelPipeline pl = ch.pipeline();
							pl.addLast(new ServerChildHandler());
						}
					}) //加在了client的socket
					//监听8888端口
					.bind(8888) 
					//sync之后，产生一个ChannelFuture
					//sync，这个阻塞，是看看bind有没有成功，bind成功了，才会继续往下执行
					//如果不写这个sync，就不知道bind是否成功了
					//记住这个方法是异步的，你要想得到结果，就得加sync让它同步
					.sync(); 
			System.out.println("server started!");
			//如果没有这句话，不能起到阻塞，程序会执行到main结束
			//closeFuture是餐厅等着关门的那个人，什么时候调用close，这句话才会继续执行
			f.channel().closeFuture().sync();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}
}

//这里用来处理客人进来之后，对其要求的处理
class ServerChildHandler extends ChannelInboundHandlerAdapter {
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		//通知client
		//client执行f.channel().closeFuture().sync();然后client主线程结束
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
			//此方法用于接收客户端连接，读取客户端内容到msg
			buf = (ByteBuf) msg;
			System.out.println(buf);
			//有多少人引用了buf
			//如果不用try...finally...块包裹并且释放buf，此值>0
			System.out.println(buf.refCnt());
			//将接收到的内容转换成String
			byte[] bytes = new byte[buf.readableBytes()];
			buf.getBytes(buf.readerIndex(), bytes);
			System.out.println(new String(bytes));
			//此方法自动释放buf对操作系统的引用，需要把ReferenceCountUtil.release(buf);注释掉
			//ctx.writeAndFlush(msg);
			
			//拿出通道组中的每条通道写数据
			Server.clients.writeAndFlush(msg);
		} finally {
			//释放buf对操作系统内存的引用，不释放容易引起内存泄露
			//if(buf != null) ReferenceCountUtil.release(buf);
			//System.out.println(buf.refCnt());
		}
	}
}

//ChannelInboundHandlerAdapter和ChannelOutboundHandlerAdapter的区别
//为什么服务端和客户端都是用的ChannelInboundHandlerAdapter？
//因为站在client的角度，server端发过来的数据就是输入，所以用ChannelInboundHandlerAdapter
//而站在server的角度，client端发过来的数据也是输入，所以也用ChannelInboundHandlerAdapter
//但是client端接收到的这条数据，在server那边看来就是ChannelOutboundHandlerAdapter
//同理，server端接收到的这条数据，站在client端看来就是ChannelOutboundHandlerAdapter
//ChannelOutboundHandlerAdapter一般用不太上

