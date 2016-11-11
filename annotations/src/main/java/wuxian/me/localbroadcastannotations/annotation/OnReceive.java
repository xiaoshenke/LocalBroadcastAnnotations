package wuxian.me.localbroadcastannotations.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by wuxian on 11/11/2016.
 *
 * OnReceive的IntentFilter两个参数:Action,Category
 *
 * 用法
 * @OnReceive(ACTION_LOAD_BOOKS,CATEGORY_CHINEASE)
 * void loadChineaseBooks(List<Book> books){...}
 *
 * @OnReceive(ACTION_LOAD_PEOPLE)
 * void loadChineaseBooks(List<People> peoples){...}
 */

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface OnReceive {
    String value() default "None";

    String category() default "None";
}
