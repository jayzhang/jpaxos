package jay;

import com.gbdata.common.util.Object2Json;

public class AccepterState{
	
	public String instance;
	
	public Proposal recentAcceptProposal = new Proposal();

	public String maxNumber;

	public String toString()
	{
		return Object2Json.toJsonString(this);
	}
}
