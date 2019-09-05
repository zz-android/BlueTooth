package top.zhost.buletooth.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import top.zhost.buletooth.BluetoothConnecion;
import top.zhost.buletooth.ClsUtils;
import top.zhost.buletooth.R;
import top.zhost.buletooth.adapter.DeviceListAdapter;

public class OverViewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_over_view);
    }

    public void goUpdateBinClick(View view){
        Intent intent = new Intent(OverViewActivity.this,UpdateBinActivity.class);
        startActivity(intent);
    }

    public void goCommandClick(View view){
        Intent intent = new Intent(OverViewActivity.this,CommandActivity.class);
        startActivity(intent);
    }

    public void goUpdateApkClick(View view){
        Intent intent = new Intent(OverViewActivity.this,UpdateApkActivity.class);
        startActivity(intent);
    }

    public void goMainClick(View view){
        Intent intent = new Intent(OverViewActivity.this,MainActivity.class);
        startActivity(intent);
    }
}
