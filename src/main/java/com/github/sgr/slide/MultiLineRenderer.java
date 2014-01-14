// -*- coding: utf-8-unix -*-
package com.github.sgr.slide;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

public class MultiLineRenderer implements TableCellRenderer {
    private static Color ODD_ROW_BACKGROUND = new Color(241, 246, 250);
    private MultiLineText _txtRenderer = null;
    private ImageLabel    _imgRenderer = null;
    private SimpleDateFormat _df = null;

    public MultiLineRenderer(String datePattern) {
	_txtRenderer = new MultiLineText();
	_imgRenderer = new ImageLabel();
	if (datePattern != null) {
	    _df = new SimpleDateFormat(datePattern);
	} else {
	    _df = new SimpleDateFormat("HH:mm:ss");
	}
    }

    private JComponent getTxtTableCellRendererComponent(JTable table, String value, boolean isSelected, boolean hasFocus, int row, int column) {
	int width = table.getTableHeader().getColumnModel().getColumn(column).getWidth();
	_txtRenderer.setSize(new Dimension(width, 1000));
	_txtRenderer.setText(value);
	return _txtRenderer;
    }
    
    private JComponent getImgTableCellRendererComponent(JTable table, Icon value, boolean isSelected, boolean hasFocus, int row, int column) {
	_imgRenderer.setIcon(value);
	return _imgRenderer;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	JComponent c = null;
	if (value instanceof String) {
	    c = getTxtTableCellRendererComponent(table, (String)value, isSelected, hasFocus, row, column);
	} else if (value instanceof Date) {
	    String dateString = _df.format((Date)value);
	    c = getTxtTableCellRendererComponent(table, dateString, isSelected, hasFocus, row, column);
	} else if (value instanceof Icon) {
	    c = getImgTableCellRendererComponent(table, (Icon)value, isSelected, hasFocus, row, column);
	} else {
	    if (value != null) {
		c = getTxtTableCellRendererComponent(table, value.toString(), isSelected, hasFocus, row, column);
	    } else {
		_imgRenderer.setIcon(null);
		c = _imgRenderer;
	    }
	}
	// render stripe
	if (isSelected) {
	    c.setForeground(table.getSelectionForeground());
	    c.setBackground(table.getSelectionBackground());
	} else {
	    c.setForeground(table.getForeground());
	    c.setBackground(row % 2 == 0 ? table.getBackground() : ODD_ROW_BACKGROUND);
	}
	return c;
    }

    private static class MultiLineText extends JTextArea {
	public MultiLineText() {
	    setEditable(false);
	    setLineWrap(true);
	    setWrapStyleWord(false);
	}

	// Following methods are overriden for performance reasons
	public void invalidate() {}
	public boolean isOpaque() {return true;}
	public void repaint() {}
	public void repaint(long tm, int x, int y, int width, int height) {}
    	public void repaint(Rectangle r) {}
	public void revalidate() {}
	public void validate() {}
    }

    private static class ImageLabel extends JLabel {
	public ImageLabel() {
	    setOpaque(true);
	    setHorizontalTextPosition(SwingConstants.CENTER);
	    setHorizontalAlignment(SwingConstants.CENTER);
	}

	// Following methods are overriden for performance reasons
	public void invalidate() {}
	public boolean isOpaque() {return true;}
	public void repaint() {}
	public void repaint(long tm, int x, int y, int width, int height) {}
    	public void repaint(Rectangle r) {}
	public void revalidate() {}
	public void validate() {}
    }
}
