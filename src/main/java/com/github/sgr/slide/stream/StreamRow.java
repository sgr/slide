// -*- coding: utf-8-unix -*-
package com.github.sgr.slide.stream;
import java.util.HashSet;
import java.util.Iterator;

public abstract class StreamRow {
    public static interface Observer {
	public void update(int optional);
    }

    // Following instance method must be overridden
    protected void dispose() {};

    // Implementation of StreamRow
    protected HashSet<StreamRow.Observer> _observers = null;

    protected StreamRow() {
	_observers = new HashSet<StreamRow.Observer>();
    }

    public void addObserver(StreamRow.Observer observer) {
	synchronized (_observers) {
	    _observers.add(observer);
	}
    }

    public synchronized void removeObserver(StreamRow.Observer observer) {
	synchronized (_observers) {
	    _observers.remove(observer);
	    if (_observers.size() == 0) {
		dispose();
	    }
	}
    }

    protected void fireRowUpdated(int optional) {
	synchronized (_observers) {
	    for (StreamRow.Observer o : _observers) {
		if (o != null) {
		    o.update(optional);
		}
	    }
	}
    }
}
