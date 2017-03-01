package cn.udesk.model;

/**
 * 解决InputMethodManager 内存溢出的问题
 * Created by bauer_bao on 17/3/1.
 */

public class TypeObject {
    private final Object object;
    private final Class type;

    public TypeObject(final Object object, final Class type) {
        this.object = object;
        this.type = type;
    }

    public Object getObject() {
        return object;
    }

    public Class getType() {
        return type;
    }
}
