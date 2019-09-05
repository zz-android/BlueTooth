package com.ruijie.commonlib.application;

import android.app.Activity;
import android.app.Application;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class RootApplication extends Application {
	
    //对于新增和删除操作add和remove，LinedList比较占优势，因为ArrayList实现了基于动态数组的数据结构，要移动数据。LinkedList基于链表的数据结构,便于增加删除 
    private List<Activity> activityList = new LinkedList<Activity>();
    private static RootApplication instance;
    private RootApplication(){ }
    //单例模式中获取唯一的MyApplication实例RootApplication.getInstance().addActivity(this);
    public static RootApplication getInstance() {
        synchronized (RootApplication.class){
            if(null == instance) {
                instance = new RootApplication();
            }
        }
        return instance;
    }
    //添加Activity到容器中
    public void addActivity(Activity activity)  {
       activityList.add(activity);
    }
    //添加Activity到容器中
    public void addActivityAndFinishOther(Activity activityAdd)  {
//        for(Activity activity:activityList) {
//            activity.finish();
//            activityList.remove(activity);
//        }
        Iterator<Activity> iterator = activityList.iterator();
        while (iterator.hasNext()) {
            Activity activity = iterator.next();
            activity.finish();
            iterator.remove();
        }
        activityList.add(activityAdd);
    }
    //遍历所有Activity并finish
    public void exit(){
        for(Activity activity:activityList) {
             activity.finish();
        }
        System.exit(0);
    }
    //遍历所有Activity并finish
    public void finishAll(){
        for(Activity activity:activityList) {
            activity.finish();
        }
    }
    //获取最后一起个
    public Activity getLastActivity(){
        return activityList.get(activityList.size()-1);
    }

    public Integer test(){
        return  activityList.size();
    }
}
