// -*- coding: utf-8-unix -*-
package com.github.sgr.slide.logging;
import java.awt.Component;
import java.util.logging.Level;
import javax.swing.JTable;
import com.github.sgr.slide.MultiLineRenderer;

public class LogRecordRenderer extends MultiLineRenderer {

    public LogRecordRenderer() {
	super("yyyy/MM/dd HH:mm:ss.SSS");
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	if (value instanceof Level) {
	    return super.getTableCellRendererComponent(table, value.toString(), isSelected, hasFocus, row, column);
	} else {
	    return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}
    }
}
