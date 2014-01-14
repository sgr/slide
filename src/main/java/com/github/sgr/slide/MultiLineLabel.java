// -*- coding: utf-8-unix -*-
package com.github.sgr.slide;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.AttributedString;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.text.html.HTMLEditorKit;

public class MultiLineLabel extends JComponent implements Scrollable {
    public static Cursor LINK_CURSOR = (new HTMLEditorKit()).getLinkCursor();
    public static Cursor DEFAULT_CURSOR = Cursor.getDefaultCursor();
    public static String LINE_SEPARATOR = System.getProperty("line.separator");
    private static BufferedImage CIMG = GraphicsEnvironment.getLocalGraphicsEnvironment().
	getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(50, 50);

    private AttributedString _atext;
    private Font _font;
    private String _text = null;
    private LinkRect[] _links = null;
    private MouseAdapter _mouseAdapter = null;

    private int _width = 0;
    private Dimension _preferredSize = null;
    private int _maxRows = 0;
    private int _maxHeight = 0;
    private int _minHeight = 0;
    private Color _linkColor = null;

    public MultiLineLabel(String text, AttributedString atext) {
	this();
	setText(text, atext);
    }

    public MultiLineLabel(String text) {
	this();
	setText(text);
    }

    public MultiLineLabel() {
	setOpaque(false);
	_mouseAdapter = new MouseAdapter() {
		@Override
		public void mouseMoved(MouseEvent e) {
		    if (_links != null) {
			int x = e.getX();
			int y = e.getY();
			boolean changed = false;
			boolean eachHit = false;
			for (LinkRect l : _links) {
			    boolean b = false;
			    for (Rectangle2D r : l.rects) {
				if (r.contains(x, y)) {
				    b = true;
				    eachHit = true;
				    break;
				}
			    }
			    if (l.hit != b) {
				l.hit = b;
				changed = true; // 変更があった
				setUnderline(l.link.start, l.link.end, b);
			    }
			}
			if (changed) {
			    repaint();
			}
			if (eachHit) {
			    setCursor(LINK_CURSOR);
			} else {
			    setCursor(DEFAULT_CURSOR);
			}
		    }
		}
		@Override
		public void mouseClicked(MouseEvent e) {
		    if (_links != null) {
			int x = e.getX();
			int y = e.getY();
			for (LinkRect l : _links) {
			    boolean b = false;
			    for (Rectangle2D r : l.rects) {
				if (r.contains(x, y)) {
				    b = true;
				    break;
				}
			    }
			    if (b && l.link.uri != null) {
				try {
				    Desktop.getDesktop().browse(l.link.uri);
				} catch (Exception t) {
				}
			    }
			}
		    }
		}
		private void clear() {
		    if (_links != null) {
			boolean changed = false;
			for (LinkRect l : _links) {
			    if (l.hit) {
				l.hit = false;
				setUnderline(l.link.start, l.link.end, false);
				changed = true;
			    }
			}
			if (changed) {
			    repaint();
			}
		    }
		}
		@Override
		public void mouseEntered(MouseEvent e) {
		    clear();
		}
		@Override
		public void mouseExited(MouseEvent e) {
		    clear();
		}
	    };
	addMouseListener(_mouseAdapter);
	addMouseMotionListener(_mouseAdapter);
    }

    public void dispose() {
	for (MouseListener l : getMouseListeners()) {
	    removeMouseListener(l);
	}
	for (MouseMotionListener l : getMouseMotionListeners()) {
	    removeMouseMotionListener(l);
	}
    }

    private void setText(String text, AttributedString atext) {
	setCursor(DEFAULT_CURSOR);
	_text = null;
	_atext = null;
	_links = null;
	if (text != null && atext != null) {
	    _text = text;
	    _atext = atext;
	    if (_font != null) {
		_atext.addAttribute(TextAttribute.FONT, _font);
	    }
	    _preferredSize = null;
	    updateSize();
	}
    }

    public void setText(String text) {
	setText(text, new AttributedString(text));
    }

    public void setText(String text, Link[] links) {
	setText(text, new AttributedString(text));
	if (links != null && links.length > 0)  {
	    _links = new LinkRect[links.length];
	    for (int i = 0; i < links.length; i++) {
		_links[i] = new LinkRect(links[i]);
	    }
	}
	applyLinkColor();
    }

    private void applyLinkColor() {
	if (_atext != null && _linkColor != null) {
	    _atext.addAttribute(TextAttribute.FOREGROUND, getForeground());
	    if (_links != null) {
		for (LinkRect l : _links) {
		    _atext.addAttribute(TextAttribute.FOREGROUND, _linkColor, l.link.start, l.link.end);
		}
	    }
	}
    }

    public void setLinkColor(Color c) {
	_linkColor = c;
	applyLinkColor();
    }

    public void setUnderline(int start, int end, boolean b) {
	if (_atext != null) {
	    if (b) {
		_atext.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, start, end);
	    } else {
		_atext.addAttribute(TextAttribute.UNDERLINE, -1, start, end);
	    }
	}
    }

    public void ignoreMouse(boolean b) {
	if (b) {
	    disableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK);
	    for (MouseListener l : getMouseListeners()) {
		removeMouseListener(l);
	    }
	    for (MouseMotionListener l : getMouseMotionListeners()) {
		removeMouseMotionListener(l);
	    }
	} else {
	    enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK);
	    addMouseListener(_mouseAdapter);
	    addMouseMotionListener(_mouseAdapter);
	}
    }

    public void setMaximumRows(int rows) {
	_maxRows = rows;
	_maxHeight = (int)Math.ceil(MultiLineLabel.getHeight(rows));
	updateSize();
    }

    public void setMinimumRows(int rows) {
	_minHeight = (int)Math.ceil(MultiLineLabel.getHeight(rows));
	updateSize();
    }

    public void setFont(Font font) {
	_font = null;
	if (font != null) {
	    _font = font;
	    if (_atext != null) {
		_atext.addAttribute(TextAttribute.FONT, _font);
		updateSize();
	    }
	}
    }

    private Dimension calcSize(Graphics2D g, int width) {
	Dimension sz = renderText(g, width, false);
	int w = width != Integer.MAX_VALUE ? Math.max(width, sz.width) : sz.width;
	int h = _minHeight > 0 ? Math.max(sz.height, (int)Math.ceil(_minHeight)) : sz.height;
	return new Dimension(w, h);
    }

    private Dimension calcSize(int width) {
	Graphics2D g2 = CIMG.createGraphics();
	Dimension sz = calcSize(g2, width);
	g2.dispose();
	return sz;
    }

    private void updateSize(Graphics2D g) {
	Dimension d = calcSize(g, (_width > 0 ? _width : getWidth()));
	_preferredSize = d;
    }

    private void updateSize() {
	Graphics2D g2 = CIMG.createGraphics();
	updateSize(g2);
	g2.dispose();
    }

    private LinkRect nextLink(int pos) {
	if (_links != null) {
	    for (LinkRect link : _links) {
		if (link.link.start >= pos) {
		    return link;
		}
	    }
	}
	return null;
    }

    private Dimension renderText(Graphics2D g2, int width, boolean draw) {
	width = width != 0 ? width : Integer.MAX_VALUE;
	if (draw) {
	    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
	}
	if (_links != null) {
	    for (LinkRect lr : _links) {
		lr.rects.clear();
	    }
	}

	FontRenderContext fc = g2.getFontRenderContext();
      	LineBreakMeasurer m = new LineBreakMeasurer(_atext.getIterator(), fc);
	int rows = 0;
	int pos = 0;
	float maxWidth = 0;
	float currY = 0;
	float lastHeight = 0;
	LinkRect currLink = null;
	float restWidth = width; // 行の途中でレイアウトを分ける場合があるため
	while ((pos = m.getPosition()) < _text.length()) {
	    int nextNL = _text.indexOf(LINE_SEPARATOR, pos); // 現在位置から最も近い改行
	    LinkRect nlink = nextLink(pos); // 現在位置から最も近い次のリンク

	    // next layoutを取得する
	    if (pos == nextNL) { // 改行のみの行
		m.setPosition(pos + 1);
		currY += lastHeight;
		rows++;
		restWidth = width;
		continue; // ただちに次の物理行の処理に移る
	    } else {
		if (currLink != null && pos >= currLink.link.end) { // リンクが終わった
		    currLink = null;
		}

		int offsetLimit = -1;
		if (currLink != null) {
		    offsetLimit = nextNL != -1 ? Math.min(nextNL, currLink.link.end) : currLink.link.end;
		} else if (nlink != null) {
		    if (pos == nlink.link.start) {
			currLink = nlink;
			offsetLimit = nextNL != -1 ? Math.min(nextNL, nlink.link.end) : nlink.link.end;
		    } else {
			offsetLimit = nextNL != -1 ? Math.min(nextNL, nlink.link.start) : nlink.link.start;
		    }
		} else {
		    offsetLimit = nextNL;
		}
		TextLayout l = (offsetLimit != -1) ?
		    m.nextLayout(restWidth, offsetLimit, false) :
		    m.nextLayout(restWidth, _text.length(), false);

		if (l != null) {
		    if (restWidth < width &&
			restWidth < l.getVisibleAdvance()) { // 幅を超えたTextLayoutが得られることがある。その場合は行を改めてやり直し
			// System.err.println(String.format("l(%f < %f): %s",
			// 				 restWidth, l.getVisibleAdvance(), _text.substring(pos, m.getPosition())));
		    	m.setPosition(pos); // 戻す
		    	currY += lastHeight;
		    	rows++;
		    	restWidth = width;
		    	continue;
		    }

		    // next layoutの位置を決め、描画する
		    float height = l.getAscent() + l.getDescent() + l.getLeading();
		    float x = l.isLeftToRight() ? width - restWidth : restWidth - l.getAdvance();
		    float y = currY + l.getAscent();
		    if (draw &&
			(((0 < _maxRows) && ((rows + 1) <= _maxRows)) || (currY + height) <= getHeight())) {
			l.draw(g2, x, y);
		    }
		    lastHeight = height;
		    maxWidth = Math.max(maxWidth, width - restWidth);

		    // リンク内の場合、得られたテキストレイアウトから矩形を取得
		    if (currLink != null) {
			Rectangle2D r = l.getBounds();
			r.setRect(x, currY, r.getWidth(), r.getHeight());
			currLink.rects.add(r);
		    }

		    if (offsetLimit == -1 ||
			offsetLimit > m.getPosition() ||
			restWidth <= l.getAdvance()) { // 幅ぴったり
			currY += height;
			rows++;
			restWidth = width;
		    } else if (m.getPosition() == nextNL) { // 行末の改行
			currY += height;
			rows++;
			restWidth = width;
			m.setPosition(m.getPosition() + 1); // 改行の処理は終わった
		    } else if (m.getPosition() == _text.length()) { // 末尾に到達
			currY += height;
			rows++;
		    } else {
			restWidth -= l.getAdvance();
		    }
		} else {
		    if (m.getPosition() < _text.length()) {
			// restWidthが既に１文字も取れないくらい小さい場合だが、
			// LineBreakMeasurer#nextLayoutでfalse指定してるのでここに落ちてくることはないはず
			currY += lastHeight;
			rows++;
			restWidth = width;
			continue;
		    } else {
			break;
		    }
		}

	    }
	}
	return new Dimension((int)Math.ceil(maxWidth),
			     _maxHeight > 0 ? Math.min(_maxHeight, (int)Math.ceil(currY)) : (int)Math.ceil(currY));
    }

    @Override
    public void paintComponent(Graphics g) {
	super.paintComponent(g);
	if (_atext != null) {
	    renderText((Graphics2D)g, (_width > 0 ? _width : getWidth()), true);
	}
    }

    private void setWidth(int width) {
	if (_width != width) {
	    _width = width;
	    if (_atext != null) {
		updateSize();
	    }
	}
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
	super.setBounds(x, y, width, height);
	setWidth(width);
    }

    @Override
    public void setBounds(Rectangle r) {
	super.setBounds(r);
	setWidth(r.width);
    }

    @Override
    public Dimension getPreferredSize() {
	if (_preferredSize == null) {
	    if (_atext != null) {
		updateSize();
	    } else {
		return super.getPreferredSize();
	    }
	}
	return _preferredSize;
    }

    @Override
    public Dimension getMinimumSize() {
	Dimension preferredSize = getPreferredSize();
	if (0 < _minHeight) {
	    return new Dimension(preferredSize.width, Math.max(preferredSize.height, _minHeight));
	} else {
	    return preferredSize;
	}
    }

    @Override
    public Dimension getMaximumSize() {
	Dimension preferredSize = getPreferredSize();
	if (0 < _maxHeight) {
	    return new Dimension(preferredSize.width, Math.min(preferredSize.height, _maxHeight));
	} else {
	    return preferredSize;
	}
    }

    @Override
    public int getHeight() {
	int superHeight = super.getHeight();
	if (0 < _maxHeight && _maxHeight < superHeight) {
	    return _maxHeight;
	} else {
	    return superHeight;
	}
    }

    // Scrollable start
    public Dimension getPreferredScrollableViewportSize() {
	Container c = getParent();
	if (c instanceof JViewport) {
	    JScrollPane p = (JScrollPane)c.getParent();
	    Rectangle r = p.getViewportBorderBounds();
	    int scrollbarWidth = p.getVerticalScrollBar().getPreferredSize().width;
	    // 以下を実行すると、viewportBorderの大きさだけが上から決まることがわかる。
	    // System.err.println(String.format("border[c.getViewSize]: %d",   ((JViewport)c).getViewSize().width));
	    // System.err.println(String.format("border[c.getExtentSize]: %d", ((JViewport)c).getExtentSize().width));
	    // System.err.println(String.format("border[p.viewportBorder]: %d", r.width));
	    // System.err.println(String.format("scrollbar.width: %d", scrollbarWidth));
	    if (r.width > 0) {
		//setWidth(((JViewport)c).getViewSize().width); // これはうまくいかない
		setWidth(r.width);
		if (getPreferredSize().height > r.height) {
		    //System.err.println(String.format("label.height(%d) > view.height(%d)", getPreferredSize().height, r.height));
		    setWidth(r.width - scrollbarWidth);
		}
	    }
	}
	return getPreferredSize();
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
	if (_font != null) {
	    return _font.getSize();
	} else {
	    return getFont().getSize();
	}
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
	return getScrollableBlockIncrement(visibleRect, orientation, direction);
    }

    public boolean getScrollableTracksViewportHeight() {
	return false;
    }

    public boolean getScrollableTracksViewportWidth() {
	return true;
    }
    // Scrollable end

    private static AttributedString sampleText = new AttributedString("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZあいう日本語");

    public static float getHeight(int rows) {
	Graphics2D g2 = CIMG.createGraphics();
	FontRenderContext fc = g2.getFontRenderContext();
	TextLayout l = new TextLayout(sampleText.getIterator(), fc);
	float height = l.getAscent() + l.getDescent() + l.getLeading();
	g2.dispose();
	return height * rows;
    }

    private static class LinkRect {
	public boolean hit = false;
	public Link link = null;
	public ArrayList<Rectangle2D> rects = null;

	public LinkRect(Link link) {
	    this.link = link;
	    rects = new ArrayList<Rectangle2D>();
	}
    }
}
