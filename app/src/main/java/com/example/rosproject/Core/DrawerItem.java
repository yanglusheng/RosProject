package com.example.rosproject.Core;

public class DrawerItem {
    String itemName;
    int imgResID;

    public DrawerItem(String itemName, int imgResID){
        super();
        this.itemName = itemName;
        this.imgResID = imgResID;
    }

    public String getItemName(){
        return itemName;
    }

    public void setItemName(String itemName){
        this.itemName = itemName;
    }

    public int getImgResID(){
        return imgResID;
    }

    public void setImgResID(int imgResID){
        this.imgResID = imgResID;
    }
}
