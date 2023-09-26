package com.example.rosproject.Core;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rosproject.R;

import java.util.List;

public class NavDrawerAdapter extends ArrayAdapter<DrawerItem> {
    Context context;
    List<DrawerItem> drawerItemList;
    int layoutResID;

    public NavDrawerAdapter(Context context, int layoutResourceID, List<DrawerItem> listItems){
        super(context,layoutResourceID, listItems);
        this.context = context;
        this.drawerItemList = listItems;
        this.layoutResID = layoutResourceID;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        DrawerItemHolder drawerHolder;
        View view = convertView;

        if(view == null){
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            drawerHolder = new DrawerItemHolder();
            view = inflater.inflate(layoutResID, parent, false);
            drawerHolder.ItemName = (TextView) view.findViewById(R.id.nav_drawer_item_text_view);
            drawerHolder.icon = (ImageView) view.findViewById(R.id.nav_drawer_item_image_view);

            view.setTag(drawerHolder);
        }else{
            drawerHolder = (DrawerItemHolder) view.getTag();
        }

        DrawerItem dItem = this.drawerItemList.get(position);

        drawerHolder.icon.setImageDrawable(view.getResources().getDrawable(dItem.getImgResID()));
        drawerHolder.ItemName.setText(dItem.getItemName());

        return view;
    }

    private static class DrawerItemHolder{
        TextView ItemName;
        ImageView icon;
    }
}
