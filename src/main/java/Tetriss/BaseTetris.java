package Tetriss;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import static Tetriss.Configuration.*;
import static Window.ConstructUtil.*;

class BaseTetris {
    private BufferedImage[] blockImages;//方块的图片
    private BufferedImage[] numberImages;//数字的图片
    private BufferedImage nextBlockPanelImage;//下个方块面板图片
    private BufferedImage scorePanelImage;//分数面板图片
    private BufferedImage holdBlockPanelImage;//持有方块面板图片
    private BufferedImage levelPanelImage;//等级面板图片
    protected JPanel TetrisPanel;//包装所有面板的主面板
    protected BlockType[][] blocksMat;//方块的逻辑矩阵
    protected int[][] movingBlockCoordinate;//移动中的方块的坐标
    protected HashSet<Point> placementCoordinate;//方块落地的坐标
    protected BlockType movingBlockType;//移动中的方块类型
    protected BlockType nextBlockType;//下个方块的类型
    protected BlockType holdBlockType;//持有方块类型
    protected int score;//分数
    protected int level;//等级
    protected int eliminatedLines;//消融的行数
    protected boolean gameOver;//游戏结束标记
    protected EliminatePanel eliminatePanel;//操作方块移动和消融的面板
    protected NextBlockPanel nextBlockPanel;//下个方块面板
    protected ScorePanel scorePanel;//分数面板
    protected HoldBlockPanel holdBlockPanel;//持有方块面板
    protected LevelPanel levelPanel;//等级面板
    protected BaseTetris() {
        blockImages = new BufferedImage[8];
        numberImages = new BufferedImage[10];
        try {//载入图片资源
            blockImages[0] = ImageIO.read( new File(RESOURCES_PATH + "border.jpg"));
            blockImages[1] = ImageIO.read( new File(RESOURCES_PATH + "I.jpg"));
            blockImages[2] = ImageIO.read( new File(RESOURCES_PATH + "L.jpg"));
            blockImages[3] = ImageIO.read( new File(RESOURCES_PATH + "J.jpg"));
            blockImages[4] = ImageIO.read( new File(RESOURCES_PATH + "Z.jpg"));
            blockImages[5] = ImageIO.read( new File(RESOURCES_PATH + "S.jpg"));
            blockImages[6] = ImageIO.read( new File(RESOURCES_PATH + "O.jpg"));
            blockImages[7] = ImageIO.read( new File(RESOURCES_PATH + "T.jpg"));
            nextBlockPanelImage = ImageIO.read( new File(RESOURCES_PATH + "nextBlockPanel.jpg"));
            holdBlockPanelImage = ImageIO.read( new File(RESOURCES_PATH + "holdBlockPanel.png"));
            levelPanelImage = ImageIO.read( new File(RESOURCES_PATH + "levelPanel.jpg"));
            scorePanelImage = ImageIO.read(new File(RESOURCES_PATH + "scorePanel.jpg"));
            for (int i = 0; i < 10; i++) {
                numberImages[i] = ImageIO.read(new File(RESOURCES_PATH + i + ".png"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        blocksMat = new BlockType[ROWS+ROWS_OFFSET+1][COLS+COLS_OFFSET+1];
        for (int i = 0; i < ROWS+ROWS_OFFSET+1; i++) {//初始化逻辑矩阵
            for (int j = 0; j < COLS+COLS_OFFSET+1; j++) {
                if(i==0||j==0||i==ROWS+ROWS_OFFSET||j==COLS+COLS_OFFSET){
                    blocksMat[i][j]= BlockType.I;//边框均为I类型
                }
                else{
                    blocksMat[i][j]= BlockType.NOBLOCK;//其余为空类型
                }
            }
        }
        movingBlockCoordinate = new int[BLOCK_MAX_LENGTH][BLOCK_MAX_HEIGHT];
        placementCoordinate = new HashSet<>();
        movingBlockType = BlockType.I;
        nextBlockType = BlockType.I;
        holdBlockType = BlockType.NOBLOCK;
        score = 0;
        level = 1;
        eliminatedLines = 0;
        gameOver = false;
        eliminatePanel = new EliminatePanel();
        nextBlockPanel = new NextBlockPanel();
        scorePanel = new ScorePanel();
        holdBlockPanel = new HoldBlockPanel();
        levelPanel = new LevelPanel();
        TetrisPanel = new JPanel();
        TetrisPanel.setPreferredSize(new Dimension(TETRIS_PANEL_WIDTH,TETRIS_PANEL_HEIGHT));
        TetrisPanel.setLayout(null);
        TetrisPanel.add(eliminatePanel);
        TetrisPanel.add(nextBlockPanel);
        TetrisPanel.add(holdBlockPanel);
        TetrisPanel.add(levelPanel);
        TetrisPanel.add(scorePanel);
        TetrisPanel.setBounds(0,0,TETRIS_PANEL_WIDTH,TETRIS_PANEL_HEIGHT);
        TetrisPanel.setOpaque(false);
    }
    protected class EliminatePanel extends JPanel {
        EliminatePanel(){
            setPreferredSize(new Dimension(BLOCK_SIZE*COLS+BORDER_WIDTH*2,BLOCK_SIZE*ROWS+BORDER_WIDTH*2));
            setBounds(0,0,BLOCK_SIZE*COLS+BORDER_WIDTH*2,BLOCK_SIZE*ROWS+BORDER_WIDTH*2);
        }
        @Override
        public void paint(Graphics g) {
            BasicStroke basicStroke = new BasicStroke(OUTLINE_WIDTH);
            Graphics2D g2 = (Graphics2D)g;
            g2.setStroke(basicStroke);
            g2.setColor(new Color(0,124,217));//边框
            g2.fillRect(0,0,COLS*BLOCK_SIZE+2*BORDER_WIDTH,ROWS*BLOCK_SIZE+2*BORDER_WIDTH);
            for (int i = 0; i < ROWS; i++) {//玩家可见区域的内容
                for (int j = 0; j < COLS; j++) {
                    g2.drawImage(blockImages[blocksMat[i+ROWS_OFFSET][j+COLS_OFFSET].ordinal()],BORDER_WIDTH+j*BLOCK_SIZE,BORDER_WIDTH+i*BLOCK_SIZE,BLOCK_SIZE,BLOCK_SIZE,null);
                }
            }
            for (int i = 0; i < BLOCK_MAX_LENGTH; i++) {//绘制移动中的方块
                if(movingBlockCoordinate[i][1]>=3){
                    g2.drawImage(blockImages[movingBlockType.ordinal()],BORDER_WIDTH+ (movingBlockCoordinate[i][0]-COLS_OFFSET)*BLOCK_SIZE,BORDER_WIDTH+ (movingBlockCoordinate[i][1]-ROWS_OFFSET)*BLOCK_SIZE,BLOCK_SIZE,BLOCK_SIZE,null);
                }
            }
            g2.setColor(movingBlockType.getColor());//根据当前移动中的方块的类型获取对应的颜色
            if(!gameOver){//游戏未结束
                for(Point p:placementCoordinate){//绘制方块落地位置的提示框
                    //对于提示框的每个方格，哪一侧无方块就在相应侧绘制一条直线
                    if(!placementCoordinate.contains(new Point(p.x-1,p.y))){
                        g2.drawLine(
                                BORDER_WIDTH+(p.x-COLS_OFFSET)*BLOCK_SIZE,
                                BORDER_WIDTH+(p.y-ROWS_OFFSET)*BLOCK_SIZE,
                                BORDER_WIDTH+(p.x-COLS_OFFSET)*BLOCK_SIZE,
                                BORDER_WIDTH+(p.y-ROWS_OFFSET)*BLOCK_SIZE+BLOCK_SIZE
                        );
                    }
                    if(!placementCoordinate.contains(new Point(p.x+1,p.y))){
                        g2.drawLine(
                                BORDER_WIDTH+(p.x-COLS_OFFSET)*BLOCK_SIZE+BLOCK_SIZE,
                                BORDER_WIDTH+(p.y-ROWS_OFFSET)*BLOCK_SIZE,
                                BORDER_WIDTH+(p.x-COLS_OFFSET)*BLOCK_SIZE+BLOCK_SIZE,
                                BORDER_WIDTH+(p.y-ROWS_OFFSET)*BLOCK_SIZE+BLOCK_SIZE
                        );
                    }
                    if(!placementCoordinate.contains(new Point(p.x,p.y-1))){
                        g2.drawLine(
                                BORDER_WIDTH+(p.x-COLS_OFFSET)*BLOCK_SIZE,
                                BORDER_WIDTH+(p.y-ROWS_OFFSET)*BLOCK_SIZE,
                                BORDER_WIDTH+(p.x-COLS_OFFSET)*BLOCK_SIZE+BLOCK_SIZE,
                                BORDER_WIDTH+(p.y-ROWS_OFFSET)*BLOCK_SIZE
                        );
                    }
                    if(!placementCoordinate.contains(new Point(p.x,p.y+1))){
                        g2.drawLine(
                                BORDER_WIDTH+(p.x-COLS_OFFSET)*BLOCK_SIZE,
                                BORDER_WIDTH+(p.y-ROWS_OFFSET)*BLOCK_SIZE+BLOCK_SIZE,
                                BORDER_WIDTH+(p.x-COLS_OFFSET)*BLOCK_SIZE+BLOCK_SIZE,
                                BORDER_WIDTH+(p.y-ROWS_OFFSET)*BLOCK_SIZE+BLOCK_SIZE
                        );
                    }
                }
            }
            else{//游戏结束，绘制一张半透明面板及“GAME OVER”文本
                g.setColor(new Color(72, 72, 187, 168));
                g.fillRect(3,3,200,400);
                g.setFont(getSpecificFont(40,CAI978));
                g.setColor(new Color(0, 136, 238));
                g.drawString("Game",0,200);
                g.drawString("Over",0,240);
            }
        }
    }
    protected class NextBlockPanel extends JPanel{
        NextBlockPanel(){
            setPreferredSize(new Dimension(NEXT_BLOCK_PANEL_WIDTH, NEXT_BLOCK_PANEL_HEIGHT));
            setBounds(BLOCK_SIZE*COLS+BORDER_WIDTH*2+PANEL_HORIZONTAL_GAP,0,NEXT_BLOCK_PANEL_WIDTH,NEXT_BLOCK_PANEL_HEIGHT);
        }
        @Override
        public void paint(Graphics g) {
            BasicStroke basicStroke = new BasicStroke(1);
            Graphics2D g2 = (Graphics2D)g;
            g2.setStroke(basicStroke);
            g2.drawImage(nextBlockPanelImage,0,0,NEXT_BLOCK_PANEL_WIDTH,NEXT_BLOCK_PANEL_HEIGHT,null);
            if(nextBlockType!= BlockType.NOBLOCK){//根据方块类型直接绘制对应图片即可
                for(Point p:getBlockPanelCoordinate(nextBlockType)){
                    g2.drawImage(blockImages[nextBlockType.ordinal()], p.x, p.y, BLOCK_SIZE, BLOCK_SIZE, null);
                }
            }
        }
    }
    protected class ScorePanel extends JPanel{
        ScorePanel(){
            setPreferredSize(new Dimension(SCORE_PANEL_WIDTH, SCORE_PANEL_HEIGHT));
            setBounds(BLOCK_SIZE*COLS+BORDER_WIDTH*2+PANEL_HORIZONTAL_GAP,NEXT_BLOCK_PANEL_HEIGHT+HOLD_BLOCK_PANEL_HEIGHT+LEVEL_PANEL_HEIGHT+3*PANEL_VERTICAL_GAP,SCORE_PANEL_WIDTH,SCORE_PANEL_HEIGHT);
        }
        @Override
        public void paint(Graphics g) {
            BasicStroke basicStroke = new BasicStroke(100);
            Graphics2D g2 = (Graphics2D)g;
            g2.setStroke(basicStroke);
            g2.drawImage(scorePanelImage,0,0,SCORE_PANEL_WIDTH,SCORE_PANEL_HEIGHT,null);
            for (int i = 0; i < Integer.toString(score).length(); i++) {//根据分数的每一位数字从数字图片数组中取图片
                g2.drawImage(numberImages[Integer.toString(score).charAt(i)-'0'],5+12*i,29,11,17,null);
            }
        }
    }
    protected class HoldBlockPanel extends JPanel {
        HoldBlockPanel(){
            setPreferredSize(new Dimension(HOLD_BLOCK_PANEL_WIDTH, HOLD_BLOCK_PANEL_HEIGHT));
            setBounds(BLOCK_SIZE*COLS+BORDER_WIDTH*2+PANEL_HORIZONTAL_GAP,NEXT_BLOCK_PANEL_HEIGHT+PANEL_VERTICAL_GAP,HOLD_BLOCK_PANEL_WIDTH,HOLD_BLOCK_PANEL_HEIGHT);
        }
        @Override
        public void paint(Graphics g) {
            BasicStroke basicStroke = new BasicStroke(1);
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(basicStroke);
            g2.drawImage(holdBlockPanelImage, 0, 0, HOLD_BLOCK_PANEL_WIDTH, HOLD_BLOCK_PANEL_HEIGHT, null);
            if(holdBlockType!= BlockType.NOBLOCK){//根据方块类型直接绘制对应图片即可
                for(Point p:getBlockPanelCoordinate(holdBlockType)){
                    g2.drawImage(blockImages[holdBlockType.ordinal()], p.x, p.y, BLOCK_SIZE, BLOCK_SIZE, null);
                }
            }
        }
    }
    protected class LevelPanel extends JPanel{
        LevelPanel(){
            setPreferredSize(new Dimension(LEVEL_PANEL_WIDTH, LEVEL_PANEL_HEIGHT));
            setBounds(BLOCK_SIZE*COLS+BORDER_WIDTH*2+PANEL_HORIZONTAL_GAP,NEXT_BLOCK_PANEL_HEIGHT+HOLD_BLOCK_PANEL_HEIGHT+2*PANEL_VERTICAL_GAP,LEVEL_PANEL_WIDTH,LEVEL_PANEL_HEIGHT);
        }
        @Override
        public void paint(Graphics g) {
            BasicStroke basicStroke = new BasicStroke(1);
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(basicStroke);
            g2.drawImage(levelPanelImage, 0, 0, LEVEL_PANEL_WIDTH, LEVEL_PANEL_HEIGHT, null);
            String levelStr = Integer.toString(level);
            int offset_level = levelStr.length()==1?54:46;//根据视觉效果调整的偏移量
            //数字的绘制原理与分数面板一致
            for (int i = 0; i < levelStr.length(); i++) {//当前等级
                g2.drawImage(numberImages[levelStr.charAt(i)-'0'],offset_level+16*i,53,14,20,null);
            }
            for (int i = 0; i < Integer.toString(10-eliminatedLines).length(); i++) {//升到下一级需要消除的行数
                g2.drawImage(numberImages[Integer.toString(10-eliminatedLines).charAt(i)-'0'],94+8*i,106,7,9,null);
            }
        }
    }
    public JPanel getTetrisPanel(){
        return this.TetrisPanel;
    }
}
