package com.megatech.fms.model;

import android.content.Context;
import android.content.SharedPreferences;

public class UserInfo {
    public UserInfo() {
    }

    public UserInfo(int userId, String userName, String token) {
        this.userId = userId;
        this.userName = userName;
        this.token = token;
    }

    private int userId = 0;
    private String userName = "";

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    private String token ="";

    public  String getUserName(){
        return  userName;

    }

    public int getUserId(){
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;

    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void addToSharePreferences(Context ctx)
    {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("FMS",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("USER_NAME", this.userName);
        editor.putInt("USER_ID", this.userId);
        editor.putString("TOKEN", this.token);
        editor.commit();
    }
    public void readFromSharedPreferences(Context ctx)
    {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("FMS",Context.MODE_PRIVATE);
        this.userId = sharedPreferences.getInt("USER_ID", 0);
        this.userName = sharedPreferences.getString("USER_NAME", "");
        this.token = sharedPreferences.getString("TOKEN", "");

    }

    public static UserInfo fromSharedPreferences(Context ctx){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("FMS",Context.MODE_PRIVATE);
        UserInfo user = new UserInfo();
        user.userId = sharedPreferences.getInt("USER_ID", 0);
        user.userName = sharedPreferences.getString("USER_NAME", "");
        user.token = sharedPreferences.getString("TOKEN", "");
        return user;
    }
    public static void logout(Context ctx){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("FMS",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("USER_NAME");
        editor.remove("USER_ID");
        editor.remove("TOKEN");
        editor.commit();
    }
}
