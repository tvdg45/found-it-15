
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSonParser {
	
	private static JSONParser parser = new JSONParser();
	
	public static String parseJsonId(String json) {
		
		String id = null;
		try {
	 
			Object obj = parser.parse(json);
	 
			JSONObject jsonObject = (JSONObject) obj;
	 
			id = (String) jsonObject.get("id");
			//System.out.println(id);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return id;
	}
	
	public static JSONObject writeJSonObject(Map<String, String> entries) {
		
		JSONObject obj = new JSONObject();
		
		Iterator it = entries.entrySet().iterator();
	    while (it.hasNext()) {
	        Entry entry = (Entry)it.next();
	        obj.put(entry.getKey(), entry.getValue());

	        it.remove(); // avoids a ConcurrentModificationException
	    }
		return obj;
	}
}
