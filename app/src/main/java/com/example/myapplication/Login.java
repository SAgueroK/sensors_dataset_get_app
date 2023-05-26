package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Login extends AppCompatActivity{
    Button button_login;
    Button button_register;
    EditText editText_name;
    EditText editText_password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        button_login = findViewById(R.id.button_login);
        button_register = findViewById(R.id.button_register);
        editText_name = findViewById(R.id.user_name);
        editText_password = findViewById(R.id.password);
        Intent intent =getIntent();
        if(intent != null){
            editText_name.setText(intent.getStringExtra("username"));
            editText_password.setText(intent.getStringExtra("password"));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();


        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String username = String.valueOf(editText_name.getText());
                    String password = String.valueOf(editText_password.getText());
                    if(username.isEmpty() || password.isEmpty()){
                        Toast.makeText(Login.this,"用户名和密码不能为空",Toast.LENGTH_SHORT).show();
                    }else{
                        postForm("https://www.aichibingdealang.cn/login", username,password,0);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(Login.this, register.class);
                startActivity(intent);
            }
        });

    }

    private static Request request = null;
    private static Call call = null;
    private static int TimeOut = 120;
    //单例获取ohttp3对象
    private static OkHttpClient client = null;
    /**
     * OkHttpClient的构造方法，通过线程锁的方式构造
     * @return OkHttpClient对象
     */
    private static synchronized OkHttpClient getInstance() {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .readTimeout(TimeOut, TimeUnit.SECONDS)
                    .connectTimeout(TimeOut, TimeUnit.SECONDS)
                    .writeTimeout(TimeOut, TimeUnit.SECONDS)
                    .build();
        }
        return client;
    }

    /**
     * callback接口
     * 异步请求时使用
     */
    static class MyCallBack implements Callback {
        private OkHttpCallback okHttpCallBack;

        public MyCallBack(OkHttpCallback okHttpCallBack) {
            this.okHttpCallBack = okHttpCallBack;
        }

        @Override
        public void onFailure(Call call, IOException e) {
            okHttpCallBack.onFailure(e);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            okHttpCallBack.onSuccess(response);
        }
    }
    /**
     * 异步post请求
     */
    public void postForm(String url, String username ,String password,int type) throws Exception {
        OkHttpClient okHttpClient = getInstance();
        RequestBody formBody = new FormBody.Builder()
                .add("name", username)
                .add("password",password)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback()
        {

            @Override
            public void onFailure(Call call, IOException e)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(Login.this,"失败",Toast.LENGTH_SHORT).show();

                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException
            {
                final String res = response.body().string();;
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {

                        if(type==0&&res.equals("登录成功！")) {
                            Intent intent = new Intent();
                            intent.setClass(Login.this, MainActivity.class);
                            intent.putExtra("username", username);
                            intent.putExtra("password", password);
                            startActivity(intent);
                        }
                        Toast.makeText(Login.this,res,Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}