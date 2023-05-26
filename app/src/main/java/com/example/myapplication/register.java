package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class register extends AppCompatActivity {
    Button button_register;
    EditText editText_name;
    EditText editText_password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        button_register = findViewById(R.id.button_register);
        editText_name = findViewById(R.id.user_name);
        editText_password = findViewById(R.id.password);
    }
    @Override
    protected void onResume() {
        super.onResume();
        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String username = String.valueOf(editText_name.getText());
                    String password = String.valueOf(editText_password.getText());
                    if(username.isEmpty() || password.isEmpty()){
                        Toast.makeText(register.this,"注册的用户名和密码不能为空",Toast.LENGTH_SHORT).show();
                    }else {
                        postForm("https://www.aichibingdealang.cn/register", username, password, 1);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }
    private static Request request = null;
    private static Call call = null;
    private static int TimeOut = 120;
    //单例获取ohttp3对象
    private static OkHttpClient client = null;
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
                        Toast.makeText(register.this,"失败",Toast.LENGTH_SHORT).show();

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

                        if(type==1&&res.equals("注册成功！")) {
                            Intent intent = new Intent();
                            intent.setClass(register.this, Login.class);
                            intent.putExtra("username", username);
                            intent.putExtra("password", password);
                            startActivity(intent);
                        }
                        Toast.makeText(register.this,res,Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}