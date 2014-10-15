// -*- coding: utf-8-unix -*-
package com.github.sgr.slide.logging;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import com.github.sgr.slide.stream.StreamTableRow;

public class LogRecordRow extends StreamTableRow {
    // 時刻, スレッドID, ログレベル, ロガー名, クラス名, メソッド名, メッセージ
    private static Class[] colClasses = new Class[] {Date.class, Integer.class, Level.class, String.class, String.class, String.class, String.class};

    public static int getColumnCount() {
	return colClasses.length;
    }

    public static Class<?> getColumnClass(int column) {
	return colClasses[column];
    }

    private LogRecord _record = null;
    private Date _date = null;
    private Integer _threadID = null;

    public LogRecordRow(LogRecord record) {
	_record = record;
    }

    public LogRecord getRecord() {
	return _record;
    }

    private Date getRecordDate() {
	// lazy generation
	if (_date == null) {
	    _date = new Date(_record.getMillis());
	}
	return _date;
    }

    private Integer getRecordThreadID() {
	// lazy generation
	if (_threadID == null) {
	    _threadID = new Integer(_record.getThreadID());
	}
	return _threadID;
    }

    @Override public Object getValueAt(int column) {
	Object value = null;
	switch (column) {
	case 0:
	    value = getRecordDate();
	    break;
	case 1:
	    value = getRecordThreadID();
	    break;
	case 2:
	    value = _record.getLevel();
	    break;
	case 3:
	    value = _record.getLoggerName();
	    break;
	case 4:
	    value = _record.getSourceClassName();
	    break;
	case 5:
	    value = _record.getSourceMethodName();
	    break;
	case 6:
	    value = _record.getMessage();
	    break;
	}
	return value;
    }

    @Override protected void dispose() {};
}
