package jay;

import com.gbdata.common.json.JSONObject;

public class Proposal {

	public String number;
	
	public String value;
	
	public void fromJson(JSONObject obj)
	{
		if(obj == null)
			return ;
		
		number = obj.getString("number");
		
		value = obj.getString("value");
	}
	
	public static boolean isGreaterThan(String propNum1, String propNum2)
	{
		if(propNum2 == null)
			return true;
		
		if(propNum1 == null)
			return false;
		
		int cmp = propNum1.compareTo(propNum2);
		return cmp >= 0;
	}
}
