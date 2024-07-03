package Tetriss;


import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.Arrays;

public class Configuration {//Tetris的配置类
    private Configuration(){}

    public static final String RESOURCES_PATH = "src/main/resources/";//资源路径
    public static final int ROWS = 20;//可见的行数
    public static final int COLS = 10;//可见的列数
    public static final int ROWS_OFFSET = 3;//行偏移量
    public static final int COLS_OFFSET = 1;//列偏移量
    public static final int BLOCK_MAX_LENGTH = 4;//方块最大长度
    public static final int BLOCK_MAX_HEIGHT = 2;//当方块长度最大时的最大宽度

    //各面板的最佳视角效果比例
    private static final double NEXT_BLOCK_PANEL_RATIO = 147.0/120;
    private static final double HOLD_BLOCK_PANEL_RATIO = 147.0/120;
    private static final double LEVEL_PANEL_RATIO = 295.0/292;
    private static final double SCORE_PANEL_RATIO = 147.0/65;
    private static final double MAINWINDOW_RATIO = 1787.0/1125;

    public static final int BLOCK_SIZE = 20;//每个方格的大小（像素）
    public static final int BORDER_WIDTH = 3;//边框宽度
    public static final int OUTLINE_WIDTH = 2;//提示框线条的宽度

    //各个面板的尺寸
    public static final int NEXT_BLOCK_PANEL_WIDTH = 120;
    public static final int NEXT_BLOCK_PANEL_HEIGHT = (int) (NEXT_BLOCK_PANEL_WIDTH / NEXT_BLOCK_PANEL_RATIO + 0.5);
    public static final int HOLD_BLOCK_PANEL_WIDTH = 120;
    public static final int HOLD_BLOCK_PANEL_HEIGHT = (int) (HOLD_BLOCK_PANEL_WIDTH / HOLD_BLOCK_PANEL_RATIO + 0.5);
    public static final int LEVEL_PANEL_WIDTH = 120;
    public static final int LEVEL_PANEL_HEIGHT = (int) (LEVEL_PANEL_WIDTH / LEVEL_PANEL_RATIO + 0.5);
    public static final int SCORE_PANEL_WIDTH = 120;
    public static final int SCORE_PANEL_HEIGHT = (int) (SCORE_PANEL_WIDTH / SCORE_PANEL_RATIO + 0.5);
    public static final int MAINWINDOW_WIDTH = 1000;
    public static final int MAINWINDOW_HEIGHT = (int) (MAINWINDOW_WIDTH / MAINWINDOW_RATIO + 0.5);


    public static final int PANEL_HORIZONTAL_GAP = 10;//面板间的水平间隙
    public static final int TETRIS_PANEL_WIDTH = COLS*BLOCK_SIZE+BORDER_WIDTH*2+PANEL_HORIZONTAL_GAP+NEXT_BLOCK_PANEL_WIDTH;//主面板的宽度
    public static final int TETRIS_PANEL_HEIGHT = ROWS*BLOCK_SIZE+BORDER_WIDTH*2;//主面板的高度
    public static final int PANEL_VERTICAL_GAP = (int)((TETRIS_PANEL_HEIGHT-NEXT_BLOCK_PANEL_HEIGHT-HOLD_BLOCK_PANEL_HEIGHT-LEVEL_PANEL_HEIGHT-SCORE_PANEL_HEIGHT)/3.0);//面板间的垂直间隙




    public static int[] freqs = {1,1,1,1,1,1,1};//I L J Z S O T 的频数
    public static int freq_sum = Arrays.stream(freqs).sum();//总频数

    public static int getFreqs(int i) {
        return freqs[i];
    }

    public static void setFreqs(int i,int freq) {
        freqs[i] = freq;
    }

    private static int[] player_keySet= {//玩家的键位设置
                    //P1
                    KeyEvent.VK_A,
                    KeyEvent.VK_D,
                    KeyEvent.VK_S,
                    KeyEvent.VK_W,
                    KeyEvent.VK_1,
                    KeyEvent.VK_SPACE,
                    KeyEvent.VK_Q,
                    //P2
                    KeyEvent.VK_LEFT,
                    KeyEvent.VK_RIGHT,
                    KeyEvent.VK_DOWN,
                    KeyEvent.VK_2,
                    KeyEvent.VK_UP,
                    KeyEvent.VK_ENTER,
                    KeyEvent.VK_CONTROL

    };
    public static void setPlayerKey(int player_code, Movement movement, int keyCode){
        player_keySet[(player_code-1)* Movement.values().length+movement.ordinal()] = keyCode;
    }
    public static int getPlayerKey(int player_code, Movement movement){
        return  player_keySet[(player_code-1)* Movement.values().length+movement.ordinal()];
    }

    private static Point[][] blocksPanelCoordinate= {//各类型方块在提示面板中的坐标
            {
                    new Point(20,51),
                    new Point(40,51),
                    new Point(60,51),
                    new Point(80,51)
            },
            {
                    new Point(70,41),
                    new Point(30,61),
                    new Point(50,61),
                    new Point(70,61)
            },
            {
                    new Point(30,41),
                    new Point(30,61),
                    new Point(50,61),
                    new Point(70,61)
            },
            {
                    new Point(30,41),
                    new Point(50,41),
                    new Point(50,61),
                    new Point(70,61)
            },
            {
                    new Point(50,41),
                    new Point(70,41),
                    new Point(30,61),
                    new Point(50,61)
            },
            {
                    new Point(40,41),
                    new Point(60,41),
                    new Point(40,61),
                    new Point(60,61)
            },
            {
                    new Point(50,41),
                    new Point(30,61),
                    new Point(50,61),
                    new Point(70,61)
            },
    };
    public static Point[] getBlockPanelCoordinate(BlockType type){
        if(type!= BlockType.NOBLOCK){
            return blocksPanelCoordinate[type.ordinal()-1];
        }
        return null ;
    }

    public static boolean hasSound = true;//是否开启音效

    public static void saveConfiguration() throws IOException {//将配置保存进文件
        try {
            FileOutputStream fileOut = new FileOutputStream(RESOURCES_PATH + "config.bin");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(player_keySet);
            out.writeObject(freqs);
            out.writeObject(hasSound);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }
    public static void loadConfiguration(){//从文件中载入配置
        try {
            FileInputStream fileIn = new FileInputStream(RESOURCES_PATH + "config.bin");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            player_keySet = (int[]) in.readObject();
            freqs = (int[]) in.readObject();
            hasSound = (boolean)in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException c) {
            System.out.println("Class not found");
            c.printStackTrace();
        }

    }
}
