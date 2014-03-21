package jay;

public class ClientRes extends Message{
	
	public String value;
	
	public String status ;

	public ClientRes()
	{
		super.type = "ClientRes";
	}
}
