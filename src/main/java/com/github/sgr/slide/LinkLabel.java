// -*- coding: utf-8-unix -*-
package com.github.sgr.slide;
import java.awt.AWTEvent;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLEditorKit;

public class LinkLabel extends AttributedLabel implements MouseListener {
    public static Cursor LINK_CURSOR = (new HTMLEditorKit()).getLinkCursor();
    public static Cursor DEFAULT_CURSOR = Cursor.getDefaultCursor();
    public static LinkHandler DEFAULT_LINK_HANDLER = new DefaultLinkHandler();

    private static final Logger log = Logger.getLogger(LinkLabel.class.getCanonicalName());

    private URI _uri = null;
    private LinkHandlers _lhdrs = null;

    public LinkLabel() {
	super();
	addMouseListener(this);
	setLinkHandlers(new LinkHandlers () {
		public int getHandlerCount() {
		    return 1;
		}
		public LinkHandler getHandler(int idx) {
		    return DEFAULT_LINK_HANDLER;
		}
	    });
    }

    public LinkLabel(String text) {
	this();
	setText(text);
    }

    public void dispose() {
	removeMouseListener(this);
	_uri = null;
	_lhdrs = null;
    }

    public void setURI(URI uri) {
	_uri = uri;
    }

    public void setLinkHandlers(LinkHandlers lhdrs) {
	_lhdrs = lhdrs;
    }

    public void ignoreMouse(boolean b) {
	if (b) {
	    disableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK);
	    for (MouseListener l : getMouseListeners()) {
		removeMouseListener(l);
	    }
	} else {
	    enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK);
	    addMouseListener(this);
	}
    }

    private void popup(MouseEvent e) {
	if (_uri != null && _lhdrs != null && e.isPopupTrigger()) {
	    log.log(Level.FINER, MessageFormat.format("EVENT: {0}", e.paramString()));
	    JComponent c = (JComponent)e.getSource();
	    JPopupMenu pmenu = new JPopupMenu("Open with");
	    for (int i = 0; i < _lhdrs.getHandlerCount(); i++) {
		LinkHandler lh = _lhdrs.getHandler(i);
		lh.setURI(_uri);
		pmenu.add(lh);
	    }
	    pmenu.show(c, e.getX(), e.getY());
	    e.consume();
	}
    }

    // MouseListener
    public void mouseClicked(MouseEvent e) {
	if (_lhdrs != null && !SwingUtilities.isRightMouseButton(e)) {
	    _lhdrs.getHandler(0).browse(_uri);
	}
    }

    public void mouseEntered(MouseEvent e) {
	if (_uri != null) {
	    setCursor(LINK_CURSOR);
	    setUnderline(true);
	    repaint();
	}
    }

    public void mouseExited(MouseEvent e) {
	setCursor(DEFAULT_CURSOR);
	setUnderline(false);
	repaint();
    }

    public void mousePressed(MouseEvent e) {
	popup(e);
    }

    public void mouseReleased(MouseEvent e) {
	popup(e);
    }
}

