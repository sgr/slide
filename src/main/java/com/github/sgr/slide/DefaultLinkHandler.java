// -*- coding: utf-8-unix -*-
package com.github.sgr.slide;
import java.awt.Desktop;
import java.net.URI;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultLinkHandler extends LinkHandler {
    private static final Logger log = Logger.getLogger(DefaultLinkHandler.class.getCanonicalName());

    public DefaultLinkHandler() {
	super("Default Browser");
    }

    public void browse(URI uri) {
	try {
	    if (uri != null) {
		synchronized (this) {
		    Desktop.getDesktop().browse(uri);
		}
	    }
	} catch (Exception t) {
	    log.log(Level.SEVERE, MessageFormat.format("failed to open: {0}", uri.toString()), t);
	}
    }
}
