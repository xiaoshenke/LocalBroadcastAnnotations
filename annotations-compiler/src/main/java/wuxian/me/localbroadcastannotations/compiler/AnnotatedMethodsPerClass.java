package wuxian.me.localbroadcastannotations.compiler;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.annotation.Annotation;

import wuxian.me.localbroadcastannotations.annotation.OnReceive;

/**
 * Created by wuxian on 11/11/2016.
 * <p>
 * collect all Annotated method of a specific class.
 */

public class AnnotatedMethodsPerClass {
    @NonNull
    private String className;


    public AnnotatedMethodsPerClass(@NonNull String className) {
        this.className = className;
    }

    public void add(@NonNull AnnotatedMethod method) throws ProcessingException {
        this.add(OnReceive.class, method);
    }

    /**
     * 支持多个annotation,不过目前只有一个annotation
     * <p>
     * 对于一个使用OnReceive注解来说 ACTION+CATEGRORY"两者共同"确定了唯一的一个函数method
     * TODO: To be finished
     */
    private void add(@NonNull Class<? extends Annotation> annotationClass, @NonNull AnnotatedMethod method) throws ProcessingException {

    }

}
