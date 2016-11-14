package wuxian.me.localbroadcastannotations.compiler.poet;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import wuxian.me.localbroadcastannotations.compiler.AnnotatedMethod;
import wuxian.me.localbroadcastannotations.compiler.AnnotatedMethodsPerClass;
import wuxian.me.localbroadcastannotations.compiler.ProcessingException;

/**
 * Created by wuxian on 14/11/2016.
 */

public class SuperClassReceiverPoet implements IReceiverBinderPoet {
    protected static final String SUFFIX = "$$ReceiverBinder";

    protected static final ClassName ANNOTATEDMETHODS_PERCLASS
            = ClassName.get("wuxian.me.localbroadcastannotations.compiler", "AnnotatedMethodsPerClass");
    protected static final ClassName ON_RECEIVE
            = ClassName.get("wuxian.me.localbroadcastannotations.annotation", "OnReceive");
    protected static final ClassName ANNOTATED_METHOD
            = ClassName.get("wuxian.me.localbroadcastannotations.compiler", "AnnotatedMethod");
    protected static final ClassName RECEIVER_BINDER
            = ClassName.get("wuxian.me.localbroadcastannotations", "RecevierBinder");

    protected static final ClassName INVOCATION_TARGET_EXCEPTION = ClassName.get("java.lang.reflect", "InvocationTargetException");

    protected static final ClassName METHOD = ClassName.get("java.lang.reflect", "Method");
    protected static final ClassName CLASS = ClassName.get("java.lang", "Class");

    protected static final ClassName INTEGER = ClassName.get("java.lang", "Integer");
    protected static final ClassName SET = ClassName.get("java.util", "Set");
    protected static final ClassName MAP = ClassName.get("java.util", "Map");
    protected static final ClassName HASHMAP = ClassName.get("java.util", "HashMap");

    protected static final ClassName LOCAL_BROADCAST_MANAGER = ClassName.get("android.support.v4.content", "LocalBroadcastManager");
    protected static final ClassName INTENT = ClassName.get("android.content", "Intent");
    protected static final ClassName CONTEXT = ClassName.get("android.content", "Context");
    protected static final ClassName BROADCAST_RECEIVER = ClassName.get("android.content", "BroadcastReceiver");
    protected static final ClassName INTENT_FILTER = ClassName.get("android.content", "IntentFilter");

    protected static final FieldSpec FIELD_CONTEXT =
            FieldSpec.builder(CONTEXT, "context")
                    .addModifiers(Modifier.PROTECTED, Modifier.FINAL)
                    .build();

    protected static final FieldSpec FIELD_RECEIVER =
            FieldSpec.builder(BROADCAST_RECEIVER, "receiver")
                    .addModifiers(Modifier.PROTECTED)
                    .build();

    protected static final FieldSpec FIELD_FILTER =
            FieldSpec.builder(INTENT_FILTER, "filter")
                    .addModifiers(Modifier.PROTECTED, Modifier.FINAL)
                    .build();

    protected static final FieldSpec FIELD_METHOD_MAP =
            FieldSpec.builder(ParameterizedTypeName.get(MAP, INTEGER, METHOD), "methodMap")
                    .addModifiers(Modifier.PROTECTED, Modifier.FINAL)
                    .build();

    private static final MethodSpec UNBIND_METHOD = getBaseMethodBuilder("unbind")
            .addStatement("$T.getInstance(this.$N).unregisterReceiver(this.$N)", LOCAL_BROADCAST_MANAGER, FIELD_CONTEXT, FIELD_RECEIVER)
            .build();

    protected Elements elementUtils;
    protected AnnotatedMethodsPerClass groupedMethods;
    protected TypeElement classTypeElement;
    private ParameterSpec targetParameter;

    public SuperClassReceiverPoet(@NonNull Elements elementUtils, @NonNull AnnotatedMethodsPerClass groupedMethods) {
        this.elementUtils = elementUtils;
        this.groupedMethods = groupedMethods;

        this.classTypeElement = elementUtils.getTypeElement(groupedMethods.getEnclosingClassName()); //get class element

        //For eg. "ReceiverBinder<ExampleActivity>"
        ParameterizedTypeName parameterizedInterface = ParameterizedTypeName.get(RECEIVER_BINDER, TypeName.get(classTypeElement.asType()));

        //(MainActivity target)
        this.targetParameter =
                ParameterSpec.builder(TypeName.get(classTypeElement.asType()), "target")
                        .addModifiers(Modifier.FINAL)
                        .build();

    }

    @Override
    public TypeSpec buildReceiverClass() throws ProcessingException {
        //For eg. "ReceiverBinder<ExampleActivity>"
        ParameterizedTypeName parameterizedInterface = ParameterizedTypeName.get(RECEIVER_BINDER, TypeName.get(classTypeElement.asType()));

        TypeSpec binderClass =
                TypeSpec.classBuilder(classTypeElement.getSimpleName() + SUFFIX)
                        .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
                        .addSuperinterface(parameterizedInterface)
                        .addField(FIELD_RECEIVER)
                        .addField(FIELD_FILTER)
                        .addField(FIELD_CONTEXT)
                        .addField(FIELD_METHOD_MAP)
                        .addMethod(createConstructorMethod())
                        .addMethod(createBindMethod())
                        .addMethod(createUnbindMethod())
                        .build();
        return binderClass;
    }

    @Override
    public MethodSpec createConstructorMethod() throws ProcessingException {
        ParameterSpec targetParameter = this.targetParameter;
        Map<Integer, AnnotatedMethod> itemsMap = this.groupedMethods.getAnnotatedMethods();

        ParameterSpec contextParameter = ParameterSpec.builder(CONTEXT, "context").build();

        //add code: this.context = context; this.filter = new IntentFilter();
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(contextParameter)
                .addParameter(targetParameter)
                .addStatement("this.$N = context", FIELD_CONTEXT)
                .addStatement("this.$N = new IntentFilter()", FIELD_FILTER)
                .addStatement("this.methodMap = new $T<>()", HASHMAP);

        //TODO: 这里改成runtime处理
        // 因为使用静态的java Element难以拿到父类element 也就难以拿到父类的annotated method。
        // 而intentfilter需要子类和父类中所有的Action Category
        for (AnnotatedMethod method : itemsMap.values()) {
            constructorBuilder.addStatement("this.$N.addAction($L)", FIELD_FILTER, method.getAction());
            if (method.getCategory().equals(AnnotatedMethod.NONE)) {
                continue;
            }
            constructorBuilder.addStatement("this.$N.addCategory($L)", FIELD_FILTER, method.getCategory());
        }

        //add code: Class<?> clazz = target.getClass() ....
        constructorBuilder.addStatement("$T<?> clazz = target.getClass()", CLASS);
        constructorBuilder.beginControlFlow("for ($T method : clazz.getDeclaredMethods())", METHOD)
                .addStatement("$L onReceive = method.getAnnotation($L.class);", ON_RECEIVE, ON_RECEIVE)
                .beginControlFlow("if (onReceive == null)").addStatement("continue").endControlFlow()
                .addStatement("int id = $L.generateId(onReceive.value(), onReceive.category());", ANNOTATEDMETHODS_PERCLASS)
                .beginControlFlow("if (this.$N.containsKey(id))", FIELD_METHOD_MAP).addStatement("continue").endControlFlow()
                .addStatement("this.$N.put(id, method)", FIELD_METHOD_MAP)
                .endControlFlow();

        return constructorBuilder.build();
    }

    /**
     * 1 init receiver
     * 2 LocalBroadcast.getInstance(context).register();
     */
    @Override
    public MethodSpec createBindMethod() throws ProcessingException {
        ParameterSpec targetParameter = this.targetParameter;
        AnnotatedMethodsPerClass methodsPerClass = this.groupedMethods;

        MethodSpec.Builder builder = getBaseMethodBuilder("bind")
                .addParameter(targetParameter)
                .beginControlFlow("if(this.$N == null)", FIELD_RECEIVER)
                .addCode(createReceiverBlock(methodsPerClass))
                .endControlFlow()
                .addStatement("$T.getInstance(this.$N).registerReceiver(this.$N,this.$N)", LOCAL_BROADCAST_MANAGER, FIELD_CONTEXT, FIELD_RECEIVER, FIELD_FILTER);

        return builder.build();
    }

    private CodeBlock createReceiverBlock(@NonNull AnnotatedMethodsPerClass methodsPerClass) throws ProcessingException {
        CodeBlock receiverBlock = CodeBlock.builder()
                .add("new $T() {\n", BROADCAST_RECEIVER)
                .indent()
                .add(createOnReceiveListenerMethod(methodsPerClass).toString())
                .unindent()
                .add("}")
                .build();
        return CodeBlock.builder()
                .addStatement("this.$N = $L", FIELD_RECEIVER, receiverBlock)
                .build();
    }

    @NonNull
    private MethodSpec createOnReceiveListenerMethod(@NonNull AnnotatedMethodsPerClass methodsPerClass) throws ProcessingException {

        MethodSpec.Builder builder = getBaseMethodBuilder("onReceive").addParameter(CONTEXT, "context")
                .addParameter(INTENT, "intent")
                .addStatement("String action = intent.getAction()")
                .addStatement("$T<String> categories = intent.getCategories()", SET)
                .addStatement("String category = $T.NONE", ANNOTATED_METHOD)
                .beginControlFlow("if (categories != null && categories.size() != 0)")
                .addStatement("category = categories.iterator().next()")
                .endControlFlow()
                .addStatement("int id = $T.generateId(action, category)", ANNOTATEDMETHODS_PERCLASS)
                .beginControlFlow("if ($N.containsKey(id))", FIELD_METHOD_MAP)
                .beginControlFlow("try")
                .addStatement("$N.get(id).invoke(target, new Object[]{context, intent})", FIELD_METHOD_MAP)
                .nextControlFlow("catch (IllegalAccessException e)")
                .addStatement("e.printStackTrace()")
                .nextControlFlow("catch ($T e)", INVOCATION_TARGET_EXCEPTION)
                .addStatement("e.printStackTrace()")
                .endControlFlow()
                .endControlFlow();
        return builder.build();
    }

    @Override
    public MethodSpec createUnbindMethod() throws ProcessingException {
        return UNBIND_METHOD;
    }

    protected static MethodSpec.Builder getBaseMethodBuilder(@NonNull String name) {
        return MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addAnnotation(Override.class);
    }

}
