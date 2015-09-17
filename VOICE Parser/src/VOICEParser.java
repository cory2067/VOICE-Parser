import java.util.HashMap;
import java.util.Map;

/** 
 * @author Cory Lynch
 */
public class VOICEParser 
{
	public static void main(String[] args)
	{
		/*Map<String, String> tagged = TaggingModel.POSTag("I like to eat sandwiches. They are my favorite food.");
		for(String key : tagged.keySet())
		{
			System.out.println(key + ": " + tagged.get(key));
		}*/
		/**
		 * categories:
		 * 
		 * challenge
		 * innovation
		 * features
		 * opportunity
		 * application
		 */
		String[] s = TaggingModel.getSentences("Mr. Memer discovered a cure for disease. It will be realeased on Nov. 24. It is version 3.5.");
		for(String sent : s)
		{
			System.out.println("S: " + sent);
		}
	}
}
