package org.phnq.clients.lastfm;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author pgostovic
 */
public abstract class LastFMEntity {

    private Element element;
    private XPath xpath;

    protected LastFMEntity(Element element) {
        this.element = element;
        this.xpath = XPathFactory.newInstance().newXPath();
    }

    protected float getXPathFloat(String path) {
        Float f = getXPathFloatObject(path);
        return f == null ? 0 : f;
    }

    protected Float getXPathFloatObject(String path) {
        try {
            return new Float(getXPathString(path));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    protected int getXPathInt(String path) {
        Integer integer = getXPathInteger(path);
        return integer == null ? 0 : integer;
    }

    protected Integer getXPathInteger(String path) {
        try {
            return new Integer(getXPathString(path));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    protected String getXPathString(String path) {
        return (String) getXPath(path, XPathConstants.STRING);
    }

    protected Node getXPathNode(String path) {
        return (Node) getXPath(path, XPathConstants.NODE);
    }

    protected NodeList getXPathNodes(String path) {
        return (NodeList) getXPath(path, XPathConstants.NODESET);
    }

    private Object getXPath(String path, QName type) {
        try {
            XPathExpression expr = xpath.compile(path);
            return expr.evaluate(this.element, type);
        } catch (XPathExpressionException ex) {
            return null;
        }
    }
}
