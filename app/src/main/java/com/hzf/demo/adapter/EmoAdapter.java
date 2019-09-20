package com.hzf.demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.hzf.demo.R;
import com.hzf.demo.utils.Utils;

/**
 * Created by gigabud on 15-12-2.
 */
public class EmoAdapter extends BaseAdapter {

    private Context mConext;
    private String[] resStickersArray;
    private int mNum;

    public EmoAdapter(Context context, String[] resStickersArray, int num) {
        mConext = context;
        this.resStickersArray = resStickersArray;
        this.mNum = num;
    }


    @Override
    public int getCount() {
        return resStickersArray == null ? mNum : resStickersArray.length + mNum;
    }

    @Override
    public Object getItem(int position) {
        return position < resStickersArray.length ? resStickersArray[position] : "";
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
            convertView = LayoutInflater.from(mConext).inflate(R.layout.layout_item_emo, null);
            viewHolder.mIv = convertView.findViewById(R.id.ivEmo);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if(position<resStickersArray.length) {
            viewHolder.mIv.setVisibility(View.VISIBLE);
            viewHolder.mIv.setImageResource(Utils.getDrawableIdByName(resStickersArray[position]));
        }else{
            viewHolder.mIv.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    class ViewHolder {
        ImageView mIv;
    }
}
