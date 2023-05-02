import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;
import javax.jws.*;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import javax.print.attribute.standard.Destination;
public class AuctionSystem implements MessageListener {
 private ConnectionFactory connectionFactory;
 private Connection connection;
 private Session session;
 private Destination auctionTopic;
 private Map<String, Double> itemBids;
 private Map<String, String> itemWinners;
 private DataSource dataSource;
 public AuctionSystem() throws Exception {
 // Initialize JMS components
 InitialContext context = new InitialContext();
 connectionFactory = (ConnectionFactory) 
context.lookup("ConnectionFactory");
 connection = connectionFactory.createConnection();
 connection.start();
 session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 auctionTopic = (Destination) context.lookup("topic/auction");
 // Initialize database
 dataSource = (DataSource) context.lookup("jdbc/myDatabase");
 itemBids = new HashMap<>();
 itemWinners = new HashMap<>();
 }
 public void start() throws Exception {
 MessageConsumer consumer = session.createConsumer(auctionTopic);
 consumer.setMessageListener(this);
 }
 public void stop() throws Exception {
 connection.close();
 }
 @Override
 public void onMessage(Message message) {
 try {
 String item = message.getStringProperty("item");
 double bid = message.getDoubleProperty("bid");
 String bidder = message.getStringProperty("bidder");
 // Check if item exists in the database
 if (!itemBids.containsKey(item)) {
 try (Connection conn = dataSource.getConnection()) {
 PreparedStatement stmt = conn.prepareStatement("INSERT INTO items (name) VALUES (?)");
 stmt.setString(1, item);
 stmt.executeUpdate();
 }
 itemBids.put(item, 0.0);
 }
 // Check if bid is higher than the current highest bid
 if (bid > itemBids.get(item)) {
 // Update highest bid and winner in the database
 try (Connection conn = dataSource.getConnection()) {
 PreparedStatement stmt = conn.prepareStatement("UPDATE items SET highest_bid = ?, winner = ? WHERE name = ?");
 stmt.setDouble(1, bid);
 stmt.setString(2, bidder);
 stmt.setString(3, item);
 stmt.executeUpdate();
 }
 itemBids.put(item, bid);
 itemWinners.put(item, bidder);
 // Notify all bidders of the new highest bid
 sendNotification("New highest bid for " + item + ": $" + bid + " by " + bidder);
 }
 }
 catch (JMSException | SQLException e) {
     e.printStackTrace();
 }
 }
 private void sendNotification(String message) throws JMSException {
 MessageProducer producer = session.createProducer(auctionTopic);
 TextMessage notification = session.createTextMessage(message);
 producer.send(notification);
 }
 public static void main(String[] args) {
 try {
 AuctionSystem auctionSystem = new AuctionSystem();
 auctionSystem.start();
 System.out.println("Auction system started");
 Scanner scanner = new Scanner(System.in);
 System.out.println("Press enter to stop the auction system");
 scanner.nextLine();
 auctionSystem.stop();
 System.out.println("Auction system stopped");
 } catch (Exception e) {
 e.printStackTrace();
 }
 }
}
