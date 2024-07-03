package Window;

import Tetriss.BlockType;


import java.awt.*;
import java.io.*;
import java.util.ArrayList;

import static Tetriss.Configuration.RESOURCES_PATH;

public class ConstructUtil {
    public static final String CAI978 = "204-CAI978.ttf";//字体文件名
    public static final String ANATEVKA = "Anatevka.otf";//字体文件名

    private ConstructUtil(){}
    public static Font getSpecificFont(int size,String fontName){//根据尺寸和字体文件名创建字体
        InputStream is = null;
        Font baseTTF = null;
        try {
            is = new BufferedInputStream(new FileInputStream(RESOURCES_PATH+fontName));
            baseTTF = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (FontFormatException e) {
            throw new RuntimeException(e);
        }
        return baseTTF.deriveFont(Font.PLAIN,size);
    }
    public static ArrayList<BlockType> getRandomBlocks(){//返回包含300个方块的随机序列
        ArrayList<BlockType> res = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            res.add(BlockType.values()[(int)(Math.random()*7)+1]);
        }
        return res;
    }
}
