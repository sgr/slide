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
import javax.swing.text.html.HTMLEditorKit;

public class LinkLabel extends AttributedLabel implements MouseListener {
    public static Cursor LINK_CURSOR = (new HTMLEditorKit()).getLinkCursor();
    public static Cursor DEFAULT_CURSOR = Cursor.getDefaultCursor();

    private static final Logger log = Logger.getLogger(LinkLabel.class.getCanonicalName());

    private URI _uri = null;

    public LinkLabel() {
	super();
	addMouseListener(this);
    }

    public LinkLabel(String text) {
	this();
	setText(text);
    }

    public void dispose() {
	removeMouseListener(this);
    }

    public void setURI(URI uri) {
	_uri = uri;
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

    // MouseListener
    public void mouseClicked(MouseEvent e) {
	if (_uri != null) {
	    try {
		Desktop.getDesktop().browse(_uri);
	    } catch (IOException t) {
		log.log(Level.SEVERE, MessageFormat.format("failed to open: {0}", _uri.toString()), t);
	    }
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
    }

    public void mouseReleased(MouseEvent e) {
    }
}

