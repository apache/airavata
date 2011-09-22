/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.airavata.wsmg.matching.XPath.performance;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import edu.berkeley.cs.db.yfilterplus.queryparser.QueryParser;
import edu.berkeley.cs.db.yfilterplus.queryparser.XPQueryParser;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axiom.soap.SOAPEnvelope;

public class XppXPath {

    private Vector xPathExpressions = new Vector();
    protected BufferedReader m_in = null;
    private static final boolean DEBUG = false;
    long total = 0;

    public int[] getMatchedLinks(String message) {

        return null;
    }

    public Vector checkQueries(String query) {
        Vector result = new Vector();
        Set xPathExpressionsSet = new HashSet(xPathExpressions);

        int size = xPathExpressions.size();

        for (int i = 0; i < size; i++) {

            boolean match = query.equals(xPathExpressions.get(i));

            // long start=System.nanoTime();
            if (match) {

                result.add(new Integer(i));

            }
            // long end=System.nanoTime();
            // total+=(end-start);
        }

        // System.out.println("Total="+total);
        return result;
    }

    public boolean checkQueriesVectorToSet(Set queries) {
        // Vector result=new Vector();
        Set xPathExpressionsSet = new HashSet(xPathExpressions);

        int size = xPathExpressions.size();
        Iterator iter = queries.iterator();
        int counter = 0;
        while (iter.hasNext()) {
            Object query = iter.next();
            for (int i = 0; i < size; i++) {

                boolean match = query.equals(xPathExpressions.get(i));

                // long start=System.nanoTime();
                if (match) {

                    return true;

                }
                // long end=System.nanoTime();
                // total+=(end-start);
            }
            counter++;
            // System.out.println("counter="+counter);
        }
        // System.out.println("Total="+total);
        return false;
    }

    public boolean checkQueriesBySet(Set queries) {
        Set xPathExpressionsSet = new HashSet(xPathExpressions);
        Iterator iter = queries.iterator();
        int counter = 0;
        while (iter.hasNext()) {
            if (xPathExpressionsSet.contains(iter.next())) {
                // System.out.println("counter="+counter);
                return true;
            }
            counter++;
        }
        return false;
    }

    public void addXPathExpressions(String xPathExpression) {
        xPathExpressions.add(xPathExpression);
    }

    public void readQueriesFromFile(String queryFile) {
        int noQueries = Integer.MAX_VALUE;
        int qNum = 0;
        // QueryParser qp = new XFQueryParser(queryFile);
        QueryParser qp = new XPQueryParser(queryFile);
        // Query query;
        String queryString;
        while (qNum < noQueries && ((queryString = qp.readNextQueryString()) != null)) {
            if (DEBUG)
                System.out.println(queryString);
            addXPathExpressions(queryString);
            qNum++;
        }
    }

    public String getARandomQuery() {
        int index = (int) (xPathExpressions.size() * (Math.random()));
        return (String) xPathExpressions.get(index);
    }

    // From http://www.rgagnon.com/javadetails/java-0052.html
    public static String readFile(String filename) throws IOException {
        String lineSep = System.getProperty("line.separator");
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String nextLine = "";
        StringBuffer sb = new StringBuffer();
        while ((nextLine = br.readLine()) != null) {
            sb.append(nextLine);
            //
            // note:
            // BufferedReader strips the EOL character.
            //
            sb.append(lineSep);
        }
        return sb.toString();
    }

    /**
     * @param args
     * @throws IOException
     * @throws FactoryConfigurationError
     * @throws XMLStreamException
     */
    public static void main(String[] args) throws IOException, XMLStreamException, FactoryConfigurationError {
        String queryFile = "C:\\YiFile\\yfilter-1.0\\yfilter-1.0\\queries2.txt";
        XppXPath xppXPath = new XppXPath();
        xppXPath.readQueriesFromFile(queryFile);
        long total = 0;
        final int round = 100;
        String message = readFile("c:\\YiFile\\testdata\\soap2.txt");

        int messageStartPoint = message.indexOf('<');
        String xpathList = message.substring(0, messageStartPoint);
        System.out.println("XpathList=" + xpathList);
        System.out.println("*****************************************");
        long start0 = System.nanoTime();
        StringTokenizer parser0 = new StringTokenizer(xpathList, ";");
        Set xpathTokens = new HashSet();
        while (parser0.hasMoreTokens()) {
            xpathTokens.add(parser0.nextToken());
        }
        long end0 = System.nanoTime();
        long total0 = (end0 - start0);
        // for(int i=0;i<xpathTokens.size();i++){
        // System.out.println((String)xpathTokens.get(i));
        // }
        System.out.println("Avg Time to token=" + (total0));
        System.out.println("Total token=" + xpathTokens.size());
        boolean result = false;
        for (int i = 0; i < round; i++) {
            String randomQuery = xppXPath.getARandomQuery();
            // xpathTokens.add(randomQuery);
            // Vector result=null;

            long start = System.nanoTime();
            // result=xppXPath.checkQueries(randomQuery);
            result = xppXPath.checkQueriesBySet(xpathTokens);
            // result=xppXPath.checkQueriesVectorToSet(xpathTokens);
            long end = System.nanoTime();
            total += (end - start);
        }
        System.out.println("Match result=" + result);
        System.out.println("Avg Time for Checking=" + (total / round));

        // XSUL
        // long start=System.nanoTime();
        // XmlElement messageEl = builder.parseFragmentFromReader(new
        // StringReader(
        // message));
        // XmlElement messageIdEl= messageEl.element(null,
        // "Header").element(null,"MessageID");
        // String messageId=messageIdEl.requiredTextContent();
        // System.out.println("MessageId="+messageId);
        // long end=System.nanoTime();
        // total=(end-start);
        // System.out.println("Avg Time="+(total));

        // AXIOM
        // long start=System.nanoTime();
        // create the parser
        // XMLStreamReader parser =
        // XMLInputFactory.newInstance().createXMLStreamReader(new
        // FileReader("c:\\YiFile\\testdata\\soap_only.txt"));
        // create the builder
        String message1 = readFile("c:\\YiFile\\testdata\\soap_only.txt");
        long start = System.nanoTime();
        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(
                new ByteArrayInputStream(message1.getBytes()));

        OMXMLParserWrapper builder = OMXMLBuilderFactory.createStAXSOAPModelBuilder(
                OMAbstractFactory.getSOAP11Factory(), parser);
        // get the root element (in this case the envelope)

        SOAPEnvelope envelope = (SOAPEnvelope) builder.getDocumentElement();

        // // create the parser
        // XMLStreamReader parser =
        // XMLInputFactory.newInstance().createXMLStreamReader(new
        // FileReader("c:\\YiFile\\testdata\\soap.txt"));
        // // create the builder
        // OMXMLParserWrapper builder =
        // OMXMLBuilderFactory.createStAXSOAPModelBuilder(OMAbstractFactory.getOMFactory(),parser);
        // // get the root element (in this case the envelope)
        // SOAPEnvelope envelope = (SOAPEnvelope)builder.getDocumentElement();
        //
        //
        String messageIDString = envelope.getHeader().getFirstChildWithName(new QName(null, "MessageID")).getText();
        long end = System.nanoTime();
        total = (end - start);
        System.out.println("Avg Time for Axiom=" + (total));
        // OMElement headerEl=envelope.getHeader().getFirstChildWithName(new
        // QName("http://schemas.xmlsoap.org/soap/envelope/", "Header"));
        envelope.getHeader().getFirstChildWithName(new QName(null, "MessageID")).serialize(System.out);
        System.out.println();
        //
        // headerEl.getFirstChildWithName(new QName(null, "MessageID"));

    }

}
