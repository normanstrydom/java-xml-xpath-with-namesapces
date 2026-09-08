package com.example.xpath;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.InputStream;
import java.util.*;

public final class XmlUtils {
    private XmlUtils() { }

    public static Document loadDocument(File file) throws Exception {
        try (InputStream in = java.nio.file.Files.newInputStream(file.toPath())) {
            return loadDocument(in);
        }
    }

    public static Document loadDocument(InputStream in) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(in);
    }

    public static List<String> evaluateXPath(Document doc, String expression) throws Exception {
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        xpath.setNamespaceContext(new DocumentNamespaceContext(doc));

        XPathExpression expr = xpath.compile(expression);
        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        List<String> out = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n instanceof Attr) {
                out.add(((Attr) n).getValue());
            } else if (n.getNodeType() == Node.ELEMENT_NODE || n.getNodeType() == Node.DOCUMENT_NODE) {
                out.add(n.getTextContent());
            } else if (n.getNodeValue() != null) {
                out.add(n.getNodeValue());
            }
        }
        return out;
    }

    /**
     * Convenience: allow XPath expressions that omit a prefix for elements in the
     * document's default namespace by automatically adding the given prefix
     * to unprefixed element names in the expression before evaluation.
     */
    public static List<String> evaluateXPathWithDefault(Document doc, String expression, String defaultPrefix) throws Exception {
        String rewritten = addDefaultPrefix(expression, defaultPrefix);
        return evaluateXPath(doc, rewritten);
    }

    private static String addDefaultPrefix(String expr, String prefix) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("[A-Za-z_][A-Za-z0-9._-]*").matcher(expr);
        StringBuilder sb = new StringBuilder();
        int last = 0;
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            String token = m.group();

            // Determine context characters
            char before = start == 0 ? '\0' : expr.charAt(start - 1);
            char after = end >= expr.length() ? '\0' : expr.charAt(end);

            // Quick guards: skip if token is already a QName (has ':' after) or part of a function call (followed by '(')
            boolean hasColonRight = (after == ':');
            int i = end;
            while (i < expr.length() && Character.isWhitespace(expr.charAt(i))) i++;
            boolean isFunction = (i < expr.length() && expr.charAt(i) == '(');

            boolean shouldPrefix = false;
            if (!hasColonRight && !isFunction) {
                // Only prefix when token appears where an element name would (start of expr or after '/', '(', '[', '|')
                if (start == 0 || before == '/' || before == '(' || before == '[' || before == '|' ) {
                    // exclude common node tests and XPath keywords
                    if (!token.equals("text") && !token.equals("node") && !token.equals("comment") && !token.equals("processing-instruction") && !token.equals("attribute")) {
                        shouldPrefix = true;
                    }
                }
            }

            if (shouldPrefix) {
                sb.append(expr, last, start);
                sb.append(prefix).append(':').append(token);
                last = end;
            }
        }
        sb.append(expr.substring(last));
        return sb.toString();
    }

    private static final class DocumentNamespaceContext implements NamespaceContext {
        private final Map<String,String> prefixToUri = new HashMap<>();
        private final Map<String,String> uriToPrefix = new HashMap<>();

        DocumentNamespaceContext(Document doc) {
            Node root = doc.getDocumentElement();
            if (root != null) {
                NamedNodeMap attrs = root.getAttributes();
                for (int i = 0; i < attrs.getLength(); i++) {
                    Node a = attrs.item(i);
                    String name = a.getNodeName();
                    String value = a.getNodeValue();
                    if ("xmlns".equals(name)) {
                        // default namespace
                        prefixToUri.put("d", value);
                        uriToPrefix.put(value, "d");
                    } else if (name != null && name.startsWith("xmlns:")) {
                        String p = name.substring(6);
                        prefixToUri.put(p, value);
                        uriToPrefix.put(value, p);
                    }
                }
            }
        }

        @Override
        public String getNamespaceURI(String prefix) {
            if (prefix == null) return null;
            if (prefixToUri.containsKey(prefix)) return prefixToUri.get(prefix);
            if ("xml".equals(prefix)) return javax.xml.XMLConstants.XML_NS_URI;
            return javax.xml.XMLConstants.NULL_NS_URI;
        }

        @Override
        public String getPrefix(String namespaceURI) {
            return uriToPrefix.get(namespaceURI);
        }

        @Override
        public Iterator<String> getPrefixes(String namespaceURI) {
            String p = uriToPrefix.get(namespaceURI);
            if (p == null) return Collections.emptyIterator();
            return Collections.singletonList(p).iterator();
        }
    }
}
