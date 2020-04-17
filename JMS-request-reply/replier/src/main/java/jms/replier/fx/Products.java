package jms.replier.fx;

public class Products {

        private String productID;
        private String productName;
        private int quantity;

        Products(String productName,int quantity) {
            this.productName = productName;
            this.quantity=quantity;
        }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}

