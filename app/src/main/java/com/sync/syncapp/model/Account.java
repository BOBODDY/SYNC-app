package com.sync.syncapp.model;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;

/**
 * Created by nick on 6/29/15.
 */
public class Account {

    String userId;
    String connection;
    String email;
    String provider;
    String nickname;
    String pictureUrl;

    public Account(String userId, String connection, String email, String provider, String nickname, String pictureUrl) {
        this.userId = userId;
        this.connection = connection;
        this.email = email;
        this.provider = provider;
        this.nickname = nickname;
        this.pictureUrl = pictureUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
}
