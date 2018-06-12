package com.example.jasonchen.smartnote;

import android.app.Application;

import com.example.jasonchen.smartnote.Handler.MyMessageHandler;
import com.example.jasonchen.smartnote.base.UniversalImageLoader;
import com.orhanobut.logger.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;


import cn.bmob.newim.BmobIM;

/**
 * Created by Jason Chen on 2018/3/28.
 */
public class SmartNote extends Application {

    private static SmartNote INSTANCE;

    public static SmartNote INSTANCE(){
        return INSTANCE;
    }

    private void setInstance(SmartNote app){
        setSmartNote(app);
    }

    private static void setSmartNote(SmartNote app) {
        SmartNote.INSTANCE = app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setInstance(this);
        //TODO 1.8、初始化IM SDK，并注册消息接收器，只有主进程运行的时候才需要初始化
        if (getApplicationInfo().packageName.equals(getMyProcessName())) {
            BmobIM.init(this);
            BmobIM.registerDefaultMessageHandler(new MyMessageHandler(this));
        }
        Logger.init("SmartNote");
        UniversalImageLoader.initImageLoader(this);
    }


    public static String getMyProcessName() {
        try {
            File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
            BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
            String processName = mBufferedReader.readLine().trim();
            mBufferedReader.close();
            return processName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
