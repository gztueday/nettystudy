package com.mashibing.nettystudy.s01;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class BIOServerTest {
	public static void main(String[] args) throws IOException {
		ServerSocket ss = new ServerSocket();
		ss.bind(new InetSocketAddress(8888));
		Socket s = ss.accept();
		System.out.println("a client connect!");
	}
}
