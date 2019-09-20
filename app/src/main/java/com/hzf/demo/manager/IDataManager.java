package com.hzf.demo.manager;

import java.util.ArrayList;

/**
 * Created by gigabud on 17-12-14.
 */

public interface IDataManager {

    public void addDataChangeListener(IDataChangeListener listener);

    public void removeDataChangeListener(IDataChangeListener listener);

    public void notifyData(String data);

    public ArrayList<IDataChangeListener> getDataChangeListener();

    public void setObject(Object object);

    public Object getObject();
}
