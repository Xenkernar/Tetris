package Window;

import Components.Action;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.stream.Stream;

public class TInputBox extends TMenu {
    TInputBox(int x,int y,String defaultText,String[] buttonTexts){
        super(x,y,true,0,
                Stream.concat(Arrays.stream(new String[]{defaultText}), Arrays.stream(buttonTexts)).toArray(String[]::new)//在按钮前插入一个文本为defaultText的按钮
        );
        //为第一个按钮添加类似TEditField的监听器
        buttons[0].addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if(buttons[0].isOnFocus()){
                            if(buttons[0].getContent().length()>0&&e.getKeyCode()==KeyEvent.VK_BACK_SPACE){
                                String newStr = buttons[0].getContent().substring(0,buttons[0].getContent().length()-1);
                                buttons[0].setContent(newStr);
                                buttons[0].repaint();
                            }
                            if(e.getKeyCode()==110||e.getKeyCode()==KeyEvent.VK_PERIOD){
                                buttons[0].setContent( buttons[0].getContent()+'.');
                                buttons[0].repaint();
                            }
                            if((e.getKeyCode()>=0x0030&&e.getKeyCode()<=0x0039)){
                                buttons[0].setContent( buttons[0].getContent()+(char)(e.getKeyCode()));
                                buttons[0].repaint();
                            }
                            if((e.getKeyCode()>=0x0060&&e.getKeyCode()<=0x0069)){
                                buttons[0].setContent( buttons[0].getContent()+(char)(e.getKeyCode()-0x0030));
                                buttons[0].repaint();
                            }
                        }
                    }
                });
        addAllKeyListeners(buttons[0]);
    }
    //获取输入框的内容
    public String getContent() {
        return buttons[0].getContent();
    }

    @Override
    public void setActions(Action[] actions) {//设置按钮响应事件
        for (int i = 0; i < actions.length; i++) {
            buttons[i+1].setAction(actions[i]);
            addKeyListener(buttons[i+1].getKeyListeners()[0]);
        }

    }
}
