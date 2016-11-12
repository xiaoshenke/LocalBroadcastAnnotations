package wuxian.me.localbroadcastannotationsdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import wuxian.me.localbroadcastannotations.LocalBroadcastAnnotations;
import wuxian.me.localbroadcastannotations.RecevierBinder;
import wuxian.me.localbroadcastannotations.annotation.OnReceive;

public class MainActivity extends AppCompatActivity {
    private static final String ACTION_TEXT_BLUE = "ACTION_TEXT_BLUE";
    private static final String ACTION_TEXT_RED = "ACTION_TEXT_RED";

    private RecevierBinder binder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @OnReceive(ACTION_TEXT_BLUE)
    void onTextBlue() {
        Toast.makeText(this, "onTextBlue", Toast.LENGTH_LONG).show();
    }

    @OnReceive(ACTION_TEXT_RED)
    void onTextRed() {
        Toast.makeText(this, "onTextRed", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (binder == null) {
            binder = new MainActivity$$Binder();
        }
        binder.bind(this);
        //LocalBroadcastAnnotations.bind(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (binder != null) {
            binder.unbind();
        }
        //LocalBroadcastAnnotations.unbind(this);
    }
}
