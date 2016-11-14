package wuxian.me.localbroadcastannotationsdemo;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import wuxian.me.localbroadcastannotations.LocalBroadcastAnnotations;
import wuxian.me.localbroadcastannotations.annotation.OnReceive;

/**
 * Created by wuxian on 13/11/2016.
 */

public class SubMainActivity extends MainActivity {
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastAnnotations.bind(this);
    }

    @OnReceive(value = ACTION_TEXT_BLUE)
    public void onTextBlue(Context context, Intent intent) {
        Toast.makeText(this, "onTextBlue from sub class", Toast.LENGTH_LONG).show();
    }

}
