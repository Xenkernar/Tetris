package Tetriss;

import java.awt.*;

public enum BlockType {//方块类型
    NOBLOCK(Color.white),//无方块类型
    I(new Color(6551295)),
    L(new Color(16690767)),
    J(new Color(6317567)),
    Z(new Color(16729927)),
    S(new Color(6414678)),
    O(new Color(16777049)),
    T(new Color(13002751));
    private Color color;//方块显示的颜色
    BlockType(Color color){
        this.color = color;
    }
    public Color getColor(){
        return this.color;
    }
}