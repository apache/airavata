package org.apache.airavata.wsmg.client.protocol;

import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.airavata.wsmg.commons.WsmgNameSpaceConstants;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;

public class WSNTProtocolClient {

        public static OMElement createSubscriptionMsg(EndpointReference eventSinkLocation, String topicExpression,
                String xpathExpression) throws AxisFault {
            OMFactory factory = OMAbstractFactory.getOMFactory();

            OMElement message = factory.createOMElement("SubscribeRequest", WsmgNameSpaceConstants.WSNT_NS);

            if (topicExpression != null) {
                OMElement topicExpEl = factory.createOMElement("TopicExpression", WsmgNameSpaceConstants.WSNT_NS,
                        message);

                topicExpEl.addAttribute("Dialect", WsmgCommonConstants.TOPIC_EXPRESSION_SIMPLE_DIALECT,
                        WsmgNameSpaceConstants.WSNT_NS);
                topicExpEl.declareNamespace(WsmgNameSpaceConstants.WIDGET_NS);
                topicExpEl.setText(WsmgNameSpaceConstants.WIDGET_NS.getPrefix() + ":" + topicExpression);
            }

            if (xpathExpression != null) {
                OMElement xpathExpEl = factory.createOMElement("Selector", WsmgNameSpaceConstants.WSNT_NS, message);
                xpathExpEl.addAttribute("Dialect", WsmgCommonConstants.XPATH_DIALECT, null);
                xpathExpEl.setText(xpathExpression);
            }

            OMElement useNotifyEl = factory.createOMElement("UseNotify", message.getNamespace(), message);
            useNotifyEl.setText("true");// check wether we still need this

            OMElement eprCrEl = EndpointReferenceHelper.toOM(factory, eventSinkLocation,
                    new QName("ConsumerReference"), WsmgNameSpaceConstants.WSA_NS.getNamespaceURI());

            message.addChild(eprCrEl);
            eprCrEl.setNamespace(message.getNamespace());

            return message;
        }

        public static String decodeSubscriptionResponse(OMElement subscriptionReference) throws AxisFault {

            String subscriptionId = null;

            EndpointReference subscriptionReferenceEPR = EndpointReferenceHelper.fromOM(subscriptionReference);

            Map<QName, OMElement> referenceParams = subscriptionReferenceEPR.getAllReferenceParameters();

            if (referenceParams != null) {
                QName identifierQName = new QName(WsmgNameSpaceConstants.WSNT_NS.getNamespaceURI(),
                        WsmgCommonConstants.SUBSCRIPTION_ID);

                OMElement identifierEl = referenceParams.get(identifierQName);
                subscriptionId = (identifierEl != null) ? identifierEl.getText() : null;

            }

            return subscriptionId;
        }

        public static OMElement createUnsubscribeMsg() {
            OMFactory factory = OMAbstractFactory.getOMFactory();
            OMElement message = factory.createOMElement("UnsubsribeRequest", WsmgNameSpaceConstants.WSNT_NS);

            return message;
        }

        public static OMElement encodeNotification(String topic, OMElement message, EndpointReference producerReference)
                throws AxisFault {
            OMFactory factory = OMAbstractFactory.getOMFactory();

            OMElement topicExpEl = factory.createOMElement("Topic", WsmgNameSpaceConstants.WSNT_NS);
            topicExpEl.addAttribute("Dialect", WsmgCommonConstants.TOPIC_EXPRESSION_SIMPLE_DIALECT, null);
            topicExpEl.declareNamespace(WsmgNameSpaceConstants.WIDGET_NS);
            topicExpEl.setText(WsmgNameSpaceConstants.WIDGET_NS.getPrefix() + ":" + topic);

            OMElement messageToNotify = factory.createOMElement("Notify", WsmgNameSpaceConstants.WSNT_NS);
            messageToNotify.declareNamespace(WsmgNameSpaceConstants.WSNT_NS);
            messageToNotify.declareNamespace(WsmgNameSpaceConstants.WSA_NS);
            OMElement notificationMesssageEl = factory.createOMElement("NotificationMessage",
                    messageToNotify.getNamespace(), messageToNotify);

            notificationMesssageEl.addChild(topicExpEl);

            notificationMesssageEl
                    .addChild(EndpointReferenceHelper.toOM(factory, producerReference, new QName(notificationMesssageEl
                            .getNamespace().getNamespaceURI(), "ProducerReference", notificationMesssageEl
                            .getNamespace().getPrefix()), WsmgNameSpaceConstants.WSA_NS.getNamespaceURI()));

            OMElement messageEl = factory.createOMElement("Message", notificationMesssageEl.getNamespace(),
                    notificationMesssageEl);

            messageEl.addChild(message);
            return messageToNotify;
        }

    }