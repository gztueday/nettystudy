package com.mashibing.io.bio;

import java.io.IOException;
import java.net.Socket;

public class Client {
	
	public static void main(String[] args) throws IOException {
		
		//��һ�仰��client�Ѿ����ӵ��������ˣ���������֮�佨����һ��ͨ�����ͽ�socket��
		//client�˿��������s���������ͨ����ͨ�����s��������������д���ݣ�Ҳ���Զ����ݡ�
		//���д�أ�
		Socket s = new Socket("127.0.0.1", 8888);
		s.getOutputStream().write("HelloServer".getBytes());
		s.getOutputStream().flush();
		System.out.println("write over, waiting for msg back...");
		byte[] bytes = new byte[1024];
		int len = s.getInputStream().read(bytes);
		System.out.println("Client���յ��Ļظ���" + new String(bytes, 0, len));
		s.close();
	}
	
}
