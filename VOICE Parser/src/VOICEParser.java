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

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/** 
 * @author Cory Lynch
 */
public class VOICEParser 
{
	private static final double keywordBiasFactor = 0.08;
	
	private static final String[] 
			OPPORTUNITY = {"$", "dollars", "million", "market"},
			FEATURES = {"improved", "less toxic", "faster", "targeted therapy", "affected", "toxicity",
						"specificity", "more effective", "advantages"},
			INNOVATION = {"were shown to", "novel", "more natural", "we have discovered", "discovery",
						"new therapeutic use", "great promise", "non-invasive", "innovative", "solution",
						"minimize risk"},
			CHALLENGE = {"penetrate blood brain barrier", "traditional", "lower toxicity", "higher specificity",
						"situation", "limited", "although", "however"},
			APPLICATION = {"targeted to", "application", "can be used", "potential products", 
						"differentiate", "treatment"};
	
	private static final String[] OUTPUT_LABELS = {"Market Opportunity", "Key Features",
							"Our Innovation", "Scientific Challenge - Unmet Needs", "Projected Application"};
												
	private static TokenizerModel tmodel;
	private static SentenceModel smodel;
	private static DoccatModel dmodel;
	private static TokenNameFinderModel[] nmodels = new TokenNameFinderModel[3];
	
	private static JTextArea input;
	private static JFrame frame;
	private static JTextArea out;
			
	public static void main(String[] args)
	{
		//Although peptides have shown some promise in clinical trials as therapeutic agents, their success has largely been limited by several factors, including rapid degradation by peptidases, poor cell permeability, and a lack of binding specificity resulting from conformational inflexibility. Science has overcome these limitations with the advance of peptidomimetics--a system for the production of modified chemical compounds capable of mimicking the structural and or functional properties of peptides. A fertile ground for such efforts lies in the discovery of ligands for nervous system receptors that mediate the sensation of pain. Researchers at Harvard University have developed a novel approach to generate nonpeptidic ligands to a key receptor involved in pain relief, the mu opioid receptor (MOR). This approach has far-reaching implications for the discovery of next-generation therapeutics for the treatment of pain. The peptidomimetic mu receptor ligands generated as a result of this synthetic platform are lead compounds for the treatment of pain. The market for pain therapeutics is very large. In one example, there are 45 million Americans that suffer from chronic headaches, while nearly 6 million reported case of chest pain.
		frame = new JFrame("VOICE Parser");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new FlowLayout());
		JLabel msg = new JLabel("Initializing...");
		msg.setFont(new Font("Arial", Font.BOLD, 24));
		frame.add(msg);
		frame.setSize(640, 700);
		frame.setVisible(true);
		frame.setResizable(false);
		
		try {
			init();
		}
		catch(Exception e) {
			JOptionPane.showMessageDialog(null, "Error: Could not initialize:\n" + e.getMessage());
			System.exit(-1);
		}
		
		input = new JTextArea("");
		input.setRows(16);
		input.setSize(600, 600);
		input.setLineWrap(true);
	    JScrollPane scroll = new JScrollPane(input);
	    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		JButton submit = new JButton("Submit");
		submit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				submit();
			}
		});
		
		out = new JTextArea("");
		out.setRows(20);
		out.setSize(600, 600);
		out.setLineWrap(true);
		out.setEditable(false);
	    JScrollPane scrollOut = new JScrollPane(out);
	    scrollOut.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		msg.setText("Enter a summary:");
		frame.add(scroll);
		frame.add(submit);
		frame.add(scrollOut);
		frame.setVisible(true);
	}
	
	public static void submit()
	{
		out.setText(process(input.getText()));
	    out.setCaretPosition(0);
		frame.setVisible(true);
	}
	
	public static void init() throws Exception
	{
		InputStream streams[] = {new FileInputStream("models/en-token.bin"), 
								 new FileInputStream("models/en-sent.bin"),
								 new FileInputStream("models/voice-classify.bin"),
								 new FileInputStream("models/en-ner-person.bin"),
								 new FileInputStream("models/en-ner-location.bin"),
								 new FileInputStream("models/en-ner-organization.bin")};
		
		tmodel = new TokenizerModel(streams[0]);
		smodel = new SentenceModel(streams[1]);
		dmodel = new DoccatModel(streams[2]);
		nmodels[0] = new TokenNameFinderModel(streams[3]);
		nmodels[1] = new TokenNameFinderModel(streams[4]);
		nmodels[2] = new TokenNameFinderModel(streams[5]);
		
		for(InputStream i : streams)
			i.close();
	}
	
	/* Output array:
	 * 0: person
	 * 1: location
	 * 2: organization
	 * 3: opportunity
	 * 4: features
	 * 5: innovation
	 * 6: challenge
	 * 7: application
	 */
	public static String process(String text)
	{
		StringBuilder output = new StringBuilder();
		
		NameFinderME nameFinder[] = new NameFinderME[3];
		Tokenizer tokenizer = new TokenizerME(tmodel);
		String[] tokens = tokenizer.tokenize(text);
		final String[] label = {"Names" , "Locations", "Organizations"};
		for(int a=0; a<3; a++)
		{
			nameFinder[a] = new NameFinderME(nmodels[a]);
			Span nameSpans[] = nameFinder[a].find(tokens);
			nameFinder[a].clearAdaptiveData();
			
			if(nameSpans.length > 0)
				output.append(label[a] + " detected:\n");
			else
				continue;
			
			StringBuilder all = new StringBuilder();
			for(Span s : nameSpans)
			{
				String[] name = Arrays.copyOfRange(tokens, s.getStart(), s.getEnd());
				StringBuilder builder = new StringBuilder();
				for(String n : name) {
				    builder.append(n + " ");
				}
				System.out.println(builder.toString().trim());
				all.append(builder.toString().trim() + "; ");
			}
			if(all.length() != 0)
				all.delete(all.length()-2, all.length()); 
			output.append(all + "\n\n");
		}


		String sentences[] = {};
		DocumentCategorizerME classificationME = null;
		SentenceDetectorME detector = new SentenceDetectorME(smodel);
		sentences = detector.sentDetect(text);
		
		classificationME = new DocumentCategorizerME(dmodel);
		
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
			
			sents[a].vals[opIndex] += bias(sentences[a], OPPORTUNITY);
			sents[a].vals[feIndex] += bias(sentences[a], FEATURES);
		 	sents[a].vals[inIndex] += bias(sentences[a], INNOVATION);
			sents[a].vals[chIndex] += bias(sentences[a], CHALLENGE);
			sents[a].vals[apIndex] += bias(sentences[a], APPLICATION);
		}
			
		SortSent s[] = new SortSent[sents.length * 5];
		for(int a = 0; a < sents.length; a++)
			for(int b = 0; b < 5; b++)
			{
				s[a*5+b] = new SortSent(a, b, sents[a].vals[b]);
			}
		
		Arrays.parallelSort(s);		
		for(SortSent q : s)
			System.out.println(q.weight);
			
		String[] categories = {"opportunity", "features", "innovation", "challenge", "application"};
		
		for(int q=0; q<categories.length; q++)
		{
			String cat = categories[q];
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
			
			output.append(OUTPUT_LABELS[q] + ":\n");
			output.append(sents[best].content + "\n");
			output.append("Confidence: " + (int)(max*100) + "%\n\n");
			sents[best] = null;
		}
		
		return output.toString();
	}
	
	public static double bias(String s, String[] words)
	{
		double counter = 0.0;
		for(String word : words)
			counter += s.contains(word) ? 1 : 0;
		return keywordBiasFactor * counter;
	}
}
