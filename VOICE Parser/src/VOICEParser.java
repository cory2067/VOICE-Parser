import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

/** 
 * @author Cory Lynch
 */
public class VOICEParser 
{
	private static final double keywordBiasFactor = 0.08;
	
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
		//process("Although peptides have shown some promise in clinical trials as therapeutic agents, their success has largely been limited by several factors, including rapid degradation by peptidases, poor cell permeability, and a lack of binding specificity resulting from conformational inflexibility. Science has overcome these limitations with the advance of peptidomimetics--a system for the production of modified chemical compounds capable of mimicking the structural and or functional properties of peptides. A fertile ground for such efforts lies in the discovery of ligands for nervous system receptors that mediate the sensation of pain. Researchers at Harvard University have developed a novel approach to generate nonpeptidic ligands to a key receptor involved in pain relief, the mu opioid receptor (MOR). This approach has far-reaching implications for the discovery of next-generation therapeutics for the treatment of pain. The peptidomimetic mu receptor ligands generated as a result of this synthetic platform are lead compounds for the treatment of pain. The market for pain therapeutics is very large. In one example, there are 45 million Americans that suffer from chronic headaches, while nearly 6 million reported case of chest pain. For more information, please contact Daniel Nadis.");
		//process("Our company, Occams Resources, does cool stuff. For more information, please contact Daniel Nadis. Or, you can stop by the office, located at 59 Elm Street, New Haven, Connecticut.");
		//process("Pierre Vinken has developed an innovative new approach to synthesizing RNA-623, a major breakthrough for the medical field. This method provides a more efficient solution than previous methods, and creates the substance quickly and effectively. 55 million Americans could benefit from this, and it has incredible promise. However, this method has certain limitations, as there is a maximum amount of substance that can be produced. Nevertheless, such a product has unlimited applications in the field of medicine.");
		process("Technology Summary 6-2009-2241 | Liquid precursor composition that upon solidification forms a sustained release varnish Steinberg Doron, HUJI, Faculty of Dental Medicine, Oral Medicine Friedman Michael, HUJI, School of Pharmacy, Pharmaceutics Delivers treatment in response to pH conditions in mouth Categories Coatings, Biomaterials, Polymers, Composites, Dental Care Development Stage Successful clinical trials – ready for commercialization Patent Status Patent pending Market The oral drug delivery market, the largest segment of the drug delivery market, is a $35 billion industry expected to grow up to 10% per year Highlights Novel dental coating that delivers therapeutics over time, as required, in response to sensed changes in acidic/basic conditions in the oral cavity Dental disorders occur when there are changes in oral hygiene, diet, medicaments or age The main advantage of current therapeutic agents and drug delivery systems is their high substantivity in the target organ, in contrast to the applications currently used. Application-targeted reagent is suitable for oral disorders as: tooth decay, periodontal diseases, implantitis, halitosis, tooth whitening, hypersensitivity, cancer, fungal infection, repair and regeneration of bone, cartilage, tendon and ligament defects. Our Innovation The application is a pharmaceutical platform that various types of drugs can be incorporated into. Key Features Increases exposure time to medication, preventing infections even in procedures in the root canal Enables sustained release treatment effective against bacteria, yeasts, viruses and fungi Reduces amount of drugs required, minimizing side effects. These advantages result in better clinical improvement and better patient compliance. Built-in pH sensor increases the rate of release of medication in response to changes in pH that occur in many pathological oral disorders such as infections, caries, inflammations, dry mouth, halitosis, candidiasis, delivering medication as required Simple production process; straightforward scale-up Development Milestones Seeking industry cooperation for commercialization The Opportunity Additional applications in medicine in human or in animals http://www.yissum.co.il/sites/default/files/imagecache/category_icon_s/category_icons/import_id/93_1.png Contact for more information: http://www.yissum.co.il/sites/default/files/imagecache/project_resp_person/users/picture-1754.jpg Shoshana Keynan VP, Head of Business Development, Healthcare http://www.yissum.co.il/sites/default/themes/yissum/css/images/op_send.gif+972-2-6586683 All projects by: Friedman Michael (4) Steinberg Doron (7) Related projects (6) Yissum Research Development Company of the Hebrew University of Jerusalem Hi-Tech Park, Edmond J. Safra Campus, Givat-Ram, Jerusalem P.O. Box 39135, Jerusalem 91390 Israel Telephone: 972-2-658-6688, Fax: 972-2-658-6689");
	}
	
	public static void process(String text)
	{
		InputStream modelPerIn = null, modelTokIn = null;
		try {
			modelTokIn = new FileInputStream("models/en-token.bin");
			TokenizerModel tmodel = new TokenizerModel(modelTokIn);
			TokenNameFinderModel[] nmodels = {
					new TokenNameFinderModel(new FileInputStream("models/en-ner-person.bin")),
					new TokenNameFinderModel(new FileInputStream("models/en-ner-location.bin")),
					new TokenNameFinderModel(new FileInputStream("models/en-ner-organization.bin"))};
			NameFinderME nameFinder[] = new NameFinderME[3];
			Tokenizer tokenizer = new TokenizerME(tmodel);
			String[] tokens = tokenizer.tokenize(text);
			final String[] label = {"Names" , "Locations", "Organizations"};
			for(int a=0; a<3; a++)
			{
				nameFinder[a] = new NameFinderME(nmodels[a]);
				Span nameSpans[] = nameFinder[a].find(tokens);
				nameFinder[a].clearAdaptiveData();
				
				System.out.println(label[a] + " detected:");
				for(Span s : nameSpans)
				{
					String[] name = Arrays.copyOfRange(tokens, s.getStart(), s.getEnd());
					StringBuilder builder = new StringBuilder();
					for(String n : name) {
					    builder.append(n + " ");
					}
					System.out.println(builder.toString().trim());
				}
			}
		}
		catch (Exception e) {
		  e.printStackTrace();
		}
		finally {
		  if (modelTokIn != null) {
		    try {
		      modelTokIn.close();
		    }
		    catch (Exception e) {
		    }
		  }
		  if (modelPerIn != null) {
			    try {
			      modelPerIn.close();
			    }
			    catch (Exception e) {
			    }
		  }
		}

		
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
			if(best == -1)
			{
				System.out.println("Couldn't figure out something for " + cat);
				continue;
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
