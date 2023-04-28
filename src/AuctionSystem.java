import java.io.Serializable;

public class AuctionItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String description;
    private double startingPrice;
    private double currentBid;

    public AuctionItem(String name, String description, double startingPrice) {
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentBid = startingPrice;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public double getCurrentBid() {
        return currentBid;
    }

    public void setCurrentBid(double currentBid) {
        this.currentBid = currentBid;
    }
}