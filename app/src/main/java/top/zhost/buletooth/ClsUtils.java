package top.zhost.buletooth;

import android.bluetooth.BluetoothDevice;

import java.lang.reflect.Method;

public class ClsUtils {

    //自动配对设置Pin值
    static public boolean autoBond(Class btClass, BluetoothDevice device, String strPin) throws Exception {
        Method autoBondMethod = btClass.getMethod("setPin",new Class[]{byte[].class});
        Boolean result = (Boolean)autoBondMethod.invoke(device,new Object[]{strPin.getBytes()});
        return result;
    }

    //开始配对
    static public boolean createBond(Class btClass,BluetoothDevice device) throws Exception {
        Method createBondMethod = btClass.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        return returnValue.booleanValue();
    }

    static public  boolean  removeBond(Class btClass, BluetoothDevice btDevice) throws Exception {
        Method removeBondMethod = btClass.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    // 取消用户输入
    @SuppressWarnings("unchecked")
    static public boolean cancelPairingUserInput(Class btClass, BluetoothDevice device) throws Exception
    {
        Method createBondMethod = btClass.getMethod("cancelPairingUserInput");
// cancelBondProcess()
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        //Log.d("returnValue", "cancelPairingUserInput is success " + returnValue.booleanValue());
        return returnValue.booleanValue();
    }
}
