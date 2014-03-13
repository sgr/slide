// -*- coding: utf-8-unix -*-
package com.github.sgr.slide;
import java.awt.Component;
import java.awt.Dimension;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;

public class MultiLineTable extends JTable {
    private static class RowHeightUpdator implements Runnable {
	private JTable _tbl = null;
	private LinkedBlockingQueue<Integer> _invalidRows;

	public RowHeightUpdator(JTable table) {
	    _tbl = table;
	    _invalidRows = new LinkedBlockingQueue<Integer>();
	}

	public void addInvalidRow(Integer row) {
	    _invalidRows.offer(row);
	}

	private int maxRowHeight(int row) {
	    // ここでのrowはビューの行
	    int maxHeight = -1;
	    for (int col = 0; col < _tbl.getColumnCount(); col++) {
		if (col >= 0) {
		    Component c = _tbl.prepareRenderer(_tbl.getCellRenderer(row, col), row, col);
		    maxHeight = Math.max(maxHeight, c.getPreferredSize().height);
		}
	    }
	    return maxHeight;
	}

	private void updateRowHeight(int row) {
	    int r = _tbl.convertRowIndexToView(row);
	    int mrh = maxRowHeight(r);
	    if (mrh > 0 && _tbl.getRowHeight(r) != mrh) {
		_tbl.setRowHeight(r, mrh);
	    }
	}

	public void run() {
	    synchronized (this) {
		if (_invalidRows.size() > 0) {
		    Integer r = -1;
		    while ((r = _invalidRows.poll()) != null) {
			updateRowHeight(r);
		    }
		} else {
		    for (int r = 0; r < _tbl.getRowCount(); r++) {
			updateRowHeight(r);
		    }
		}
	    }
	}
    }

    private RowHeightUpdator _rhUpdator = null;

    public MultiLineTable() {
	super();
	_rhUpdator = new RowHeightUpdator(this);
        setShowHorizontalLines(false);
        setShowVerticalLines(false);
        //setIntercellSpacing(new Dimension(0,0));
    }

    @Override
    public void columnMarginChanged(ChangeEvent evt) {
    	super.columnMarginChanged(evt);
	if (getTableHeader().getResizingColumn() != null) {
	    updateRowHeights();
	}
    }

    @Override
    public void setModel(TableModel model) {
	super.setModel(model);
	updateRowHeights();
    }

    @Override
    public void tableChanged(TableModelEvent evt) {
	super.tableChanged(evt);
	if (_rhUpdator != null) {
	    if (evt.getType() == TableModelEvent.INSERT ||
		evt.getType() == TableModelEvent.UPDATE) {
		for (int mr = evt.getFirstRow(); mr < evt.getLastRow(); mr++) {
		    _rhUpdator.addInvalidRow(convertRowIndexToView(mr));
		}
		updateRowHeights();
	    }
	}
    }

    private void updateRowHeights() {
	if (_rhUpdator != null) {
	    SwingUtilities.invokeLater(_rhUpdator);
	}
    }
}
