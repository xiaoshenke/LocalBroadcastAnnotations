package wuxian.me.localbroadcastannotations;

import android.content.Context;
import android.support.annotation.Nullable;

/**
 * Created by wuxian on 11/11/2016.
 * TODO
 */

public class LocalBroadcastAnnotations {

    public static void bind(@Nullable Context context) {
        bind(context, context);
    }

    public static void bind(@Nullable Object target, @Nullable Context context) {
        ;
    }

    public static void unbind(@Nullable Context context) {
        ;
    }

}
