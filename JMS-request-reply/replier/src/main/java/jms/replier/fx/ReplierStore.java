package jms.replier.fx;

import com.google.gson.Gson;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

//office replier
public class ReplierStore {

    public static void main(String[] args) {
        gui gui = new gui().invoke();
        DefaultListModel<MessageStyler> listModel = gui.getListModel();
        JList<MessageStyler> list = gui.getList();
        JTextField replyField = gui.getReplyField();
        JTextField quantity = gui.getQuantity();
        JButton sendButton = gui.getSendButton();
        connection(listModel, list, replyField, quantity, sendButton);
    }

    private static void connection(DefaultListModel<MessageStyler> listModel, JList<MessageStyler> list, JTextField replyField, JTextField quantity, JButton sendButton) {
        // set up the connection
        Connection connection; // to connect to the ActiveMQ
        Session session; // session for creating messages, producers and
        Destination requestQueue; // reference to a queue/topic destination
        MessageConsumer consumer;

        MessageProducer producer;// for sending messages

        //create connection
        try {
            ConnectionFactory connectionFactory;
            connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            System.out.println("Running...");

            requestQueue = session.createQueue("RequestQueue");
            consumer = session.createConsumer(requestQueue);
            producer = session.createProducer(null);

            // Establish connection and receive messages
            connection.start();

            // (list stores MessageStyler objects which has Question object)
            HashMap<Products, Message> hashMap = new HashMap<>();// hash map to know to which Message in the list we should send the reply to
            request_listener(listModel, replyField, consumer, hashMap);

            replier_btn(list, replyField, quantity, sendButton, session, producer, hashMap);


        } catch (JMSException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void request_listener(DefaultListModel<MessageStyler> listModel, JTextField replyField, MessageConsumer consumer, HashMap<Products, Message> hashMap) throws JMSException {
        //replier to request
        consumer.setMessageListener(request -> {
            try {
                // Gson lib
                Gson gson = new Gson();
                Products question = gson.fromJson(((TextMessage) request).getText(), Products.class);
                hashMap.put(question, request);


                replyField.setText(question.getProductName());// product name field text is not used to be sent back to the store manager

                // message replay in gui
                MessageStyler customMessage = new MessageStyler(question, null);

                listModel.addElement(customMessage);

            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
    }

    private static void replier_btn(JList<MessageStyler> list, JTextField replyField, JTextField quantity, JButton sendButton, Session session, MessageProducer producer, HashMap<Products, Message> hashMap) {
        // on click button reply
        sendButton.addActionListener(e -> {
            try {
                //check answers if null
                if (replyField.getText().equals("") || quantity.getText().equals("")) {
                    JOptionPane.showMessageDialog(null, "Answer cant be empty fill boxes: " + JOptionPane.INFORMATION_MESSAGE);

                } else {
                    // get request from the selected item in the GUI list
                    MessageStyler customMessage = list.getSelectedValue();
                    Message request = hashMap.get(customMessage.getRequest());
                    // create a reply manage reply style
                    String className = ReplierStore.class.getName();
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    Date date = new Date();
                    Message reply = session.createTextMessage(replyField.getText() + " Quantity: " + quantity.getText() + " -replied at: " + formatter.format(date));
                    Destination returnAddress = request.getJMSReplyTo();
                    reply.setJMSCorrelationID(request.getJMSMessageID());

                    customMessage.setReply(reply);
                    list.updateUI();

                    producer.send(returnAddress, reply); // send reply
                }
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        });

        list.addListSelectionListener(e -> {
            //Display product with ID request
            replyField.setText(list.getSelectedValue().getRequest().getProductID());
        });
    }

    private static class gui {
        //GUI using swing
        private DefaultListModel<MessageStyler> listModel;
        private JList<MessageStyler> list;
        private JTextField replyField;
        private JTextField quantity;
        private JButton sendButton;

        public DefaultListModel<MessageStyler> getListModel() {
            return listModel;
        }

        public JList<MessageStyler> getList() {
            return list;
        }

        public JTextField getReplyField() {
            return replyField;
        }

        public JTextField getQuantity() {
            return quantity;
        }

        public JButton getSendButton() {
            return sendButton;
        }

        public gui invoke() {

            JFrame frame = new JFrame();
            frame.setTitle("Office replier");

            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());

            panel.setBackground(new Color(204, 166, 166));
            panel.setPreferredSize(new Dimension(500, 300));

            //list
            listModel = new DefaultListModel<>();

            list = new JList<>(listModel);
            list.setPreferredSize(new Dimension(450, 200));
            //list.setAlignmentX(Component.TOP_ALIGNMENT);

            // select one message per request to reply
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            replyField = new JTextField();
            replyField.setPreferredSize(new Dimension(200, 30));

            quantity = new JTextField();
            quantity.setPreferredSize(new Dimension(50, 30));

            sendButton = new JButton();
            sendButton.setText("Reply");

            //GUI elements
            panel.add(list);
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            panel.add(new Label("Reply: "));
            panel.add(replyField);
            panel.add(new Label("Quantity: "));
            panel.add(quantity);
            panel.add(sendButton);

            // set up the frame
            frame.add(panel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
            return this;
        }
    }
}
