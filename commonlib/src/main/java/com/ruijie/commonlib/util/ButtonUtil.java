package com.ruijie.commonlib.util;

/**
 * Created by zz on 2018/4/2.
 */

public class ButtonUtil {
    private static long lastClickTime = System.currentTimeMillis();
    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if ( 0 < timeD && timeD < 500) {       //500毫秒内按钮无效，这样可以控制快速点击，自己调整频率
            return true;
        }
        lastClickTime = time;
        return false;
    }
}



