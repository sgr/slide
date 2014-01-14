// -*- coding: utf-8-unix -*-
package com.github.sgr.slide.stream;
import javax.swing.SwingUtilities;
import javax.swing.AbstractListModel;

public class StreamListModel<T extends StreamListRow> extends AbstractListModel implements StreamBuffer.Listener {
    private StreamBuffer<T> _sbuf = null;

    public StreamListModel(int capacity) {
	_sbuf = new StreamBuffer<T>(capacity);
	_sbuf.setListener(this);
    }

    @Override
    public int getSize() {
	return _sbuf.getSize();
    }

    public T getRowData(int row) {
	return _sbuf.getRowData(row);
    }

    @Override
    public Object getElementAt(int row) {
	T rowData = _sbuf.getRowData(row);
	if (rowData != null) {
	    return rowData.getElementAt();
	} else {
	    return null;
	}
    }

    public void addRow(T rowData) {
	_sbuf.addRow(rowData);
    }

    public void fireRowDeleted(final int row) {
	if (SwingUtilities.isEventDispatchThread()) {
	    fireIntervalRemoved(this, row, row);
	} else {
	    final StreamListModel self = this;
	    SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			synchronized (self) {
			    fireIntervalRemoved(self, row, row);
			}
		    }
		});
	}
    }

    public void fireRowInserted(final int row) {
	if (SwingUtilities.isEventDispatchThread()) {
	    fireIntervalAdded(this, row, row);
	} else {
	    final StreamListModel self = this;
	    SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			synchronized (self) {
			    fireIntervalAdded(self, row, row);
			}
		    }
		});
	}
    }

    public void fireRowUpdated(final int row, final int optional) {
	// optional は無視される
	if (SwingUtilities.isEventDispatchThread()) {
	    fireContentsChanged(this, row, row);
	} else {
	    final StreamListModel self = this;
	    SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			synchronized (self) {
			    fireContentsChanged(self, row, row);
			}
		    }
		});
	}
    }
}
