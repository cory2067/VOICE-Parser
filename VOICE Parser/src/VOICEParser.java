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
		String[] s = TaggingModel.getSentences("2.3 million people suffer from Disease X. Mr. Scientist has discovered a new cure. The results of the study will be released on Nov. 25.");
		for(String sent : s)
		{
			System.out.println("S: " + sent);
		}
	}
}
