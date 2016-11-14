package wuxian.me.localbroadcastannotations.compiler.poet;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import wuxian.me.localbroadcastannotations.compiler.ProcessingException;

/**
 * Created by wuxian on 14/11/2016.
 * <p>
 * used to generate Whatever$$Binder class
 */

public interface IReceiverBinderPoet {

    TypeSpec buildReceiverClass() throws ProcessingException;

    MethodSpec createConstructorMethod() throws ProcessingException;

    MethodSpec createBindMethod() throws ProcessingException;

    MethodSpec createUnbindMethod() throws ProcessingException;
}
