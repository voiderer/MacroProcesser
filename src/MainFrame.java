import javax.swing.*;
import javax.swing.plaf.TabbedPaneUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.*;

/**
 * Created by autoy on 2015/11/26.
 */
public class MainFrame extends JFrame implements ActionListener {
    public static void main (String arg[]) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        new MainFrame();
    }

    JMenuBar menuBar;
    JMenu menu;
    JMenuItem itemOpen, itemClose;
    JTabbedPane tabbedPane;
    ImageIcon icon;
    LinkedList<MyTextPanel> panelList;

    MainFrame() {
        super("C语言宏常量计算器");
        panelList=new LinkedList<>();
        menuBar = new JMenuBar ();

        itemOpen = new JMenuItem("打开文件");
        itemOpen.addActionListener(this);
        itemClose = new JMenuItem("关闭文件");
        itemClose.addActionListener(this);
        menu=new JMenu("开始");
        menu.add(itemOpen);
        menu.add(itemClose);
        menuBar.add(menu);
        icon = new ImageIcon(MainFrame.class.getResource("C.jpg"));
        this.setIconImage(icon.getImage());
        this.setJMenuBar(menuBar);
        this.add(tabbedPane = new JTabbedPane(), BorderLayout.CENTER);
        tabbedPane.setBackground(new Color(43,43,43));
        MyTextPanel textPanel;
        tabbedPane.addTab("新建文档", icon, new JScrollPane(textPanel=new MyTextPanel()));
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
        panelList.add(textPanel);
        this.setSize(1000, 700);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == itemOpen) {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File myFile = fileChooser.getSelectedFile();
                MyTextPanel panel=new MyTextPanel(myFile);
                panel.highlight();
                tabbedPane.addTab(myFile.getName(), icon, new JScrollPane(panel));
                tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
                panelList.add(panel);
            }
        } else if (source == itemClose) {
            if(tabbedPane.getTabCount()==0)
            {
                return;
            }
            panelList.remove(tabbedPane.getSelectedIndex());
            tabbedPane.removeTabAt(tabbedPane.getSelectedIndex());
        }
    }
}