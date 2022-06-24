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
                .applicationId("sRWg3HB1JvKhP0Y67fdOSQ7WhHUfdj0hWWDTW5R3")
                .clientKey("sSSm3KqdwCfPjeF3aRfe298lAwire5MMthEsfBIf")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
