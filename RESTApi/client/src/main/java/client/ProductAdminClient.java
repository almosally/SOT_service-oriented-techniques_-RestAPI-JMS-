package client;
import service.model.Product;
import org.glassfish.jersey.client.ClientConfig;

import javax.ws.rs.client.*;
import javax.ws.rs.core.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;



public class ProductAdminClient {
    /** Creates the webtarget for the service,
     * opens the input stream that is passed to the functions as well, and closes it in the end
     * Seems like a long method, but mostly consists of text to show the menu and fire the called method. */
    public static void main(String[] args) throws IOException {

        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        URI baseUri = UriBuilder.fromUri("http://localhost:9090/rest/products").build();
        WebTarget serviceTarget = client.target(baseUri);

        System.out.println("WELCOME!\n");
        BufferedReader br = new BufferedReader(new InputStreamReader((System.in)));

        int choice = -1;
        while (choice != 0) {

            System.out.println("\nPLEASE CHOOSE AN ACTION:\n===============");
            System.out.println("1: List of products (default)");
            System.out.println("2: Find a Product by id");
            System.out.println("3: Find Products");
            System.out.println("4: Add Product");
            System.out.println("5: Delete Product by id");
            System.out.println("6: Sell a Product by id");
            System.out.println("7: Return a Product by id 'Add quantity'");
            System.out.println("0: EXIT PROGRAM");


            System.out.println("Your input: ");

            try {
                choice = Integer.parseInt(br.readLine());
            } catch (NumberFormatException e){
                System.out.println("That is not an integer. Listing all products... ");
            }

            switch (choice) {
                default:
                case 1:getAllProduct(serviceTarget);
                    break;
                case 2: getProductById(serviceTarget, br);
                    break;
                case 3: searchProduct(serviceTarget, br);
                    break;
                case 4: addProduct(serviceTarget, br);
                    break;
                case 5: removeProduct(serviceTarget, br);
                    break;
                case 6: sellProduct(serviceTarget, br);
                    break;
                case 7: returnProduct(serviceTarget, br);
                    break;

                case 0:
                    break;

            }

        }
        br.close();
    }


    /** Calls http://localhost:9090/rest/products/all to fetch all items. */
    public static void getAllProduct(WebTarget serviceTarget) {
        Invocation.Builder requestBuilder = serviceTarget.path("all").request().accept(MediaType.APPLICATION_JSON);
        Response response = requestBuilder.get();

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            GenericType<ArrayList<Product>> genericType = new GenericType<>() {};
            ArrayList<Product> entity = response.readEntity(genericType);
            for (Product product : entity) {
                System.out.println("ID: "+product.getId()+"\tProduct : "+ product.getName()+ "\tType: "+product.getType() +"\tPrice: "+product.getPrice()+ "\tQuantity: "+product.getQuantity());
            }
        } else {
            System.out.println("ERROR: Cannot get: " + response);
        }
    }

    /** Calls http://localhost:9090/rest/products/{id} to fetch the product with given ID.. */
    public static Product getProductById(WebTarget serviceTarget, BufferedReader br) {
        System.out.println("Product ID: ");
        int productId = tryInputParse(br);
        if (productId == 0) {
            System.out.println("ERROR: There is no product with this id, plz check input.");
            return null;
        }
        Invocation.Builder requestBuilder =
                serviceTarget.path("/")
                        .queryParam("id", productId)
                        .request()
                        .accept(MediaType.APPLICATION_JSON);

        Response response = requestBuilder.get();

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            GenericType<Product> genericType = new GenericType<>() {};
            Product entity = response.readEntity(genericType);
            System.out.println("ID: "+entity.getId()+"\tProduct : "+ entity.getName()+ "\tType: "+entity.getType() +"\tPrice: "+entity.getPrice()+"\tQuantity: "+entity.getQuantity());
            return entity;
        } else {
            System.out.println("ERROR: Cannot get: " + response);
        }
        return null;
    }


    /** Calls http://localhost:9090/rest/products/search?{id}&{name}&{price}
     * to fetch all items that comply to the QueryParams.
     * Zero or more QueryParam can be defined, the returned items are complying for all of them.
     * If none is given, the whole productlist is returned. */
    public static void searchProduct(WebTarget serviceTarget, BufferedReader br) throws IOException {
        System.out.println("1: Find a product by ID (default)");
        System.out.println("2: Find a product by name");
        System.out.println("3: Find a product by price");

        System.out.println("Your input: ");

        int choice = tryInputParse(br);
        String paramName;

        switch (choice) {
            default:
            case 1:
                System.out.println("ID: ");
                paramName = "id";
                break;
            case 2:
                System.out.println("name: ");
                paramName = "name";
                break;
            case 3:
                System.out.println("price: ");
                paramName = "price";
                break;

        }

        String keyword = br.readLine();

        Invocation.Builder requestBuilder =
                serviceTarget
                        .path("search")
                        .queryParam(paramName, keyword)
                        .request()
                        .accept(MediaType.APPLICATION_JSON);

        Response response = requestBuilder.get();

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            GenericType<ArrayList<Product>> genericType = new GenericType<>() {};
            ArrayList<Product> entity = response.readEntity(genericType);
            for (Product product : entity) {
                System.out.println("ID: "+product.getId()+"\tProduct : "+ product.getName()+ "\tType: "+product.getType() +"\tPrice: "+product.getPrice()+"\tQuantity: "+product.getQuantity());
            }
        } else {
            System.out.println("ERROR: Cannot get: " + response);
        }
    }


    /** Makes a POST request to http://localhost:9090/rest/products/ with FormParams to create a new product.. */
    public static void addProduct(WebTarget serviceTarget, BufferedReader br) throws IOException {
        Form form = new Form();
        System.out.println("ID: ");
        int id=Integer.parseInt(br.readLine());
        System.out.println("Product: ");
        String name = br.readLine();
        System.out.println("Type ");
        String type = br.readLine();
        System.out.println("Price ");
        double price = Double.parseDouble(br.readLine());
        System.out.println("Quantity ");
        int quantity = Integer.parseInt(br.readLine());
        form.param("id", String.valueOf(id));
        form.param("name", name);
        form.param("type", type);
        form.param("price", String.valueOf(price));
        form.param("quantity", String.valueOf(quantity));

        Entity<Form> entity = Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED);
        Response response = serviceTarget.request().accept(MediaType.TEXT_PLAIN).post(entity);
        if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
            System.out.println("Created a product. " + response+ "\nProduct; "+name + "\tWith ID "+id+ "\tQuantity "+quantity);
        } else {
            System.out.println("ERROR: Cannot add product: " + response);
        }
    }




    /** Makes a DELETE request to http://localhost:9090/rest/product/delete/{id} to remove a product with given ID. */
    public static void removeProduct(WebTarget serviceTarget, BufferedReader br) {

        System.out.println("Product id to be deleted: ");
        int productId = tryInputParse(br);
        WebTarget resourceTarget = serviceTarget.path("delete/" + productId);
        Invocation.Builder requestBuilder = resourceTarget.request().accept(MediaType.TEXT_PLAIN);
        Response response = requestBuilder.delete();

        if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
            System.out.println("Successfully deleted product with id " + productId);
        } else {
            System.out.println("ERROR: Cannot delete: " + response);
        }
    }

    /** Makes a PUT request to http://localhost:9090/rest/product/ to add a sell to given product. */
    public static void sellProduct(WebTarget serviceTarget, BufferedReader br) throws IOException {

        Product product = getProductById(serviceTarget, br);
        if (product.getId()==product.getId()) {

            System.out.println("number of sell ");
            int prod =  Integer.parseInt(br.readLine());
           try{
            if (product.getQuantity()>=prod){
                 int newquantity =product.getQuantity()-prod;
            product.setQuantity(newquantity);}}
           catch(Exception e) {System.out.println("ERROR: the quantity of the product is not enough to sell : " + prod);


            }

            Entity<Product> entity = Entity.entity(product, MediaType.APPLICATION_JSON);
            Response response = serviceTarget.request().accept(MediaType.TEXT_PLAIN).put(entity);

            if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
                System.out.println("number of sell: " + prod +"\t Of product "+product);
            } else {
                System.out.println("ERROR: Cannot get: product " + response +"\nPlease try valid product ID ");
            }
        } else System.out.println("ERROR: product has already been sold. ");
    }


    /** Makes a PUT request to http://localhost:9090/rest/product/ to remove a product from given supermarket. */
    public static void returnProduct(WebTarget serviceTarget, BufferedReader br) throws IOException {
        Product product = getProductById(serviceTarget, br);
        if (product.getId()==product.getId()) {
            System.out.println("Quantity to add ");
            int prod =  Integer.parseInt(br.readLine());
            if (prod < 0 ){ System.out.println("entry positive numbers");}
            else {int x=product.getQuantity()+prod;
                product.setQuantity(x);}

            Entity<Product> entity = Entity.entity(product, MediaType.APPLICATION_JSON);
            Response response = serviceTarget.request().accept(MediaType.TEXT_PLAIN).put(entity);

            if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
                System.out.println("Product has been added to the shop with quantity " + prod);
            } else {
                System.out.println("ERROR: Cannot get: " + response+"\nPlease try valid product ID" );
            }
        } else System.out.println("ERROR: Product hasn't been sold yet.");
    }



    /** Helper method to correctly format user input as int. */
    public static int tryInputParse(BufferedReader br) {
        try {
            return Integer.parseInt(br.readLine());
        } catch (NumberFormatException | IOException e) {
            return 0;
        }
    }


}
