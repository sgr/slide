// -*- coding: utf-8-unix -*-
package com.github.sgr.slide.stream;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

public class StreamTableModel<T extends StreamTableRow> extends AbstractTableModel implements StreamBuffer.Listener {
    private StreamBuffer<T> _sbuf = null;

    public StreamTableModel(int capacity) {
	_sbuf = new StreamBuffer<T>(capacity);
	_sbuf.setListener(this);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
	return false;
    }

    @Override
    public int getRowCount() {
	return _sbuf.getSize();
    }

    @Override
    public int getColumnCount() {
	return T.getColumnCount();
    }

    @Override
    public Class<?> getColumnClass(int column) {
	return T.getColumnClass(column);
    }

    public T getRowData(int row) {
	return _sbuf.getRowData(row);
    }

    @Override
    public Object getValueAt(int row, int column) {
	T data = (T)_sbuf.getRowData(row);
	if (data != null) {
	    return data.getValueAt(column);
	} else {
	    return null;
	}
    }

    public void addRow(T rowData) {
	_sbuf.addRow(rowData);
    }

    public void fireRowDeleted(final int row) {
	if (SwingUtilities.isEventDispatchThread()) {
	    fireTableRowsDeleted(row, row);
	} else {
	    final StreamTableModel self = this;
	    SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			synchronized (self) {
			    fireTableRowsDeleted(row, row);
			}
		    }
		});
	}
    }

    public void fireRowInserted(final int row) {
	if (SwingUtilities.isEventDispatchThread()) {
	    fireTableRowsInserted(row, row);
	} else {
	    final StreamTableModel self = this;
	    SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			synchronized (self) {
			    fireTableRowsInserted(row, row);
			}
		    }
		});
	}
    }

    public void fireRowUpdated(final int row, final int optional) {
	// optional は column として扱う
	if (SwingUtilities.isEventDispatchThread()) {
	    fireTableCellUpdated(row, optional);
	} else {
	    final StreamTableModel self = this;
	    SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			synchronized (self) {
			    fireTableCellUpdated(row, optional);
			}
		    }
		});
	}
    }
}
