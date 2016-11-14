package wuxian.me.localbroadcastannotations.compiler.poet;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Map;

import javax.lang.model.element.Modifier;

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

    @Override
    public TypeSpec buildReceiverClass() {
        return null;
    }

    @Override
    public MethodSpec createConstructorMethod(ParameterSpec targetParameter, Map<Integer, AnnotatedMethod> itemsMap) throws ProcessingException {
        return null;
    }

    @Override
    public MethodSpec createBindMethod(ParameterSpec targetParameter, AnnotatedMethodsPerClass methodsPerClass) throws ProcessingException {
        return null;
    }

    @Override
    public MethodSpec createUnbindMethod(ParameterSpec targetParameter, AnnotatedMethodsPerClass methodsPerClass) throws ProcessingException {
        return null;
    }
}
