package cn.udesk.widget;

import android.app.Activity;
import android.content.Context;
import android.os.IBinder;
import android.view.View;

import java.lang.reflect.Method;

import cn.udesk.model.TypeObject;

/**
 * Created by bauer_bao on 17/3/1.
 */

public class KeyBoardUtil {
    /**
     * 解决inputmethodManager内存溢出问题
     * @param activity
     */
    public static void fixFocusedViewLeak(Activity activity) {
        final Object imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        final TypeObject windowToken = new TypeObject(activity.getWindow().getDecorView().getWindowToken(), IBinder.class);

        invokeMethodExceptionSafe(imm, "windowDismissed", windowToken);

        final TypeObject view = new TypeObject(null, View.class);

        invokeMethodExceptionSafe(imm, "startGettingWindowFocus", view);
    }

    private static void invokeMethodExceptionSafe(final Object methodOwner, final String method, final TypeObject... arguments) {
        if (null == methodOwner) {
            return;
        }

        try {
            final Class<?>[] types = null == arguments ? new Class[0] : new Class[arguments.length];
            final Object[] objects = null == arguments ? new Object[0] : new Object[arguments.length];

            if (null != arguments) {
                for (int i = 0, limit = types.length; i < limit; i++) {
                    types[i] = arguments[i].getType();
                    objects[i] = arguments[i].getObject();
                }
            }

            final Method declaredMethod = methodOwner.getClass().getDeclaredMethod(method, types);

            declaredMethod.setAccessible(true);
            declaredMethod.invoke(methodOwner, objects);
        } catch (Throwable ignored) {
            ignored.printStackTrace();
        }
    }
}
