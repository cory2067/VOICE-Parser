
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import opennlp.tools.cmdline.PerformanceMonitor;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.WhitespaceTokenizer;

public class TaggingModel {

	public static Map<String, String> POSTag(String input) {
		Map<String, String> wordsTag = new HashMap<String, String>();
		
		//part of speech model
		POSModel model = new POSModelLoader().load(new File("models\\en-pos-maxent.bin"));
		PerformanceMonitor perfMon = new PerformanceMonitor(System.err, "sent");
		POSTaggerME tagger = new POSTaggerME(model);

		Scanner reader = new Scanner(input);
		//ObjectStream<String> lineStream = new PlainTextByLineStream(new StringReader(input));


		perfMon.start();
		while (reader.hasNext()) {
			String line = reader.nextLine();
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
		reader.close();
		perfMon.stopAndPrintFinalResult();
		return wordsTag;
	}
	
	
	public static String[] getSentences(String text)
	{
		String sentences[] = {};
		try {
			InputStream modelIn = new FileInputStream("models/en-sent.bin");
			SentenceModel model = new SentenceModel(modelIn);
			SentenceDetectorME detector = new SentenceDetectorME(model);
			sentences = detector.sentDetect(text);
			
			modelIn.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return sentences;
	}
 
	public static void main(String[] args) throws IOException {
		String input = "write  for 1-5 min eppendorf max 4° C";
		System.out.print(TaggingModel.POSTag(input).keySet().toString());
		
	}
}
