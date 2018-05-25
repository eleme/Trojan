package me.ele.trojan.lancet;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ListView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import me.ele.lancet.base.Origin;
import me.ele.lancet.base.Scope;
import me.ele.lancet.base.This;
import me.ele.lancet.base.annotations.ImplementedInterface;
import me.ele.lancet.base.annotations.Insert;
import me.ele.lancet.base.annotations.TargetClass;
import me.ele.trojan.Trojan;
import me.ele.trojan.config.LogConstants;

public class LancetHook {

    private static final String SUPPORT_FRAGMENT_CLASS = "android.support.v4.app.Fragment";
    private static final String FRAGMENT_CLASS = "android.app.Fragment";
    //最大层级数
    private static final int MAX_HIERARCHY_COUNT = 5;

    //注意:这个tag不能随意设置，要满足两点:第一，唯一性;第二，app中的res id不会用到;第三,framework中的res id不会用到。
    //View.setTag()方法的注释如下
    /**
     * * Sets a tag associated with this view and a key. A tag can be used
     * to mark a view in its hierarchy and does not have to be unique within
     * the hierarchy. Tags can also be used to store data within a view
     * without resorting to another data structure.
     * <p>
     * The specified key should be an id declared in the resources of the
     * application to ensure it is unique (see the <a
     * href="{@docRoot}guide/topics/resources/more-resources.html#Id">ID resource type</a>).
     * Keys identified as belonging to
     * the Android framework or not associated with any package will cause
     * an {@link IllegalArgumentException} to be thrown.
     */
    private static final int TROJAN_FRAG_KEY = 0x8f123456;

    @Insert("printLog")
    @TargetClass("com.socks.library.KLog")
    private static void printLog(int type, String tag, Object... objects) throws Throwable {
        List<String> msgList = new LinkedList<>();
        if (!TextUtils.isEmpty(tag)) {
            msgList.add(tag);
        }
        if (objects != null) {
            for (Object obj : objects) {
                if (obj != null) {
                    msgList.add(obj.toString());
                }
            }
        }
        Trojan.log(LogConstants.KLOG_TAG, msgList);
        Origin.callVoid();
    }

    //如果要知道这个View是属于哪个Activity或者Fragment中，要怎么做呢?
    //有一个思路就是重写Fragment的onCreateView方法，并且对该View进行setTag,将Fragment的对象名放入其中
    //到后面再向上检索(即一路getParent()),一直到某个View中getTag()不为空则取出
    @Insert("onClick")
    @ImplementedInterface(value = "android.view.View$OnClickListener", scope = Scope.LEAF)
    public void onClick(View v) throws Throwable {
        hookClick(v);
        Origin.callVoid();
    }

    public static void hookClick(View v) {
        if (v == null) {
            return;
        }
        List<String> msgList = new LinkedList<>();
        msgList.add(v.getClass().getName());
        if (v.getId() != View.NO_ID) {
            try {
                msgList.add(v.getResources().getResourceName(v.getId()));
            } catch (Exception e) {
                e.printStackTrace();
                msgList.add("~");
            }
        } else {
            msgList.add("~");
        }
        //还要获取到View在哪个页面，即如果是在Fragment中则打印出Fragment信息，否则打印出Activity信息
        String pageInfo = getPageInfo(v);
        if (TextUtils.isEmpty(pageInfo)) {
            msgList.add(v.toString());
        } else {
            msgList.add(pageInfo);
        }
        if (v instanceof TextView
                && ((TextView) v).getText() != null
                && ((TextView) v).getText().toString() != null) {
            msgList.add(((TextView) v).getText().toString());
        } else {
            msgList.add("~");
        }
        //如果是ListView或RecycleView中的某个item,则需要打印出它是第几个ItemView,现详细一点的话甚至需要打印出ItemView中TextView中的信息
        //争光报了bug,先注释这个功能
        //setIndexIfNeed(v, msgList);

        Trojan.log(LogConstants.VIEW_CLICK_TAG, msgList);
    }

    /**
     * 如果是ListView或RecyclerView的ItemView的话就返回index
     *
     * @param view
     * @return
     */
    private static void setIndexIfNeed(View view, List<String> msgList) {
        if (view == null) {
            return;
        }
        int index = 0;
        ViewParent parent = view.getParent();
        while (true) {
            if (index > MAX_HIERARCHY_COUNT) {
                return;
            }
            if (null == parent || !(parent instanceof View)) {
                return;
            }
            if (parent instanceof ListView) {
                msgList.add(getItemInfo((View) parent, view));
                return;
            }
            if (parent instanceof RecyclerView) {
                msgList.add(getItemInfo((View) parent, view));
                return;
            }
            parent = parent.getParent();
            ++index;
        }

    }

    private static String getItemInfo(View parent, View view) {
        if (parent == null || view == null) {
            return "";
        }
        StringBuilder info = new StringBuilder();
        info.append(parent.getClass().getName());
        info.append("@");
        info.append(getHexId(parent));
        info.append("-->Item[");
        if (parent instanceof ListView) {
            info.append((((ListView) parent).getPositionForView(view)));
        } else {
            info.append(((RecyclerView) parent).getChildAdapterPosition(view));
        }
        info.append("]");
        return info.toString();
    }

    private static String getHexId(View view) {
        return Integer.toHexString(view.getId());
    }


    /**
     * 获取页面信息
     *
     * @param view
     * @return
     */
    private static String getPageInfo(View view) {
        String fragInfo = getFragmentTag(view);
        if (!TextUtils.isEmpty(fragInfo)) {
            return fragInfo;
        }
        return getActivityName(view.getContext());
    }

    private static String getActivityName(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return context.getClass().getName();
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return "";
    }

    /**
     * 一路向上回溯
     *
     * @param view
     * @return
     */
    private static String getFragmentTag(View view) {
        View parent = view;
        ViewParent tmpParent;
        String fragTag;
        while (true) {
            fragTag = (String) parent.getTag(TROJAN_FRAG_KEY);
            if (TextUtils.isEmpty(fragTag)) {
                tmpParent = parent.getParent();
                if (null == tmpParent || !(tmpParent instanceof View)) {
                    break;
                }
                parent = (View) tmpParent;
            } else {
                break;
            }
        }
        return fragTag;
    }

    @TargetClass(value = FRAGMENT_CLASS, scope = Scope.LEAF)
    @Insert(value = "onCreateView", mayCreateSuper = true)
    public View onFragCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
        View rootView = (View) Origin.call();
        //要考虑到有些Fragment是没有View的
        if (rootView != null) {
            //在这里放入Fragment对象的名称
            rootView.setTag(TROJAN_FRAG_KEY, getFragmentInfo((android.app.Fragment) This.get()));
        }
        return rootView;
    }

    @TargetClass(value = SUPPORT_FRAGMENT_CLASS, scope = Scope.LEAF)
    @Insert(value = "onCreateView", mayCreateSuper = true)
    public View onSupportFragCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                        @Nullable Bundle savedInstanceState) {
        View rootView = (View) Origin.call();
        if (rootView != null) {
            rootView.setTag(TROJAN_FRAG_KEY, getSupportFragInfo((Fragment) This.get()));
        }
        return rootView;
    }

    private static String getFragmentInfo(android.app.Fragment fragment) {
        StringBuilder info = new StringBuilder();
        String className = fragment.getClass().getName();
        info.append(className);
        if (fragment.getTag() != null) {
            info.append(":");
            info.append(fragment.getTag());
        }
        return info.toString();
    }

    private static String getSupportFragInfo(Fragment fragment) {
        StringBuilder info = new StringBuilder();
        String className = fragment.getClass().getName();
        info.append(className);
        if (fragment.getTag() != null) {
            info.append(":");
            info.append(fragment.getTag());
        }
        return info.toString();
    }

    @TargetClass(value = FRAGMENT_CLASS, scope = Scope.LEAF)
    @Insert(value = "onCreate", mayCreateSuper = true)
    public void fragOnCreate(@Nullable Bundle savedInstanceState) {
        List<String> msgList = new LinkedList<>();
        msgList.add(This.get().getClass().getName());
        msgList.add("onCreate:Bundle" + (savedInstanceState == null ? "=null" : "!=null"));
        Trojan.log(LogConstants.FRAGMENT_LIFE_TAG, msgList);
        Origin.callVoid();
    }

    @TargetClass(value = SUPPORT_FRAGMENT_CLASS, scope = Scope.LEAF)
    @Insert(value = "onCreate", mayCreateSuper = true)
    public void supportFragOnCreate(@Nullable Bundle savedInstanceState) {
        List<String> msgList = new LinkedList<>();
        msgList.add(This.get().getClass().getName());
        msgList.add("onCreate:Bundle" + (savedInstanceState == null ? "=null" : "!=null"));
        Trojan.log(LogConstants.FRAGMENT_LIFE_TAG, msgList);
        Origin.callVoid();
    }

    @TargetClass(value = FRAGMENT_CLASS, scope = Scope.LEAF)
    @Insert(value = "onStart", mayCreateSuper = true)
    public void fragOnStart() {
        List<String> msgList = new LinkedList<>();
        msgList.add(This.get().getClass().getName());
        msgList.add("onStart");
        Trojan.log(LogConstants.FRAGMENT_LIFE_TAG, msgList);
        Origin.callVoid();
    }

    @TargetClass(value = SUPPORT_FRAGMENT_CLASS, scope = Scope.LEAF)
    @Insert(value = "onStart", mayCreateSuper = true)
    public void supportFragOnStart() {
        List<String> msgList = new LinkedList<>();
        msgList.add(This.get().getClass().getName());
        msgList.add("onStart");
        Trojan.log(LogConstants.FRAGMENT_LIFE_TAG, msgList);
        Origin.callVoid();
    }

    @TargetClass(value = FRAGMENT_CLASS, scope = Scope.LEAF)
    @Insert(value = "onResume", mayCreateSuper = true)
    public void fragOnResume() {
        List<String> msgList = new LinkedList<>();
        msgList.add(This.get().getClass().getName());
        msgList.add("onResume");
        Trojan.log(LogConstants.FRAGMENT_LIFE_TAG, msgList);
        Origin.callVoid();
    }

    @TargetClass(value = SUPPORT_FRAGMENT_CLASS, scope = Scope.LEAF)
    @Insert(value = "onResume", mayCreateSuper = true)
    public void supportFragOnResume() {
        List<String> msgList = new LinkedList<>();
        msgList.add(This.get().getClass().getName());
        msgList.add("onResume");
        Trojan.log(LogConstants.FRAGMENT_LIFE_TAG, msgList);
        Origin.callVoid();
    }

    @TargetClass(value = FRAGMENT_CLASS, scope = Scope.LEAF)
    @Insert(value = "onPause", mayCreateSuper = true)
    public void fragOnPause() {
        List<String> msgList = new LinkedList<>();
        msgList.add(This.get().getClass().getName());
        msgList.add("onPause");
        Trojan.log(LogConstants.FRAGMENT_LIFE_TAG, msgList);
        Origin.callVoid();
    }

    @TargetClass(value = SUPPORT_FRAGMENT_CLASS, scope = Scope.LEAF)
    @Insert(value = "onPause", mayCreateSuper = true)
    public void supportFragOnPause() {
        List<String> msgList = new LinkedList<>();
        msgList.add(This.get().getClass().getName());
        msgList.add("onPause");
        Trojan.log(LogConstants.FRAGMENT_LIFE_TAG, msgList);
        Origin.callVoid();
    }

    @TargetClass(value = FRAGMENT_CLASS, scope = Scope.LEAF)
    @Insert(value = "onHiddenChanged", mayCreateSuper = true)
    public void fragOnHiddenChanged() {
        List<String> msgList = new LinkedList<>();
        msgList.add(This.get().getClass().getName());
        msgList.add("onHiddenChanged");
        Trojan.log(LogConstants.FRAGMENT_LIFE_TAG, msgList);
        Origin.callVoid();
    }

    @TargetClass(value = SUPPORT_FRAGMENT_CLASS, scope = Scope.LEAF)
    @Insert(value = "onHiddenChanged", mayCreateSuper = true)
    public void supportFragOnHiddenChanged() {
        List<String> msgList = new LinkedList<>();
        msgList.add(This.get().getClass().getName());
        msgList.add("onHiddenChanged");
        Trojan.log(LogConstants.FRAGMENT_LIFE_TAG, msgList);
        Origin.callVoid();
    }

    @TargetClass(value = FRAGMENT_CLASS, scope = Scope.LEAF)
    @Insert(value = "onStop", mayCreateSuper = true)
    public void fragOnStop() {
        List<String> msgList = new LinkedList<>();
        msgList.add(This.get().getClass().getName());
        msgList.add("onStop");
        Trojan.log(LogConstants.FRAGMENT_LIFE_TAG, msgList);
        Origin.callVoid();
    }

    @TargetClass(value = SUPPORT_FRAGMENT_CLASS, scope = Scope.LEAF)
    @Insert(value = "onStop", mayCreateSuper = true)
    public void supportFragOnStop() {
        List<String> msgList = new LinkedList<>();
        msgList.add(This.get().getClass().getName());
        msgList.add("onStop");
        Trojan.log(LogConstants.FRAGMENT_LIFE_TAG, msgList);
        Origin.callVoid();
    }

    @TargetClass(value = "android.app.Dialog", scope = Scope.LEAF)
    @Insert(value = "show", mayCreateSuper = true)
    public void showDialog() {
        List<String> msgList = new LinkedList<>();
        msgList.add(This.get().getClass().getName());
        msgList.add("show");
        Trojan.log(LogConstants.DIALOG_TAG, msgList);
        Origin.callVoid();
    }

    @TargetClass(value = "android.app.Dialog", scope = Scope.LEAF)
    @Insert(value = "hide", mayCreateSuper = true)
    public void hideDialog() {
        List<String> msgList = new LinkedList<>();
        msgList.add(This.get().getClass().getName());
        msgList.add("hide");
        Trojan.log(LogConstants.DIALOG_TAG, msgList);
        Origin.callVoid();
    }

    @TargetClass(value = "android.app.Dialog", scope = Scope.LEAF)
    @Insert(value = "dismiss", mayCreateSuper = true)
    public void dismissDialog() {
        List<String> msgList = new LinkedList<>();
        msgList.add(This.get().getClass().getName());
        msgList.add("dismiss");
        Trojan.log(LogConstants.DIALOG_TAG, msgList);
        Origin.callVoid();
    }

    @TargetClass(value = "android.app.DialogFragment", scope = Scope.LEAF)
    @Insert(value = "show", mayCreateSuper = true)
    public void showDialogFragment(FragmentManager manager, String tag) {
        List<String> msgList = new LinkedList<>();
        msgList.add(This.get().getClass().getName());
        msgList.add("show");
        Trojan.log(LogConstants.DIALOG_TAG, msgList);
        Origin.callVoid();
    }

    @TargetClass(value = "android.app.DialogFragment", scope = Scope.LEAF)
    @Insert(value = "show", mayCreateSuper = true)
    public int showDialogFragment(FragmentTransaction transaction, String tag) {
        List<String> msgList = new LinkedList<>();
        msgList.add(This.get().getClass().getName());
        msgList.add("show");
        Trojan.log(LogConstants.DIALOG_TAG, msgList);
        return (int) Origin.call();
    }

    @TargetClass(value = "android.support.v4.app.DialogFragment", scope = Scope.LEAF)
    @Insert(value = "show", mayCreateSuper = true)
    public void showSupportDialogFragment(android.support.v4.app.FragmentManager manager, String tag) {
        List<String> msgList = new LinkedList<>();
        msgList.add(This.get().getClass().getName());
        msgList.add("show");
        Trojan.log(LogConstants.DIALOG_TAG, msgList);
        Origin.callVoid();
    }

    @TargetClass(value = "android.support.v4.app.DialogFragment", scope = Scope.LEAF)
    @Insert(value = "show", mayCreateSuper = true)
    public int showSupportDialogFragment(android.support.v4.app.FragmentTransaction transaction, String tag) {
        List<String> msgList = new LinkedList<>();
        msgList.add(This.get().getClass().getName());
        msgList.add("show");
        Trojan.log(LogConstants.DIALOG_TAG, msgList);
        return (int) Origin.call();
    }

    @TargetClass(value = "android.app.DialogFragment", scope = Scope.LEAF)
    @Insert(value = "dismiss", mayCreateSuper = true)
    public void dismissDialogFragment() {
        List<String> msgList = new LinkedList<>();
        msgList.add(This.get().getClass().getName());
        msgList.add("dismiss");
        Trojan.log(LogConstants.DIALOG_TAG, msgList);
        Origin.callVoid();
    }

    @TargetClass(value = "android.support.v4.app.DialogFragment", scope = Scope.LEAF)
    @Insert(value = "dismiss", mayCreateSuper = true)
    public void dismissSupportDialogFragment() {
        List<String> msgList = new LinkedList<>();
        msgList.add(This.get().getClass().getName());
        msgList.add("dismiss");
        Trojan.log(LogConstants.DIALOG_TAG, msgList);
        Origin.callVoid();
    }

    @TargetClass(value = "android.app.DialogFragment", scope = Scope.LEAF)
    @Insert(value = "dismissAllowingStateLoss", mayCreateSuper = true)
    public void dismissAllowingDialogFragment() {
        List<String> msgList = new LinkedList<>();
        msgList.add(This.get().getClass().getName());
        msgList.add("dismissAllowingStateLoss");
        Trojan.log(LogConstants.DIALOG_TAG, msgList);
        Origin.callVoid();
    }

    @TargetClass(value = "android.support.v4.app.DialogFragment", scope = Scope.LEAF)
    @Insert(value = "dismissAllowingStateLoss", mayCreateSuper = true)
    public void dismissAllowingSupportDialogFragment() {
        List<String> msgList = new LinkedList<>();
        msgList.add(This.get().getClass().getName());
        msgList.add("dismissAllowingStateLoss");
        Trojan.log(LogConstants.DIALOG_TAG, msgList);
        Origin.callVoid();
    }

}