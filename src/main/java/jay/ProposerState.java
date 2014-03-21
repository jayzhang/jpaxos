package jay;

import java.util.HashMap;
import java.util.Map;

import com.gbdata.common.util.Object2Json;

public class ProposerState {

	public String instance;
	
	public Proposal currentProposal = new Proposal();
	
	public Map<String, PrepareRes> prepareResponses = new HashMap<String, PrepareRes>(); // accepter-->prepare response
	
	public Map<String, AcceptRes> acceptResponses = new HashMap<String, AcceptRes>(); // accepter-->accept response
	
	public String status; // "Prepare", "Accept", "Fail/Success"
	
	
	public String toString()
	{
		return Object2Json.toJsonString(this);
	}
}
