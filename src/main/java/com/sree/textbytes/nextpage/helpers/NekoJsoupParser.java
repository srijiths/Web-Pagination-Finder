package com.sree.textbytes.nextpage.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.log4j.Logger;
import org.cyberneko.html.parsers.SAXParser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Due to bugs in the Jsoup parser, we want a class that uses Neko to do the parse.
 * The same trick could be played with JSoup.
 * 
 * @author Java-Readability
 */
public class NekoJsoupParser {
    private static final Logger logger = Logger.getLogger(NekoJsoupParser.class.getName());

    public NekoJsoupParser() {
        //
    }

    private final class LocalErrorHandler implements ErrorHandler {
        public void error(SAXParseException e) throws SAXException {
            logger.error("Parse error", e);
            throw e;
        }

        public void fatalError(SAXParseException e) throws SAXException {
            logger.error("Parse error", e);
            throw e;
        }

        public void warning(SAXParseException e) throws SAXException {
            logger.warn("Parse warning", e);
        }
    }

    private class Handler extends DefaultHandler {
        private Document document;
        private Element currentElement;
        private int depth;
        Handler(Document document) {
            this.document = document;
        }
        @Override
        public void characters(char[] data, int start, int length) throws SAXException {
            assert currentElement != null;
            currentElement.appendText(new String(data, start, length));
        }
        @Override
        public void endDocument() throws SAXException {
            assert depth == 0;
        }
        @Override
        public void endElement(String uri, String localName, String qname) throws SAXException {
            logger.debug("end element " + qname);
            currentElement = currentElement.parent();
            depth--;
        }
        @Override
        public void ignorableWhitespace(char[] data, int start, int length) throws SAXException {
            characters(data, start, length);
        }
        @Override
        public void startDocument() throws SAXException {
            currentElement = document;
        }
        @Override
        public void startElement(String uri, String localName, String qname, Attributes attrs) throws SAXException {
            logger.debug("start element " + qname + " " + depth);
            Element newElement;
            newElement = currentElement.appendElement(localName);

            for (int ax = 0; ax < attrs.getLength(); ax++) {
                String name = attrs.getQName(ax);
                String value = attrs.getValue(ax);
                newElement.attr(name, value);
            }
            currentElement = newElement;
            depth++;
        }
    }

    public Document parse(InputStream data, String baseUri) throws SAXException, IOException {
        InputSource source = new InputSource();
        source.setByteStream(data);
        SAXParser nekoParser = new SAXParser();
        Document document = new Document(baseUri);
        nekoParser.setContentHandler(new Handler(document));
        nekoParser.setErrorHandler(new LocalErrorHandler());
        nekoParser.parse(source);
        return document;
    }

    public Document parse(String data, String baseUri) throws SAXException, IOException {
        InputSource source = new InputSource();
        source.setCharacterStream(new StringReader(data));
        SAXParser nekoParser = new SAXParser();
        Document document = new Document(baseUri);
        nekoParser.setContentHandler(new Handler(document));
        nekoParser.setErrorHandler(new LocalErrorHandler());
        nekoParser.parse(source);
        return document;
    }
}
