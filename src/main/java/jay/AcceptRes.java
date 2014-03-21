package jay;

public class AcceptRes extends Message{

	public String response; // "accept" or "nack"
	
	public String value;
	
	public AcceptRes()
	{
		super.type = "AcceptRes";
	}
}
