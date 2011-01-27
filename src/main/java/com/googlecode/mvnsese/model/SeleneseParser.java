package com.googlecode.mvnsese.model;

import com.googlecode.mvnsese.model.Command;
import com.googlecode.mvnsese.model.Selenese;
import com.googlecode.mvnsese.model.SeleneseSuite;
import com.googlecode.mvnsese.model.SeleneseTest;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SeleneseParser {

    static XPathFactory xpathFactory = XPathFactory.newInstance();
    static DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

    static {
        docFactory.setNamespaceAware(false);
        docFactory.setValidating(false);
    }

    public static Selenese parse(File f) throws Exception {
        Element doc = parseDocument(f);
        XPath xpath = xpathFactory.newXPath();

        Attr isSuite = (Attr) xpath.evaluate("/html/body/table/@id", doc, XPathConstants.NODE);
        if (isSuite != null && "suiteTable".equals(isSuite.getTextContent())) {
            return loadSuite(doc, f);
        }

        Attr isTest = (Attr) xpath.evaluate("/html/head/@profile", doc, XPathConstants.NODE);
        if (isTest != null && "http://selenium-ide.openqa.org/profiles/test-case".equals(isTest.getTextContent())) {
            return loadTest(doc, f);
        }
        throw new Exception(String.format("Unknown Selenese file format for file %s",f.getPath()));
    }

    public static SeleneseSuite loadSuite(Element doc, File f) throws Exception {
        SeleneseSuite suite = new SeleneseSuite();
        XPath xpath = xpathFactory.newXPath();

        suite.setFileName(f.getPath());
        Element suiteTitle = (Element) xpath.evaluate("/html/body/table/tbody/tr[1]/td/b", doc, XPathConstants.NODE);
        if (suiteTitle != null) {
            suite.setTitle(suiteTitle.getTextContent());
        }

         NodeList testList = (NodeList) xpath.evaluate("/html/body/table/tbody//tr/td/a", doc, XPathConstants.NODESET);
        if (testList != null) {
            List<SeleneseTest> tests = new ArrayList<SeleneseTest>();
            for (int i = 0; i < testList.getLength(); i++) {
                Element testNode = (Element) testList.item(i);
                SeleneseTest test = (SeleneseTest)parse(new File(f.getParentFile(),testNode.getAttribute("href")));
                test.setTitle(testNode.getTextContent());
                test.setFileName(testNode.getAttribute("href"));
                tests.add(test);
            }
            suite.setTests(tests);
        }

        return suite;
    }

    public static SeleneseTest loadTest(Element doc, File f) throws Exception {
        SeleneseTest test = new SeleneseTest();
        XPath xpath = xpathFactory.newXPath();

        test.setFileName(f.getPath());
        Element testTitle = (Element) xpath.evaluate("/html/body/table/thead/tr/td", doc, XPathConstants.NODE);
        if (testTitle != null) {
            test.setTitle(testTitle.getTextContent());
        }
        test.setTitle(testTitle.getTextContent());

        Attr baseURL = (Attr) xpath.evaluate("/html/head/link[@rel='selenium.base']/@href", doc, XPathConstants.NODE);
        if (baseURL != null) {
            test.setBaseURL(baseURL.getTextContent());
        }
        test.setTitle(testTitle.getTextContent());

        NodeList commandList = (NodeList) xpath.evaluate("/html/body/table/tbody/tr", doc, XPathConstants.NODESET);
        if (commandList != null) {
            List<Command> commands = new ArrayList<Command>();
            XPathExpression child = xpath.compile("td");
            for (int i = 0; i < commandList.getLength(); i++) {
                Element commandNode = (Element) commandList.item(i);
                NodeList children = (NodeList) child.evaluate(commandNode, XPathConstants.NODESET);
                Command command = new Command();
                command.setName(((Element) children.item(0)).getTextContent());
                command.setTarget(((Element) children.item(1)).getTextContent());
                command.setValue(((Element) children.item(2)).getTextContent());
                commands.add(command);
            }
            test.setCommands(commands);
        }

        return test;
    }

    public static Element parseDocument(File f) throws Exception {

        DocumentBuilder builder = docFactory.newDocumentBuilder();
        builder.setEntityResolver(new EntityResolver() {

            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                return new InputSource(new ByteArrayInputStream(new byte[0]));
            }
        });
        Document doc = builder.parse(f);
        return doc.getDocumentElement();

    }
}
