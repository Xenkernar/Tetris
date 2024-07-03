package Components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;

public class TSwitch extends JPanel {
    private boolean on;//开关状态
    private boolean changeable;//当前是否可以切换开关状态
    private int LeftTopX;//渐变方块左上角当前的x轴坐标
    private int opacity;//渐变方块的不透明度
    public TSwitch(int x, int y, int width, int height){
        setPreferredSize(new Dimension(width,height));
        setOpaque(true);
        setBounds(x,y,width,height);
        on =false;
        changeable=true;
        LeftTopX = 5;
        opacity = 50;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {//在开关显示区域内点击开关时执行turn
                if(e.getButton()==MouseEvent.BUTTON1&& 0<=e.getX()&&e.getX()<=width&&0<=e.getY()&&e.getY()<=height){
                    turn();
                }
            }
        });
    }
    @Override
    public int getWidth() {
        return getPreferredSize().width;
    }
    @Override
    public int getHeight() {
        return getPreferredSize().height;
    }

    @Override
    public void paint(Graphics g) {
        setBackground(new Color(9, 145, 244, 128));//背景
        g.clearRect(0,0,getWidth(),getHeight());//每次刷新清除上次绘制的图案
        super.paint(g);
        g.setColor(new Color(79, 83, 247));
        g.fillRect(0,0,5,getHeight());//左边框
        g.fillRect(getWidth()-5,0,5,getHeight());//右边框
        g.setColor(new Color(0, 255, 255, opacity));//渐变方块当前的颜色
        g.fillRoundRect(LeftTopX,0,(getWidth()-10)/2,getHeight(),5,5);//绘制渐变方块

    }

    public boolean isOn() {
        return on;
    }
    public void setOn(boolean on){
        this.on = on;
        if(!on){
            LeftTopX = 5;
            opacity = 50;
        }else{
            LeftTopX = getWidth() / 2;
            opacity = 255;
        }
    }
    public void turn() {
        if(changeable){//如果当前可以改变开关状态
            try {
                displayAnimation();//播放动画
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            this.on = !on;//切换开关
        }
    }
    private void displayAnimation() throws InterruptedException {
            if(!on){//若开关由 关->开
                Timer timer = new java.util.Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        changeable = false;//播放动画时无法切换开关
                        LeftTopX++;//方块右移
                        if (opacity < 255) {
                            opacity += 3;//透明度递增
                        }
                        TSwitch.super.repaint();//刷新开关
                        if (LeftTopX >= getWidth() / 2) {//到达中点停止
                            timer.cancel();
                            changeable =true;
                        }
                    }
                },0,5);
            }else {//若开关由 开->关
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        changeable = false;
                        LeftTopX--;//方块左移
                        if(opacity>50){
                            opacity-=3;//透明度递减
                        }
                        TSwitch.super.repaint();
                        if(LeftTopX<=5){//到达起点停止
                            timer.cancel();
                            changeable = true;
                        }
                    }
                },0,5);
            }

    }
}
