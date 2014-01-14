// -*- coding: utf-8-unix -*-
package com.github.sgr.slide;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.AttributedString;
import java.text.SimpleDateFormat;
import javax.swing.JLabel;

public class AttributedLabel extends JLabel {
    private static BufferedImage CIMG = GraphicsEnvironment.getLocalGraphicsEnvironment().
	getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(50, 50);

    private String _text = null;
    private AttributedString _atext = null;
    protected Font _font = null;
    protected Color _color = Color.BLACK;

    private Dimension _preferredSize = null;
    private int _ascent = -1;

    public AttributedLabel() {
	setOpaque(false);
    }

    public AttributedLabel(String text) {
	this();
	setText(text);
    }

    @Override
    public void setText(String text) {
	_preferredSize = null;
	_atext = null;
	_text = null;
	if (text != null && text.length() > 0) {
	    _text = text;
	    _atext = new AttributedString(text);
	    if (_font != null) {
		applyFont(_font, 0, _text.length());
	    }
	    Graphics2D g2 = CIMG.createGraphics();
	    _preferredSize = preferredStrSize(g2);
	    g2.dispose();
	}
    }

    protected void applyFont(Font font, int beginIndex, int endIndex) {
	if (_atext != null) {
	    _atext.addAttribute(TextAttribute.FONT, font, beginIndex, endIndex);
	}
    }

    @Override
    public void setFont(Font font) {
	_font = null;
	if (font != null) {
	    _font = font;
	    if (_text != null) {
		applyFont(font, 0, _text.length());
	    }
	}
    }

    protected void applyForegroundColor(Color c, int beginIndex, int endIndex) {
	if (_atext != null) {
	    _atext.addAttribute(TextAttribute.FOREGROUND, c, beginIndex, endIndex);
	}
    }

    public void setForegroundColor(Color c) {
	_color = null;
	if (c != null) {
	    _color = c;
	    if (_text != null) {
		applyForegroundColor(c, 0, _text.length());
	    }
	}
    }

    protected void setUnderline(boolean b) {
	if (_atext != null) {
	    if (b) {
		_atext.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, 0, _text.length());
	    } else {
		_atext.addAttribute(TextAttribute.UNDERLINE, -1, 0, _text.length());
	    }
	}
    }

    private Dimension preferredStrSize(Graphics g) {
	Graphics2D g2 = (Graphics2D)g;
	FontRenderContext fc = g2.getFontRenderContext();
	TextLayout l = new TextLayout(_atext.getIterator(), fc);
	Rectangle2D b = l.getBounds();

	_ascent = (int)Math.ceil(l.getAscent());
	return new Dimension((int)Math.ceil(b.getWidth()), (int)Math.ceil(b.getHeight()));
    }

    @Override
    public Dimension getPreferredSize() {
	if (_preferredSize != null) {
	    return _preferredSize;
	} else {
	    return super.getPreferredSize();
	}
    }

    @Override
    public void paintComponent(Graphics g) {
	_preferredSize = preferredStrSize(g);

	super.paintComponent(g);
	if (_atext != null) {
	    Graphics2D g2 = (Graphics2D)g;
	    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
	    g.drawString(_atext.getIterator(), 0, _ascent);
	}
    }
}

