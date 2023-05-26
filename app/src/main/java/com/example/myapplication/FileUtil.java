package com.example.myapplication;



import android.content.Context;
import android.widget.Toast;

import com.github.mikephil.charting.data.Entry;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by 邹奇 on 2017/7/18.
 * 文件工具类
 */

public class FileUtil {


    public static void saveFile(List<Entry> entries_x, List<Entry> entries_y, List<Entry> entries_z, String file_name, float record_time, DecimalFormat df,Context ctx){
        try {
            FileOutputStream save = ctx.openFileOutput(file_name, ctx.MODE_PRIVATE);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(save));
            for(int i=0;i<entries_x.size();i++){

                StringBuilder sb = new StringBuilder();
                Entry tmp_x = entries_x.get(i);
                Entry tmp_y = entries_y.get(i);
                Entry tmp_z = entries_z.get(i);
                if(tmp_x.getX()<=record_time)continue;
                sb.append(df.format(tmp_x.getX()-record_time)).append(" ")
                        .append(df.format(tmp_x.getY())).append(" ")
                        .append(df.format(tmp_y.getY())).append(" ")
                        .append(df.format(tmp_z.getY())+"\r\n");
                bufferedWriter.append(sb.toString());

                bufferedWriter.flush();// 清理缓冲区的数据流
            }
            bufferedWriter.close();// 关闭输出流
            save.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    /**
     * 删除已存储的文件
     */
    public static void delete_file(String fileName) {
        try {
            // 找到文件所在的路径并删除该文件
            File file = new File("/data/data/com.example.myapplication/files/"+fileName);
            if(file.exists()){
                file.delete();
                //Toast.makeText(MainActivity.this, "存在", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
