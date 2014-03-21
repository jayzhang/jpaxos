package jay;

import com.gbdata.common.util.Object2Json;

public class Message {

	public String sid;
	
	public String type;
	
	public String sender;	
	
	public String instance;
	
	public String toString()
	{
		return Object2Json.toJsonString(this);
	}

}
