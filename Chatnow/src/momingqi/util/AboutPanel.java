package momingqi.util;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class AboutPanel extends JPanel
{
	public AboutPanel()
	{
		Font font = new Font("Dialog.plain", Font.PLAIN, 16);
		Icon icon = new ImageIcon("./server_resources/author.jpg");
		JPanel msgPanel = new JPanel(new GridLayout(4, 1));
		JLabel label1 = new JLabel("作者：莫铭棋");
		JLabel label2 = new JLabel("学号：3115005325");
		JLabel label3 = new JLabel("创作时间：2016/11/25");
		label1.setFont(font);
		label2.setFont(font);
		label3.setFont(font);
		msgPanel.add(label1);
		msgPanel.add(label2);
		msgPanel.add(label3);
		this.add(msgPanel);
		this.add(new JLabel(icon));
		this.setBorder(BorderFactory.createTitledBorder("作者信息"));
	}
}
