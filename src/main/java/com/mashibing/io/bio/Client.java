package com.mashibing.io.bio;

import java.io.IOException;
import java.net.Socket;

public class Client {
	
	public static void main(String[] args) throws IOException {
		
		//这一句话，client已经连接到服务器了，它们两个之间建立了一个通道，就叫socket。
		//client端看到的这个s，就是这个通道。通过这个s，可以往服务器写内容，也可以读内容。
		//如何写呢？
		Socket s = new Socket("127.0.0.1", 8888);
		s.getOutputStream().write("HelloServer".getBytes());
		s.getOutputStream().flush();
		System.out.println("write over, waiting for msg back...");
		byte[] bytes = new byte[1024];
		int len = s.getInputStream().read(bytes);
		System.out.println("Client接收到的回复：" + new String(bytes, 0, len));
		s.close();
	}
	
}
