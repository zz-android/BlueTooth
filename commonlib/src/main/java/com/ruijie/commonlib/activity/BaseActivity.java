package com.ruijie.commonlib.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

import com.ruijie.commonlib.application.RootApplication;
import com.ruijie.commonlib.util.Toast;

public abstract class BaseActivity extends Activity {

    private long touchWaitTime = 2000;
    private long touchTime = 0;
    protected boolean touchSwitch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RootApplication.getInstance().addActivity(this);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN && KeyEvent.KEYCODE_BACK == keyCode) {
            if(!touchSwitch){
                return super.onKeyDown(keyCode, event);
            }
            long currentTime = System.currentTimeMillis();
            if((currentTime-touchTime)>=touchWaitTime) {
                Toast.show(this, "再按一次退出", Toast.LENGTH_SHORT);
                touchTime = currentTime;
            }else {
//                Comm.stopScan();
//                Comm.powerDown();
                RootApplication.getInstance().exit();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
