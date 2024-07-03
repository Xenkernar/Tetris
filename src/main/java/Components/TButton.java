package Components;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class TButton extends TLabel{
    private boolean onFocus;//是否获得焦点
    public TButton(int x, int y, int width, int height, String content, Font font){
        super(x,y,width,height,content,font);
        onFocus=false;//默认不获取焦点
    }

    public boolean isOnFocus() {
        return onFocus;
    }
    public void setOnFocus(boolean onFocus) {
        this.onFocus = onFocus;
    }
    public void setAction(Action action){
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(onFocus&&e.getKeyCode()==KeyEvent.VK_ENTER){//当获取焦点时通过按下Enter键触发action
                    try {
                        action.action();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
    }
}
