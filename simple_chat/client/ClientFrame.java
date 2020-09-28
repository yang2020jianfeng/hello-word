package client;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
/**
 * 客户端视图窗口
 */
public class ClientFrame extends JFrame {
	private JTextArea allmsg;//系统消息
	private JTextField welcome,chatmsg;//客户端消息，聊天信息
	private JButton send;
	private JScrollPane js;//滚动条
	private boolean isConnected = true;
	public DataOutputStream out;
	public DataInputStream in;
	public Socket s = null;
	String nic; //保存用户昵称 

	/**
	 * 初始化客户端资源 
	 * 1.获取从client类中login()方法传递过来的参数
	 * 2.初始化界面元素 
	 * 3.初始化通信所需要的资源
	 * EG：输入/输出流(DataInputStream/DataOutputStream)
	 */
	public ClientFrame(String name, Socket socket) {
		this.setSize(310, 660);
		this.setLocation(290, 50);
		this.setTitle("聊天室客户端<" + name + ">");/* -- 指定窗口的标题 -- */
		this.s = socket;/* -- 接收从client类中login()方法传递过来的Socket -- */
		this.nic = name + " 说： ";
		welcome = new JTextField(" < " + name + " >欢迎您来到聊天室 ", 100);
		welcome.setBackground(Color.blue);
		welcome.setEnabled(false);		
		allmsg = new JTextArea();
		allmsg.setEditable(false);
		allmsg.append("                  系统消息: 欢迎登录在线聊天室 \n");
		js = new JScrollPane(allmsg);// 为JTextArea添加滚动条
		chatmsg = new JTextField("在此输入聊天信息");
		chatmsg.addActionListener(new listen());
		send = new JButton("发送");
		send.addActionListener(new listen());/* -- 添加事件监听器 -- */
		try {
			out = new DataOutputStream(s.getOutputStream());
			in = new DataInputStream(s.getInputStream());
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "系统异常", "错误", JOptionPane.OK_CANCEL_OPTION);
		}
		addcomponettocontainer();
		
		/* -- 当用户关闭窗口时进行相关的处理 eg:Socket Data(Input/Output)Stream 的关闭-- */
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				sendmsg("quit&logout");/* -- 向服务器端发送关闭信息 -- */
				isConnected = false;
				destory();/* -- 销毁窗口资源 -- */
			}
		});
		new Thread(new linread()).start();/* -- 启动读取信息线程 -- */
	}
	
	/*
	 * 将组件添加到容器
	 */
	public void addcomponettocontainer() {
		Container c = this.getContentPane();
		c.setLayout(null);
		welcome.setBounds(75, 10, 150, 20);
		js.setBounds(10, 50, 280, 500);
		chatmsg.setBounds(10, 560, 180, 30);
		send.setBounds(220, 560, 70, 30);
		c.add(welcome);
		c.add(js);
		c.add(chatmsg);
		c.add(send);
		this.setVisible(true);
		this.setResizable(false);
	}
	
	/*
	 * 聊天信息发送
	 */
	class listen implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			if (e.getSource() == send || e.getSource() == chatmsg) {
				String msg = chatmsg.getText().trim();
				if ("".equals(msg)) {
					JOptionPane.showMessageDialog(null, "发送信息不能为空!", "错误", JOptionPane.OK_OPTION);
				} else {
					sendmsg((new Date()).toLocaleString() + "\n" + nic + msg + "\n");
					chatmsg.setText("");
				}
			}
		}
	}

	/*
	 * 向服务器端发送信息
	 */
	public void sendmsg(String m) {
		if (isConnected)// 如果socket的输出流没关闭
		{
			try {
				out.writeUTF(m);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "发送信息失败!(系统异常)", "错误", JOptionPane.OK_OPTION);
			}
		} else {
			JOptionPane.showMessageDialog(null, "发送信息失败!(服务器关闭/网络故障)", "错误", JOptionPane.OK_OPTION);
		}
	}

	/*
	 * 读取信息线程
	 */
	class linread implements Runnable {
		public void run() {
			read();
		}

		public void read() {
			while (isConnected) {
				try {
					String msg = in.readUTF();
					if ("SYSTEM_CLOSED".equals(msg)) {
						JOptionPane.showMessageDialog(null, "读取消息失败(服务器关闭/网络故障)！", "错误", JOptionPane.OK_OPTION);
						isConnected = false;
					} else
						allmsg.append(msg + "\n");
				} catch (IOException e) {
				}
			} // end while
			JOptionPane.showMessageDialog(null, "读取消息失败(客户端关闭/网络故障)！", "错误", JOptionPane.OK_OPTION);
		}// end read()
	}

	public void destory() {
		try {
			this.out.close();
			this.in.close();
			this.s.close();
		} catch (IOException e) {
		}
		this.dispose();
	}
}
