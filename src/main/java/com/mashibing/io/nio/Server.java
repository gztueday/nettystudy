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

//NIO单线程模型
//这个模型还是简单的，bytebuffer是臭名昭著的难用，所以netty里面用bytebuf。
//工作中也不会说让你写NIO。
public class Server {
	
	public static void main(String[] args) throws IOException {
		//ServerSocketChannel全双工，写的时候可以读，读的时候可以写
		//open()方法，准备接受客户端的连接
		ServerSocketChannel ssc = ServerSocketChannel.open();
		ssc.socket().bind(new InetSocketAddress("127.0.0.1", 8888));
		ssc.configureBlocking(false); //非常重要，Nio默认是阻塞的，这样设置就是非阻塞的
		
		System.out.println("server started, listening on : " + ssc.getLocalAddress());
		
        // 大管家，管理所有的事情，无论多少连接上来都是它自己管理
		// 它的管理方式是轮询，不断的轮询，轮训客户端连接，看看哪个桌叫人呢
		// 开门迎客，客人都来了，就一个大管家，速度特别快
		// 来回的轮询，看哪里有事。哪里有事，就去哪处理
        // 大管家能接受的事件，就那么几种
        // 大管家可以管理很多channel，现在管理的是server这个channel
		Selector selector = Selector.open();
		
		// 在这个channel上把管家注册上，大管家只处理客户端连接的事件
		// 我首先有一个ServerSocketChannel，起来了，然后说大管家，你帮我管理这个ServerSocketChannel
		// 大管家说，好，我来了，我帮你管，但是我只管理OP_ACCEPT，有这件事发生的时候，我来处理
		// OP_ACCEPT，就是有客户端要连上来，这个时候就会触发一些事件
		ssc.register(selector, SelectionKey.OP_ACCEPT);
		
		//这里开始就是大管家怎么来处理这件事
		while(true) { // 大管家处理事件，如果想让程序正常结束，不能用while（true），可以用boolean类型变量
			// 阻塞，任何大管家关心的事件发生，此方法返回
			// 也是阻塞方法，大管家可以管理很多channel，有任何一个channel有selector关心的事件发生的时候，返回
            // 如果大管家管理了很多channel，它会轮询管理，并且把每个channel的事件都取出来
			//此时大管家只管理了server这个channel，而且只关心客户端连接这个server的事件
			selector.select();
			
            // 返回了之后呢，就把拿到的那些事件，这些事件是装在SelectionKey里面
			// 然后把这些SelectionKey拿出来，进行处理
			Set<SelectionKey> keys = selector.selectedKeys();
			Iterator<SelectionKey> it = keys.iterator();
			while(it.hasNext()) {
				SelectionKey key = it.next(); // 把SelectionKey 拿出来处理
				// 处理事件之前先把它remove，不然下回又要处理这个事件
                // remove()、handle()方法不能换顺序，因为可能多个线程同时在运行，如果不及时remove，则会带来问题
                it.remove(); // 必须先执行remove
				handle(key);
			}
		}
	}

	private static void handle(SelectionKey key) {
		 // 判断客户端连接是否可接受
		if(key.isAcceptable()) {
			try {
				ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
				
				// 把客户端迎接进来
                // 客户端迎接进来之后，它也会产生一个channel，和客户端连接
				SocketChannel sc = ssc.accept();
				sc.configureBlocking(false);
				// 在客户端的channel上注册我关心的事件，现在关心客户端往我这里写东西的事件
				sc.register(key.selector(), SelectionKey.OP_READ);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			}
		} else if(key.isReadable()) { // 如果key是可读，读进来，然后再写出去
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
