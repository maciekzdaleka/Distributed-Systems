public class Items
{
	private String name;
	private String price;
	
    public Items(String n, String p)
    {
       name = n;
       price = p;  
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
    
}
