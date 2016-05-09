package com.udacity.windy.nettest;

/**
 * Created by windog on 2016/5/6.
 *
 * interface 中的方法都没有方法体，在具体实现类中去写方法逻辑
 */
public interface HttpCallbackListener {

    void onFinish(String response);

    void onError(Exception e);
}
