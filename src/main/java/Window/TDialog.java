package Window;

import Components.Action;
import Components.TButton;
import Components.TLabel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TDialog extends JFrame {
    private final int GAP = 15;//按钮间隔
    //各组件尺寸
    private final int BUTTON_HEIGHT=44;
    private final int BUTTON_WIDTH = 307;
    private final int DIALOG_WIDTH = 337;
    private final int DIALOG_HEIGHT = 400;
    private JPanel panel;
    private TButton[] buttons;//按钮
    private int selected_index;//当前被选中的按钮的下标
    private int pre_button_index;//上一个被选中的按钮的下标
    public TDialog(String query, String[] buttonTexts){
        setLayout(null);//绝对布局
        setUndecorated(true);//无装饰
        setBackground(new Color(0,0,0,0));//背景透明
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();//屏幕尺寸
        setBounds((screenSize.width-DIALOG_WIDTH)/2,(screenSize.height-DIALOG_HEIGHT)/2,DIALOG_WIDTH,DIALOG_HEIGHT-120);//设置窗口大小
        panel = new JPanel();
        panel.setPreferredSize(new Dimension(DIALOG_WIDTH,DIALOG_HEIGHT));//面板的大小
        panel.setBounds(0,0,DIALOG_WIDTH,DIALOG_HEIGHT);//面板的位置
        panel.setOpaque(false);//设为透明面板
        panel.setLayout(null);//绝对布局
        //添加提示信息
        panel.add(new TLabel(10,10,DIALOG_WIDTH-20,DIALOG_HEIGHT/2,query,ConstructUtil.getSpecificFont(35,ConstructUtil.ANATEVKA)));
        this.buttons = new TButton[buttonTexts.length];
        //添加按钮
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new TButton(
                    (i+1)*GAP-5+(BUTTON_WIDTH*28/55+10)*i,
                    (DIALOG_HEIGHT/2+GAP+10),
                    BUTTON_WIDTH*28/55 -20,BUTTON_HEIGHT ,
                    buttonTexts[i], ConstructUtil.getSpecificFont(BUTTON_HEIGHT * 2 / 5, ConstructUtil.CAI978)
            );
            panel.add(buttons[i]);
        }
        //默认选中第一个
        selected_index = 0;
        pre_button_index = 0;
        this.buttons[0].setOnFocus(true);
        //添加通过上下键切换按钮的键盘监听器
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_LEFT){//向上移动光标
                    buttons[pre_button_index].setOnFocus(false);//移除上个按钮的焦点
                    selected_index = (selected_index+(buttons.length-1))%buttons.length;//+N后再取模放防止下标为负
                    buttons[selected_index].setOnFocus(true);//当前按钮获得焦点
                    pre_button_index = selected_index;//更新上个按钮的下标
                    repaint();//刷新面板
                }
                if(e.getKeyCode()==KeyEvent.VK_RIGHT){
                    buttons[pre_button_index].setOnFocus(false);
                    selected_index = (selected_index+1)%buttons.length;
                    buttons[selected_index].setOnFocus(true);
                    pre_button_index = selected_index;
                    repaint();
                }
            }
        });
        add(panel);//将面板添加到窗口
        addKeyListener(panel.getKeyListeners()[0]);//添加监听器
    }
    //设置按钮的响应事件
    public void setActions(Action[] actions){
        for (int i = 0; i < actions.length; i++) {
            buttons[i].setAction(actions[i]);
            panel.addKeyListener(buttons[i].getKeyListeners()[0]);
            addKeyListener(buttons[i].getKeyListeners()[0]);
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        //绘制光标
        g.setColor(Color.WHITE);
        g.fillRect(buttons[selected_index].getX()-7, buttons[selected_index].getY()-7, 20, 4);
        g.fillRect(buttons[selected_index].getX()-7, buttons[selected_index].getY()-7, 4, 20);
        g.fillRect(buttons[selected_index].getX()+buttons[selected_index].getWidth()+7-20, buttons[selected_index].getY()-7, 20, 4);
        g.fillRect(buttons[selected_index].getX()+buttons[selected_index].getWidth()+7-4, buttons[selected_index].getY()-7, 4,20);
        g.fillRect(buttons[selected_index].getX()-7, buttons[selected_index].getY()+buttons[selected_index].getHeight()+7-20, 4, 20);
        g.fillRect(buttons[selected_index].getX()-7, buttons[selected_index].getY()+buttons[selected_index].getHeight()+7-4, 20, 4);
        g.fillRect(buttons[selected_index].getX()+buttons[selected_index].getWidth()+7-4, buttons[selected_index].getY()+buttons[selected_index].getHeight()+7-20, 4, 20);
        g.fillRect(buttons[selected_index].getX()+buttons[selected_index].getWidth()+7-20, buttons[selected_index].getY()+buttons[selected_index].getHeight()+7-4, 20,4);
    }
}
