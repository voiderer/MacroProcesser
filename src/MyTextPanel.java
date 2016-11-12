import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by autoy on 2015/11/26.
 */
public class MyTextPanel extends JTextPane{
    StyledDocument doc;
    MyTextPanel()
    {
        setFont(new Font("隶书",Font.BOLD,16));
        setBackground(new Color(43,43,43));
        setForeground(new Color(168,182,164));
        doc = getStyledDocument();
        SimpleAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, Color.black);
        // setBorder(new LineNumberBorder());
        this.setCaretColor(Color.white);
        addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
             /*   char a=0;
                try {
                    a=doc.getText(getCaretPosition()-2,1).charAt(0);
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                    return;
                }
                if(!Processor.identifierBodyTable.isInclude(e.getKeyChar())&&Processor.identifierBodyTable.isInclude(a))*/
                highlight();
            }
        });
    }
    MyTextPanel(File myFile) {
        setFont(new Font("隶书",Font.BOLD,16));
        setBackground(new Color(43,43,43));
        setForeground(new Color(168,182,164));
        doc = getStyledDocument();

        SimpleAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, Color.black);
       // setBorder(new LineNumberBorder());
        this.setCaretColor(Color.white);
        try {
            FileReader fr = new FileReader(myFile);
            BufferedReader br = new BufferedReader(fr);
            String temp;
            while (((temp = br.readLine()) != null)) {
                doc.insertString(doc.getLength(),temp+"\n",attr);
            }
        } catch (IOException ee) {
            System.out.print(ee);
        } catch (BadLocationException e) {
            e.printStackTrace();
        } finally {
        }
        addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
             /*   char a=0;
                try {
                    a=doc.getText(getCaretPosition()-2,1).charAt(0);
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                    return;
                }
                if(!Processor.identifierBodyTable.isInclude(e.getKeyChar())&&Processor.identifierBodyTable.isInclude(a))*/
                highlight();
            }
        });
    }
    void highlight()
    {
        new Processor().analyze(this);
    }
}

