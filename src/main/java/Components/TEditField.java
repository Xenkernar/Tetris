package Components;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TEditField extends TButton{
    private String str;//内容
    private boolean onFocus;//是否获得焦点
    public TEditField(int x, int y, int width, int height, String content, Font font){
        super(x,y,width,height,content,font);
        this.str = content;
        this.onFocus = false;//默认不获得
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(onFocus){//当获取焦点时
                    if(str.length()>0&&e.getKeyCode()==KeyEvent.VK_BACK_SPACE){//按下退格键，删除一个字符
                        str = str.substring(0,str.length()-1);
                        setContent(str);
                        repaint();
                    }
                    if(e.getKeyCode()==110||e.getKeyCode()==KeyEvent.VK_PERIOD){//按下小数点键追加小数点
                        str+='.';
                        setContent(str);
                        repaint();
                    }
                    if((e.getKeyCode()>=0x0030&&e.getKeyCode()<=0x0039)){//按下数字键追加数字（非小键盘区域）
                        str+=(char)(e.getKeyCode());
                        setContent(str);
                        repaint();
                    }
                    if((e.getKeyCode()>=0x0060&&e.getKeyCode()<=0x0069)){//按下数字键追加数字（小键盘区域）
                        str+=(char)(e.getKeyCode()-0x0030);
                        setContent(str);
                        repaint();
                    }
                }
            }
        });
    }

    public boolean isOnFocus() {
        return onFocus;
    }
    public void setOnFocus(boolean onFocus) {
        this.onFocus = onFocus;
    }
}
