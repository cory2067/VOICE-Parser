
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import opennlp.tools.cmdline.PerformanceMonitor;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;

public class TaggingModel {

	public static Map<String, String> syntacticTagging(String input) throws IOException {
		Map<String, String> wordsTag = new HashMap<String, String>();
		
		//part of speech model
		POSModel model = new POSModelLoader().load(new File("models\\en-pos-maxent.bin"));
		PerformanceMonitor perfMon = new PerformanceMonitor(System.err, "sent");
		POSTaggerME tagger = new POSTaggerME(model);

		ObjectStream<String> lineStream = new PlainTextByLineStream(new StringReader(input));


		perfMon.start();
		String line;
		while ((line = lineStream.read()) != null) {

			String whitespaceTokenizerLine[] = WhitespaceTokenizer.INSTANCE.tokenize(line);
			String[] tags = tagger.tag(whitespaceTokenizerLine);
			//for tag reference, see: http://blog.dpdearing.com/2011/12/opennlp-part-of-speech-pos-tags-penn-english-treebank/
			
			for(int i=0; i<whitespaceTokenizerLine.length;i++){
				wordsTag.put(whitespaceTokenizerLine[i], tags[i]);
			}

			//POSSample sample = new POSSample(whitespaceTokenizerLine, tags);
			//sample.getTags();
			//System.out.println(sample.toString());


			perfMon.incrementCounter();
		}
		perfMon.stopAndPrintFinalResult();
		return wordsTag;
	}
	
	
 
	public static void main(String[] args) throws IOException {
		String input = "write  for 1-5 min eppendorf max 4° C";
		System.out.print(TaggingModel.syntacticTagging(input).keySet().toString());
		
	}
}
