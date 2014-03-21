package jay;

public class PrepareReq extends Message{

	public Proposal proposal = new Proposal();
	
	public PrepareReq()
	{
		super.type = "PrepareReq";
	}
	
}
