import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/** 
 * @author Cory Lynch
 */
public class VOICEParser 
{
	private static final double keywordBiasFactor = 0.05;
	
	private static final String[] 
			opportunity = {"$", "dollars", "million", "market"},
			features = {"improved", "less toxic", "faster", "targeted therapy", "affected", "toxicity",
						"specificity", "more effective", "advantages"},
			innovation = {"were shown to", "novel", "more natural", "we have discovered", "discovery",
						"new therapeutic use", "great promise", "non-invasive", "innovative", "solution",
						"minimize risk"},
			challenge = {"penetrate blood brain barrier", "traditional", "lower toxicity", "higher specificity",
						"situation", "limited", "although", "however"},
			application = {"targeted to", "application", "can be used", "potential products", 
						"differentiate", "treatment"};
			
	public static void main(String[] args)
	{
		categorize("Although peptides have shown some promise in clinical trials as therapeutic agents, their success has largely been limited by several factors, including rapid degradation by peptidases, poor cell permeability, and a lack of binding specificity resulting from conformational inflexibility. Science has overcome these limitations with the advance of peptidomimetics--a system for the production of modified chemical compounds capable of mimicking the structural and or functional properties of peptides. A fertile ground for such efforts lies in the discovery of ligands for nervous system receptors that mediate the sensation of pain. Researchers at Harvard University have developed a novel approach to generate nonpeptidic ligands to a key receptor involved in pain relief, the mu opioid receptor (MOR). This approach has far-reaching implications for the discovery of next-generation therapeutics for the treatment of pain. The peptidomimetic mu receptor ligands generated as a result of this synthetic platform are lead compounds for the treatment of pain. The market for pain therapeutics is very large. In one example, there are 45 million Americans that suffer from chronic headaches, while nearly 6 million reported case of chest pain.");
	}
	
	public static void categorize(String text)
	{
		String sentences[] = {};
		DocumentCategorizerME classificationME = null;
		try {
			InputStream modelIn = new FileInputStream("models/en-sent.bin");
			SentenceModel model = new SentenceModel(modelIn);
			SentenceDetectorME detector = new SentenceDetectorME(model);
			sentences = detector.sentDetect(text);
			modelIn.close();
			
			 File test = new File("models/voice-classify.bin");
			  String classificationModelFilePath = test.getAbsolutePath();
				classificationME = new DocumentCategorizerME(
				    new DoccatModel(
				      new FileInputStream(classificationModelFilePath)));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error: could not initialize");
		}
		
		Sentence[] sents = new Sentence[sentences.length];
		int opIndex = classificationME.getIndex("opportunity");
		int feIndex = classificationME.getIndex("features");
		int inIndex = classificationME.getIndex("innovation");
		int chIndex = classificationME.getIndex("challenge");
		int apIndex = classificationME.getIndex("application");
	
		for(int a = 0; a < sentences.length; a++)
		{
			sents[a] = new Sentence(sentences[a],
					classificationME.categorize(sentences[a]));
			
			sents[a].vals[opIndex] += bias(sentences[a], opportunity);
			sents[a].vals[feIndex] += bias(sentences[a], features);
			sents[a].vals[inIndex] += bias(sentences[a], innovation);
			sents[a].vals[chIndex] += bias(sentences[a], challenge);
			sents[a].vals[apIndex] += bias(sentences[a], application);
		}
		
		String[] categories = {"opportunity", "features", "innovation", "challenge", "application"};
		
		for(String cat : categories)
		{
			double max = -1.0;
			int best = -1;
			for(int a = 0; a < sents.length; a++)
			{
				if(sents[a] == null)
					continue;
				
				double score = sents[a].vals[classificationME.getIndex(cat)];
				if(score > max)
				{
					max = score;
					best = a;
				}
			}
			
			System.out.println("The choice for " + cat + ":");
			System.out.println(sents[best].content);
			System.out.println("Confidence: " + (int)(max*100) + "%\n");
			sents[best] = null;
		}
	}
	
	public static double bias(String s, String[] words)
	{
		double counter = 0.0;
		for(String word : words)
			counter += s.contains(word) ? 1 : 0;
		return keywordBiasFactor * counter;
	}
}
