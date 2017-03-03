package cn.udesk.widget;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.MessageQueue;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by bauer_bao on 17/3/1.
 */

public class KeyBoardUtil {

//    public static void fixFocusedViewLeak(Activity activity) {
//        final Object imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE);
//
//        final TypeObject windowToken = new TypeObject(activity.getWindow().getDecorView().getWindowToken(), IBinder.class);
//
////        invokeMethodExceptionSafe(imm, "windowDismissed", windowToken);
//
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            final TypeObject view = new TypeObject(null, View.class);
//            invokeMethodExceptionSafe(imm, "startGettingWindowFocus", view);
//
//        } else {
//            final TypeObject view = new TypeObject(null, View.class);
//            invokeMethodExceptionSafe(imm, "onPreWindowFocus", view);
//
//        }
//    }
//
//    private static void invokeMethodExceptionSafe(final Object methodOwner, final String method, final TypeObject... arguments) {
//        if (null == methodOwner) {
//            return;
//        }
//
//        try {
//            final Class<?>[] types = null == arguments ? new Class[0] : new Class[arguments.length];
//            final Object[] objects = null == arguments ? new Object[0] : new Object[arguments.length];
//
//            if (null != arguments) {
//                for (int i = 0, limit = types.length; i < limit; i++) {
//                    types[i] = arguments[i].getType();
//                    objects[i] = arguments[i].getObject();
//                }
//            }
//
//            final Method declaredMethod = methodOwner.getClass().getDeclaredMethod(method, types);
//            if (declaredMethod == null) {
//            System.out.println("find class---> " + method + " success");
//
//            } else {
//            System.out.println("find class---> " + method + " unsuccess");
//
//            }
//
//            declaredMethod.setAccessible(true);
//            declaredMethod.invoke(methodOwner, objects);
//        } catch (Throwable ignored) {
//            ignored.printStackTrace();
//        }
//    }
    /**
     * 解决inputmethodManager内存溢出问题
     *
     * @param activity
     */
    public static void fixFocusedViewLeak(Activity activity) {
        // Don't know about other versions yet.
        //23的依旧有问题，24以上，应该系统已经修复这个问题（没去测试）
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 || Build.VERSION.SDK_INT > 22) {
            return;
        }

        final InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        final Field mServedViewField;
        final Field mHField;
        final Method finishInputLockedMethod;
        final Method focusInMethod;
        try {
            mServedViewField = InputMethodManager.class.getDeclaredField("mServedView");
            mServedViewField.setAccessible(true);
            mHField = InputMethodManager.class.getDeclaredField("mServedView");
            mHField.setAccessible(true);
            finishInputLockedMethod = InputMethodManager.class.getDeclaredMethod("finishInputLocked");
            finishInputLockedMethod.setAccessible(true);
            focusInMethod = InputMethodManager.class.getDeclaredMethod("focusIn", View.class);
            focusInMethod.setAccessible(true);
        } catch (Exception unexpected) {
            Log.e("IMMLeaks", "Unexpected reflection exception", unexpected);
            return;
        }

        activity.getApplication().registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityDestroyed(Activity activity) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                ReferenceCleaner cleaner = new ReferenceCleaner(inputMethodManager, mHField, mServedViewField,
                        finishInputLockedMethod);
                View rootView = activity.getWindow().getDecorView().getRootView();
                ViewTreeObserver viewTreeObserver = rootView.getViewTreeObserver();
                viewTreeObserver.addOnGlobalFocusChangeListener(cleaner);
            }
        });
    }

    static class ReferenceCleaner implements MessageQueue.IdleHandler, View.OnAttachStateChangeListener,
            ViewTreeObserver.OnGlobalFocusChangeListener {

        private final InputMethodManager inputMethodManager;
        private final Field mHField;
        private final Field mServedViewField;
        private final Method finishInputLockedMethod;

        ReferenceCleaner(InputMethodManager inputMethodManager, Field mHField, Field mServedViewField,
                         Method finishInputLockedMethod) {
            this.inputMethodManager = inputMethodManager;
            this.mHField = mHField;
            this.mServedViewField = mServedViewField;
            this.finishInputLockedMethod = finishInputLockedMethod;
        }

        @Override
        public void onGlobalFocusChanged(View oldFocus, View newFocus) {
            if (newFocus == null) {
                return;
            }
            if (oldFocus != null) {
                oldFocus.removeOnAttachStateChangeListener(this);
            }
            Looper.myQueue().removeIdleHandler(this);
            newFocus.addOnAttachStateChangeListener(this);
        }

        @Override
        public void onViewAttachedToWindow(View v) {
        }

        @Override
        public void onViewDetachedFromWindow(View v) {
            v.removeOnAttachStateChangeListener(this);
            Looper.myQueue().removeIdleHandler(this);
            Looper.myQueue().addIdleHandler(this);
        }

        @Override
        public boolean queueIdle() {
            clearInputMethodManagerLeak();
            return false;
        }

        private void clearInputMethodManagerLeak() {
            try {
                Object lock = mHField.get(inputMethodManager);
                // This is highly dependent on the InputMethodManager implementation.
                synchronized (lock) {
                    View servedView = (View) mServedViewField.get(inputMethodManager);
                    if (servedView != null) {

                        boolean servedViewAttached = servedView.getWindowVisibility() != View.GONE;

                        if (servedViewAttached) {
                            // The view held by the IMM was replaced without a global focus change. Let's make
                            // sure we get notified when that view detaches.

                            // Avoid double registration.
                            servedView.removeOnAttachStateChangeListener(this);
                            servedView.addOnAttachStateChangeListener(this);
                        } else {
                            // servedView is not attached. InputMethodManager is being stupid!
                            Activity activity = extractActivity(servedView.getContext());
                            if (activity == null || activity.getWindow() == null) {
                                // Unlikely case. Let's finish the input anyways.
                                finishInputLockedMethod.invoke(inputMethodManager);
                            } else {
                                View decorView = activity.getWindow().peekDecorView();
                                boolean windowAttached = decorView.getWindowVisibility() != View.GONE;
                                if (!windowAttached) {
                                    finishInputLockedMethod.invoke(inputMethodManager);
                                } else {
                                    decorView.requestFocusFromTouch();
                                }
                            }
                        }
                    }
                }
            } catch (Exception unexpected) {
                Log.e("IMMLeaks", "Unexpected reflection exception", unexpected);
            }
        }

        private Activity extractActivity(Context context) {
            while (true) {
                if (context instanceof Application) {
                    return null;
                } else if (context instanceof Activity) {
                    return (Activity) context;
                } else if (context instanceof ContextWrapper) {
                    Context baseContext = ((ContextWrapper) context).getBaseContext();
                    // Prevent Stack Overflow.
                    if (baseContext == context) {
                        return null;
                    }
                    context = baseContext;
                } else {
                    return null;
                }
            }
        }
    }
}
