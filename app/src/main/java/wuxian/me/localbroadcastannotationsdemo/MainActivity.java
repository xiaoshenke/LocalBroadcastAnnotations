package wuxian.me.localbroadcastannotationsdemo;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import wuxian.me.localbroadcastannotations.LocalBroadcastAnnotations;
import wuxian.me.localbroadcastannotations.RecevierBinder;
import wuxian.me.localbroadcastannotations.annotation.OnReceive;

public class MainActivity extends AppCompatActivity {
    public static final String ACTION_TEXT_BLUE = "ACTION_TEXT_BLUE";
    public static final String ACTION_TEXT_RED = "ACTION_TEXT_RED";

    private RecevierBinder binder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.tv_blue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(ACTION_TEXT_BLUE);
                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
            }
        });

        findViewById(R.id.tv_red).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(ACTION_TEXT_RED);
                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
            }
        });
    }

    @OnReceive(ACTION_TEXT_BLUE)
    public void onTextBlue(Context context, Intent intent) {

        Toast.makeText(this, "onTextBlue", Toast.LENGTH_LONG).show();
    }

    @OnReceive(ACTION_TEXT_RED)
    public void onTextRed(Context context, Intent intent) {
        Toast.makeText(this, "onTextRed", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (binder == null) {
            binder = new FakeMainActivity$$Binder(this, this);
        }
        //binder.bind(this);
        LocalBroadcastAnnotations.bind(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (binder != null) {
            binder.unbind();
        }
        LocalBroadcastAnnotations.unbind(this);
    }
}
