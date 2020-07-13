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
	
	//50������Ա
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
		System.out.println("����������ɹ���");
	}
	
	public void listen() throws IOException {
		while(true) { // ��ѯ
			selector.select();
			Iterator it = this.selector.selectedKeys().iterator();
			while(it.hasNext()) {
				SelectionKey key = (SelectionKey) it.next();
				it.remove();
				if(key.isAcceptable()) { // ʲôʱ���ܹ�acceptable��ʲôʱ��Ž���
					ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
					SocketChannel sc = ssc.accept();
					sc.configureBlocking(false);
					// Ȼ��������ע��selector���۲�OP_READ�¼�
					sc.register(this.selector, SelectionKey.OP_READ);
				} else if(key.isReadable()) { // ʲôʱ����read �ˣ�ʲôʱ��Ž����������̳߳�ִ��
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
			ByteBuffer buffer = ByteBuffer.allocate(1024); //������һ��1024��byte������Ļ�����
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
