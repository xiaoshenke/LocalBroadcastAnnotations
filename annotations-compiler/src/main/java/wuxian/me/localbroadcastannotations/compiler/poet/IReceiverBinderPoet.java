package wuxian.me.localbroadcastannotations.compiler.poet;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.Map;

import wuxian.me.localbroadcastannotations.compiler.AnnotatedMethod;
import wuxian.me.localbroadcastannotations.compiler.AnnotatedMethodsPerClass;
import wuxian.me.localbroadcastannotations.compiler.ProcessingException;

/**
 * Created by wuxian on 14/11/2016.
 * <p>
 * used to generate Whatever$$Binder class
 */

public interface IReceiverBinderPoet {

    TypeSpec buildReceiverClass();

    MethodSpec createConstructorMethod(ParameterSpec targetParameter, Map<Integer, AnnotatedMethod> itemsMap) throws ProcessingException;

    MethodSpec createBindMethod(ParameterSpec targetParameter, AnnotatedMethodsPerClass methodsPerClass) throws ProcessingException;

    MethodSpec createUnbindMethod(ParameterSpec targetParameter, AnnotatedMethodsPerClass methodsPerClass) throws ProcessingException;
}
