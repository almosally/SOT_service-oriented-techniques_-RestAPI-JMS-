package service.resources;

import service.model.Product;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;



@Singleton
@Path("products")
public class ProductsResources {

    private List<Product> productList = new ArrayList<>();
    public ProductsResources(){
        productList.add(new Product(1,"Pepsi","Drinks",1.2,1));
        productList.add(new Product(2,"Chitos","Chips",1.1,20));
        productList.add(new Product(3,"Beans","Legumes",2,15));
        productList.add(new Product(4,"Rice","Food",1,13));
    }



    @GET
    @Path("all")
    @Produces((MediaType.APPLICATION_JSON))
    public List<Product> getAllProduct() {
        return productList;
    }


    @GET
    @Produces((MediaType.APPLICATION_JSON))
    public Product getProductById(@QueryParam("id") int tId) {
        for (Product product : productList) {
            if (product.getId() == tId) return product;
        }
        return null;
    }

    @GET
    @Path("search")
    @Produces((MediaType.APPLICATION_JSON))
    public List<Product> getProducts(
            @QueryParam("id") @DefaultValue("0") int id,
            @QueryParam("name") @DefaultValue("") String name,
            @QueryParam("price")@DefaultValue("0") int price

            //@DefaultValue("1") @QueryParam("valInt") int valInt,
            //@RequestParam(required=true,defaultValue="1") Integer veri
    ) {
        List<Product> products = new ArrayList<>();
        for (Product product : productList) {
            if (
                            (id == 0|| product.getId() == id) &&
                            ( name.equals("") || product.getName().equalsIgnoreCase(name))&&
                            ( price==0 || product.getPrice()==price)

            ) {
                products.add(product);
            }

        }
        return products;
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void addProduct(
            @FormParam("id") int id,
            @FormParam("name") String name,
            @FormParam("type") String type,
            @FormParam("price") double price,
            @FormParam("quantity") int quantity
    ) {
        Product productX = new Product(id,name, type, price,quantity);
        productList.add(productX);
    }


    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void buyOrReturnProduct(Product product) {
        Product existingProduct = this.getProductById(product.getId());
        if (existingProduct != null)
        {
            existingProduct.setQuantity(product.getQuantity());
        } else {
            throw new RuntimeException("Error put/update: Product with id " + product.getId() + " does not exist!");
        }
    }

    @DELETE
    @Path("delete/{id}")
    public void removeProduct(@PathParam("id") int id) {
        Product product = this.getProductById(id);
        if (product != null)
        {
            productList.remove(product);
        } else throw new RuntimeException("Cannot find product with id " + id + ".");
    }

}

