package Window;


import Components.Action;
import Tetriss.BlockType;
import Tetriss.SpecifiedTetris;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;

import static Tetriss.Configuration.TETRIS_PANEL_HEIGHT;
import static Tetriss.Configuration.TETRIS_PANEL_WIDTH;
public class OffLinePVPWindow {
    private JFrame window;
    private SpecifiedTetris tetris1;
    private SpecifiedTetris tetris2;
    OffLinePVPWindow(){
        window = new JFrame("Tetris : OFFLINE PVP");
        //获取方块序列并初始化玩家1和玩家2的游戏面板
        ArrayList<BlockType> arr =  ConstructUtil.getRandomBlocks();
        tetris1 = new SpecifiedTetris(1,arr);
        tetris2 = new SpecifiedTetris(2,arr);
        BackgroundPanel backgroundPanel =  new BackgroundPanel(702,430);//添加背景图片
        backgroundPanel.setLayout(null);//设为绝对布局
        //将玩家游戏面板添加到背景图片面板上
        backgroundPanel.add(tetris1.getTetrisPanel());
        backgroundPanel.add(tetris2.getTetrisPanel());
        //将背景图片面板添加到窗口中
        window.add(backgroundPanel);
        //设置面板尺寸
        tetris1.getTetrisPanel().setBounds(10,10,TETRIS_PANEL_WIDTH+10,TETRIS_PANEL_HEIGHT+10);
        tetris2.getTetrisPanel().setBounds(TETRIS_PANEL_WIDTH+20,10,TETRIS_PANEL_WIDTH+10,TETRIS_PANEL_HEIGHT+10);
        window.pack();
        //设置窗口居中显示
        window.setLocation(
                (Toolkit.getDefaultToolkit().getScreenSize().width - window.getWidth()) / 2,
                (Toolkit.getDefaultToolkit().getScreenSize().height - window.getHeight()) / 2
        );
        //固定窗口大小
        window.setResizable(false);
        //添加游戏面板键盘监听器
        window.addKeyListener(tetris1.getKeyListener());
        window.addKeyListener(tetris2.getKeyListener());

        //添加Esc暂停功能
        final boolean[] isPaused = {false};
        window.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_ESCAPE){
                    if(!isPaused[0]){
                        tetris1.pauseGame();
                        tetris2.pauseGame();
                        window.removeKeyListener(tetris1.getKeyListener());
                        window.removeKeyListener(tetris2.getKeyListener());
                        isPaused[0] = true;
                    }else{
                        tetris1.continueGame();
                        tetris2.continueGame();
                        window.addKeyListener(tetris1.getKeyListener());
                        window.addKeyListener(tetris2.getKeyListener());
                        isPaused[0] = false;
                    }
                }
            }
        });
    }
    //开始游戏
    public void start(){
        window.setVisible(true);
        tetris1.start();
        tetris2.start();
    }
    //设置窗口关闭时的行为
    public void setWindowCloseAction(Action action){
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    action.action();
                    window.dispose();
                    tetris1=null;
                    tetris2=null;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }
}
