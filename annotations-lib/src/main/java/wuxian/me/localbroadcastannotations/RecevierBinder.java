package wuxian.me.localbroadcastannotations;

/**
 * Created by wuxian on 11/11/2016.
 */

public interface RecevierBinder<T> {

    void bind(T target);

    void unbind();
}
