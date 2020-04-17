package service.model;


public class Product {

    private int id;
    private String name;
    private String type;
    private double price;
    private int quantity;


    public Product() {
    }

    public Product(int id ,String name, String type, double price,int quantity) {

        this.setId(id);
        this.setName(name);
        this.setType(type);
        this.setPrice(price);

        this.setQuantity(quantity);

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }


    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }



    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return String.format(
                "Product { ID: %s, Name: %s, Type: %s, Price: %s, Quantity: %s }\n",
                id, name, type, price,quantity
        );
    }
}
