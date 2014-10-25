// -*- coding: utf-8-unix -*-
package com.github.sgr.slide.logging;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import com.github.sgr.slide.stream.StreamTableRow;
import logutil.Log4JLikeFormatter;

public class LogRecordRow extends StreamTableRow {
    private static Log4JLikeFormatter formatter = new Log4JLikeFormatter();
    // 時刻, スレッドID, ログレベル, ロガー名, クラス名, メソッド名, メッセージ
    private static Class[] colClasses = new Class[] {Date.class, Integer.class, Level.class, String.class, String.class, String.class, String.class};

    public static int getColumnCount() {
	return colClasses.length;
    }

    public static Class<?> getColumnClass(int column) {
	return colClasses[column];
    }

    private Date _date = null;
    private Integer _threadID = null;
    private Level _level = null;
    private String _loggerName = null;
    private String _sourceClassName = null;
    private String _sourceMethodName = null;
    private String _message = null;
    private String _formattedString = null;

    public LogRecordRow(LogRecord record) {
	_date = new Date(record.getMillis());
	_threadID = new Integer(record.getThreadID());
	_level = record.getLevel();
	_loggerName = record.getLoggerName();
	_sourceClassName = record.getSourceClassName();
	_sourceMethodName = record.getSourceMethodName();
	_message = record.getMessage();
	_formattedString = formatter.format(record);
    }

    @Override public Object getValueAt(int column) {
	Object value = null;
	switch (column) {
	case 0:
	    value = _date;
	    break;
	case 1:
	    value = _threadID;
	    break;
	case 2:
	    value = _level;
	    break;
	case 3:
	    value = _loggerName;
	    break;
	case 4:
	    value = _sourceClassName;
	    break;
	case 5:
	    value = _sourceMethodName;
	    break;
	case 6:
	    value = _message;
	    break;
	}
	return value;
    }

    public String formattedString() {
	return _formattedString;
    }

    @Override protected void dispose() {
	_date = null;
	_threadID = null;
	_level = null;
	_loggerName = null;
	_sourceClassName = null;
	_sourceMethodName = null;
	_message = null;
	_formattedString = null;
    }
}
