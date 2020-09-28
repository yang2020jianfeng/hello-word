package client;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
/**
 * 用户登录窗口
 * 连接服务器
 *创建客户端视图窗口
 */
public class client extends JFrame {
	private JTextField name;//用户昵称
	private JTextField ip;//服务器IP地址
	private JButton ok, cancle;
	public Socket socket;
	
	/*
	 * 用户登录窗口
	 */
	public client() {
		super("登录框");
		this.setSize(400, 80);
		this.setLocation(100, 100);
		name = new JTextField("昵称");
		ip = new JTextField("127.0.0.1");
		ok = new JButton("登录");
		cancle = new JButton("取消");
		ok.addActionListener(new listenEvent());
		cancle.addActionListener(new listenEvent());
		addcomponettocontainer();
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	/*
	 * 将组件添加到容器
	 */
	public void addcomponettocontainer() {
		Container c = this.getContentPane();
		c.setLayout(null);
		name.setBounds(10, 10, 100, 30);
		ip.setBounds(120, 10, 100, 30);
		ok.setBounds(230, 10, 70, 30);
		cancle.setBounds(310, 10, 70, 30);
		c.add(name);
		c.add(ip);
		c.add(ok);
		c.add(cancle);
		this.setVisible(true);
		this.setResizable(false);
	}
	
	/*
	 * 为按钮注册事件
	 */
	public class listenEvent implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if (event.getSource() == ok) {
				String n = name.getText().trim();
				String i = ip.getText().trim();
				if ("".equals(n) || "".equals(i)) {
					JOptionPane.showMessageDialog(null, "昵称、IP不能够为空!", "错误", JOptionPane.OK_OPTION);
				} else {
					login(n, i);
				}
			}
			if (event.getSource() == cancle) {
				name.setText("");
				ip.setText("");
			}
		}
	}

	/*
	 * 用户上线
	 */
	public void login(String name, String ip) {
		try {
			socket = new Socket(ip, 7777);
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeUTF(name);
			out.flush();// 输出缓存中的内容
			// out.close();
			new ClientFrame(name, socket);
			destroywindow();
		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(null, "找不到主机地址(IP错误/网络故障)！", "错误", JOptionPane.OK_OPTION);
		} catch (IOException e) {
		}
	}

	public void destroywindow() {
		this.dispose();
	}

	public static void main(String[] args) {
		new client();
	}
}
