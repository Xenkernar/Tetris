package Window;


import Components.TEditField;
import Components.TLabel;
import Components.TSwitch;
import Tetriss.BlockType;
import Tetriss.Configuration;
import Tetriss.Movement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


public class SettingWindow extends JFrame {

    //玩家键位设置面板
    private class SetPlayerKeyMapPanel extends JPanel{
        //键位采集器
        private class KeyCollector extends TLabel{
            //键盘编码和按键名称的键值对
            public static HashMap<Integer,String> keyNameMap;
            static {
                keyNameMap = new HashMap<>();
                Field[] fields = KeyEvent.class.getFields();//用反射获取KeyEvent的所有字段
                for (Field field : fields) {
                    //筛选所有按键编码常量字段
                    if (Modifier.isStatic(field.getModifiers()) && field.getType() == int.class && field.getName().startsWith("VK_")) {
                        try {
                            //将按键编码和字段名称（除去"VK_"）作为键值对插入keyNameMap
                            keyNameMap.put(field.getInt(null), field.getName().substring(3));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            private boolean onFocus;//是否获取焦点
            private int maskOpacity;//透明度
            private int sign;//透明度的增量（1或-1）
            private Timer timer;//动画定时器
            KeyCollector(int x, int y, String keyName, int player_code, Movement movement){
                super(x,y,150,40,keyName,ConstructUtil.getSpecificFont(17,ConstructUtil.CAI978));
                onFocus = false;//默认不获取焦点
                maskOpacity = 0;//默认不透明
                sign = 1;//默认增加透明度

                addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if(onFocus){//在获取焦点时捕获键盘按键
                            //提示用户当前捕获了哪个按键
                            setContent(keyNameMap.get(e.getKeyCode()));
                            //停止播放动画
                            ceaseAnimation();
                            //修改配置类中的键位设置
                            Configuration.setPlayerKey(player_code,movement,e.getKeyCode());
                            //移除焦点
                            onFocus = false;
                            //刷新画面
                            repaint();
                        }
                    }
                });
            }

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                if(onFocus){
                    g.setColor(new Color(0,255,255,maskOpacity));
                    g.fillRect(5,0,140,40);
                }
            }
            //播放动画，匀速修改透明度（到达边界往相反方向修改）
            public void displayAnimation(){
                timer =new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if(maskOpacity>=255){
                            sign=-1;
                        }
                        if(maskOpacity<=0){
                            sign = 1;
                        }
                        maskOpacity+=sign;
                        repaint();
                    }
                },0,2);
            }
            //停止播放动画
            public void ceaseAnimation(){
                timer.cancel();
            }
        }
        private KeyCollector[] keyCollectors;
        SetPlayerKeyMapPanel(int x,int y,int player_code){
            setPreferredSize(new Dimension(360,415));
            setBounds(x,y,360,415);
            setLayout(null);
            setOpaque(false);
            //添加面板标题
            add(new TLabel(0,0,360,50,player_code+" Player Key",ConstructUtil.getSpecificFont(26,ConstructUtil.CAI978)));
            //各采集器对应的键位功能（左移、右移、加速下降、顺时针旋转、逆时针旋转、直接到底、暂存方块）
            String[] operates={"Move to left","Move to right","Accelerated fall","Clockwise rotate","Anticlockwise rotate","Move to bottom","Hold Block"};
            keyCollectors=new KeyCollector[operates.length];
            //创建键位采集器并添加到指定位置
            for (int i = 0; i < operates.length; i++) {
                add(new TLabel(10,75+50*i,180,40,operates[i],ConstructUtil.getSpecificFont(28,ConstructUtil.ANATEVKA)));
                keyCollectors[i] =new KeyCollector(200,75+50*i,
                        KeyCollector.keyNameMap.get(Configuration.getPlayerKey(player_code, Movement.values()[i])),
                        player_code,
                        Movement.values()[i]);
                add(keyCollectors[i]);
            }
            //添加鼠标监听器
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    for (int i = 0; i < keyCollectors.length; i++) {
                        if(200<=e.getX()&&e.getX()<=350&&(75+50*i)<=e.getY()&&e.getY()<=(115+50*i)&&e.getButton()==MouseEvent.BUTTON1&&!keyCollectors[i].onFocus){
                            keyCollectors[i].onFocus = true;
                            keyCollectors[i].maskOpacity = 0;
                            keyCollectors[i].displayAnimation();
                        }else{
                            if(keyCollectors[i].onFocus){
                                keyCollectors[i].onFocus = false;
                                keyCollectors[i].maskOpacity = 0;
                                keyCollectors[i].ceaseAnimation();
                                keyCollectors[i].repaint();
                            }

                        }
                    }
                }
            });
        }

        @Override
        public Component add(Component comp) {
            for (int i = 0; i < comp.getKeyListeners().length; i++) {
                addKeyListener(comp.getKeyListeners()[i]);
            }
            for (int i = 0; i < comp.getMouseListeners().length; i++) {
                addMouseListener(comp.getMouseListeners()[i]);
            }
            return super.add(comp);
        }
    }
    private class FreqencySetPanel extends JPanel{
        private TEditField[] tEditFields;//方块频数输入框
        private int selected_index;//默认选中的输入框下标
        FreqencySetPanel(int x,int y){
            //设置外观
            setPreferredSize(new Dimension(740,160));
            setBounds(x,y,740,160);
            setLayout(null);
            setOpaque(false);
            tEditFields = new TEditField[7];
            selected_index = -1;
            //添加面板标题
            add(new TLabel(0,0,740,60,"Frequency of Blocks\n(Single Play Only)",ConstructUtil.getSpecificFont(26,ConstructUtil.CAI978)));
            //添加输入框及其监听器
            for (int i = 0; i < 7; i++) {
                add(new TLabel(11+104*i,80,94,30, BlockType.values()[i+1].name(),ConstructUtil.getSpecificFont(26,ConstructUtil.CAI978)));
                tEditFields[i] = new TEditField(11 + 104 * i, 120, 94, 30, Integer.toString(Configuration.getFreqs(i)), ConstructUtil.getSpecificFont(26, ConstructUtil.CAI978));
                add(tEditFields[i]);
            }
            //添加鼠标监听器
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    boolean hasSelected = false;//默认无选中的输入框
                    for (int i = 0; i < tEditFields.length; i++) {//如果鼠标落在某个输入框内
                        if((11+i*104)<=e.getX()&&e.getX()<=(105+i*104)&&120<=e.getY()&&e.getY()<=150&&e.getButton()==MouseEvent.BUTTON1&&!tEditFields[i].isOnFocus()){
                            tEditFields[i].setOnFocus(true);//让其获取焦点
                            selected_index = i;//获取其下标
                            hasSelected = true;//修改选中状态
                            repaint();
                        }else{//其他的输入框移除焦点
                            if(tEditFields[i].isOnFocus()){
                                tEditFields[i].setOnFocus(false);
                            }
                        }
                    }
                    if(!hasSelected){//如果未选中任何输入框（鼠标移出了输入框）
                        selected_index = -1;//修改下标
                        for (int i = 0; i < 7; i++) {
                            //将合法的频数录入配置类
                            if(!tEditFields[i].getContent().equals("")&&!tEditFields[i].getContent().contains(new String("."))){
                                Configuration.setFreqs(i,Integer.parseInt(tEditFields[i].getContent()));
                            }
                        }
                        repaint();
                    }
                }
            });
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            Graphics2D g2 = (Graphics2D)g;
            g2.setStroke(new BasicStroke(4));
            g2.setColor(Color.CYAN);
            g2.drawRect(2,72,736,87);
            //如果有选中的输入框则为其绘制提示框
            if(selected_index!=-1){
                g2.setStroke(new BasicStroke(4));
                g.setColor(Color.WHITE);
                g.drawRect(tEditFields[selected_index].getX()-3,tEditFields[selected_index].getY()-3,tEditFields[selected_index].getWidth()+6,tEditFields[selected_index].getHeight()+6);
            }
        }

        @Override
        public Component add(Component comp) {
            for (int i = 0; i < comp.getKeyListeners().length; i++) {
                addKeyListener(comp.getKeyListeners()[i]);
            }
            for (int i = 0; i < comp.getMouseListeners().length; i++) {
                addMouseListener(comp.getMouseListeners()[i]);
            }
            return super.add(comp);
        }
    }
    private class SoundSwitchPanel extends JPanel{
        SoundSwitchPanel(int x,int y){
            //设置外观
            setPreferredSize(new Dimension(400,90));
            setBounds(x,y,400,90);
            setLayout(null);
            setOpaque(false);
            //添加标题和提示文本
            add(new TLabel(0,0,400,40,"SOUND EFFECT",ConstructUtil.getSpecificFont(26,ConstructUtil.CAI978)));
            add(new TLabel(0,50,120,40,"OFF",ConstructUtil.getSpecificFont(20,ConstructUtil.CAI978)));
            add(new TLabel(280,50,120,40,"ON",ConstructUtil.getSpecificFont(20,ConstructUtil.CAI978)));
            //创建开关
            TSwitch tSwitch = new TSwitch(130, 50, 140, 40);
            //根据hasSound设置开关的默认状态
            tSwitch.setOn(Configuration.hasSound);
            //添加鼠标监听器，点击时切换状态（如果当前允许切换）
            tSwitch.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    Configuration.hasSound = tSwitch.isOn();
                }
            });
            //添加开关
            add(tSwitch);
        }
        @Override
        public Component add(Component comp) {
            for (int i = 0; i < comp.getKeyListeners().length; i++) {
                addKeyListener(comp.getKeyListeners()[i]);
            }
            for (int i = 0; i < comp.getMouseListeners().length; i++) {
                addMouseListener(comp.getMouseListeners()[i]);
            }
            return super.add(comp);
        }
    }
    //设置面板尺寸
    private final int WINDOW_WIDTH = 800;
    private final int WINDOW_HEIGHT= 800;
    SettingWindow(){
        //设置尺寸
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-WINDOW_WIDTH)/2,(screenSize.height-WINDOW_HEIGHT)/2,WINDOW_WIDTH,WINDOW_HEIGHT);
        setUndecorated(true);
        //添加背景面板
        BackgroundPanel backgroundPanel =  new BackgroundPanel(WINDOW_WIDTH,WINDOW_HEIGHT);
        backgroundPanel.setLayout(null);
        //创建面板并添加到背景面板
        backgroundPanel.add(new SetPlayerKeyMapPanel(30,30,1));
        backgroundPanel.add(new SetPlayerKeyMapPanel(410,30,2));
        backgroundPanel.add(new FreqencySetPanel(30,480));
        backgroundPanel.add(new SoundSwitchPanel(30,670));

        //创建保存和退出标签并添加键盘监听器（ESC退出（不保存），F10保存并退出）
        TLabel save = new TLabel(500, 670, 270, 40, "Press F10 SAVE", ConstructUtil.getSpecificFont(16, ConstructUtil.CAI978));
        TLabel exit = new TLabel(500, 720, 270, 40, "Press ESC EXIT", ConstructUtil.getSpecificFont(16, ConstructUtil.CAI978));
        backgroundPanel.add(save);
        backgroundPanel.add(exit);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_ESCAPE){
                    dispose();
                }
                if(e.getKeyCode()==KeyEvent.VK_F10){
                    try {
                        Configuration.saveConfiguration();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    dispose();
                }
            }
        });
        //将背景面板添加到窗口
        add(backgroundPanel);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setStroke(new BasicStroke(10));
        g2.setColor(new Color(9, 145, 244, 100));
        g2.drawRect(5,5,WINDOW_WIDTH-10,WINDOW_HEIGHT-10);
        g2.setColor(Color.CYAN);
        g2.setStroke(new BasicStroke(4));
        g2.drawRect(0,0,WINDOW_WIDTH,WINDOW_HEIGHT);
        g2.drawRect(32,97,355,355);
        g2.drawRect(412,97,355,355);
    }
    @Override
    public Component add(Component comp) {
        for (int i = 0; i < comp.getKeyListeners().length; i++) {
            addKeyListener(comp.getKeyListeners()[i]);
        }
        for (int i = 0; i < comp.getMouseListeners().length; i++) {
            addMouseListener(comp.getMouseListeners()[i]);
        }
        return super.add(comp);
    }

}
