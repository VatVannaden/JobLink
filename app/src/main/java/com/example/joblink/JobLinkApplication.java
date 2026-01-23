package com.example.joblink;

import android.app.Application;
import android.util.Log;
import com.google.firebase.database.FirebaseDatabase;

public class JobLinkApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            // Enable disk persistence so data loads instantly from cache
//            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            
            // REMOVED: keepSynced(true) on "posts" node. 
            // If the database has many posts, this can cause the app to hang on start 
            // while trying to sync the entire node, blocking other operations.
            // FirebaseDatabase.getInstance().getReference("posts").keepSynced(true);
            
        } catch (Exception e) {
            Log.e("JobLinkApp", "Firebase initialization error", e);
        }
    }
}
