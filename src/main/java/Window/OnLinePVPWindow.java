package Window;

import Components.Action;
import Tetriss.BlockType;
import Tetriss.DisplayOnlyTetris;
import Tetriss.SpecifiedTetris;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;

import static Tetriss.Configuration.TETRIS_PANEL_HEIGHT;
import static Tetriss.Configuration.TETRIS_PANEL_WIDTH;

public class OnLinePVPWindow {
    private JFrame window;
    private SpecifiedTetris specifiedTetris;//玩家的游戏面板
    private DisplayOnlyTetris displayOnlyTetris;//对手的展示面板
    private String opponentIP;//对手的IP
    private int opponentPort;//对手用于接收己方游戏进度的端口
    public OnLinePVPWindow(String opponentIP ,int opponentPort,int receiveProgressPort) throws SocketException {
        window = new JFrame("Tetris : ONLINE PVP");
        ArrayList<BlockType> arr =  ConstructUtil.getRandomBlocks();//生成己方的方块序列
        specifiedTetris = new SpecifiedTetris(1,arr);
        displayOnlyTetris = new DisplayOnlyTetris(receiveProgressPort);//用乙方接收游戏进度的端口创建展示面板
        this.opponentPort = opponentPort;
        this.opponentIP = opponentIP;
        BackgroundPanel backgroundPanel =  new BackgroundPanel(702,430);
        backgroundPanel.setLayout(null);//设为绝对布局
        //将游戏面板和展示添加到背景图片面板上
        backgroundPanel.add(specifiedTetris.getTetrisPanel());
        backgroundPanel.add(displayOnlyTetris.getTetrisPanel());
        //将背景图片面板添加到窗口中
        window.add(backgroundPanel);
        //设置面板尺寸
        specifiedTetris.getTetrisPanel().setBounds(10,10,TETRIS_PANEL_WIDTH+10,TETRIS_PANEL_HEIGHT+10);
        displayOnlyTetris.getTetrisPanel().setBounds(TETRIS_PANEL_WIDTH+20,10,TETRIS_PANEL_WIDTH+10,TETRIS_PANEL_HEIGHT+10);
        window.pack();
        //设置窗口居中显示
        window.setLocation(
                (Toolkit.getDefaultToolkit().getScreenSize().width - window.getWidth()) / 2,
                (Toolkit.getDefaultToolkit().getScreenSize().height - window.getHeight()) / 2
        );
        //固定窗口大小
        window.setResizable(false);
        //添加游戏面板键盘监听器
        window.addKeyListener(specifiedTetris.getKeyListener());
    }

    //开始游戏
    public void start() throws IOException {
        window.setVisible(true);
        specifiedTetris.start();
        specifiedTetris.sendGameProgress(opponentIP,opponentPort);
        displayOnlyTetris.reveiveGameProgress();
    }

    public SpecifiedTetris getSpecifiedTetris() {
        return specifiedTetris;
    }

    //设置窗口关闭时的行为
    public void setWindowCloseAction(Action action){
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    action.action();
                    window.dispose();
                    specifiedTetris=null;
                    displayOnlyTetris=null;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }
}
