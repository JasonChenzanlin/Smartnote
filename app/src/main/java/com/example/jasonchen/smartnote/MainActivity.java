package com.example.jasonchen.smartnote;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.jasonchen.smartnote.Fragment.conversation_Fragment;
import com.example.jasonchen.smartnote.Fragment.friend_list_fragment;
import com.example.jasonchen.smartnote.Fragment.myself_fragment;
import com.example.jasonchen.smartnote.Fragment.save_voice_fragment;
import com.example.jasonchen.smartnote.adapter.MyFragmentPagerAdapter;
import com.example.jasonchen.smartnote.bean.User;
import com.example.jasonchen.smartnote.event.RefreshEvent;
import com.example.jasonchen.smartnote.util.IMMLeaks;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.listener.ConnectListener;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;


public class MainActivity extends AppCompatActivity {
    private List<View> viewList;
    private ViewPager viewPager;
    private List<String> titleList;
    private TabLayout tabLayout;
    private List<Fragment> fragmentList;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SpeechUtility.createUtility(this, SpeechConstant.APPID +"=59f457ce");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final User user = BmobUser.getCurrentUser(User.class);

        //TODO 连接：3.1、登录成功、注册成功或处于登录状态重新打开应用后执行连接IM服务器的操作
        if (!TextUtils.isEmpty(user.getObjectId())) {
            BmobIM.connect(user.getObjectId(), new ConnectListener() {
                @Override
                public void done(String uid, BmobException e) {
                    if (e == null) {
                        //TODO 连接成功后再进行修改本地用户信息的操作，并查询本地用户信息
                        EventBus.getDefault().post(new RefreshEvent());
                        //服务器连接成功就发送一个更新事件，同步更新会话及主页的小红点
                        //TODO 会话：3.6、更新用户资料，用于在会话页面、聊天页面以及个人信息页面显示
                        BmobIM.getInstance().
                                updateUserInfo(new BmobIMUserInfo(user.getObjectId(),
                                        user.getUsername(), user.getAvatar()));

                    } else {
                        Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG);
                    }
                }
            });
            //TODO 连接：3.3、监听连接状态，可通过BmobIM.getInstance().getCurrentStatus()来获取当前的长连接状态
//            BmobIM.getInstance().setOnConnectStatusChangeListener(new ConnectStatusChangeListener() {
//                @Override
//                public void onChange(ConnectionStatus status) {
//                    Toast.makeText(MainActivity.this,status.getMsg(),Toast.LENGTH_LONG);
////                    Logger.i(BmobIM.getInstance().getCurrentStatus().getMsg());
//                }
//            });
        }
        //解决leancanary提示InputMethodManager内存泄露的问题
        IMMLeaks.fixFocusedViewLeak(getApplication());

//        if (!TextUtils.isEmpty(user.getObjectId())) {
//            BmobIM.connect(user.getObjectId(), new ConnectListener() {
//                @Override
//                public void done(String uid, BmobException e) {
//                    if (e == null) {
//                        //TODO 连接成功后再进行修改本地用户信息的操作，并查询本地用户信息
//                        EventBus.getDefault().post(new RefreshEvent());
//                        //服务器连接成功就发送一个更新事件，同步更新会话及主页的小红点
//                        //TODO 会话：3.6、更新用户资料，用于在会话页面、聊天页面以及个人信息页面显示
//                        BmobIM.getInstance().
//                                updateUserInfo(new BmobIMUserInfo(user.getObjectId(),
//                                        user.getUsername(), user.getAvatar()));
//
//                    } else {
//                        toast(e.getMessage());
//                    }
//                }
//            });


            init();
    }

    public void init(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        tabLayout=(TabLayout) findViewById(R.id.tab);
        setSupportActionBar(toolbar);


        fragmentList =new ArrayList<Fragment>();
        fragmentList.add(new save_voice_fragment());
        fragmentList.add(new conversation_Fragment());
        fragmentList.add(new friend_list_fragment());
        fragmentList.add(new myself_fragment());

        titleList=new ArrayList<String>();
        titleList.add("录音");
        titleList.add("消息");
        titleList.add("朋友列表");
        titleList.add("我");

        viewPager=(ViewPager) findViewById(R.id.pager);
        MyFragmentPagerAdapter adapter = new MyFragmentPagerAdapter(getSupportFragmentManager(),fragmentList,titleList);
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.Openfile) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
