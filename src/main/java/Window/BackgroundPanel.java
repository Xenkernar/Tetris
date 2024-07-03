package Window;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static Tetriss.Configuration.RESOURCES_PATH;

public class BackgroundPanel extends JPanel {
    private BufferedImage backgroundImage;//游戏窗口的背景图片
    private int width;
    private int height;
    BackgroundPanel(int width, int height){
        super();
        try {
            backgroundImage = ImageIO.read(new File(RESOURCES_PATH + "background.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.width = width;
        this.height = height;
        this.setPreferredSize(new Dimension(width,height));
        setBounds(0,0,width,height);
    }
    @Override
    protected void paintComponent(Graphics g) {//绘制组件后绘制背景图片
        super.paintComponent(g);
        g.drawImage(backgroundImage,0,0,width,height,null);
    }
    @Override
    public Component add(Component comp) {//在添加组件时添加组件的监听器
        for (int i = 0; i < comp.getKeyListeners().length; i++) {
            addKeyListener(comp.getKeyListeners()[i]);
        }
        for (int i = 0; i < comp.getMouseListeners().length; i++) {
            addMouseListener(comp.getMouseListeners()[i]);
        }
        return super.add(comp);
    }
}