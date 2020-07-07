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
		ssc.configureBlocking(false); // nioĬ���������ģ��������þ��Ƿ�������
		
		System.out.println("server started, listening on : " + ssc.getLocalAddress());
		// ��ܼң�����server���channel��
		Selector selector = Selector.open();
		
		// �Ѵ�ܼ�ע�ᵽchannel����ܼ�ֻ����ͻ������ӵ��¼�
		ssc.register(selector, SelectionKey.OP_ACCEPT);
		
		while(true) {
			// �������κδ�ܼҹ��ĵ��¼��������˷�������
			selector.select();
			Set<SelectionKey> keys = selector.selectedKeys();
			Iterator<SelectionKey> it = keys.iterator();
			while(it.hasNext()) {
				SelectionKey key = it.next();
				it.remove(); // ������ִ��remove
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
