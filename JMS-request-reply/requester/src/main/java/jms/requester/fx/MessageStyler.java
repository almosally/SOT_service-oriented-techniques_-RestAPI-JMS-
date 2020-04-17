package jms.requester.fx;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

public class MessageStyler {

    private Products request;
    private Message reply;

    public MessageStyler(Products request, Message reply) {
        this.request = request;
        this.reply = reply;
    }

    public Message getReply() {
        return reply;
    }

    public void setReply(Message reply) {
        this.reply = reply;
    }

    public Products getRequest() {
        return request;
    }

    public void setRequest(Products request) {
        this.request = request;
    }

    @Override
    public String toString() {
        try {
            if ( getReply() == null ) {
                return "Product:" + request.getProductName() + " !!!!" + "waiting for reply"+ "!!!!";
            } else {
                return "About:"+ request.getProductName() + " \uD83D\uDCE7 " + ((TextMessage) reply).getText();
            }

        } catch (JMSException ex) {
            ex.printStackTrace();
        }

        return "";
    }
}
