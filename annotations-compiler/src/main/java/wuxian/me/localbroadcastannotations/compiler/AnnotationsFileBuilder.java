package wuxian.me.localbroadcastannotations.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Created by wuxian on 11/11/2016.
 * <p>
 * 生成annotation binder class
 */

public class AnnotationsFileBuilder {

    private static final ClassName RECEIVER_BINDER = ClassName.get("wuxian.me.localbroadcastannotations", "RecevierBind");

    public static void generateFile(@NonNull Map<String, AnnotatedMethodsPerClass> groupedMethodsMap,
                                    @NonNull Elements elementUtils, @NonNull Filer filer) throws IOException, ProcessingException {
        for (AnnotatedMethodsPerClass groupedMethods : groupedMethodsMap.values()) {

            TypeElement classTypeElement = elementUtils.getTypeElement(groupedMethods.getEnclosingClassName());

            // begin to create java class
            //For eg. "ReceiverBinder<ExampleActivity>"
            ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(RECEIVER_BINDER, TypeName.get(classTypeElement.asType()));
            //TODO
        }
    }

    /**
     * TODO
     */
    private static @NonNull MethodSpec createConstructor(@NonNull ParameterSpec targetParameter,
                                                         @NonNull Map<Integer, Map<Class, AnnotatedMethod>> itemsMap) throws ProcessingException {
        return null;
    }

    /**
     * TODO
     */
    @NonNull
    private static MethodSpec createBindMethod(@NonNull ParameterSpec targetParameter,
                                               @NonNull AnnotatedMethodsPerClass annotatedMethodsPerClass) throws ProcessingException {
        return null;
    }
}
