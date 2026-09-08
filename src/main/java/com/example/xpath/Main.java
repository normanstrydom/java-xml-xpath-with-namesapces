package com.example.xpath;

import org.w3c.dom.Document;

import java.io.InputStream;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        InputStream xmlStream = Main.class.getResourceAsStream("/sample.xml");
        if (xmlStream == null) {
            System.err.println("sample.xml not found in resources");
            System.exit(2);
        }

        Document doc = XmlUtils.loadDocument(xmlStream);

        // Examples using automatic default-namespace handling (no 'd:' prefix required)
        List<String> defaults = XmlUtils.evaluateXPathWithDefault(doc, "/root/child", "d");
        System.out.println("Default-namespace child values: " + defaults);

        List<String> nsChildren = XmlUtils.evaluateXPathWithDefault(doc, "/root/ns:child", "d");
        System.out.println("ns:child values: " + nsChildren);

        // Attribute lookup (still uses the namespace prefix for prefixed names)
        List<String> attrs = XmlUtils.evaluateXPathWithDefault(doc, "//ns:child/@attr", "d");
        System.out.println("ns:child @attr values: " + attrs);
    }
}
