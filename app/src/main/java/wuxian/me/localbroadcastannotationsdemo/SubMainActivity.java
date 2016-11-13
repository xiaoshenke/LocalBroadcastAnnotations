package wuxian.me.localbroadcastannotationsdemo;

import wuxian.me.localbroadcastannotations.LocalBroadcastAnnotations;

/**
 * Created by wuxian on 13/11/2016.
 */

public class SubMainActivity extends MainActivity {
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
