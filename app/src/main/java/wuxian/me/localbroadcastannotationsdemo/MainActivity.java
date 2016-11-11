package wuxian.me.localbroadcastannotationsdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import wuxian.me.localbroadcastannotations.LocalBroadcastAnnotations;
import wuxian.me.localbroadcastannotations.annotation.OnReceive;

public class MainActivity extends AppCompatActivity {
    private static final String ACTION_TEXT_BLUE = "ACTION_TEXT_BLUE";
    private static final String ACTION_TEXT_RED = "ACTION_TEXT_RED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @OnReceive(ACTION_TEXT_BLUE)
    void onTextBlue() {
        ;
    }

    @OnReceive(ACTION_TEXT_RED)
    void onTextRed() {
        ;
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastAnnotations.bind(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastAnnotations.unbind(this);
    }
}
