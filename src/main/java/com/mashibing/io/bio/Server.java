package com.mashibing.io.bio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	public static void main(String[] args) throws IOException {
		ServerSocket ss = new ServerSocket();
		ss.bind(new InetSocketAddress("127.0.0.1", 8888));
		while(true) { // 不断的接收客户端的连接
			Socket s = ss.accept(); // 接受客户端的连接，阻塞方法。如果没有客户端连上来，就一直在这里等
			new Thread(()->{
				handle(s);
			}) .start();
		}
	}
	public static void handle(Socket s) {
		try {
			byte[] bytes = new byte[1024];
			int len = s.getInputStream().read(bytes);
			System.out.println("Server接收到的Client信息：" + new String(bytes, 0, len));
			s.getOutputStream().write(bytes);
			s.getOutputStream().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
