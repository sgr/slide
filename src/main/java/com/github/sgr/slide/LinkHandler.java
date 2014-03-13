// -*- coding: utf-8-unix -*-
package com.github.sgr.slide;
import java.awt.event.ActionEvent;
import java.net.URI;
import javax.swing.AbstractAction;
import javax.swing.Icon;

public abstract class LinkHandler extends AbstractAction {
    private URI _uri = null;

    public LinkHandler(String name) {
	super(name);
    }

    public LinkHandler(String name, Icon icon) {
	super(name, icon);
    }

    public void setURI(URI uri) {
	_uri = uri;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	browse(_uri);
    }

    public abstract void browse(URI uri);
}
