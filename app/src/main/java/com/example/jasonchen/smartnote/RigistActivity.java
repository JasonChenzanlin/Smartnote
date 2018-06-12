package com.example.jasonchen.smartnote;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class RigistActivity extends AppCompatActivity {

    EditText useremail;
    EditText username;
    EditText userpassword;
    Button Rigsit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rigist);
        init();
    }

    public void init(){
        useremail= (EditText) findViewById(R.id.Rigist_useremail);
        username= (EditText) findViewById(R.id.Rigist_username);
        userpassword= (EditText) findViewById(R.id.Rigist_password);
    }

    public void Rigist(View view){
        BmobUser bu = new BmobUser();
        bu.setUsername(username.getText().toString());
        bu.setPassword(userpassword.getText().toString());
        bu.setEmail(useremail.getText().toString());
//注意：不能用save方法进行注册
        bu.signUp(new SaveListener<BmobUser>() {
            @Override
            public void done(BmobUser s, BmobException e) {
                if(e==null){
                    Toast.makeText(RigistActivity.this,"注册成功:" +s.toString(),Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(RigistActivity.this,e.toString(),Toast.LENGTH_LONG).show();
                }
            }
        });

        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
    }

}
