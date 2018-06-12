package com.example.jasonchen.smartnote;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jasonchen.smartnote.base.BaseActivity;
import com.example.jasonchen.smartnote.bean.User;
import com.example.jasonchen.smartnote.model.UserModel;

import java.io.IOException;

import butterknife.OnClick;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.SaveListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends BaseActivity {
//    private TextView issuccess;
    private EditText username;
    private EditText password;
    private CheckBox checksaveid;
    private CheckBox checksavepassword;
    private Button loginButton;
    private SharedPreferences userpref;
    private SharedPreferences.Editor editor;
    private String mUrl ="http://192.168.191.1:8080/SmartNoteServer/";
    private OkHttpClient okHttpClient = new OkHttpClient();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginlayout);
        Bmob.initialize(this, "06fe6fe9e15b76f41cf716a5ebae8de6");
        init();
        SQLiteDatabase db = openOrCreateDatabase("SmartNote.db", MODE_PRIVATE, null);
        db.execSQL("create table if not exists userfile (userid text not null, filepath text not null )");
    }

    public void init(){
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        checksaveid = (CheckBox) findViewById(R.id.checkSaveid);
        checksavepassword = (CheckBox) findViewById(R.id.checkSavePassword);
//        issuccess = (TextView) findViewById(R.id.issucces);
        password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        userpref = getSharedPreferences("UserInfo",MODE_PRIVATE);
        editor = userpref.edit();
        String Id = userpref.getString("username","");
        String pw = userpref.getString("password","");
        if (Id == null){
            checksaveid.setChecked(false);
        } else {
            checksaveid.setChecked(true);
            username.setText(Id);
        }

        if (pw == null){
            checksavepassword.setChecked(false);
        } else {
            checksavepassword.setChecked(true);
            password.setText(pw);
        }
    }

    public void Login(View view){
        UserModel.getInstance().login(username.getText().toString(), password.getText().toString(), new LogInListener() {

            @Override
            public void done(Object o, BmobException e) {
                if (e == null) {
                    //登录成功
                    startActivity(MainActivity.class, null, true);
                } else {
                    Toast.makeText(LoginActivity.this,e.getMessage() + "(" + e.getErrorCode() + ")",Toast.LENGTH_LONG);
                }
            }
        });
    }

    public void Rigist(View view){
        Intent intent = new Intent(this,RigistActivity.class);
        startActivity(intent);
    }




//    private Boolean executellgin(String id, String pw) {
//        FormBody.Builder builder= new FormBody.Builder();
//        builder.add("userid", id).add("password",pw);
//        Request request= new Request.Builder()
//                .url(mUrl+"login")
//                .post(builder.build())
//                .build();
//        Call call=okHttpClient.newCall(request);
//
//        call.enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                Log.d("Failure","Failure"+e.getMessage());
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                final String res = response.body().string();
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        issuccess.setText(res);
//                    }
//                });
//            }
//        });
//        return true;
//    }


}
