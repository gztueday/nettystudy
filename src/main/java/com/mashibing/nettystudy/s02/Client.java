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
	
	//channel相当于网络里面的socket，必须要用channel发消息
	//也可以用ChannelHandlerContext来发消息，因为在ChannelHandlerContext里面也是调用自己的channel()方法，
	//拿到那个channel，再发消息
	//一个客户端连接到服务器上，会有一个channel存在
	//channel什么时候初始化呢？应该是在连上服务器的时候
	private Channel channel = null;
	
	public void connect() {
		//大管家
		EventLoopGroup group = new NioEventLoopGroup(1);
		//靴子带，起一个socket，去连接远程服务器
		Bootstrap b = new Bootstrap();
		
		try {
			ChannelFuture f = b.group(group) //启动时指定线程池
					.channel(NioSocketChannel.class) //指定连接到服务器的channel类型
					.handler(new ClientChannelInitializer()) //当channel上有事件的时候，交给哪个handler处理
					.connect("localhost", 8888);
					
			f.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if(future.isSuccess()) {
						System.out.println("connected!");
						//channel在确认client连接server成功之后，初始化
						channel = future.channel();
					} else {
						System.out.println("not connected!");
					}
				}
			});
			
			f.sync(); //阻塞住
			//监听close
			f.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			group.shutdownGracefully(); //优雅的关闭
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
		//ChannelInitializer是做channel初始化的
		//当client连接到服务器上后，调用initChannel方法，往Server端写数据
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
		//网络都是通过比特流01000101110，来传数据
		//所以任何东西想放到网上来写的话，只有一个办法，就是转换成字节数组，就是二进制
		//在NIO里面有一个ByteBuffer，特别难用
		//所以在Netty里面，有一个ByteBuf
		//在Netty里面，写任何数据，最终都是由ByteBuf写出去的，而且效率特别高
		//在Netty里面，读数据也是ByteBuf
		//client往server写了个数据，hello
		ByteBuf buf = Unpooled.copiedBuffer("hello，我上线了！".getBytes());
		ctx.writeAndFlush(buf);
	}
}
