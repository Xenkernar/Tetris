package Components;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class TLabel extends JPanel {
    //文本的对齐方式
    public static final int CENTER = 0;//居中
    public static final int LEFT = 1;//水平方向左对齐
    public static final int TOP = 2;//垂直方向上对齐

    private int horizontalAlignment;//水平对齐方式
    private int verticalAlignment;//垂直对齐方式
    private Font font;//文本字体
    private String content;//文本内容
    private boolean autoFeed;//是否自动换行
    public TLabel(int x, int y, int width, int height, String content, Font font){
        setBackground(new Color(9, 145, 244, 128));//设置半透明背景色
        setPreferredSize(new Dimension(width,height));
        setOpaque(true);
        setBounds(x,y,width,height);
        this.content = content;
        this.font = font;
        horizontalAlignment = CENTER;
        verticalAlignment = CENTER;
        autoFeed=true;
    }
    public void setHorizontalAlignment(int alignment){
        this.horizontalAlignment = alignment;
    }
    public void setVerticalAlignment(int alignment){
        this.verticalAlignment = alignment;
    }
    @Override
    public int getWidth() {
        return getPreferredSize().width;
    }
    @Override
    public int getHeight() {
        return getPreferredSize().height;
    }
    public void setContent(String content){
        this.content = content;
    }
    public String getContent() {
        return content;
    }
    public void setAutoFeed(boolean autoFeed) {
        this.autoFeed = autoFeed;
    }

    @Override
    public void paint(Graphics g) {

        g.clearRect(0,0,getWidth(),getHeight());//每次更新文本内容时清空上次绘制的文本
        super.paint(g);
        g.setColor(Color.cyan);
        g.setFont(font);
        g.fillRect(0,0,5,getHeight());//左边框
        g.fillRect(getWidth()-5,0,5,getHeight());//右边框
        int textWidth = g.getFontMetrics().stringWidth(content);//文本的总宽度
        int textHeight = font.getSize();//单行文本的高度
        if(content.equals("")){//如果文本为空则不用绘制
            return;
        }
        if(!autoFeed){//非自动换行的情况，直接绘制
            int drawX = horizontalAlignment==CENTER?(getWidth()-textWidth)/2:8;
            int drawY = verticalAlignment==CENTER?(getHeight()-textHeight)/2+textHeight:textHeight;
            drawY-=textHeight/5;
            g.drawString(content,drawX,drawY);
        }
        else{//自动换行的情况
            char[] chars = content.toCharArray();//将文本转为字符数组
            int rows = 1;//存储最终需要绘制的行数
            int curWidth = 8;//存储每一行在x轴上开始绘制的坐标
            int charWidth;//当前字符宽度
            ArrayList<Integer> colNums=new ArrayList<>();//每行的字符数
            ArrayList<Integer> colWidths=new ArrayList<>();//每行的总字符宽度
            for (int i = 0; i < chars.length; i++) {
                charWidth = g.getFontMetrics().charWidth(chars[i]);//获取当前字符宽度
                curWidth+=charWidth;//累加字符宽度
                if(curWidth>getWidth()-8||chars[i]=='\n'||i==chars.length-1){
                    if(i!=chars.length-1){//若加上当前字符后超过了可显示的最大宽度或遇到了换行符
                        colNums.add(i);//当前字符不添加进当前行，当前行的字符数为i+1-1
                        colWidths.add(curWidth-charWidth-8);//当前字符的宽度不添加到当前行
                        rows++;//行数+1
                    }else{//若到达字符串结尾
                        colNums.add(i+1);//当前字符添加进当前行，当前行的字符数为i+1
                        colWidths.add(curWidth-8);//当前字符的宽度添加到当前行（最后一行）
                    }
                    curWidth = 8+(chars[i]!='\n'?charWidth:0);//将当前字符添加到下一行
                }
            }

            int drawY = verticalAlignment==CENTER?(getHeight()-textHeight*rows)/2+textHeight:textHeight;//根据行数和对齐方式计算第一行的Y轴坐标
            drawY-=textHeight/5;//根据视觉效果进行微调
            for (int i = 0; i < rows; i++) {//开始根据行数和每行字符数输出字符串
                int drawX = horizontalAlignment==CENTER?(getWidth()-colWidths.get(i))/2:8;
                g.drawString(content.substring(i!=0?colNums.get(i-1):0,colNums.get(i)),drawX,drawY);
                drawY+=textHeight;
            }
        }
    }
}
