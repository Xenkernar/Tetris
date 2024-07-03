package Tetriss;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;

import static Tetriss.Configuration.*;
import static Tetriss.Configuration.BLOCK_MAX_HEIGHT;
public class SpecifiedTetris extends OperableTetris{
    private ArrayList<BlockType> blocks;//自己的方块序列
    private ArrayList<BlockType> opponentBlocks;//对手的方块序列
    private Iterator<BlockType> iterator;//自己的方块序列的迭代器
    private final byte[] progress;//游戏进度包
    public SpecifiedTetris(int identifier, ArrayList<BlockType> blocks){
        super(identifier);
        this.blocks = blocks;
        iterator = blocks.iterator();
        blocksGenerator = ()-> getSequentialBlock();//重新指定父类的方块生成规则
        progress = new byte[512];
    }

    //每次从方块序列中按顺序读取一个方块，当到达序列结尾时重新从第一个开始读取
    private BlockType getSequentialBlock(){
        if(!iterator.hasNext()){
            iterator = blocks.iterator();
        }
        return iterator.next();
    }

    //发送自己的方块序列
    public void sendBlocks(DataOutputStream outTo) throws IOException {
        byte[] bytes = new byte[blocks.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte)(blocks.get(i).ordinal());
        }
        outTo.write(bytes);
    }

    //接收对手的方块序列
    public void receiveBlocks(BufferedReader inFrom) throws IOException {
        int b;
        opponentBlocks = new ArrayList<>();
        while ((b=inFrom.read())!=-1){
            opponentBlocks.add(BlockType.values()[b]);
        }
    }

    //将自己和对手的方块序列中的每一个方块加和取模
    public void mixBlocks(){
        for (int i = 0; i < blocks.size(); i++) {
            blocks.set(i, BlockType.values()[(blocks.get(i).ordinal()+opponentBlocks.get(i).ordinal()-2)%(BlockType.values().length-1)+1]);
        }
    }

    //发送游戏进度包
    public void sendProgress(String ip,int port) throws IOException {
        DatagramSocket datagramSocket= new DatagramSocket();
        InetAddress address = InetAddress.getByName(ip);
        getLastProgress();
        DatagramPacket datagramPacket = new DatagramPacket(progress,progress.length,address,port);//打包
        datagramSocket.send(datagramPacket);
        datagramSocket.close();
    }

    //更新游戏进度包中的数据
    private void getLastProgress(){
        int byteNums=0;
        for (int i = 0; i < ROWS+ROWS_OFFSET+1; i++) {
            for (int j = 0; j < COLS+COLS_OFFSET+1; j++) {
                progress[byteNums++] =(byte)(blocksMat[i][j].ordinal());
            }
        }
        for (int i = 0; i < BLOCK_MAX_LENGTH; i++) {
            for (int j = 0; j < BLOCK_MAX_HEIGHT; j++) {
                progress[byteNums++] = (byte)movingBlockCoordinate[i][j];
            }
        }
        for(Point p:placementCoordinate){
            progress[byteNums++] = (byte)p.x;
            progress[byteNums++] = (byte)p.y;
        }
        progress[byteNums++] = (byte) (movingBlockType.ordinal());
        progress[byteNums++] = (byte) (nextBlockType.ordinal());
        progress[byteNums++] = (byte) (holdBlockType.ordinal());
        byte[] bytes =  Integer.toString(score).getBytes();
        progress[byteNums++] = (byte)bytes.length;
        for (int i = 0; i < bytes.length; i++) {
            progress[byteNums++] = bytes[i];
        }
        progress[byteNums++] = (byte) level;
        progress[byteNums++] = (byte) eliminatedLines;
        progress[byteNums] = (byte) (gameOver?1:0);
    }

    //不断发送自己的游戏进度包
    public void sendGameProgress(String ip,int port){
        Timer timer = new Timer(1, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    getLastProgress();
                    sendProgress(ip,port);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        timer.start();
    }
}
