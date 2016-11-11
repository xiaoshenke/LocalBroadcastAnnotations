package wuxian.me.localbroadcastannotations.compiler;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.annotation.Annotation;

import javax.lang.model.element.ExecutableElement;

import wuxian.me.localbroadcastannotations.annotation.OnReceive;

/**
 * Created by wuxian on 11/11/2016.
 */

public class AnnotatedMethod {

    public static final String NONE = "None";
    private ExecutableElement element;
    private String action;
    private String category;

    AnnotatedMethod(@NonNull ExecutableElement methodElement,
                    @NonNull Class<? extends Annotation> annotationClass) throws IllegalArgumentException {
        Annotation annotation = methodElement.getAnnotation(annotationClass);
        this.element = methodElement;
        this.action = getActionFrom(annotation);
        this.category = getCategoryFrom(annotation);

        if (this.action.equals(NONE)) {  //value 不能为空
            throw new IllegalArgumentException(String.format(
                    "No sensor type specified in @%s for method %s."
                            + " Set a sensor type such as Sensor.TYPE_ACCELEROMETER.",
                    annotationClass.getSimpleName(), methodElement.getSimpleName().toString()));
        }
    }

    @NonNull
    public ExecutableElement getExecutableElement() {
        return element;
    }

    public String getAction() {
        return action;
    }

    public String getCategory() {
        return category;
    }

    private String getCategoryFrom(@NonNull Annotation annotation) {
        return ((OnReceive) annotation).category();
    }

    private String getActionFrom(@NonNull Annotation annotation) {
        return ((OnReceive) annotation).value();
    }
}
