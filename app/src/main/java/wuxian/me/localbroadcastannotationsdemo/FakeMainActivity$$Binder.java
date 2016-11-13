package wuxian.me.localbroadcastannotationsdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import wuxian.me.localbroadcastannotations.RecevierBinder;
import wuxian.me.localbroadcastannotations.annotation.OnReceive;
import wuxian.me.localbroadcastannotations.compiler.AnnotatedMethod;
import wuxian.me.localbroadcastannotations.compiler.AnnotatedMethodsPerClass;

/**
 * Created by wuxian on 12/11/2016.
 *
 * fake binder,implement by runtime reflection.
 */

public class FakeMainActivity$$Binder implements RecevierBinder<MainActivity> {

    private Context context;
    private Map<Integer, Method> methodMap = new HashMap<>();

    public FakeMainActivity$$Binder(Context context, MainActivity target) {
        this.context = context;
        Class<?> clazz = target.getClass();

        for (Method method : clazz.getDeclaredMethods()) {
            OnReceive onReceive = method.getAnnotation(OnReceive.class);
            if (onReceive == null) {
                continue;
            }
            int id = AnnotatedMethodsPerClass.generateId(onReceive.value(), onReceive.category());
            if (methodMap.containsKey(id)) {
                continue;
            } else {
                methodMap.put(id, method);
            }
        }
    }

    @Override
    public void bind(final MainActivity target) {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Set<String> categories = intent.getCategories();
                String category = AnnotatedMethod.NONE;
                if (categories != null && categories.size() != 0) {
                    category = categories.iterator().next();  //拿到第一个category
                }
                int id = AnnotatedMethodsPerClass.generateId(action, category);
                if (methodMap.containsKey(id)) {
                    try {
                        methodMap.get(id).invoke(target, new Object[]{context, intent});
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.ACTION_TEXT_BLUE);
        filter.addAction(MainActivity.ACTION_TEXT_RED);
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter); //intentfilter 已经被初始化

    }

    @Override
    public void unbind() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(null);

    }
}
