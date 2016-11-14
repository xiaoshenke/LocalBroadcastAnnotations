package wuxian.me.localbroadcastannotations.compiler;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;

import wuxian.me.localbroadcastannotations.annotation.OnReceive;

/**
 * Created by wuxian on 11/11/2016.
 * <p>
 * collect all Annotated method of a specific class.
 */

public class AnnotatedMethodsPerClass {
    @NonNull
    private String className;

    private Map<Integer, AnnotatedMethod> mItemsMap = new LinkedHashMap<>();

    public String getEnclosingClassName() {
        return className;
    }

    public Map<Integer, AnnotatedMethod> getAnnotatedMethods() {
        return mItemsMap;
    }

    public AnnotatedMethodsPerClass(@NonNull String className) {
        this.className = className;
    }

    public void add(@NonNull AnnotatedMethod method) throws ProcessingException {
        this.add(OnReceive.class, method);
    }

    public static int generateId(String s1, String s2) {
        int id = 0;
        for (int i = 0; i < s1.length(); i++) {
            id += s1.charAt(i);
        }

        for (int i = 0; i < s2.length(); i++) {
            id += s2.charAt(i);
        }
        return id;
    }

    /**
     * 支持多个annotation,不过目前只有一个annotation
     * <p>
     * 对于一个使用OnReceive注解来说 ACTION+CATEGRORY"两者共同"确定了唯一的一个函数method
     */
    private void add(@NonNull Class<? extends Annotation> annotationClass, @NonNull AnnotatedMethod method) throws ProcessingException {
        int id = generateId(method.getAction(), method.getCategory());

        if (mItemsMap.get(id) != null) {
            String error =
                    String.format("@%s is already annotated on a different method in class %s",
                            annotationClass.getSimpleName(), method.getExecutableElement().getSimpleName());
            throw new ProcessingException(method.getExecutableElement(), error);
        }

        mItemsMap.put(id, method);

    }

    public boolean contains(@NonNull AnnotatedMethod method) {
        return mItemsMap.containsKey(generateId(method.getAction(), method.getCategory()));
    }

}
