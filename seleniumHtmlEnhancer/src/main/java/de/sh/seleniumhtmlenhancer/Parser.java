package de.sh.seleniumhtmlenhancer;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author user
 */
public class Parser {

    static Logger LOG = LoggerFactory.getLogger(Parser.class);
    
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, TransformerException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(new File("src/main/resources/testcase.xml"));
        // normalize text representation
        doc.getDocumentElement().normalize();

        Node tbodyNode = removeSpaces(doc);
        NodeList listOfTrs = doc.getElementsByTagName("tr");
             
        for (int s = 0; s < listOfTrs.getLength(); s++) {

            Node trNode = listOfTrs.item(s);
            if (trNode.getNodeType() == Node.ELEMENT_NODE) {
                NodeList listOfTds = trNode.getChildNodes();
                Node tdNodeCommand = listOfTds.item(0);
                LOG.debug("command " + tdNodeCommand.getTextContent());
                Node tdNodeTarget = listOfTds.item(1);
                if (null != tdNodeTarget) {
                    LOG.debug("target " + tdNodeTarget.getTextContent());
                }

                if (foundChangeButtonClick(tdNodeCommand, tdNodeTarget)) {
                    
                    Node trNodeSibling = trNode.getNextSibling();                    
                    Node newTrNodeSibling = trNodeSibling.cloneNode(true);
                    NodeList newTrChilds = newTrNodeSibling.getChildNodes();

                    Node newTdNodeCommand = newTrChilds.item(0);
                    newTdNodeCommand.setTextContent("waitForElementPresent");
                    tbodyNode.insertBefore(newTrNodeSibling, trNodeSibling);
                    //neuen Knoten ignorieren
                    s++;
                }               
            }//end of if clause

        }//end of for loop with s var
        writeDomDocumentToFile(doc);
    }

    private static Node removeSpaces(Document doc) {
        NodeList listTBody = doc.getElementsByTagName("tbody");
        Node tbodyNode = listTBody.item(0);
        stripSpace(tbodyNode);
        return tbodyNode;
    }

    private static boolean foundChangeButtonClick(Node tdNodeCommand, Node tdNodeTarget) throws DOMException {
        return tdNodeCommand != null
                && tdNodeTarget != null
                && "contentform:next".equals(tdNodeTarget.getTextContent())
                && "click".equals(tdNodeCommand.getTextContent());
    }

    private static void writeDomDocumentToFile(Document doc) throws TransformerException, TransformerFactoryConfigurationError, TransformerConfigurationException {
        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();       
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File("src/main/resources/newtestcase.xml"));

        transformer.transform(source, result);
        // Output to console for testing
        StreamResult consoleResult = new StreamResult(System.out);
        
        transformer.transform(source, consoleResult);
    }

    private static void stripSpace(Node node) {
        Node child = node.getFirstChild();
        while (child != null) {
            // save the sibling of the node that will
            // perhaps be removed and set to null
            Node c = child.getNextSibling();
            if ((child.getNodeType() == Node.TEXT_NODE
                    && child.getNodeValue().trim().length() == 0)
                    || ((child.getNodeType() != Node.TEXT_NODE)
                    && (child.getNodeType() != Node.ELEMENT_NODE))) {
                node.removeChild(child);
            } else // process children recursively
            {
                stripSpace(child);
            }
            child = c;
        }
    }
}
