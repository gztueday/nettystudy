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

//NIO���߳�ģ��
//���ģ�ͻ��Ǽ򵥵ģ�bytebuffer�ǳ������������ã�����netty������bytebuf��
//������Ҳ����˵����дNIO��
public class Server {
	
	public static void main(String[] args) throws IOException {
		//ServerSocketChannelȫ˫����д��ʱ����Զ�������ʱ�����д
		//open()������׼�����ܿͻ��˵�����
		ServerSocketChannel ssc = ServerSocketChannel.open();
		ssc.socket().bind(new InetSocketAddress("127.0.0.1", 8888));
		ssc.configureBlocking(false); //�ǳ���Ҫ��NioĬ���������ģ��������þ��Ƿ�������
		
		System.out.println("server started, listening on : " + ssc.getLocalAddress());
		
        // ��ܼң��������е����飬���۶������������������Լ�����
		// ���Ĺ���ʽ����ѯ�����ϵ���ѯ����ѵ�ͻ������ӣ������ĸ���������
		// ����ӭ�ͣ����˶����ˣ���һ����ܼң��ٶ��ر��
		// ���ص���ѯ�����������¡��������£���ȥ�Ĵ���
        // ��ܼ��ܽ��ܵ��¼�������ô����
        // ��ܼҿ��Թ���ܶ�channel�����ڹ������server���channel
		Selector selector = Selector.open();
		
		// �����channel�ϰѹܼ�ע���ϣ���ܼ�ֻ����ͻ������ӵ��¼�
		// ��������һ��ServerSocketChannel�������ˣ�Ȼ��˵��ܼң�����ҹ������ServerSocketChannel
		// ��ܼ�˵���ã������ˣ��Ұ���ܣ�������ֻ����OP_ACCEPT��������·�����ʱ����������
		// OP_ACCEPT�������пͻ���Ҫ�����������ʱ��ͻᴥ��һЩ�¼�
		ssc.register(selector, SelectionKey.OP_ACCEPT);
		
		//���￪ʼ���Ǵ�ܼ���ô�����������
		while(true) { // ��ܼҴ����¼���������ó�������������������while��true����������boolean���ͱ���
			// �������κδ�ܼҹ��ĵ��¼��������˷�������
			// Ҳ��������������ܼҿ��Թ���ܶ�channel�����κ�һ��channel��selector���ĵ��¼�������ʱ�򣬷���
            // �����ܼҹ����˺ܶ�channel��������ѯ�������Ұ�ÿ��channel���¼���ȡ����
			//��ʱ��ܼ�ֻ������server���channel������ֻ���Ŀͻ����������server���¼�
			selector.select();
			
            // ������֮���أ��Ͱ��õ�����Щ�¼�����Щ�¼���װ��SelectionKey����
			// Ȼ�����ЩSelectionKey�ó��������д���
			Set<SelectionKey> keys = selector.selectedKeys();
			Iterator<SelectionKey> it = keys.iterator();
			while(it.hasNext()) {
				SelectionKey key = it.next(); // ��SelectionKey �ó�������
				// �����¼�֮ǰ�Ȱ���remove����Ȼ�»���Ҫ��������¼�
                // remove()��handle()�������ܻ�˳����Ϊ���ܶ���߳�ͬʱ�����У��������ʱremove������������
                it.remove(); // ������ִ��remove
				handle(key);
			}
		}
	}

	private static void handle(SelectionKey key) {
		 // �жϿͻ��������Ƿ�ɽ���
		if(key.isAcceptable()) {
			try {
				ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
				
				// �ѿͻ���ӭ�ӽ���
                // �ͻ���ӭ�ӽ���֮����Ҳ�����һ��channel���Ϳͻ�������
				SocketChannel sc = ssc.accept();
				sc.configureBlocking(false);
				// �ڿͻ��˵�channel��ע���ҹ��ĵ��¼������ڹ��Ŀͻ�����������д�������¼�
				sc.register(key.selector(), SelectionKey.OP_READ);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			}
		} else if(key.isReadable()) { // ���key�ǿɶ�����������Ȼ����д��ȥ
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
