package com.greenbird.prettyprinter.benchmark.utils;

import javax.xml.xpath.XPathException;

import org.w3c.dom.Document;

public interface XPathFilter {

	void filter(Document document) throws XPathException;
}
