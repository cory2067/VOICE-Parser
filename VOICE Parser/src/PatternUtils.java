
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternUtils {
	
	
	/*
	 * this method implements the pattern matching for the xml files
	 */
	public static synchronized String getWordAccordingToPattern(String text, String pattern){
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(text);
		String result = "";

		if(m.find()){
			result = text.substring(m.start(),m.end()).trim();
		}
		return result;
	}
	
	public static synchronized List<String> getWordListAccordingToPattern(String text, String pattern){
		List<String> list = new ArrayList<String>();
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(text);
		int i=0;
		while(i<text.length()){
			if(m.find(i)){
			list.add(text.substring(m.start(), m.end()).trim());
			i=i+m.end();
		}else{
			break;
		}
		}
		return list;
	}

}
