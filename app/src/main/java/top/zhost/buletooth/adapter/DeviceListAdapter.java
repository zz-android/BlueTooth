package top.zhost.buletooth.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;



import java.util.List;

import top.zhost.buletooth.R;

/**
 * Created by zz on 2018/1/23.
 */

public class DeviceListAdapter extends BaseAdapter {
    private List<BluetoothDevice> items;
    private LayoutInflater layoutInflater;



    public DeviceListAdapter(Context context, List<BluetoothDevice> list) {
        this.items = list;
        this.layoutInflater = LayoutInflater.from(context);

    }
    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    class Ms{
        private TextView info;

    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        Ms ms = null;
        if(view == null){
            //view = View.inflate(getApplicationContext(), R.layout.task_list_item, null);
            view = layoutInflater.inflate(R.layout.listview_device, null);
            ms = new Ms();
            ms.info = (TextView)view.findViewById(R.id.info);

            view.setTag(ms);
        }else{
            ms = (Ms)view.getTag();
        }

        ms.info.setText(items.get(position).getName()+" "+items.get(position).getAddress());

        return view;
    }

}

