package wuxian.me.localbroadcastannotations.compiler;

import com.google.common.base.Joiner;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

import wuxian.me.localbroadcastannotations.annotation.ListenerMethod;
import wuxian.me.localbroadcastannotations.annotation.OnReceive;

/**
 * Created by wuxian on 11/11/2016.
 * <p>
 * 生成annotation binder class
 */

public class AnnotationsFileBuilder {
    private static final String SUFFIX = "$$ReceiverBinder";

    private static final ClassName BROADCAST_RECEIVER =
            ClassName.get("android.content", "BroadcastReceiver");
    private static final ClassName INTENT_FILTER =
            ClassName.get("android.content", "IntentFilter");

    private static final ClassName CONTEXT = ClassName.get("android.content", "Context");

    private static final ClassName RECEIVER_BINDER = ClassName.get("wuxian.me.localbroadcastannotations", "RecevierBind");

    private static final FieldSpec FIELD_CONTEXT =
            FieldSpec.builder(CONTEXT, "context")
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                    .build();

    private static final FieldSpec FIELD_RECEIVER =
            FieldSpec.builder(BROADCAST_RECEIVER, "receiver")
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                    .build();

    private static final FieldSpec FIELD_FILTER =
            FieldSpec.builder(INTENT_FILTER, "filter")
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                    .build();

    public static void generateFile(@NonNull Map<String, AnnotatedMethodsPerClass> groupedMethodsMap,
                                    @NonNull Elements elementUtils, @NonNull Filer filer) throws IOException, ProcessingException {
        for (AnnotatedMethodsPerClass groupedMethods : groupedMethodsMap.values()) {

            TypeElement classTypeElement = elementUtils.getTypeElement(groupedMethods.getEnclosingClassName());

            // begin to create java class
            //For eg. "ReceiverBinder<ExampleActivity>"
            ParameterizedTypeName parameterizedInterface = ParameterizedTypeName.get(RECEIVER_BINDER, TypeName.get(classTypeElement.asType()));

            //(MainActivity target)
            ParameterSpec targetParameter =
                    ParameterSpec.builder(TypeName.get(classTypeElement.asType()), "target")
                            .addModifiers(Modifier.FINAL)
                            .build();

            MethodSpec constructor = createConstructor(targetParameter, groupedMethods.getAnnotatedMethods());
            MethodSpec bindMethod = createBindMethod(targetParameter, groupedMethods);

            TypeSpec binderClass =
                    TypeSpec.classBuilder(classTypeElement.getSimpleName() + SUFFIX)
                            .addModifiers(Modifier.FINAL)
                            .addSuperinterface(parameterizedInterface)
                            .addField(FIELD_RECEIVER)
                            .addField(FIELD_FILTER)
                            .addField(FIELD_CONTEXT)
                            .addMethod(constructor)
                            .addMethod(bindMethod)
                            .build();

            // Output our generated file with the same package as the target class.
            PackageElement packageElement = elementUtils.getPackageOf(classTypeElement);
            JavaFileObject jfo =
                    filer.createSourceFile(classTypeElement.getQualifiedName() + SUFFIX);
            Writer writer = jfo.openWriter();
            JavaFile.builder(packageElement.toString(), binderClass)
                    .addFileComment("This class is generated code from LocalBroadcastAnnotation Lib. Do not modify!")
                    //.addStaticImport(CONTEXT, "SENSOR_SERVICE") //TODO
                    .build()
                    .writeTo(writer);
            writer.close();
        }
    }

    private static @NonNull MethodSpec createConstructor(@NonNull ParameterSpec targetParameter,
                                                         @NonNull Map<Integer, AnnotatedMethod> itemsMap) throws ProcessingException {

        ParameterSpec contextParameter = ParameterSpec.builder(CONTEXT, "context").build();
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(contextParameter)
                .addParameter(targetParameter)
                .addStatement("this.$N = context", FIELD_CONTEXT)
                .addStatement("this.$N = new IntentFilter()", FIELD_FILTER);

        for (AnnotatedMethod method : itemsMap.values()) {
            constructorBuilder.addStatement("this.$N.addAction($L)", FIELD_FILTER, method.getAction());
            if (method.getCategory().equals(AnnotatedMethod.NONE)) {
                continue;
            }
            constructorBuilder.addStatement("this.$N.addCategory($L)", FIELD_FILTER, method.getCategory());
        }
        return constructorBuilder.build();
    }

    /**
     * 1 init receiver
     * 2 LocalBroadcast.getInstance(context).register();
     */
    @NonNull
    private static MethodSpec createBindMethod(@NonNull ParameterSpec targetParameter,
                                               @NonNull AnnotatedMethodsPerClass methodsPerClass) throws ProcessingException {

        MethodSpec.Builder builder = getBaseMethodBuilder("bind")
                .addParameter(targetParameter)
                .addCode(createReceiverBlock(methodsPerClass))
                .addStatement("LocalBroadcastManager.getInstance($N).registerReceiver($N,$N)", FIELD_CONTEXT, FIELD_RECEIVER, FIELD_FILTER);

        return builder.build();
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
                throw new ProcessingException(element, error);
            }
        }

    }

    /**
     * create receiver block
     */
    private static CodeBlock createReceiverBlock(@NonNull AnnotatedMethodsPerClass methodsPerClass) throws ProcessingException {

        //first check all annotated method
        for (AnnotatedMethod method : methodsPerClass.getAnnotatedMethods().values()) {
            checkAnnotatedMethod(method.getExecutableElement(), OnReceive.class);
        }

        //TODO 开大招 这里使用runtime获取annotation的方式 不过目测效率会差一些 尤其是当localbroadcast的频率很高的时候

        return null;
    }

    /**
     * @Overrid pubic void
     */
    private static MethodSpec.Builder getBaseMethodBuilder(@NonNull String name) {
        return MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addAnnotation(Override.class);
    }
}
