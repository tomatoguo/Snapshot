package com.guomato.snapshot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MenuAdapter extends ArrayAdapter<String> {

    private int mSelectPosition;

    private int mMenuItemLayoutResource;

    private ArrayList<String> mAlbumItems;

    public MenuAdapter(Context context, int menuItemLayoutResource, ArrayList<String> albumItems) {
        super(context, R.layout.menu_item, albumItems);

        mAlbumItems = albumItems;
        mSelectPosition = 0;
        mMenuItemLayoutResource = menuItemLayoutResource;
    }

    @Override
    public int getCount() {
        return mAlbumItems.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder VH;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(mMenuItemLayoutResource, parent, false);

            VH = new ViewHolder();
            VH.titleTextView = (TextView) convertView.findViewById(R.id.menu_item);

            convertView.setTag(VH);
        } else {
            VH = (ViewHolder) convertView.getTag();
        }

        VH.titleTextView.setText(mAlbumItems.get(position));
        VH.titleTextView.setTextColor(getContext().getResources().getColor(position == mSelectPosition ? android.R.color.white : android.R.color.darker_gray));
        VH.titleTextView.setBackgroundResource(position == mSelectPosition ? android.R.color.darker_gray : android.R.color.white);

        return convertView;
    }

    /**
     * 设置列表的当前选中位置，并通知ListView数据变更
     *
     * @param selectPosition 要设置的选中位置
     */
    public void setSelectPosition(int selectPosition) {
        mSelectPosition = selectPosition;

        notifyDataSetChanged();
    }

    /**
     * 获得列表的当前选中位置
     *
     * @return 列表当前选中位置
     */
    public int getSelectPosition() {
        return mSelectPosition;
    }

    private class ViewHolder {
        TextView titleTextView;
    }
}
