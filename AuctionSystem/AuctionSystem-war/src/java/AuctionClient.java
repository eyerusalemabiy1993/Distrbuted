import javax.jms.*;
import javax.naming.InitialContext;
import java.util.Scanner;

public class AuctionClient implements MessageListener {

    private ConnectionFactory connectionFactory;
    private javax.jms.Connection jmsConnection;
    private Session session;
    private Destination auctionTopic;

    public AuctionClient() throws Exception {
        // Initialize JMS components
        InitialContext context = new InitialContext();
        connectionFactory = (javax.jms.ConnectionFactory) context.lookup("jejeje");
        jmsConnection = connectionFactory.createConnection();
        jmsConnection.start();
        session = jmsConnection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
        auctionTopic = (Destination) context.lookup("jejejedest");

        // Set up message consumer for listening to notifications
        MessageConsumer consumer = session.createConsumer(auctionTopic);
        consumer.setMessageListener(this);
    }

    public void start() throws Exception {
        // Prompt the user for item and bid details
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the name of the item you want to bid on:");
        String item = scanner.nextLine();
        System.out.println("Enter your bid amount:");
        double bid = scanner.nextDouble();

        // Create a message with item and bid details
        MessageProducer producer = session.createProducer(auctionTopic);
        TextMessage bidMessage = session.createTextMessage();
        bidMessage.setStringProperty("item", item);
        bidMessage.setDoubleProperty("bid", bid);

        // Send the message
        producer.send(bidMessage);

        // Wait for a response from the server
        System.out.println("Waiting for response...");
        Thread.sleep(2000);

        // Close the connection
        jmsConnection.close();
    }

    @Override
    public void onMessage(Message message) {
        try {
            // Print the notification message received from the server
            System.out.println(((TextMessage) message).getText());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            AuctionClient auctionClient = new AuctionClient();
            auctionClient.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
