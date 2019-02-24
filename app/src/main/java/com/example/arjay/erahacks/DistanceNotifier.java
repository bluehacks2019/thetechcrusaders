package com.example.arjay.erahacks;

import android.app.NotificationManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class DistanceNotifier extends AppCompatActivity {

    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distance_notifier);


        linearLayout = (LinearLayout)findViewById(R.id.linearNotifier);



        final MediaPlayer ring= MediaPlayer.create(DistanceNotifier.this,R.raw.notify);
        ring.start();
        ring.setLooping(true);

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ring.stop();
                finish();
                return;
            }
        });



    }
}
