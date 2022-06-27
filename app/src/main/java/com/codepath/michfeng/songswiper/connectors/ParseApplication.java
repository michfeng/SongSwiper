package com.codepath.michfeng.songswiper.connectors;

import android.app.Application;

import com.codepath.michfeng.songswiper.models.Post;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Register parse models
        ParseObject.registerSubclass(Post.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("T7DI0iX9koclfF8MFLpJyoCAnhLvI5N6Xs2kLG5V")
                .clientKey("zJ3ZmUfq4Iz15F9YlL1aTwKD85c8g6WQ3QZRPF9M")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
