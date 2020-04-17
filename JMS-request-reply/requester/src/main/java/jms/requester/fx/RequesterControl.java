package jms.requester.fx;

import com.google.gson.Gson;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

// Supermarket Manager
public class RequesterControl {

    public static void main(String[] args) {
        gui gui = new gui().invoke();
        JPanel panel = gui.getPanel();
        DefaultListModel<MessageStyler> listModel = gui.getListModel();
        JTextField productName = gui.getProductName();
        JButton sendButton = gui.getSendButton();
        connection(panel, listModel, productName, sendButton);
    }

    private static void connection(JPanel panel, DefaultListModel<MessageStyler> listModel, JTextField productName, JButton sendButton) {
        // set up the connection
        Connection connection; // to connect to the ActiveMQ
        Session session; // session for creating messages, producers and

        Destination requestQueue; // reference to a queue/topic destination
        Destination replyQueue;

        MessageProducer producer; // for sending messages
        MessageConsumer messageConsumer; // for receiving replies


        try {
            ConnectionFactory connectionFactory;
            connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // for receiving replies
            //Return address pattern to make sure that reply will attached to the correct request
            replyQueue = session.createTemporaryQueue();
            messageConsumer = session.createConsumer(replyQueue);

            // for making requests
            requestQueue = session.createQueue("RequestQueue");
            producer = session.createProducer(requestQueue);


            HashMap<Products, Message> hashMap = new HashMap<>(); //use hash map to correlate Question object to the Message
            requester_btn(listModel, productName, sendButton, session, requestQueue, replyQueue, producer, hashMap);

            listener(panel, listModel, messageConsumer, hashMap);

            //connecting
            connection.start();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private static void requester_btn(DefaultListModel<MessageStyler> listModel, JTextField productName, JButton sendButton, Session session, Destination requestQueue, Destination replyQueue, MessageProducer producer, HashMap<Products, Message> hashMap) {
        // on button event
        sendButton.addActionListener(e -> {
            // send the message
            try {
                // create a new request object
                String productNameFill = productName.getText();
                if (productName.getText().equals("")) {
                    JOptionPane.showMessageDialog(null, "Insert product name in the textbox: " + JOptionPane.INFORMATION_MESSAGE);

                } else {
                    Products question = new Products(productNameFill);
                    // serialize to JSON using Gson lib
                    Gson gson = new Gson();
                    String jsonString = gson.toJson(question);
                    Message request = session.createTextMessage(jsonString);
                    hashMap.put(question, request);

                    // this is for correlating request to reply in the GUI
                    MessageStyler customMessage = new MessageStyler(question, null);
                    request.setJMSDestination(requestQueue);
                    request.setJMSReplyTo(replyQueue);


                    producer.send(request);  //send a request message
                    listModel.addElement(customMessage);   // add msg to the list
                }

            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        });
    }

    private static void listener(JPanel panel, DefaultListModel<MessageStyler> listModel, MessageConsumer messageConsumer, HashMap<Products, Message> hashMap) throws JMSException {
        // listening for replies here
        messageConsumer.setMessageListener(reply -> {
            try {
                System.out.println(((TextMessage) reply).getText() + " " + reply.getJMSCorrelationID());

                for (int i = 0; i < listModel.size(); i++) {
                    if (hashMap.get(listModel.get(i).getRequest()).getJMSMessageID().equals(reply.getJMSCorrelationID())) {
                        listModel.get(i).setReply(reply);
                    }
                }


                panel.repaint();// refresh GUI
                panel.revalidate();

            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
    }


    private static class gui {
        //Gui using swing
        private JPanel panel;
        private DefaultListModel<MessageStyler> listModel;
        private JTextField productName;
        private JButton sendButton;

        public JPanel getPanel() {
            return panel;
        }

        public DefaultListModel<MessageStyler> getListModel() {
            return listModel;
        }

        public JTextField getProductName() {
            return productName;
        }

        public JButton getSendButton() {
            return sendButton;
        }

        public gui invoke() {

            JFrame frame = new JFrame();
            frame.setTitle("SuperMarket Manager");

            panel = new JPanel();
            panel.setLayout(new FlowLayout());
            //  panel.setBackground(Color.gray);
            panel.setBackground(new Color(193, 255, 130));
            panel.setPreferredSize(new Dimension(500, 300));
            // store messages here
            listModel = new DefaultListModel<>();

            JList<MessageStyler> list = new JList<>(listModel);
            list.setPreferredSize(new Dimension(450, 200));
            list.setAlignmentX(Component.TOP_ALIGNMENT);

            productName = new JTextField();
            productName.setText("");
            productName.setPreferredSize(new Dimension(300, 30));
            sendButton = new JButton();
            sendButton.setText("Ask");

            // make sure we can select and reply to one single message only
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


            // add GUI elements
            panel.add(list);
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            panel.add(new Label("Ask about product name: "));
            panel.add(productName);
            panel.add(sendButton);

            frame.add(panel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            frame.pack();
            frame.setVisible(true);
            return this;
        }
    }
}
