package Window;

import Components.Action;
import Tetriss.AudioPlayer;
import Tetriss.BlockType;
import Tetriss.RandomTetris;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;

import static Tetriss.Configuration.TETRIS_PANEL_HEIGHT;
import static Tetriss.Configuration.TETRIS_PANEL_WIDTH;

public class SinglePlayWindow  {
    private JFrame window;
    private RandomTetris tetris;
    SinglePlayWindow(){
        window = new JFrame("Tetris : Single Player");
        tetris = new RandomTetris(1,true);//生成分布不均匀的随即方块序列
        BackgroundPanel backgroundPanel =  new BackgroundPanel(360,430);//添加背景图片
        backgroundPanel.setLayout(null);//设为绝对布局
        //将玩家游戏面板添加到背景图片面板上
        backgroundPanel.add(tetris.getTetrisPanel());
        //将背景图片面板添加到窗口中
        window.add(backgroundPanel);
        //设置游戏面板尺寸
        tetris.getTetrisPanel().setBounds(10,10,TETRIS_PANEL_WIDTH+10,TETRIS_PANEL_HEIGHT+10);
        window.pack();
        //设置窗口居中
        window.setLocation(
                (Toolkit.getDefaultToolkit().getScreenSize().width - window.getWidth()) / 2,
                (Toolkit.getDefaultToolkit().getScreenSize().height - window.getHeight()) / 2
        );
        //固定窗口大小
        window.setResizable(false);
        //添加游戏面板键盘监听器
        window.addKeyListener(tetris.getKeyListener());

        //添加Esc暂停功能
        final boolean[] isPaused = {false};
        window.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_ESCAPE){
                    if(!isPaused[0]){
                        tetris.pauseGame();
                        window.removeKeyListener(tetris.getKeyListener());
                        isPaused[0] = true;
                    }else{
                        tetris.continueGame();
                        window.addKeyListener(tetris.getKeyListener());
                        isPaused[0] = false;
                    }
                }
            }
        });
    }
    //开始游戏
    public void start(){
        window.setVisible(true);
        tetris.start();
    }
    //设置窗口关闭时的行为
    public void setWindowCloseAction(Action action){
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    action.action();
                    window.dispose();
                    tetris=null;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }
}
