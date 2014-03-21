package jay;

public class PrepareRes extends Message{

	public String response; // "promise" or "reject"
	
	public Proposal recentAcceptProposal;
	
	public PrepareRes()
	{
		super.type = "PrepareRes";
	}
}
