package jay;

public class AcceptReq extends Message{

	public Proposal proposal = new Proposal();
	
	public AcceptReq()
	{
		super.type = "AcceptReq";
	}
}
