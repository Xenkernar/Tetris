package Tetriss;

public class RandomTetris extends OperableTetris{
    private boolean isUneven;//随机数的分布是否不均匀
    public RandomTetris(int identifier,boolean isUneven){
        super(identifier);
        this.isUneven = isUneven;
        blocksGenerator = ()-> getRandomType();

    }
    private BlockType getRandomType(){
        return BlockType.values()[isUneven?getUnevenRandom():((int)(Math.random()*7)+1)];
    }
    private int getUnevenRandom(){//获取分布不均匀的随机数
        int r =  (int)(Math.random()* Configuration.freq_sum)+1;//获取总频数范围内的随机数
        for (int i = 0; i < Configuration.freqs.length; i++) {//判断其位于哪个区间，返回对应的方块类型下标
            r-= Configuration.freqs[i];
            if(r<=0){
                return i+1;

            }
        }
        return 1;
    }

}
