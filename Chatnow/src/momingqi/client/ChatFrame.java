package momingqi.client;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class ChatFrame extends JFrame
{
	public MainFrame mf;
	private Friend f;
	public JTextArea msgArea;
	public JTextArea inputArea;
	public JLabel tipLabel;
	
	public ChatFrame(MainFrame mf, Friend f)
	{
		this.mf = mf;
		this.f= f;
		initComponent();
	}

	/**
	 * ��ʼ�����
	 */
	private void initComponent()
	{
		msgArea = new JTextArea(20, 10);
		inputArea = new JTextArea(20, 10);
		tipLabel = new JLabel("  ");
		
		JPanel infoPanel = mf.getFriendPanel(f.id);
		JPanel buttonPanel = new JPanel(new GridLayout(1,2));
		
		JButton sendButton = new JButton("����");
		JButton resetButton = new JButton("����");
		buttonPanel.add(tipLabel);
		buttonPanel.add(sendButton);
		buttonPanel.add(resetButton);
		
		//�������ʧȥ����ʱ��������ʾ��Ϣ
		inputArea.addFocusListener(new FocusListener()	
		{
			
			@Override
			public void focusLost(FocusEvent e)
			{
				tipLabel.setText("  ");
			}
			
			@Override
			public void focusGained(FocusEvent e)
			{
			}
		});
		
		sendButton.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				sendMsg();
			}

		});
		
		tipLabel.setForeground(Color.RED);
		
		this.setLayout(new FlowLayout());
		this.add(infoPanel);
		this.add(msgArea);
		this.add(inputArea);
		this.add(buttonPanel);
		this.setTitle("������" + f.nickname + "����");
		this.setVisible(true);
		this.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				setVisible(false);
				mf.removeChatFrame(f.id); 	//�Ƴ��˴���
				System.gc();				//GC����
			}
		});
	}
	
	/**
	 * ������Ͱ�ťʱ
	 */
	private void sendMsg()
	{
		String msg = inputArea.getText();
		if(msg.equals(""))	//�����Ϊ��
		{
			tipLabel.setText("���������ݣ�");
			return;
		}
		if(f.online == false)	//�Է�������
		{
			showError("�Է���������״̬���޷�������Ϣ��");
			return;
		}
		//����xml
		String msg_xml = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?><msg sender=\"%s\" receiver=\"%s\">%s</msg>", mf.getID(), f.id, msg);
		//����������Ϣ�߳�
		SendMsgThread smt = new SendMsgThread(this, msg_xml);
		smt.start();
	}
	
	/**
	 * �����������ʾmsg
	 * @param msg
	 */
	public void setMsgText(String msg)
	{
		//��ȡϵͳ��ǰʱ��
		msgArea.append("\n" + msg);
	}
	
	public OutputStream getOutputStream() throws IOException
	{
		return mf.getOutputStream();
	}

	/**
	 * ��ʾ������Ϣ
	 * @param string
	 */
	public void showError(String error)
	{
		msgArea.append("\n" + error);
	}

	public void logMsg(String msg)
	{
		
	}
}
