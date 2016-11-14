package wuxian.me.localbroadcastannotations.compiler;

import com.google.common.base.Joiner;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

import wuxian.me.localbroadcastannotations.annotation.ListenerMethod;
import wuxian.me.localbroadcastannotations.annotation.OnReceive;
import wuxian.me.localbroadcastannotations.compiler.poet.IReceiverBinderPoet;
import wuxian.me.localbroadcastannotations.compiler.poet.ReceiverPoet;

/**
 * Created by wuxian on 11/11/2016.
 * <p>
 * 生成annotation binder class
 * <p>
 * javapoet reference:https://github.com/square/javapoet
 * <p>
 */

public class AnnotationsFileBuilder {
    private static final String SUFFIX = "$$ReceiverBinder";
    private static Messager sMessager;

    private AnnotationsFileBuilder() {
        throw new NoSuchElementException();
    }

    /**
     * generate java file for every class which has method be annotated with @OnReceive annotation
     *
     * @param messager used to debug:print message to shell
     */
    public static void generateFile(@NonNull Messager messager, @NonNull Map<String, AnnotatedMethodsPerClass> groupedMethodsMap,
                                    @NonNull Elements elementUtils, @NonNull Filer filer) throws IOException, ProcessingException {

        sMessager = messager;
        for (AnnotatedMethodsPerClass groupedMethods : groupedMethodsMap.values()) {
            //检查被AnnotationMethod注解的函数是否符合约定。
            //比如 @OnReceive(String1,String2) public void onTextBlue(Context,Intent,int)就是不合规定的一个被注解函数
            for (AnnotatedMethod method : groupedMethods.getAnnotatedMethods().values()) {
                checkAnnotatedMethod(method.getExecutableElement(), OnReceive.class);
            }

            TypeElement classTypeElement = elementUtils.getTypeElement(groupedMethods.getEnclosingClassName());

            IReceiverBinderPoet poet = new ReceiverPoet(elementUtils, groupedMethods);
            TypeSpec binderClass = poet.buildReceiverClass();

            // Output our generated file with the same package as the target class.
            PackageElement packageElement = elementUtils.getPackageOf(classTypeElement);
            JavaFileObject jfo =
                    filer.createSourceFile(classTypeElement.getQualifiedName() + SUFFIX);
            Writer writer = jfo.openWriter();
            JavaFile.builder(packageElement.toString(), binderClass)
                    .addFileComment("This class is generated code from LocalBroadcastAnnotation Lib. Do not modify!")
                    .addStaticImport(ClassName.get(classTypeElement), "*")
                    .build()
                    .writeTo(writer);
            writer.close();
        }
    }


    /**
     * first check parameter length
     * then check every paramter type
     */
    private static void checkAnnotatedMethod(@NonNull ExecutableElement element, @NonNull Class<? extends Annotation> annotation) throws ProcessingException {
        ListenerMethod method = annotation.getAnnotation(ListenerMethod.class);
        String[] expectedParameters = method.parameters();

        List<? extends VariableElement> parameters = element.getParameters();
        if (parameters.size() != expectedParameters.length) {
            String error = String.format("@%s methods can only have %s parameter(s). (%s.%s)",
                    annotation.getSimpleName(), method.parameters().length,
                    element.getEnclosingElement().getSimpleName(), element.getSimpleName());

            LocalBroadcastAnnotationsProcessor.error(sMessager, null, "check length fail");
            throw new ProcessingException(element, error);
        }

        for (int i = 0; i < parameters.size(); i++) {
            VariableElement parameter = parameters.get(i);
            TypeMirror methodParameterType = parameter.asType();
            String expectedType = expectedParameters[i];
            if (!expectedType.equals(methodParameterType.toString())) {
                String error = String.format(
                        "Method parameters are not valid for @%s annotated method. Expected parameters of type(s): %s. (%s.%s)",
                        annotation.getSimpleName(), Joiner.on(", ").join(expectedParameters),
                        element.getEnclosingElement().getSimpleName(), element.getSimpleName());

                LocalBroadcastAnnotationsProcessor.error(sMessager, null, "check parameter fail");
                throw new ProcessingException(element, error);
            }
        }

    }
}
