// -*- coding: utf-8-unix -*-
package com.github.sgr.slide.stream;

public abstract class StreamTableRow extends StreamRow {
    // Following static methods must be overridden
    public static int getColumnCount() { return 0;}
    public static Class<?> getColumnClass(int column) { return Object.class;}

    // Following instance method must be overridden
    public abstract Object getValueAt(int column);
}
