package wuxian.me.localbroadcastannotations;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import wuxian.me.localbroadcastannotations.annotation.OnReceive;

/**
 * Created by wuxian on 11/11/2016.
 * <p>
 * 当涉及到继承的时候,
 * 1 子类的bind函数生成的$$Binder类应该能够响应父类的OnReceive注解的函数,(一种更加特殊的情况是子类中没有新的OnReceive函数...)
 * 2 且这个Binder类应该具有这样的继承关系 SubWhatever$$Binder extends SuperWhatever$$Binder
 * 3 子类和父类应该共享同一个receiver 不然各自拥有一个receiver带来同时响应同一个父类中的OnReceive注解函数,显然是不合逻辑的
 */

public class LocalBroadcastAnnotations {
    private static final String ANDROID_PREFIX = "android.";
    private static final String JAVA_PREFIX = "java.";
    private static final String SUFFIX = "$$ReceiverBinder";

    static final Map<Class<?>, RecevierBinder> BINDER_CACHE = new LinkedHashMap<>();

    static final RecevierBinder NO_OP_BINDER = new RecevierBinder() {
        @Override
        public void bind(Object target) {
        }

        @Override
        public void unbind() {
        }
    };

    public static void bind(@Nullable Context context) {
        bind(context, context);
    }

    public static void bind(@Nullable Object target, @Nullable Context context) {
        if (target == null || context == null) {
            throw new RuntimeException("Bind method only accepts non-null parameters.");
        }
        try {
            RecevierBinder binder = findReceiverBinder(context, target);
            if (binder != null) {
                binder.bind(target);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to bind  for " + target.getClass().getName(), e);
        }
    }

    /**
     * find specific binder using classname
     */
    private static RecevierBinder findReceiverBinderWithClass(Context context, Object target, Class<?> targetClass)
            throws IllegalAccessException, InstantiationException {
        String className = targetClass.getName();
        if (className.startsWith(ANDROID_PREFIX) || className.startsWith(JAVA_PREFIX)) {
            return NO_OP_BINDER;
        }

        RecevierBinder binder = checkCacheForBinderClass(target);
        if (binder != null) {
            return binder;
        }
        binder = NO_OP_BINDER;
        try {
            Class<?> viewBindingClass = Class.forName(className + SUFFIX);

            binder = (RecevierBinder) viewBindingClass.getConstructor(Context.class, targetClass)
                    .newInstance(context, target);
            if (binder == null) {
                return NO_OP_BINDER;
            }

            BINDER_CACHE.put(target.getClass(), binder);

        } catch (ClassNotFoundException e) {
        } catch (Exception e) {
        }

        return binder;
    }

    private static boolean shouldTrySupperClass(Class<?> clazz) {
        boolean hasReceiver = false;
        for (Method method : clazz.getDeclaredMethods()) {
            OnReceive onReceive = method.getAnnotation(OnReceive.class);
            if (onReceive == null) {
                continue;
            }
            hasReceiver = true;
            break;
        }
        return hasReceiver;
    }

    private static RecevierBinder findReceiverBinder(Context context, Object target)
            throws IllegalAccessException, InstantiationException {
        if (target == null || context == null) {
            return NO_OP_BINDER;
        }

        Class<?> clazz = target.getClass();
        RecevierBinder binder;
        while ((binder = findReceiverBinderWithClass(context, target, clazz)) == NO_OP_BINDER && shouldTrySupperClass(clazz.getSuperclass())) {
            clazz = clazz.getSuperclass();
        }

        RecevierBinder ret = checkCacheForBinderClass(target);
        return ret;
    }

    public static void unbind(@Nullable Object target) {
        if (target == null) {
            throw new RuntimeException(
                    "Null value for target parameter passed into unbind method.");
        }

        Class<?> targetClass = target.getClass();
        try {
            RecevierBinder binder = checkCacheForBinderClass(target);
            if (binder != null) {
                binder.unbind();
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to unbind sensors for " + targetClass.getName(), e);
        }
    }

    private static RecevierBinder checkCacheForBinderClass(@NonNull Object target) {
        Class<?> targetClass = target.getClass();
        RecevierBinder binder = BINDER_CACHE.get(targetClass);
        return binder != null ? binder : null;
    }

}
