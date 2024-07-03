package Window;

import Components.Action;
import Components.TButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class TMenu extends JPanel {
    private final int GAP = 15;//按钮间隔
    //各组件尺寸
    private final int BUTTON_HEIGHT=44;
    private final int BUTTON_WIDTH = 307;
    private final int MENU_WIDTH = 337;
    private final int MENU_HEIGHT = 192;
    //是否无修饰
    private boolean isUndecorated;
    private int selected_index;//当前被选中的按钮的下标
    protected TButton[] buttons;//按钮
    private int pre_button_index;//上一个被选中的按钮的下标
    public TMenu(int x, int y,boolean isUndecorated, int default_selected, String[] buttonTexts){
        setPreferredSize(new Dimension(MENU_WIDTH,MENU_HEIGHT));//尺寸
        setBounds(x,y,MENU_WIDTH,MENU_HEIGHT);//尺寸和位置
        this.isUndecorated = isUndecorated;
        setOpaque(false);//透明
        setLayout(null);//绝对布局
        this.selected_index = default_selected;
        this.buttons = new TButton[buttonTexts.length];
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new TButton(GAP, i*BUTTON_HEIGHT+(i+1)*GAP, BUTTON_WIDTH,BUTTON_HEIGHT ,
                    buttonTexts[i], ConstructUtil.getSpecificFont(BUTTON_HEIGHT * 2 / 3, ConstructUtil.CAI978)
            );
        }
        //设置默认选中的按钮
        this.buttons[default_selected].setOnFocus(true);
        pre_button_index = default_selected;
        //添加按钮
        for (TButton tb:buttons) {
            add(tb);
        }
        //添加监听器
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_UP){
                    buttons[pre_button_index].setOnFocus(false);
                    selected_index = (selected_index+(buttons.length-1))%buttons.length;
                    buttons[selected_index].setOnFocus(true);
                    pre_button_index = selected_index;
                    repaint();

                }
                if(e.getKeyCode()==KeyEvent.VK_DOWN){
                    buttons[pre_button_index].setOnFocus(false);
                    selected_index = (selected_index+1)%buttons.length;
                    buttons[selected_index].setOnFocus(true);
                    pre_button_index = selected_index;
                    repaint();
                }
            }
        });
    }

    public void setActions(Action[] actions){//设置按钮的点响应事件
        for (int i = 0; i < actions.length; i++) {
            buttons[i].setAction(actions[i]);
            addKeyListener(buttons[i].getKeyListeners()[0]);
        }
    }
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        BasicStroke basicStroke = new BasicStroke(10);
        Graphics2D g2 = (Graphics2D)g;
        g2.setStroke(basicStroke);
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

    public void addAllKeyListeners(Component comp){//添加组件时添加其所有键盘监听器
        for(KeyListener kl: comp.getKeyListeners()){
            addKeyListener(kl);
        }
    }
}
