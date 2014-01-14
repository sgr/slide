// -*- coding: utf-8-unix -*-
package com.github.sgr.slide.stream;

public class StreamBuffer<T extends StreamRow> {
    public static interface Listener {
	public void fireRowDeleted(int row);
	public void fireRowInserted(int row);
	public void fireRowUpdated(int row, int optional);
    }

    private Listener _listener = null;

    public void setListener(Listener listener) {
	_listener = listener;
    }

    private StreamEntry<T>[] _buf;
    private int _capacity;
    private int _head = 0;
    private int _tail = 0;

    @SuppressWarnings({"unchecked"})
    public StreamBuffer(int capacity) {
	super();
	_capacity = capacity + 1; // データは配列より１少なく入るため
	_buf = new StreamEntry[_capacity];
	for (int i = 0; i < _capacity; i++) {
	    _buf[i] = new StreamEntry<T>(i);
	}
    }

    private int last(int index) {
	int lidx = index - 1;
	return lidx < 0 ? lidx + _capacity : lidx;
    }

    private int idxToRow(int index) {
	int r = index - _head;
	return r >= 0 ? r : r + _capacity;
    }

    private int rowToIdx(int row) {
	int i = row + _head;
	return i < _capacity ? i : i - _capacity;
    }

    public int getSize() {
	synchronized (this) {
	    int len = _tail - _head;
	    return len < 0 ? len + _capacity : len;
	}
    }

    public T getRowData(int row) {
	if (row < getSize()) {
	    int idx = rowToIdx(row);
	    return _buf[idx].getData();
	} else {
	    return null;
	}
    }

    public void addRow(T rowData) {
	synchronized (this) {
	    final StreamBuffer self = this;
	    if (last(_head) == _tail) { // リングバッファが一杯である
		_tail = last(_tail);
		final int rowTail = idxToRow(_tail);
		if (_listener != null) {
		    _listener.fireRowDeleted(rowTail);
		}
		_buf[_tail].remData();
	    }
	    _head = last(_head);
	    _buf[_head].setData(rowData);
	    final int rowHead = idxToRow(_head);
	    if (_listener != null) {
		_listener.fireRowInserted(rowHead);
	    }
	}
    }

    protected void updateRow(int index, final int optional) {
	if (_listener != null) {
	    synchronized (this) {
		final StreamBuffer self = this;
		final int row = idxToRow(index);
		_listener.fireRowUpdated(row, optional);
	    }
	}
    }

    protected class StreamEntry<T extends StreamRow> implements StreamRow.Observer {
	private int _index = -1;
	private T _data = null;

	public StreamEntry(int index) {
	    _index = index;
	}

	public T getData() {
	    return _data;
	}

	public void setData(T data) {
	    if (data != null) {
		_data = data;
		_data.addObserver(this);
	    }
	}

	public void remData() {
	    if (_data != null) {
		_data.removeObserver(this);
		_data = null;
	    }
	}

	public void update(int optional) {
	    updateRow(_index, optional);
	}
    }
}
