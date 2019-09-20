package com.hzf.demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.giphy.sdk.core.models.Image;
import com.giphy.sdk.core.models.Media;
import com.hzf.demo.R;
import com.hzf.demo.utils.Utils;

import java.util.List;

/**
 * Created by gigabud on 17-7-5.
 */

public class GifAdapter extends BaseAdapter {
    private Context mConext;
    private List<Media> mDataList;
    private int mNum;

    public GifAdapter(Context context, int num) {
        mConext = context;
        this.mNum = num;
    }


    @Override
    public int getCount() {
        return mDataList == null ? mNum : mDataList.size() + mNum;
    }

    public int getGifCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    @Override
    public String getItem(int position) {
        return position < mDataList.size() ? mDataList.get(position).getImages().getFixedWidth().getGifUrl() : "";
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mConext).inflate(R.layout.item_gif, null);
            viewHolder.iv = convertView.findViewById(R.id.iv);
            viewHolder.progress_bar = convertView.findViewById(R.id.progress_bar);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (mDataList != null && position < mDataList.size()) {
            viewHolder.iv.setVisibility(View.VISIBLE);
            viewHolder.progress_bar.setVisibility(View.VISIBLE);
            Image image = mDataList.get(position).getImages().getFixedWidth();
            Utils.loadImage(0, image.getGifUrl(), viewHolder.iv);
        } else {
            viewHolder.iv.setVisibility(View.INVISIBLE);
            viewHolder.progress_bar.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    public void setDataList(List<Media> dataList) {
        mDataList = dataList;
        if (mDataList != null) {
            Image image;
            for (int i = 0; i < mDataList.size(); ) {
                image = mDataList.get(i).getImages().getFixedWidth();
                if (image.getWidth() == 0 || image.getHeight() == 0) {
                    mDataList.remove(i);
                    continue;
                }
                image = mDataList.get(i).getImages().getOriginal();
                if (image.getWidth() == 0 || image.getHeight() == 0) {
                    mDataList.remove(i);
                    continue;
                }
                ++i;
            }
        }
        notifyDataSetChanged();
    }

    class ViewHolder {
        ImageView iv;
        ProgressBar progress_bar;
    }
}
