package server;

import java.awt.Container;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
/**
 * 服务器端，基于UDP的群聊
 * 服务器视图窗口
 *连接客户端，用户下线，聊天信息转发
 */
public class server extends JFrame {
	private JTextArea allmsg;//系统消息
	private JTextField currnum, totalnum, chatmsg;//当前在线人数，上线总人数，要发送的系统信息
	private JButton send;
	private JScrollPane js;//滚动条
	int num1, num2, port;/* -- num1:当前在线人数 num2:总上线人数 port:服务器端口号 -- */
	private ServerSocket ss;
	ArrayList lists;// 存放所有在线用户
	
	/*
	 * 服务器窗口
	 */
	public server() {
		super("聊天室服务器端");
		this.setSize(310, 660);
		this.setLocation(200, 50);
		lists = new ArrayList();
		num1 = num2 = 0;
		port = 7777;
		currnum = new JTextField(" 当前在线人数： " + num1);
		currnum.setEnabled(false);
		totalnum = new JTextField(" 上线总人数： " + num2);
		totalnum.setEnabled(false);
		allmsg = new JTextArea();
		allmsg.append("                   --------------- 系统消息 --------------\n");
		allmsg.setEditable(false);
		allmsg.setLineWrap(true); // 允许自动换行
		js = new JScrollPane(allmsg);// 为JTextArea添加滚动条
		
		chatmsg = new JTextField("在此输入系统信息");
		chatmsg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String str = chatmsg.getText().trim();
				if (!"".equals(str)) {
					sendmsg((new Date()).toLocaleString() + " -- 系统消息： " + str);
					chatmsg.setText("");
				} else
					JOptionPane.showMessageDialog(null, "消息不能为空", "错误", JOptionPane.OK_OPTION);
				chatmsg.setText("");/* -- 发送信息后，将输入栏中的信息清空 -- */
			}
		});

		send = new JButton("发送");
		send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String str = chatmsg.getText().trim();
				if (!"".equals(str)) {
					sendmsg((new Date()).toLocaleString() + " -- 系统消息： " + str);
					chatmsg.setText("");
				} else
					JOptionPane.showMessageDialog(null, "消息不能为空", "错误", JOptionPane.OK_OPTION);
				chatmsg.setText("");/* -- 发送信息后，将输入栏中的信息清空 -- */
			}
		});

		addcomponettocontainer();
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				sendmsg("SYSTEM_CLOSED");/* -- 向客户端发送服务器关闭信息 -- */
				destory();
			}
		});

		start(); /* -- 启动连接服务 -- */
	}

	/*
	 * 将组件添加到容器
	 */
	public void addcomponettocontainer() {
		Container c = this.getContentPane();
		c.setLayout(null);
		currnum.setBounds(20, 15, 130, 20);
		totalnum.setBounds(155, 15, 125, 20);
		js.setBounds(10, 50, 280, 500);
		chatmsg.setBounds(10, 560, 180, 30);
		send.setBounds(220, 560, 70, 30);
		c.add(currnum);
		c.add(totalnum);
		c.add(js);
		c.add(chatmsg);
		c.add(send);
		this.setVisible(true);
		this.setResizable(false);
	}
	
	/*
	 * 客户端连接服务器
	 */
	public void start() {
		boolean isStarted = false;/* -- 用于标记服务器是否已经正常启动 -- */
		try {
			this.ss = new ServerSocket(port);
			isStarted = true;
			this.allmsg.append((new Date()).toLocaleString() + "    服务器启动 @ 端口: " + port + "\n");
			while (isStarted) {
				Socket client = this.ss.accept(); /* -- 监听客户端的连接 -- */
				DataInputStream in = new DataInputStream(client.getInputStream());
				String name = in.readUTF();
				user u = new user();//创建用户
				u.name = name;
				u.socket = client;
				lists.add(u); // 将该用户加到列表中去
				num1++;
				num2++;
				currnum.setText(" 当前在线人数： " + num1);
				totalnum.setText(" 上线总人数： " + num2);
				this.allmsg.append((new Date()).toLocaleString() + " : " + u.name + " 登录 \n");
				new Thread(new ClientThread(u)).start();/* -- 为该用户启动一个通信线程 -- */
			}
		} catch (IOException e) {
			System.out.println("服务器已经关闭......");
			System.exit(0);
		}
	}
	
	/*
	 * 用户下线
	 */
	class ClientThread implements Runnable {
		user user = null;
		boolean isConnected = true;
		DataInputStream dis = null;
		DataOutputStream dos = null;

		public ClientThread(user u) {
			this.user = u;
			try {
				this.dis = new DataInputStream(this.user.socket.getInputStream());
				this.dos = new DataOutputStream(this.user.socket.getOutputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void run() {
			readmsg();
		}

		/* -- 读取客户的聊天信息 -- */
		public void readmsg() {
			while (isConnected) {
				try {
					String msg = dis.readUTF();
					if ("quit&logout".equals(msg))
					// 当用户关闭客户端窗口时，客户端发送quit字符串 表示用户已经退出
					{
						num1--;
						try {
							this.dis.close();
							this.dos.close();
							this.user.socket.close();
							this.isConnected = false;
						} catch (IOException ioe) {
							ioe.printStackTrace();
						} finally {
							this.isConnected = false;
							if (dis != null)
								this.dis.close();
							if (dos != null)
								this.dos.close();
							if (this.user.socket != null)
								this.user.socket.close();
						}
						lists.remove(this.user);// 从列表中删除该用户
						currnum.setText(" 当前在线人数： " + num1);
						allmsg.append((new Date()).toLocaleString() + "  : " + this.user.name + "  退出\n");
					} else
						sendmsg(msg);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/*
	 * 将聊天信息进行转发
	 */
	public void sendmsg(String msg) {
		user us = new user();
		DataOutputStream os = null;
		if (lists.size() > 0) {
			for (int i = 0; i < lists.size(); i++) {
				us = (user) lists.get(i);
				try {
					os = new DataOutputStream(us.socket.getOutputStream());
					os.writeUTF(msg);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else
			JOptionPane.showMessageDialog(null, "当前无用户在线。发送消息失败", "失败", JOptionPane.OK_OPTION);
	}

	public void destory() {
		try {
			this.ss.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.dispose();
	}

	public static void main(String[] args) {
		server s = new server();
	}
}
