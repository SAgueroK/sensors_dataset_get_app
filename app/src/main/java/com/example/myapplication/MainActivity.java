package com.example.myapplication;

import static com.example.myapplication.FileUtil.delete_file;
import static com.example.myapplication.FileUtil.saveFile;
import static java.lang.Math.abs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    List<Entry> entries_acc_x = new ArrayList<>();
    List<Entry> entries_acc_y = new ArrayList<>();
    List<Entry> entries_acc_z = new ArrayList<>();
    List<Entry> entries_gyro_x = new ArrayList<>();
    List<Entry> entries_gyro_y = new ArrayList<>();
    List<Entry> entries_gyro_z = new ArrayList<>();
    List<Entry> entries_ori_x = new ArrayList<>();
    List<Entry> entries_ori_y = new ArrayList<>();
    List<Entry> entries_ori_z = new ArrayList<>();

    private SensorManager mSensorManager;
    private TextView mTxtValue_acc;
    private TextView mTxtValue_gyro;
    private TextView mTxtValue_orientation;
    private double time_begin = -1;

    private double time_gap = 5;
    LineData lineData_acc = new LineData();
    LineData lineData_ori = new LineData();
    LineData lineData_gyro = new LineData();
    MyColor myColor = new MyColor();
    private LineChart chart_acc ;
    private LineChart chart_ori ;
    private LineChart chart_gyro ;
    private Button button_file;
    private Button button_upload;
    private Button button_accelerate;
    private Button button_gyroscope;
    private Button button_orientation;
    private TextView textview_username;
    DecimalFormat df = new DecimalFormat("0.000");
    private boolean start_record = false;
    private float record_time ;

    String username ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTxtValue_acc = (TextView) findViewById(R.id.txt_accelerate);
        mTxtValue_gyro = (TextView) findViewById(R.id.txt_gyroscope);
        mTxtValue_orientation = (TextView) findViewById(R.id.txt_orientation);
        // 获取传感器管理对象
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        button_file = findViewById(R.id.button_file);
        button_upload = findViewById(R.id.button_upload);
        chart_all_init();

        Intent intent =getIntent();
        username = intent.getStringExtra("username");
        button_accelerate = findViewById(R.id.button_accelerate);
        button_gyroscope = findViewById(R.id.button_gyroscope);
        button_orientation = findViewById(R.id.button_orientation);
    }
    private void chart_all_init(){
        chart_acc = findViewById(R.id.chart_acc);
        chart_ori = findViewById(R.id.chart_ori);
        chart_gyro = findViewById(R.id.chart_gyro);
        initLine(chart_acc);//设置lineChart的值
        initLine(chart_ori);//设置lineChart的值
        initLine(chart_gyro);//设置lineChart的值
        setChartBasicAttr(chart_acc);
        setChartBasicAttr(chart_ori);
        setChartBasicAttr(chart_gyro);
        EntityClearAndInit();
        lineData_acc.clearValues();
        lineData_gyro.clearValues();
        lineData_ori.clearValues();
        addLine(lineData_acc,entries_acc_x,myColor.LightPink);
        addLine(lineData_acc,entries_acc_y,myColor.DarkSlateBlue);
        addLine(lineData_acc,entries_acc_z,myColor.ForestGreen);
        addLine(lineData_gyro,entries_gyro_x,myColor.LightPink);
        addLine(lineData_gyro,entries_gyro_y,myColor.DarkSlateBlue);
        addLine(lineData_gyro,entries_gyro_z,myColor.ForestGreen);
        addLine(lineData_ori,entries_ori_x,myColor.LightPink);
        addLine(lineData_ori,entries_ori_y,myColor.DarkSlateBlue);
        addLine(lineData_ori,entries_ori_z,myColor.ForestGreen);
        chart_acc.setData(lineData_acc);
        chart_gyro.setData(lineData_gyro);
        chart_ori.setData(lineData_ori);
        chart_acc.notifyDataSetChanged();
        chart_gyro.notifyDataSetChanged();
        chart_ori.notifyDataSetChanged();
        chart_acc.invalidate(); // refresh
        chart_gyro.invalidate(); // refresh
        chart_ori.invalidate(); // refresh

    }
    @Override
    protected void onResume() {
        textview_username = findViewById(R.id.username);
        textview_username.setText("欢迎"+username+"使用");
        button_accelerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chart_gyro.setVisibility(View.GONE);
                chart_ori.setVisibility(View.GONE);
                if(chart_acc.getVisibility()==View.GONE){
                    button_accelerate.setText("隐藏图表");
                    chart_acc.setVisibility(View.VISIBLE);
                }else{
                    button_accelerate.setText("显示图表");
                    chart_acc.setVisibility(View.GONE);
                }
            }
        });
        button_gyroscope.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chart_acc.setVisibility(View.GONE);
                chart_ori.setVisibility(View.GONE);
                if(chart_gyro.getVisibility()==View.GONE){
                    button_gyroscope.setText("隐藏图表");
                    chart_gyro.setVisibility(View.VISIBLE);
                }else{
                    button_gyroscope.setText("显示图表");
                    chart_gyro.setVisibility(View.GONE);
                }
            }
        });
        button_orientation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chart_gyro.setVisibility(View.GONE);
                chart_acc.setVisibility(View.GONE);
                if(chart_ori.getVisibility()==View.GONE){
                    button_orientation.setText("隐藏图表");
                    chart_ori.setVisibility(View.VISIBLE);
                }else{
                    button_orientation.setText("显示图表");
                    chart_ori.setVisibility(View.GONE);
                }
            }
        });
        button_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(start_record==false){
                    start_record = true;
                    double totalMilliSeconds = System.currentTimeMillis() - time_begin;
                    double totalSeconds = totalMilliSeconds /1000;
                    record_time = (float) (totalSeconds % 60);
                    delete_file("acc_data");
                    delete_file("ori_data");
                    delete_file("gyro_data");
                    button_file.setText("结束记录");
                }else{
                    start_record = false;
                    saveFile(entries_acc_x,entries_acc_y,entries_acc_z,"acc_data",record_time,df,MainActivity.this.getBaseContext());
                    saveFile(entries_ori_x,entries_ori_y,entries_ori_z,"ori_data",record_time,df,MainActivity.this.getBaseContext());
                    saveFile(entries_gyro_x,entries_gyro_y,entries_gyro_z,"gyro_data",record_time,df,MainActivity.this.getBaseContext());
                    EntityClearAndInit();
                    button_file.setText("开始记录");
                }
            }
        });
        button_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile(username,"https://www.aichibingdealang.cn/file_upload");
            }
        });
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        // 为方向传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
        // 为陀螺仪传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_GAME);
        // 为线性加速度传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME);
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 取消监听
        mSensorManager.unregisterListener(this);
    }

    // 当传感器的值改变的时候回调该方法
    @SuppressLint("DefaultLocale")
    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;
        // 获取传感器类型
        int type = event.sensor.getType();
        if(time_begin == -1){
            time_begin =  System.currentTimeMillis();
        }
        double totalMilliSeconds = System.currentTimeMillis() - time_begin;
        double totalSeconds = totalMilliSeconds /1000;
        double currentSecond = totalSeconds % 60;
        if(totalMilliSeconds != 0 && currentSecond == 0){
            EntityClearAndInit();
        }
        StringBuilder sb;
        switch (type){
            case Sensor.TYPE_LINEAR_ACCELERATION:
                sb = new StringBuilder();
                sb.append("线性加速度传感器返回数据：");
                sb.append("\nX方向的加速度：");
                sb.append(df.format(values[0]));
                sb.append("\nY方向的加速度：");
                sb.append( df.format(values[1]));
                sb.append("\nZ方向的加速度：");
                sb.append(df.format(values[2])) ;
                sb.append("\n时间：");
                sb.append(df.format(currentSecond)) ;
                mTxtValue_acc.setText(sb.toString());
                addEntry(lineData_acc,chart_acc,values[0],values[1],values[2], Float.parseFloat(df.format(currentSecond)),type);

                break;
            case Sensor.TYPE_ORIENTATION:
                sb = new StringBuilder();
                sb.append("\n方向传感器返回数据：");
                sb.append("\n绕x轴转过的角度：");
                sb.append(values[0]);
                sb.append("\n绕y轴转过的角度：");
                sb.append(values[1]);
                sb.append("\n绕z轴转过的角度：");
                sb.append(values[2]);
                mTxtValue_orientation.setText(sb.toString());
                addEntry(lineData_ori,chart_ori,values[0],values[1],values[2], Float.parseFloat(df.format(currentSecond)),type);
                break;
            case Sensor.TYPE_GYROSCOPE:
                sb = new StringBuilder();
                sb.append("\n陀螺仪传感器返回数据：");
                sb.append("\n绕X轴旋转的角速度：");
                sb.append(values[0]);
                sb.append("\n绕Y轴旋转的角速度：");
                sb.append( values[1]);
                sb.append("\n绕Z轴旋转的角速度：");
                sb.append(values[2]);
                mTxtValue_gyro.setText(sb.toString());
                addEntry(lineData_gyro,chart_gyro,values[0],values[1],values[2], Float.parseFloat(df.format(currentSecond)),type);
                break;
        }



    }

    // 当传感器精度发生改变时回调该方法
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void addLine(LineData lineData, List<Entry> entries, int color){
        LineDataSet dataSet = new LineDataSet(entries, "Label"); // add entries to dataset
        dataSet.setColor(color);//线条颜色
        dataSet.setDrawValues(false);                     // 设置是否显示数据点的值
        dataSet.setDrawCircleHole(false);                 // 设置数据点是空心还是实心，默认空心
        dataSet.setCircleColor(color);              // 设置数据点的颜色
        dataSet.setLineWidth(1f);
        dataSet.setCircleRadius(1f); // 设置折现点圆点半径
        dataSet.setHighLightColor(color);            // 设置点击时高亮的点的颜色
        dataSet.setDrawCircles(false);//在点上画圆 默认true
        lineData.addDataSet(dataSet);
    }
    private void addEntry(LineData lineData,LineChart chart,float y_x,float y_y,float y_z,float time,int type){
        Entry entry_x = new Entry(time, y_x); // 创建一个点
        Entry entry_y = new Entry(time, y_y);
        Entry entry_z = new Entry(time, y_z);
        if(type == Sensor.TYPE_ORIENTATION &&y_x==0){
            Toast.makeText(MainActivity.this, y_x + "", Toast.LENGTH_SHORT).show();
        }
        lineData.addEntry(entry_x, 0); // 将entry添加到指定索引处的折线中
        lineData.addEntry(entry_y, 1);
        lineData.addEntry(entry_z, 2);

        //通知数据已经改变
        lineData.notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();
        //把yValues移到指定索引的位置
       // chart.moveViewToAnimated(time,y_x , YAxis.AxisDependency.LEFT, 1000);// TODO: 2019/5/4 内存泄漏，异步 待修复

    }
    private void initLine(LineChart chart){
        //设置样式
        YAxis rightAxis = chart.getAxisRight();
        //设置图表右边的y轴禁用
        rightAxis.setEnabled(false);
        YAxis leftAxis = chart.getAxisLeft();
        //设置图表左边的y轴禁用
        leftAxis.setEnabled(true);
        leftAxis.setTextColor(Color.parseColor("#333333"));

        //设置x轴
        XAxis xAxis = chart.getXAxis();
        xAxis.setTextColor(Color.parseColor("#333333"));
        xAxis.setTextSize(11f);
        xAxis.setAxisMinimum(0f);
        xAxis.setDrawAxisLine(true);//是否绘制轴线
        xAxis.setDrawGridLines(false);//设置x轴上每个点对应的线
        xAxis.setDrawLabels(true);//绘制标签  指x轴上的对应数值
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);//设置x轴的显示位置
        xAxis.setGranularity(1f);//禁止放大后x轴标签重绘

        //final String [] xAxisName = {"周日","周一","周二","周三","周四","周五","周六"};
        //xAxis.setValueFormatter(new MyXAxisFormatter());
        //透明化图例
        Legend legend = chart.getLegend();
        legend.setForm(Legend.LegendForm.NONE);
        legend.setTextColor(Color.WHITE);

        //隐藏x轴描述
        Description description = new Description();
        description.setEnabled(false);
        chart.setDescription(description);
    }

    /**
     * 功能：设置图标的基本属性
     */
    void setChartBasicAttr(LineChart lineChart) {
        /***图表设置***/
        lineChart.setDrawGridBackground(false); //是否展示网格线
        lineChart.setDrawBorders(true); //是否显示边界
        lineChart.setDragEnabled(true); //是否可以拖动
        lineChart.setScaleEnabled(true); // 是否可以缩放
        lineChart.setTouchEnabled(true); //是否有触摸事件
        //设置XY轴动画效果
        //lineChart.animateY(2500);
        lineChart.animateX(1500);
    }
    private void EntityClearAndInit(){
        entries_acc_x.clear();
        entries_acc_y.clear();
        entries_acc_z.clear();
        entries_ori_x.clear();
        entries_ori_y.clear();
        entries_ori_z.clear();
        entries_gyro_x.clear();
        entries_gyro_y.clear();
        entries_gyro_z.clear();
        time_begin = -1;
        lineData_acc.notifyDataChanged();
        lineData_ori.notifyDataChanged();
        lineData_gyro.notifyDataChanged();
        chart_ori.notifyDataSetChanged();
        chart_acc.notifyDataSetChanged();
        chart_gyro.notifyDataSetChanged();
        chart_acc.invalidate();
        chart_ori.invalidate();
        chart_gyro.invalidate();
    }
    // 使用OkHttp上传文件
    public  void uploadFile(String username,String url) {
        File file_acc = new File("/data/data/com.example.myapplication/files/"+"acc_data");
        File file_ori = new File("/data/data/com.example.myapplication/files/"+"ori_data");
        File file_gyro = new File("/data/data/com.example.myapplication/files/"+"gyro_data");
        OkHttpClient client = new OkHttpClient();
        RequestBody fileBody_acc = RequestBody.create(MediaType.parse("multipart/form-data"), file_acc);
        RequestBody fileBody_ori = RequestBody.create(MediaType.parse("multipart/form-data"), file_ori);
        RequestBody fileBody_gyro = RequestBody.create(MediaType.parse("multipart/form-data"), file_gyro);
        // 不仅可以支持传文件，还可以在传文件的同时，传参数
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", username)
                .addFormDataPart("file_acc_data", "acc_data", fileBody_acc)
                .addFormDataPart("file_ori_data", "ori_data", fileBody_ori)
                .addFormDataPart("file_gyro_data", "gyro_data", fileBody_gyro)
                .build();
        Request request = new Request.Builder().url(url).post(requestBody).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 文件上传成功
                final String res = response.body().string();;
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(MainActivity.this,res,Toast.LENGTH_SHORT).show();

                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(MainActivity.this,"文件上传失败",Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }
}