package Window;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static Tetriss.Configuration.*;

public class MainFrame extends JFrame {
    private BufferedImage image;//主窗口的图片
    MainFrame() throws IOException {
        image = ImageIO.read(new File(RESOURCES_PATH + "Tetris.png"));
    }
    @Override
    public void paint(Graphics g) {
        g.drawImage(image,0,0,MAINWINDOW_WIDTH,MAINWINDOW_HEIGHT,null);
        super.paintComponents(g);
    }
    public void removeAllKeyListeners(Component comp){//移除组件时移除全部键盘监听器
        for(KeyListener kl: comp.getKeyListeners()){
            removeKeyListener(kl);
        }
    }
    public void addAllKeyListeners(Component comp){//添加组件时添加全部键盘监听器
        for(KeyListener kl: comp.getKeyListeners()){
            addKeyListener(kl);
        }
    }
}