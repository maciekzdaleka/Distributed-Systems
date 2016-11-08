/**
 * Created by Maciek on 07/11/2016.
 */
public class ItemsBought
{
    private String name;
    private String price;
    private String client;



    public ItemsBought(String n, String p, String c)
    {
        name = n;
        price = p;
        client =c;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }


}
