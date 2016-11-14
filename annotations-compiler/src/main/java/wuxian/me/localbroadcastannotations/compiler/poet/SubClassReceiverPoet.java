package wuxian.me.localbroadcastannotations.compiler.poet;

import org.checkerframework.checker.nullness.qual.NonNull;

import javax.lang.model.util.Elements;

import wuxian.me.localbroadcastannotations.compiler.AnnotatedMethodsPerClass;

/**
 * Created by wuxian on 14/11/2016.
 */

public class SubClassReceiverPoet extends SuperClassReceiverPoet {
    public SubClassReceiverPoet(@NonNull Elements elementUtils, @NonNull AnnotatedMethodsPerClass groupedMethods) {
        super(elementUtils, groupedMethods);
    }
}
