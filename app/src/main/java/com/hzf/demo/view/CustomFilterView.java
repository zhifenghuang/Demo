package com.hzf.demo.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hzf.demo.R;
import com.hzf.demo.utils.Utils;

import java.io.File;

/**
 * Created by gigabud on 16-8-10.
 */
public class CustomFilterView extends RelativeLayout {

    public enum FilterType {
        TYPE_NONE,
        TYPE_TIME,
        TYPE_DATE,
        TYPE_TEMP,
        TYPE_IMAGE,
        TYPE_OPEN_GPS
    }

    private FilterType mFilterType;

    public CustomFilterView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.custom_filter_view, this);
    }

    public void setFilterType(FilterType filterType) {
        mFilterType = filterType;
        RelativeLayout rlTime = findViewById(R.id.rlTime);
        TextView tvTemp = findViewById(R.id.tvTemp);
        ImageView iv = findViewById(R.id.iv);
        RelativeLayout rlEnableGPs = findViewById(R.id.rlEnableGPS);
        setBackgroundColor(Color.TRANSPARENT);
        rlEnableGPs.setVisibility(View.GONE);
        rlTime.setVisibility(View.GONE);
        tvTemp.setVisibility(View.GONE);
        iv.setVisibility(View.GONE);
        if (mFilterType == FilterType.TYPE_TIME) {
            rlTime.setVisibility(View.VISIBLE);
        } else if (mFilterType == FilterType.TYPE_DATE) {
            rlTime.setVisibility(View.VISIBLE);
        } else if (mFilterType == FilterType.TYPE_TEMP) {
            tvTemp.setVisibility(View.VISIBLE);
        } else if (mFilterType == FilterType.TYPE_IMAGE) {
            iv.setVisibility(View.VISIBLE);
        } else if (mFilterType == FilterType.TYPE_OPEN_GPS) {
            setBackgroundColor(getResources().getColor(R.color.half_transparent));
            rlEnableGPs.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.tvEnableGPS)).setText("开启定位");
            ((TextView) findViewById(R.id.tvUnlockFilters)).setText("想使用更多滤镜？");
            ((TextView) findViewById(R.id.tvEnableToSee)).setText("开启定位可以使用更多有趣的滤镜！");
            findViewById(R.id.tvEnableGPS).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(
                            Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    getContext().startActivity(intent);
                }
            });
        }
    }

    public void setText(Typeface... typeface) {
        if (mFilterType == FilterType.TYPE_TIME) {
            findViewById(R.id.rlDate).setVisibility(View.GONE);
            TextView tvTime = findViewById(R.id.tvTime);
            tvTime.setTypeface(typeface[0]);
            boolean is24Format = android.text.format.DateFormat.is24HourFormat(getContext());
            tvTime.setText(Utils.getTimeStrOnlyHourBySystem(getContext(), is24Format));
            TextView tvAMOrPM = findViewById(R.id.tvAMOrPM);
            tvAMOrPM.setTypeface(typeface[0]);
            if (!is24Format) {
                tvAMOrPM.setVisibility(View.VISIBLE);
                tvAMOrPM.setText(Utils.geAMOrPM());
            } else {
                tvAMOrPM.setVisibility(View.GONE);
            }
        } else if (mFilterType == FilterType.TYPE_DATE) {
            findViewById(R.id.rlDate).setVisibility(View.VISIBLE);
            TextView tvTime = findViewById(R.id.tvTime);
            tvTime.setTypeface(typeface[0]);
            boolean is24Format = android.text.format.DateFormat.is24HourFormat(getContext());
            tvTime.setText(Utils.getTimeStrOnlyHourBySystem(getContext(), is24Format));
            TextView tvAMOrPM = findViewById(R.id.tvAMOrPM);
            tvAMOrPM.setTypeface(typeface[0]);
            if (!is24Format) {
                tvAMOrPM.setVisibility(View.VISIBLE);
                tvAMOrPM.setText(Utils.geAMOrPM());
            } else {
                tvAMOrPM.setVisibility(View.GONE);
            }
            TextView tvWeekDay = findViewById(R.id.tvWeekday);
            tvWeekDay.setTypeface(typeface[1]);
            tvWeekDay.setText(Utils.getWeek());
            TextView tvDate = findViewById(R.id.tvDate);
            tvDate.setTypeface(typeface[2]);
            tvDate.setText(Utils.getMonth());
        } else if (mFilterType == FilterType.TYPE_TEMP) {
            TextView tvTemp = findViewById(R.id.tvTemp);
            tvTemp.setTypeface(typeface[0]);
            tvTemp.setText(getCurrentTemperature());
        }
    }



    private String getCurrentTemperature() {
        return "30°C";
    }

    public void setImage(String photoPath, int viewWidth, int viewHeight) {
        Utils.loadImage(new File(photoPath), 0, "", (ImageView) findViewById(R.id.iv));
    }
}
