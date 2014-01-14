// -*- coding: utf-8-unix -*-
package com.github.sgr.slide;
import java.net.URI;

public final class Link {
    public int start = -1;
    public int end = -1;
    public URI uri = null;

    public Link(int start, int end, URI uri) {
	this.start = start;
	this.end = end;
	this.uri = uri;
    }
}
