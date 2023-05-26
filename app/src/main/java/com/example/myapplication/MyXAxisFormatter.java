package com.example.myapplication;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

class MyXAxisFormatter extends ValueFormatter {
    final String [] xAxisName = {"周日","周一","周二","周三","周四","周五","周六"};
    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        return xAxisName[(int) value];
    }
}

