package Tetriss;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import static Tetriss.Configuration.*;
class OperableTetris extends BaseTetris{
    protected int identifier;//玩家代号（1或2）
    protected LogicController logicController;//逻辑控制器
    protected BlocksGeneratable blocksGenerator;//方块生成器

    protected class LogicController{
        private int[][] nextBlockSlot;//下一个方块的槽位
        private int[][] holdBlockSlot;//存储方块的槽位
        private Timer autoDroppingTimer;//自动下落定时器
        private Timer waitUser;//等待用户调整位置的计时器
        private Timer extendWaiting;//延长等待时间的计时器
        private boolean switchCenterOfRotation;//是否需要变换旋转中心
        private int x,y;//旋转中心
        private HashMap<Integer,Integer> projection;//存储方块的投影
        private int minimum_distance;//当前方块的下方投影距离底部方块的最小距离
        private Movement lastMovementBeforeFixed;//方块固定之前的最后一次移动类型
        private boolean canHold;//当前是否可以存储方块
        private int speed;//每次下落的时间间隔(ms)
        private final int MIN_SPEED;//最小间隔
        private KeyListener keyListener;


        LogicController(){
            projection = new HashMap<>();
            lastMovementBeforeFixed= Movement.TO_INFIMUM;
            nextBlockSlot = new int[BLOCK_MAX_LENGTH][BLOCK_MAX_HEIGHT];
            holdBlockSlot = new int[BLOCK_MAX_LENGTH][BLOCK_MAX_HEIGHT];
            x=0;
            y=0;
            gameOver=false;
            canHold = true;
            speed = 1000-level*33;//初始下落间隔
            MIN_SPEED = 33;//最小间隔
            switchCenterOfRotation = false;
            autoDroppingTimer = new Timer(speed, e -> {//自动下落
                eliminatePanel.repaint();//刷新消融面板
                if(!canMove(Movement.DOWN)) {//如果不能向下移动了
                    doWhenBlockFixed();
                }
                else{//如果还能向下移动
                    moveBlock(Movement.DOWN);
                }
            });
            waitUser = new Timer(1000, e -> {//等待用户移动，若用户在1秒后依然没有调整好
                waitUser.stop();//停止等待
                extendWaiting.stop();//不再延长等待
                updateBlocksMat();//更新方块逻辑矩阵
                estimateGameOver();//判断游戏是否结束
                if(gameOver){//若游戏结束
                    doWhenGameOver();//执行收尾工作
                    return;
                }
                updateEliminatedState();//更新消除状态（更新分数和等级）
                AudioPlayer.play();//播放音效
                canHold = true;//此时可以存储方块了
                nextBlockToBeMoving();//让下个方块槽中的方块成为移动中的方块
                generateNextBlock();//生成下个方块去填充下个方块槽

            });
            extendWaiting = new Timer(1, e -> {//延长等待
                if(canMove(Movement.DOWN)){//若用户通过调整使得方块可以继续下降，则延长等待时间
                    waitUser.restart();
                }
            });
            keyListener = new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    for (Movement movement: Movement.values()) {//对于每个移动类型都进行判断
                        if(e.getKeyCode() == getPlayerKey(identifier,movement)){//当用户按下对应的热键时
                            lastMovementBeforeFixed = movement;//记录最后一次移动
                            if(canMove(movement)) {//如果可以进行该类型的移动
                                moveBlock(movement);//移动方块
                                break;
                            }
                        }
                    }
                    if(!canMove(Movement.DOWN)) {//当方块不能继续移动时
                        doWhenBlockFixed();
                    }
                }
            };
            eliminatePanel.addKeyListener(keyListener);
        }

        //开启控制器
        public void start(){
            generateNextBlock();//生成下一个方块并放入下个方块槽
            nextBlockToBeMoving();//让下个方块槽中的方块成为移动中的方块
            generateNextBlock();//再生成下一个方块并放入下个方块槽
            autoDroppingTimer.start();//自动下落计时器开启
        }

        //根据不同种类的方块填充方块槽
        private void fillSlot(int[][] slot, BlockType type){
            switch (type){
                case I -> {
                    slot[0][0]=3+COLS_OFFSET;
                    slot[0][1]=2;
                    slot[1][0]=4+COLS_OFFSET;
                    slot[1][1]=2;
                    slot[2][0]=5+COLS_OFFSET;
                    slot[2][1]=2;
                    slot[3][0]=6+COLS_OFFSET;
                    slot[3][1]=2;
                }
                case J -> {
                    slot[0][0]=4+COLS_OFFSET;
                    slot[0][1]=1;
                    slot[1][0]=4+COLS_OFFSET;
                    slot[1][1]=2;
                    slot[2][0]=5+COLS_OFFSET;
                    slot[2][1]=2;
                    slot[3][0]=6+COLS_OFFSET;
                    slot[3][1]=2;
                }
                case L -> {
                    slot[0][0]=6+COLS_OFFSET;
                    slot[0][1]=1;
                    slot[1][0]=4+COLS_OFFSET;
                    slot[1][1]=2;
                    slot[2][0]=5+COLS_OFFSET;
                    slot[2][1]=2;
                    slot[3][0]=6+COLS_OFFSET;
                    slot[3][1]=2;
                }
                case O -> {
                    slot[0][0]=4+COLS_OFFSET;
                    slot[0][1]=2;
                    slot[1][0]=4+COLS_OFFSET;
                    slot[1][1]=1;
                    slot[2][0]=5+COLS_OFFSET;
                    slot[2][1]=2;
                    slot[3][0]=5+COLS_OFFSET;
                    slot[3][1]=1;
                }
                case S -> {
                    slot[0][0]=5+COLS_OFFSET;
                    slot[0][1]=1;
                    slot[1][0]=6+COLS_OFFSET;
                    slot[1][1]=1;
                    slot[2][0]=4+COLS_OFFSET;
                    slot[2][1]=2;
                    slot[3][0]=5+COLS_OFFSET;
                    slot[3][1]=2;
                }
                case T -> {
                    slot[0][0]=5+COLS_OFFSET;
                    slot[0][1]=1;
                    slot[1][0]=4+COLS_OFFSET;
                    slot[1][1]=2;
                    slot[2][0]=5+COLS_OFFSET;
                    slot[2][1]=2;
                    slot[3][0]=6+COLS_OFFSET;
                    slot[3][1]=2;
                }
                case Z -> {
                    slot[0][0]=4+COLS_OFFSET;
                    slot[0][1]=1;
                    slot[1][0]=5+COLS_OFFSET;
                    slot[1][1]=1;
                    slot[2][0]=5+COLS_OFFSET;
                    slot[2][1]=2;
                    slot[3][0]=6+COLS_OFFSET;
                    slot[3][1]=2;
                }
            }
        }

        //生成下个方块
        private void generateNextBlock(){
            nextBlockType = blocksGenerator.getBlock();//下个方块的类型由方块生成器生成
            fillSlot(nextBlockSlot,nextBlockType);//根据下个方块的类型添加下个方块槽
            nextBlockPanel.repaint();//刷新下个方块提示面板
        }

        //让下个方块槽中的方块成为移动中的方块
        private void nextBlockToBeMoving(){
            for (int i = 0; i < BLOCK_MAX_LENGTH; i++) {
                for (int j = 0; j < 2; j++) {
                    movingBlockCoordinate[i][j]=nextBlockSlot[i][j];//将方块槽中的坐标赋给移动中的方块坐标即可
                }
            }
            movingBlockType = nextBlockType;//修改移动中的方块类型为下个方块类型
            updatePlacementCoordinate();//更新落地提示框的坐标
        }

        //让当前存储的方块成为移动中的方块
        private void heldBlockToBeMoving(){
            for (int i = 0; i < BLOCK_MAX_LENGTH; i++) {
                for (int j = 0; j < 2; j++) {
                    movingBlockCoordinate[i][j]=holdBlockSlot[i][j];//将方块槽中的坐标赋给移动中的方块坐标即可
                }
            }
            movingBlockType = holdBlockType;//修改移动中的方块类型为存储的方块类型
            updatePlacementCoordinate();//更新落地提示框的坐标
        }

        //存储当前移动中的方块
        private void holdCurrentBlock() {
            if(holdBlockType== BlockType.NOBLOCK){//如果存储方块槽中没有方块
                fillSlot(holdBlockSlot,movingBlockType);//用移动中的方块填充槽位
                holdBlockType = movingBlockType;//修改存储的方块类型
                holdBlockPanel.repaint();//刷新存储方块面板
                nextBlockToBeMoving();//让下一个方块成为移动中的方块
                generateNextBlock();//生成下个方块并放入下个方块槽中
            }else {//若已经有存储的方块
                BlockType type = movingBlockType;//记录下移动中的方块类型
                heldBlockToBeMoving();//让存储的方块成为移动中的方块
                fillSlot(holdBlockSlot,type);//用上次移动的方块类型填充存储方块槽
                holdBlockType = type;//修改存储方块类型为上次移动的方块
                holdBlockPanel.repaint();//刷新存储方块面板
            }
        }

        //获取移动中的方块在各个方向的投影
        private void getProjection(Movement movement){
            projection.clear();//先清空上次存储投影
            switch (movement){
                case DOWN,TO_INFIMUM -> {//获取向下的投影
                    for (int i = 0; i < BLOCK_MAX_LENGTH; i++) {//初始值y设为最高点的上边一个位置
                        projection.put(movingBlockCoordinate[i][0],-1);
                    }
                    for (int i = 0; i < BLOCK_MAX_LENGTH; i++) {//对于每个x，用最低的y坐标去更新其投影的y值
                        if(movingBlockCoordinate[i][1]> projection.get(movingBlockCoordinate[i][0])){
                            projection.put(movingBlockCoordinate[i][0],movingBlockCoordinate[i][1]);
                        }
                    }

                }
                case LEFT -> {//获取向左的投影
                    for (int i = 0; i < BLOCK_MAX_LENGTH; i++) {//初始值x设为最右点的右边一个位置
                        projection.put(movingBlockCoordinate[i][1],COLS+COLS_OFFSET);
                    }
                    for (int i = 0; i < BLOCK_MAX_LENGTH; i++) {//对于每个y，用最左的x坐标去更新其投影的x值
                        if(movingBlockCoordinate[i][0]< projection.get(movingBlockCoordinate[i][1])){
                            projection.put(movingBlockCoordinate[i][1],movingBlockCoordinate[i][0]);
                        }
                    }
                }
                case RIGHT -> {//获取向右的投影
                    for (int i = 0; i < BLOCK_MAX_LENGTH; i++) {//初始值x设为最左点的左边一个位置
                        projection.put(movingBlockCoordinate[i][1],-1);
                    }
                    for (int i = 0; i < BLOCK_MAX_LENGTH; i++) {//对于每个y，用最右的x坐标去更新其投影的x值
                        if(movingBlockCoordinate[i][0]> projection.get(movingBlockCoordinate[i][1])){
                            projection.put(movingBlockCoordinate[i][1],movingBlockCoordinate[i][0]);
                        }
                    }
                }
            }
        }

        //判断是否可以向某个方向移动
        private boolean canMove(Movement movement){
            switch (movement){
                case DOWN,TO_INFIMUM -> {//是否可以向下移动一格或者直接到底
                    getProjection(Movement.DOWN);//获取下方投影
                    if(movement== Movement.DOWN){//如果是向下移动一格
                        for(int x: projection.keySet()){//判断下方投影的下一格是否有方块
                            if(blocksMat[projection.get(x)+1][x]!= BlockType.NOBLOCK){//投影中的任意一格的下一格有方块则不能下向移动
                                return false;
                            }
                        }
                        return true;//都没有方块则可以向下移动一格
                    }else{//如果是直接到底
                        return true;//任何情况下都可以
                    }

                }
                case LEFT -> {//是否可以向左移动一格
                    getProjection(Movement.LEFT);//获取左侧投影
                    for(int y: projection.keySet()){//判断左侧投影的左一格是否有方块
                        if(blocksMat[y][projection.get(y)-1]!= BlockType.NOBLOCK){
                            return false;
                        }
                    }
                    return true;
                }
                case RIGHT -> {//是否可以向右移动一格
                    getProjection(Movement.RIGHT);//获取右侧投影
                    for(int y: projection.keySet()){//判断右侧投影的右一格是否有方块
                        if(blocksMat[y][projection.get(y)+1]!= BlockType.NOBLOCK){
                            return false;
                        }
                    }
                    return true;
                }
                case ANTICLOCKWISE,CLOCKWISE -> {//是否可以旋转
                    x = 0;
                    y = 0;
                    boolean res=true;//默认可以
                    switch (movingBlockType){//根据不同类型的方块设置不同的旋转中心
                        case O -> {//O形方块不能旋转
                            return false;
                        }
                        case I -> {//I形方块的旋转中心在第二格和第三格之间切换
                            x = switchCenterOfRotation?movingBlockCoordinate[1][0]:movingBlockCoordinate[2][0];
                            y = switchCenterOfRotation?movingBlockCoordinate[1][1]:movingBlockCoordinate[2][1];
                        }
                        //其余几种方块的旋转中心不变
                        case J,L,Z,T -> {
                            x = movingBlockCoordinate[2][0];
                            y = movingBlockCoordinate[2][1];
                        }
                        case S -> {
                            x = movingBlockCoordinate[0][0];
                            y = movingBlockCoordinate[0][1];
                        }
                    }
                    int x1=0,y1=0;//模拟旋转后每个格子的位置
                    if(movement== Movement.ANTICLOCKWISE){//如果是逆时针旋转
                        for (int i = 0; i < BLOCK_MAX_LENGTH; i++) {//根据旋转矩阵推导的旋转公式
                            x1 = x - y + movingBlockCoordinate[i][1];
                            y1 = x + y - movingBlockCoordinate[i][0];
                            if((x1<1||x1>10||y1<1||y1>23)||(!(x1==x&&y1==y)&&blocksMat[y1][x1]!= BlockType.NOBLOCK)){//如果旋转后越界或者覆盖了已存在的方块
                                res = false;//则不能旋转
                            }
                        }
                    }
                    else {//顺时针旋转同理，只需要更换旋转公式
                        for (int i = 0; i < BLOCK_MAX_LENGTH; i++) {
                            x1 = y + x - movingBlockCoordinate[i][1];
                            y1 = y - x + movingBlockCoordinate[i][0];
                            if ((x1 < 1 || x1 > 10 || y1 < 1 || y1 > 23) || !(x1 == x && y1 == y) && blocksMat[y1][x1] != BlockType.NOBLOCK) {
                                res = false;
                            }
                        }

                    }
                    return res;

                }
                case HOLD -> {return canHold;}//是否可以持有方块，根据canHold判断
            }
            return true;
        }

        //移动方块
        private void moveBlock(Movement movement){
            switch (movement){//向不同方向修改坐标即可
                case DOWN -> {
                    for (int i = 0; i < BLOCK_MAX_LENGTH; i++) {
                        movingBlockCoordinate[i][1]++;
                    }
                }
                case LEFT -> {
                    for (int i = 0; i < BLOCK_MAX_LENGTH; i++) {
                        movingBlockCoordinate[i][0]--;
                    }
                }
                case RIGHT -> {
                    for (int i = 0; i < BLOCK_MAX_LENGTH; i++) {
                        movingBlockCoordinate[i][0]++;
                    }
                }
                case ANTICLOCKWISE,CLOCKWISE -> {
                    int x1=0,y1=0;
                    for (int i = 0; i < BLOCK_MAX_LENGTH; i++) {
                        if(movement== Movement.ANTICLOCKWISE){
                            x1 = x - y + movingBlockCoordinate[i][1];
                            y1 = x + y - movingBlockCoordinate[i][0];
                        }else{
                            x1 = y + x - movingBlockCoordinate[i][1];
                            y1 = y - x + movingBlockCoordinate[i][0];
                        }
                        movingBlockCoordinate[i][0] = x1;
                        movingBlockCoordinate[i][1] = y1;
                    }
                    switchCenterOfRotation=!switchCenterOfRotation;
                }
                //如果是直接到底
                case TO_INFIMUM -> {
                    minimum_distance=Integer.MAX_VALUE;//最小距离初始为最大值
                    for(int x: projection.keySet()){//遍历下方投影的坐标
                        for (int i = projection.get(x)+1; i < ROWS+ROWS_OFFSET+1; i++) {
                            if(blocksMat[i][x]!= BlockType.NOBLOCK){
                                minimum_distance= Math.min(i - projection.get(x), minimum_distance);//更新最小距离
                                break;
                            }
                        }
                    }
                    for (int i = 0; i < BLOCK_MAX_LENGTH; i++) {//根据最小距离下移当前方块
                        movingBlockCoordinate[i][1]+=minimum_distance-1;
                    }
                }
                //如果是存储方块
                case HOLD -> {
                    holdCurrentBlock();//直接存储
                    canHold = false;//修改存储标识，保证在每次方块固定之前最多只能存储一次
                }
            }
            updatePlacementCoordinate();//更新提示框的坐标
            eliminatePanel.repaint();//刷新消融面板
        }

        //方块固定时的操作
        private void doWhenBlockFixed(){
            if(!gameOver){//如果游戏未结束
                if(lastMovementBeforeFixed== Movement.TO_INFIMUM){//如果最近一次移动操作是直接到底
                    waitUser.getActionListeners()[0].actionPerformed(null);//不等待用户调整
                }
                else{//否则等待用户调整
                    waitUser.start();
                    extendWaiting.start();
                }
            }
        }

        //游戏结束时的收尾工作
        private void doWhenGameOver(){
            //按顺序停止所有的计时器
            autoDroppingTimer.stop();
            extendWaiting.stop();
            waitUser.stop();
        }

        //判断是否游戏结束
        private void estimateGameOver(){
            int ceiling=ROWS+ROWS_OFFSET+1;//方块固定时的最高点
            for (int i = 0; i < BLOCK_MAX_LENGTH; i++) {
                ceiling = Math.min(movingBlockCoordinate[i][1], ceiling);//更新最高点
                if(ceiling<ROWS_OFFSET){//如果已经超出了显示范围
                    gameOver = true;//游戏结束
                    return;
                }
            }
        }

        //更新方块矩阵
        private void updateBlocksMat(){
            for (int i = 0; i < BLOCK_MAX_LENGTH; i++) {//用移动中的方块坐标去方块矩阵中填充对应类型的方块即可
                blocksMat[movingBlockCoordinate[i][1]][movingBlockCoordinate[i][0]]=movingBlockType;
            }
        }

        //根据当前方块矩阵的状态判断消除情况，并更新方块矩阵
        private void updateEliminatedState(){
            int eliminatedState = 0;//一次消除方块的数量（0-4）
            boolean rowEliminated;//当前行是否可以消除
            ArrayList<Integer> waitingEliminateRows = new ArrayList<>();//哪些行要被消除
            for (int i = 0; i < ROWS; i++) {//对于每一行
                rowEliminated=true;//默认该行可以消除
                for (int j = 0; j < COLS; j++) {//判断该行是否每一列都有方块填充
                    if(blocksMat[i+ROWS_OFFSET][j+COLS_OFFSET]== BlockType.NOBLOCK){
                        rowEliminated=false;//只要有一个缺口则该行不能消除
                        break;
                    }
                }
                if(rowEliminated){//如果该行可以消除
                    waitingEliminateRows.add(i+ROWS_OFFSET);
                }
                if(waitingEliminateRows.size()>=4){//一次最多消4行
                    break;
                }
            }
            eliminatedState+=waitingEliminateRows.size();
            score+=50*level;//基础分数
            scorePanel.repaint();
            if(eliminatedState==0){//没有方块消除
                return;
            }
            switch (eliminatedState){//根据消除的行数获得不同分数
                case 1->score+=100*Math.pow(level,1.1);
                case 2->score+=300*Math.pow(level,1.2);
                case 3->score+=600*Math.pow(level,1.3);
                case 4->score+=1200*Math.pow(level,1.5);
            }
            eliminatedLines+=eliminatedState;//消除的行数对应增加
            if(eliminatedLines>=10){//每超过10行升一级
                eliminatedLines-=10;
                level+=1;
                speed-=(level*33);//加速
                if(speed<=MIN_SPEED){
                    speed = MIN_SPEED;//限制下落时间间隔
                }
                autoDroppingTimer.setDelay(speed);
            }
            levelPanel.repaint();
            scorePanel.repaint();
            HashSet<Integer> set = new HashSet<>();//存储某一行是否已经被用于替换
            for (int i = waitingEliminateRows.get(waitingEliminateRows.size()-1); i >=ROWS_OFFSET ; i--) {//查找应该用哪一行代替当前行
                int replaceRows;
                for (replaceRows = i-1; replaceRows >= ROWS_OFFSET+1; replaceRows--) {
                    if(!set.contains(replaceRows)&&!waitingEliminateRows.contains(replaceRows)){//如果该行未被用于替换且不属于要被消除的行
                        set.add(replaceRows);//将其加入集合
                        break;
                    }
                }
                for (int j = 0; j < COLS; j++) {//替换当前行
                    blocksMat[i][j+COLS_OFFSET]=blocksMat[replaceRows][j+COLS_OFFSET];
                }
            }
            //最上面的eliminatedState行都置为无方块状态
            for (int i = 0; i < eliminatedState; i++) {
                for (int j = 0; j < COLS; j++) {
                    blocksMat[i+ROWS_OFFSET][j+COLS_OFFSET]= BlockType.NOBLOCK;
                }
            }

        }

        //更新提示框的坐标
        private void updatePlacementCoordinate(){
            getProjection(Movement.DOWN);//获取向下的投影
            placementCoordinate.clear();//清空先前存储的坐标
            minimum_distance=Integer.MAX_VALUE;//最小距离初始化为最大值
            for(int x: projection.keySet()){//对于每一个x坐标
                for (int i = projection.get(x)+1; i < ROWS+ROWS_OFFSET+1; i++) {//遍历下方的所有y坐标
                    if(blocksMat[i][x]!= BlockType.NOBLOCK){
                        minimum_distance= Math.min(i - projection.get(x), minimum_distance);//更新最小距离
                        break;
                    }
                }
            }
            //根据最小距离确定提示框的坐标
            for (int i = 0; i < BLOCK_MAX_LENGTH; i++) {
                placementCoordinate.add(new Point(movingBlockCoordinate[i][0],movingBlockCoordinate[i][1]+minimum_distance-1));
            }
        }
    }

    protected OperableTetris(int identifier){
        this.identifier = identifier;
        logicController = new LogicController();
    }
    //开始游戏
    public void start(){
        logicController.start();
    }

    //暂停游戏
    public void pauseGame(){
        logicController.autoDroppingTimer.stop();
        logicController.extendWaiting.stop();
        logicController.waitUser.stop();
    }
    //继续游戏
    public void continueGame(){
        logicController.autoDroppingTimer.start();
    }

    public KeyListener getKeyListener(){
        return eliminatePanel.getKeyListeners()[0];
    }

}
