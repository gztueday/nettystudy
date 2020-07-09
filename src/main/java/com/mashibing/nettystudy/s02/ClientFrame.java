package com.mashibing.nettystudy.s02;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ClientFrame extends Frame {
	
	TextArea ta = new TextArea();
	TextField tf = new TextField();
	
	public ClientFrame() {
		this.setSize(600, 400);
		this.setLocation(100, 20);
		this.add(ta, BorderLayout.CENTER);
		this.add(tf, BorderLayout.SOUTH);
		
		tf.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//把字符串发送到服务器
				ta.setText(ta.getText() + "\n" + tf.getText());
				tf.setText("");
			}
		});
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		
		this.setVisible(true);
	}
	
	public static void main(String[] args) {
		new ClientFrame();
		initClient();
	}
	
	public static void initClient() {
		Client c = new Client();
	}

}
