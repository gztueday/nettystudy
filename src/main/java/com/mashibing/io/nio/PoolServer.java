package com.mashibing.io.nio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PoolServer {
	
	//50个服务员
	ExecutorService pool = Executors.newFixedThreadPool(50);
	
	private Selector selector;
	
	public static void main(String[] args) throws IOException {
		PoolServer server = new PoolServer();
		server.initServer(8888);
		server.listen();
	}
	
	public void initServer(int port) throws IOException {
		ServerSocketChannel ssc = ServerSocketChannel.open();
		ssc.configureBlocking(false);
		ssc.socket().bind(new InetSocketAddress(port));
		this.selector = Selector.open();
		ssc.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("服务端启动成功！");
	}
	
	public void listen() throws IOException {
		while(true) { // 轮询
			selector.select();
			Iterator it = this.selector.selectedKeys().iterator();
			while(it.hasNext()) {
				SelectionKey key = (SelectionKey) it.next();
				it.remove();
				if(key.isAcceptable()) { // 什么时候能够acceptable，什么时候放进来
					ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
					SocketChannel sc = ssc.accept();
					sc.configureBlocking(false);
					// 然后在上面注册selector，观察OP_READ事件
					sc.register(this.selector, SelectionKey.OP_READ);
				} else if(key.isReadable()) { // 什么时候能read 了，什么时候放进来，交给线程池执行
					key.interestOps(key.interestOps() & (-SelectionKey.OP_READ));
					pool.execute(new ThreadHandlerChannel(key));
				}
			}
		}
	}
	
	class ThreadHandlerChannel extends Thread {
		private SelectionKey key;
		ThreadHandlerChannel(SelectionKey key) {this.key = key;}
		@Override
		public void run() {
			SocketChannel channel = (SocketChannel) key.channel();
			ByteBuffer buffer = ByteBuffer.allocate(1024); //创建了一个1024个byte的数组的缓冲区
			ByteArrayOutputStream baos =  new ByteArrayOutputStream();
			try {
				int size = 0;
				while((size=channel.read(buffer)) > 0) {
					buffer.flip();
					baos.write(buffer.array(), 0, size);
					buffer.clear();
				}
				baos.close();
				
				byte[] content = baos.toByteArray();
				ByteBuffer writeBuf = ByteBuffer.allocate(content.length);
				writeBuf.put(content);
				writeBuf.flip();
				channel.write(writeBuf);
				if(size == -1) {
					channel.close();
				} else {
					key.interestOps(key.interestOps() | SelectionKey.OP_READ);
					key.selector().wakeup();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
