package wuxian.me.localbroadcastannotations;

/**
 * Created by wuxian on 11/11/2016.
 */

public interface RecevierBind<T> {

    void bind(T target);

    void unbind();
}
