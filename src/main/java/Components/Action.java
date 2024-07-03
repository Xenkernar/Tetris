package Components;

import java.io.IOException;

@FunctionalInterface
public interface Action {//当TButton被点击时触发的函数式接口
    void action() throws IOException;
}
