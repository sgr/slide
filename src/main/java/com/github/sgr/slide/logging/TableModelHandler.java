// -*- coding: utf-8-unix -*-
package com.github.sgr.slide.logging;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import com.github.sgr.slide.stream.StreamTableModel;
import com.github.sgr.slide.logging.LogRecordRow;

public class TableModelHandler extends Handler {
    private StreamTableModel<LogRecordRow> _tbl = null;

    public TableModelHandler(int capacity) {
	_tbl = new StreamTableModel<LogRecordRow>(capacity);
    }

    public StreamTableModel<LogRecordRow> getModel() {
	return _tbl;
    }

    public List<LogRecord> records() {
	ArrayList<LogRecord> r = new ArrayList<LogRecord>();
	synchronized (_tbl) {
	    for (int i = _tbl.getRowCount() - 1; i >= 0; i--) {
		r.add(_tbl.getRowData(i).getRecord());
	    }
	}
	return r;
    }

    @Override public void publish(LogRecord record) {
	if ((record != null) && isLoggable(record)) {
	    LogRecordRow row = new LogRecordRow(record);
	    _tbl.addRow(row);
	}
    }

    @Override public void close() {
    }

    @Override public void flush() {
    }
}
