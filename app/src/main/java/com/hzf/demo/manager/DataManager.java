package com.hzf.demo.manager;

import java.util.ArrayList;

/**
 * Created by gigabud on 17-5-9.
 */

public class DataManager implements IDataManager {
    private static final String TAG = "DataManager";
    private static DataManager mDataManager;

    private Object mObject;


    private ArrayList<IDataChangeListener> mListeners;

    public static IDataManager getInstance() {
        if (mDataManager == null) {
            synchronized (TAG) {
                if (mDataManager == null) {
                    mDataManager = new DataManager();
                }
            }
        }
        return mDataManager;
    }

    private DataManager() {

    }


    @Override
    public void addDataChangeListener(IDataChangeListener listener) {
        if (mListeners == null) {
            mListeners = new ArrayList<>();
        }
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    @Override
    public void removeDataChangeListener(IDataChangeListener listener) {
        if (mListeners != null) {
            mListeners.remove(listener);
        }
    }

    public void notifyData(String data) {
        if (mListeners != null) {
            for (IDataChangeListener listener : mListeners) {
                listener.onDataChange(data);
            }
        }
    }


    @Override
    public ArrayList<IDataChangeListener> getDataChangeListener() {
        return mListeners;
    }

    @Override
    public void setObject(Object object) {
        mObject = object;
    }

    @Override
    public Object getObject() {
        return mObject;
    }

}
