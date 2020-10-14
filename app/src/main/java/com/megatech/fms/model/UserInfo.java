package com.megatech.fms.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class UserInfo {
    public UserInfo() {
    }

    public UserInfo(int userId, String userName, String token, int permission) {
        this.userId = userId;
        this.userName = userName;
        this.token = token;
        this.permission = permission;
        this.lastLogin = new Date();

    }

    private Date lastLogin;
    private int userId = 0;
    private String userName = "";

    private int permission;

    public int getPermission() {
        return permission;
    }

    public void setPermission(int permission) {
        this.permission = permission;
    }

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
        editor.putLong("LOGIN_TIME", (new Date()).getTime());
        editor.putInt("PERMISSION", this.permission);
        editor.commit();
    }
    public void readFromSharedPreferences(Context ctx)
    {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("FMS",Context.MODE_PRIVATE);
        this.userId = sharedPreferences.getInt("USER_ID", 0);
        this.userName = sharedPreferences.getString("USER_NAME", "");
        this.token = sharedPreferences.getString("TOKEN", "");
        this.permission = sharedPreferences.getInt("PERMISSION", 0);

    }

    public static UserInfo fromSharedPreferences(Context ctx){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("FMS",Context.MODE_PRIVATE);
        UserInfo user = new UserInfo();
        user.userId = sharedPreferences.getInt("USER_ID", 0);
        user.userName = sharedPreferences.getString("USER_NAME", "");
        user.token = sharedPreferences.getString("TOKEN", "");
        user.permission = sharedPreferences.getInt("PERMISSION", 0);
        return user;
    }
    public static void logout(Context ctx){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("FMS",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("USER_NAME");
        editor.remove("USER_ID");
        editor.remove("TOKEN");
        editor.remove("LOGIN_TIME");
        editor.commit();
    }

    public enum USER_PERMISSION {
        @SerializedName("0") NONE(0),
        @SerializedName("1") CREATE_REFUEL(1),
        @SerializedName("2") CREATE_EXTRACT(2),
        @SerializedName("4") CREATE_CUSTOMER(4);
        private int value;

        USER_PERMISSION(int i) {
            value = i;
        }

        public int getValue() {
            return value;
        }
    }
}
