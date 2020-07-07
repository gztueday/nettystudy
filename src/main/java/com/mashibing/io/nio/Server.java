package com.mashibing.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server {
	
	public static void main(String[] args) throws IOException {
		ServerSocketChannel ssc = ServerSocketChannel.open();
		ssc.socket().bind(new InetSocketAddress("127.0.0.1", 8888));
		ssc.configureBlocking(false); // nio默认是阻塞的，这样设置就是非阻塞的
		
		System.out.println("server started, listening on : " + ssc.getLocalAddress());
		// 大管家，管理server这个channel，
		Selector selector = Selector.open();
		
		// 把大管家注册到channel，大管家只处理客户端连接的事件
		ssc.register(selector, SelectionKey.OP_ACCEPT);
		
		while(true) {
			// 阻塞，任何大管家关心的事件发生，此方法返回
			selector.select();
			Set<SelectionKey> keys = selector.selectedKeys();
			Iterator<SelectionKey> it = keys.iterator();
			while(it.hasNext()) {
				SelectionKey key = it.next();
				it.remove(); // 必须先执行remove
				handle(key);
			}
		}
	}

	private static void handle(SelectionKey key) {
		if(key.isAcceptable()) {
			try {
				ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
				SocketChannel sc = ssc.accept();
				sc.configureBlocking(false);
				
				sc.register(key.selector(), SelectionKey.OP_READ);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			}
		} else if(key.isReadable()) {
			SocketChannel sc = null;
			try {
				sc = (SocketChannel) key.channel();
				ByteBuffer buffer = ByteBuffer.allocate(512);
				buffer.clear();
				int len = sc.read(buffer);
				if(len != -1) {
					System.out.println(new String(buffer.array(), 0, len));
				}
				
				ByteBuffer bufferToWrite = ByteBuffer.wrap("HelloClient".getBytes());
				sc.write(bufferToWrite);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
