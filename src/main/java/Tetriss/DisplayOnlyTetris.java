package Tetriss;


import java.awt.*;
import java.io.IOException;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

import static Tetriss.Configuration.*;
public class DisplayOnlyTetris extends BaseTetris{
    private byte[] progress;//接收游戏进度的字节数组
    private DatagramSocket datagramSocket;//Socket对象
    private DatagramPacket datagramPacket;//接收到的数据包
    public DisplayOnlyTetris(int port) throws SocketException {
        progress = new byte[512];
        datagramSocket = new DatagramSocket(port);
        datagramPacket = new DatagramPacket(progress,progress.length);
    }
    public void receiveProgress() throws IOException {
        datagramSocket.receive(datagramPacket);//接收到数据包
        //解析数据包并更新面板中的内容
        parseProgress();
        eliminatePanel.repaint();
        nextBlockPanel.repaint();
        holdBlockPanel.repaint();
        levelPanel.repaint();
        scorePanel.repaint();
    }
    private void parseProgress(){
        int byteNums=0;//字符数组的下标
        for (int i = 0; i < ROWS+ROWS_OFFSET+1; i++) {//方块矩阵
            for (int j = 0; j < COLS+COLS_OFFSET+1; j++) {
                blocksMat[i][j] = BlockType.values()[progress[byteNums++]];
            }
        }
        for (int i = 0; i < BLOCK_MAX_LENGTH; i++) {//移动中的方块
            for (int j = 0; j < BLOCK_MAX_HEIGHT; j++) {
                movingBlockCoordinate[i][j] = progress[byteNums++];
            }
        }
        placementCoordinate.clear();//清空落地坐标
        for (int i = 0; i < 4; i++) {//更新落地坐标
            placementCoordinate.add(new Point(progress[byteNums++],progress[byteNums++]));
        }
        //各种方块类型
        movingBlockType= BlockType.values()[progress[byteNums++]];
        nextBlockType= BlockType.values()[progress[byteNums++]];
        holdBlockType= BlockType.values()[progress[byteNums++]];
        byte len = progress[byteNums++];//分数的长度
        score = Integer.parseInt(new String(progress,byteNums,len));//获取分数
        byteNums+=len;
        level=progress[byteNums++];//等级
        eliminatedLines=progress[byteNums++];//消除的行数
        gameOver = progress[byteNums] == 1;//游戏结束状态
    }
    public void reveiveGameProgress(){//不断接收数据包并更新游戏进度
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    receiveProgress();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 0, 1);
    }

}
