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

package org.apache.airavata.wsmg.matching.XPath;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.airavata.wsmg.broker.ConsumerInfo;
import org.apache.airavata.wsmg.broker.ConsumerList;
import org.apache.airavata.wsmg.broker.ConsumerListManager;
import org.apache.airavata.wsmg.broker.subscription.SubscriptionState;
import org.apache.airavata.wsmg.config.WSMGParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.berkeley.cs.db.yfilter.filter.EXfilterBasic;
import edu.berkeley.cs.db.yfilter.filter.SystemGlobals;
import edu.berkeley.cs.db.yfilterplus.queryparser.Query;
import edu.berkeley.cs.db.yfilterplus.queryparser.XPQuery;
import edu.berkeley.cs.db.yfilterplus.xmltree.XMLTree;

public class YFilterInfo {
    private static final Logger logger = LoggerFactory.getLogger(YFilterInfo.class);

    private EXfilterBasic yfilter = new EXfilterBasic();
    private HashMap<Integer, String> yFilterIdToXPath = new HashMap<Integer, String>();
    private HashMap<Integer, Query> yFilterIdToQuery = new HashMap<Integer, Query>();
    private HashMap<String, Integer> xPathToYFilterId = new HashMap<String, Integer>();
    private ConsumerListManager consumerListmanager = new ConsumerListManager();
    private int index = 0;
    private int counter = 0;

    public EXfilterBasic getYfilter() {
        return yfilter;
    }

    public void setYfilter(EXfilterBasic yfilter) {
        this.yfilter = yfilter;
    }

    public HashMap<Integer, String> getYFilterIdToXPath() {
        return yFilterIdToXPath;
    }

    public void setYFilterIdToXPath(HashMap<Integer, String> filterIdToXPath) {
        yFilterIdToXPath = filterIdToXPath;
    }

    public void addXPathQuery(String xpathExpression, String subscriptionId, SubscriptionState subscribeRequest)
            throws RuntimeException {
        index++;
        counter++;
        if (WSMGParameter.debugYFilter)
            logger.debug("QueryExp=" + xpathExpression);

        Integer yFilterIdObj = xPathToYFilterId.get(xpathExpression);
        int yFilterId = -1;
        if (yFilterIdObj != null) {
            yFilterId = yFilterIdObj.intValue();
        } else {
            Query query = XPQuery.parseQuery(xpathExpression, index);
            if (query == null) {
                throw new RuntimeException("Invalid XPath expression:" + xpathExpression);
            }
            if (WSMGParameter.debugYFilter)
                logger.debug("addSubscription " + xpathExpression + " query :" + query);
            yFilterId = yfilter.addQuery(query);
            if (WSMGParameter.debugYFilter)
                yfilter.printQueryIndex();
            xPathToYFilterId.put(xpathExpression, Integer.valueOf(yFilterId));
            yFilterIdToXPath.put(new Integer(yFilterId), xpathExpression);
            yFilterIdToQuery.put(yFilterId, query);
        }
        if (WSMGParameter.debugYFilter)
            logger.debug("YFilterId=" + yFilterId);

        consumerListmanager.addToConsumerList(xpathExpression, subscribeRequest, subscriptionId);
    }

    public int removeSubscription(String subscriptionId) {

        String xPath = consumerListmanager.getTokenBySubscriptionId(subscriptionId);
        int result = consumerListmanager.removeFromConsumerList(subscriptionId, xPath);
        if (result == 0) {
            return 0;
        }
        int currentConsumerCount = consumerListmanager.getConsumerListByToken(xPath).size();
        if (currentConsumerCount == 0) {
            Integer yFilterId = xPathToYFilterId.get(xPath);
            Query q = yFilterIdToQuery.get(yFilterId);
            yfilter.deleteQuery(q, q.getQueryId());
            yFilterIdToQuery.remove(yFilterId);
        }
        counter--;
        return result;
    }

    public List<ConsumerInfo> getMatchingConsumerList(String messageString) {
        List<ConsumerInfo> matchingConsumerList = new LinkedList<ConsumerInfo>();
        XMLTree tree = new XMLTree(new java.io.StringReader(messageString));
        if (WSMGParameter.debugYFilter)
            tree.print();
        yfilter.setEventSequence(tree.getEvents());
        yfilter.startParsing();

        // print the matched queries //
        if (SystemGlobals.hasQueries) {
            if (WSMGParameter.debugYFilter)

                yfilter.printQueryResults(System.out);
        } else {
            System.out.println("no match");
            return matchingConsumerList;
        }

        Iterator<Integer> it = (Iterator<Integer>) yfilter.getMatchedQueries().iterator();
        while (it.hasNext()) {
            Integer qid = it.next();

            String xpath = yFilterIdToXPath.get(qid);
            ConsumerList consumerList = consumerListmanager.getConsumerListByToken(xpath);

            if (consumerList != null) {// has subscription to this topic
                matchingConsumerList.addAll(consumerList.getConsumerList());
            }
        }
        yfilter.clear();
        return matchingConsumerList;
    }

    public int getCounter() {
        return counter;
    }

}
