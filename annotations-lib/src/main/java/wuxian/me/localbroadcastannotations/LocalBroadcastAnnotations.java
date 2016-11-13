package wuxian.me.localbroadcastannotations;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by wuxian on 11/11/2016.
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

    private static RecevierBinder findReceiverBinder(Context context, Object target)
            throws IllegalAccessException, InstantiationException {

        if (target == null || context == null) {

            return NO_OP_BINDER;
        }

        Class<?> targetClass = target.getClass();
        String className = targetClass.getName();

        if (className.startsWith(ANDROID_PREFIX) || className.startsWith(JAVA_PREFIX)) {
            return NO_OP_BINDER;
        }

        RecevierBinder binder = checkCacheForBinderClass(target);
        if (binder != null) {
            return binder;
        }

        try {
            Class<?> viewBindingClass = Class.forName(className + SUFFIX);

            binder = (RecevierBinder) viewBindingClass.getConstructor(Context.class, targetClass)
                    .newInstance(context, target);
            if (binder == null) {
                return NO_OP_BINDER;
            }

            BINDER_CACHE.put(targetClass, binder);
        } catch (ClassNotFoundException e) {
            //binder = findReceiverBinder(context, targetClass.getSuperclass());  //try super class //FIXME: we can't simply call super class when dealing this
            binder = NO_OP_BINDER;
        } catch (Exception e) {
            binder = NO_OP_BINDER;
        }

        return binder;
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
