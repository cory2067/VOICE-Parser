
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import opennlp.tools.util.InvalidFormatException;

public class ProtocolsParser {

	private List<ProtocolBeans> protocolItems;

	public List<ProtocolBeans> getProtocolItems() {
		return protocolItems;
	}
	public ProtocolsParser(){

		protocolItems = new ArrayList<ProtocolBeans>();
	}

	public void parseProtocol(File file) throws InvalidFormatException, IOException{
		//ProtocolsParser pp = new ProtocolsParser();
		ProtocolBeans protocolItem;
		List<String> list;
		List<String> matchers;
		File actionList = new File("listOfActions.txt");
		File chemicalsList = new File("listOfEntities.csv");
		File measurementsUnits= new File("measurementUnites.csv");
		Scanner scanner;

		String title;
		String sentence;
		String pattern="";
		String action;
		String description;
		String chemical; 
		String measures;
		String ids;
		String measureids;
		String sent;		
		String match="";
		String listOfOtherSubjects="optical density,OD,temperature,OD600,number of cells,the volume of the cells,the volume";
		String listOfEntities="the rest of the cells,aptamer library,glycerol,H2O,MB209a,MB210a,YAPD,TL-PEG,TL,TL Solution,cells,"
				+"overnight culture,culture,primary antibody,secondary antibody,supernatant,samplebuffer,gel,stock,sterile water,collagen,entity,liquid,film,blocking buffer,slice,slices,slide,slides,PBS,D-PBS,fresh medium,medium,pellet,pellets ";
		String listOfMethods="by inversion,shaking,vigorous shaking,moderate shaking,gently,horizontally,sterile,tightly,centrifugation,in aliquots";
		String listOfEquipment ="DMSO,sd-w plates,sd-w plate,hemacytometer,pipette,eppendrof,hood,well,hydrophobic pen,fibronectin-coated plate,culture vessel,"+
				"96-deep-well plates,48-deep-well plates,tube,falcon tubes,96-well-plate pcr machine,pcr 96-well plates,replicator,plates,"+
				"sterile glass beads for plating,breathable seal,electroporation device,eppendorf 8-channel ,eppendorf multipette plus,"+
				"sterile erlenmeyer flasks,plates,erlenmeyer flask,erlenmeyer flasks,2-l erlenmeyer flasks,sorvall bottles,sterile hood,sterile microscope slides,sterile velvets,water baths,flask";

		String listOfConditions="brief vortexing,obtain an OD600,thoroughly,OD,optical density at,on ice,ice-cold,at room temperature,great yield,briefly,maximum speed,completely resuspended, air dry,wrapped in foil to keep out light,low,carefully,"+
				"frequently,runs in the opposite direction,slightly,dissolved,if necessary,successively,at the right dilution,one drop per slide,pH,as appropriate,proportionally,under aseptic condition,"+
				"humidied atmosphere,until use,before use,in advance,immediately,immediately before use,enough,warmed,high density,without, gently,for longer than,complete medium," +
				"at a ratio of, at a ratio,at the ratio of,at the ratio,minimum volume,at a density of,wrapped tightly,after 3h,prechilled";

		StringBuilder text;
		StringBuilder note;
		StringBuilder entities;
		StringBuilder volumes;
		StringBuilder concentrations;
		StringBuilder equipments;
		StringBuilder conditions;
		StringBuilder sb;
		String[] descriptors;
		String[] elements;
		String[] protocol;
		Map<String, String> listOfActions= new HashMap<String, String>();
		Map<String,String> listOfChemicals= new HashMap<String, String>();
		Map<String,String> listOfMeasures=new HashMap<String,String>();
		Map<Integer, String> ent;
		Map<String, String> taggedSentence;
		TreeMap<Integer,String> diffActions;
		List<String> sentences;
		String act;
		List<String>actions ;

		//writing the list of actions and their corresponding descriptors into the map listOfActions
		try{
			list= new ArrayList<String>();
			scanner=new Scanner(actionList);
			while(scanner.hasNextLine()){
				list.add(scanner.nextLine()); //import listofactions.txt into "list"
			}
			for(String element: list){ //iterate through each line

				int i = element.indexOf(",");
				action = element.substring(0, i);
				description = element.substring(i); 
				//before first comma is action, the rest is description (may have more commas)
				listOfActions.put(action, description );
			}
			//not closing the scanner (resource leak)
			//definitely add a scanner.close() in a finally block
		}catch(FileNotFoundException e){
			System.out.println("Not found");
		}
		
		//writing the list of chemicals with their corresponding IDs into a map listOfChemicals
		try{
			//exact same process as before; would be more efficient to make a function for this
			//no reason to repeat the exact block of code
			list= new ArrayList<String>();
			scanner=new Scanner(chemicalsList);
			while(scanner.hasNextLine()){
				list.add(scanner.nextLine()); 
			}
			for(String element: list){
				int i = element.indexOf(",");
				chemical = element.substring(0, i);
				ids = element.substring(i).trim();
				listOfChemicals.put(chemical, ids );
			}
		}catch(FileNotFoundException e){
		}
		//writing the list of measurements unites with their corresponding IDs into a map listOfChemicals
				try{
					list= new ArrayList<String>();
					scanner=new Scanner(measurementsUnits);
					while(scanner.hasNextLine()){
						list.add(scanner.nextLine());
					}
					for(String element: list){
						int i = element.indexOf(",");
						measures = element.substring(0, i);
						measureids = element.substring(i).trim();
						listOfMeasures.put(measures, measureids);
					}
				}catch(FileNotFoundException e){
				}
		//read the protocol file
		try{
			text = new StringBuilder(); //pretty much a mutable version of string
			scanner = new Scanner(file);
			while(scanner.hasNextLine()){
				text.append(scanner.nextLine()).append("\n"); 
				//copies all the text from the file into the text string
			}
			//parse the protocol text into sentences
			//stringbuilder was pretty pointless; just dumps contents into an array
			protocol=text.toString().split("\n"); 
			//set the first sentence to be the title
			title=protocol[0];
			// now for each sentence create a new action item
			for(int i=1;i<protocol.length;i++){
				//this line doesn't need to be so long
				//conditions: line doesn't start with: NB, critical step, caution, or note
				if(!protocol[i].startsWith("NB")&&!protocol[i].toLowerCase().startsWith("critical step")&& !protocol[i].toLowerCase().startsWith("caution")&& !protocol[i].toLowerCase().startsWith("note")){

					sentence=protocol[i];
					taggedSentence=TaggingModel.syntacticTagging(sentence.toLowerCase()); //see TaggingModel.java
					//tagged sentence has key a word, and the value being the POS tag
					diffActions=new TreeMap<Integer, String>(); //treemap is pretty much a sorted array
					sentences=new ArrayList<String>();
					for(String key : listOfActions.keySet()){			
						//checks if one of the words matches 
						if(taggedSentence.keySet().contains(key) ){  // &&!taggedSentence.get(key).equals("NN") need to check POS tagger
							diffActions.put(sentence.indexOf(key),key); //sort by order of appearance
						}
					}
					actions=new ArrayList<String>(); 
					if(!diffActions.isEmpty()){
						for(int val: diffActions.keySet()){
							//just dumps the contents of diffaction into an arraylist
							//COMPLETELY pointless to use the intermediary treemap
							actions.add(diffActions.get(val)); 
							}
						if(actions.size()>1){
							//divide the sentence into two or three sentences based on the number of actions.
							for(int m=0;m<actions.size()-1;m++){
								//substrings for the contents of a given action
								sentences.add(sentence.substring(sentence.toLowerCase().indexOf(actions.get(m)),sentence.toLowerCase().indexOf(actions.get(m+1))));
							}
							String lastAction=actions.get(actions.size()-1);
							//workaround for the last action, substring done differently
							sentences.add(sentence.substring(sentence.toLowerCase().indexOf(lastAction), sentence.length()));
						}else{
							//only one action, so just add the whole thing
							sentences.add(sentence);
						}

						// for now there is no clear use for the tagged sentence, later it will be used for more complicated sentences especially the ones that has two actions

						//start with the actions one by one till action is found in the sentence
						for(int n=0;n<sentences.size();n++){
							sentence=sentences.get(n);
							note=new StringBuilder();
							protocolItem = new ProtocolBeans(); //trillions of getters and setters for different fields
							protocolItem.setTitle(title); //title is first line of text file
							//set the action statement, action
							protocolItem.setActionStatement(sentences.get(n).replaceAll(",", "")); //dont need to consider commas in text

							act=actions.get(n); //corresponding action fo each sentence
							protocolItem.setAction(act);
							//now after setting the action, the key value in the listOfActions is a string of the specified action
							//Note that we'll take as a starter sentences that contains one action only.
							//descriptors(taken from the first excel file of different actions)
							//now we split the descriptors and try to find them accordingly
							//						String presentDescriptors= listOfActions.get(key);
							//						if(!presentDescriptors.contains("temperature")){
							//							protocolItem.setTemperature(null);
							//						} if(!presentDescriptors.contains("volume")) {
							//							protocolItem.setVolume(null);
							//						} if(!presentDescriptors.contains("entity")) {
							//							protocolItem.setEntity(null);
							//						} if(!presentDescriptors.contains("concentration")) {
							//							protocolItem.setConcentration(null);
							//						} if(!presentDescriptors.contains("equipment")) {
							//							protocolItem.setEquipment(null);
							//						} if(!presentDescriptors.contains("period")) {
							//							protocolItem.setPeriod(null);
							//						} if(!presentDescriptors.contains("speed")) {
							//							protocolItem.setSpeed(null);
							//						}if(!presentDescriptors.contains("condition")) {
							//							protocolItem.setCondition(null);
							//}
							//now get the list of descriptors for the corresponding action and start working them out one by one
							descriptors= listOfActions.get(act).split(",");
							//incoming massive loop with if-elseif chain for all the possible descriptors
							//i'm gonna assume listOfActions.get(act) starts with a comma, so it skips the first one
							//too lazy to go back up and verify this at the moment
							for(int j=1;j<descriptors.length;j++){ 
								//could instead use a switch statement
								//that is, if the server runs JDK 7 or 8
								if(descriptors[j].equals("entity")){
									//the reason of choosing a treemap is to get the entities sorted out based on their order in the sentence.
									ent = new TreeMap<Integer, String>();
									//sent is the sentence which is amended after finding every entity by deleting it.
									sent=sentence.toLowerCase();
									entities=new StringBuilder();
									for(String entity: listOfEntities.split(",")){
										sb=new StringBuilder();
										//escape char for space, escape char for word boundary
										//so it's pretty much looking for this word surrounded by spaces
										pattern="\\s\\b"+entity.toLowerCase()+"\\b\\s?"; //some regex stuff
										match=PatternUtils.getWordAccordingToPattern(sent, pattern);
										//why does it do this? regex pattern matching seems like an unneeded complexity
										if(!match.equals("")){										
											ent.put(sentence.toLowerCase().indexOf(entity.toLowerCase()), entity);
											elements=sent.split(entity.toLowerCase());
											for(int l=0;l<elements.length;l++){
												sb.append(elements[l].trim()).append(" ");
											}
											sent=sb.toString(); //all this just deletes the entity keyword
											//absolutely not needed; just use sentence.toLowerCase.replace(entity.toLowerCase(), " ")
										}
									}
									//done checking list of entities, now check list of chemicals
									for(String chem: listOfChemicals.keySet()){
										if(sentence.toLowerCase().contains(chem.toLowerCase())){
											String[] chemNameId= listOfChemicals.get(chem).split(",");									
											ent.put(sentence.toLowerCase().indexOf(chem.toLowerCase()),chemNameId[1]+". ID:"+chemNameId[2]);
											//put full chemical name followed by ID
										}
									}
									//if the tree is not empty get the values of the keysets and write them out a s entities
									if(!ent.isEmpty()){ //now take all the entities and dump them into an arraylist
										for(int im : ent.keySet()){
											entities.append(ent.get(im)).append("::");
											//separate all the things by :: for whatever reason
										}
										protocolItem.setEntity(entities.toString()); //set this field 
									}
									else{

										//System.out.println("entity not found for action " + protocolItem.getAction());
										note.append("entity not found for action " + protocolItem.getAction()+"::");
										protocolItem.setEntity(null);
									}	
									// END ENTITIES SECTION
								}else if(descriptors[j].equals("subject")){
									for(String subj: listOfOtherSubjects.split(",")){
										pattern = "\\b"+subj+"\\b";
										match=PatternUtils.getWordAccordingToPattern(sentence, pattern);
										if(!match.equals("")){ //if sentence contains subject
											protocolItem.setActionSubject(subj);
											break; //can only be one subject i guess
										}
									}
								} 
								//the second descriptor is equipment just to be able to remove it with its corresponding volume
								else if(descriptors[j].equals("equipment")){
									//we need to establish a new string builder to be able to remove the equipment from 
									//the sentence after we figure it out
									sb=new StringBuilder();
									equipments = new StringBuilder();
									sent=sentence.toLowerCase();
									//find the corresponding equipment, if not found send a message
									for(String word: listOfEquipment.split(",")){
										//too lazy to look up what this regex means
										pattern="(\\d+?\\s?\\-?(ml|mL)?\\s?)?(prechilled)?\\s?"+word+"\\b";	
										match=PatternUtils.getWordAccordingToPattern(sent, pattern);
										if(!match.equals("")){ 								
											equipments.append(match).append("::");	
											elements=sent.split(match);

											for(int f=0;f<elements.length;f++){
												sb.append(elements[f].trim() + " ");
											}

											sent=sb.toString(); //delete segment from string
										}
									}
									//now the new sentence is the original one with the equipment removed, so the volume of the equipment wouldn't
									//be mistaken with the volume of the entity
									if(!sent.equals("")){
										sentence=sent; //if something was found, make sure it was removed
									}
									protocolItem.setEquipment(equipments.toString());

									if(protocolItem.getEquipment().equals("")){
										//System.out.println("No equipment specified for "+ protocolItem.getAction());
										note.append("No equipment specified for "+ protocolItem.getAction()+"::");
										protocolItem.setEquipment(null);
									}

								}else if(descriptors[j].equals("volume")){
									volumes=new StringBuilder();
									//find the corresponding volume, if not found send a message
									pattern = "[-+]?[+-]?\\s?\\d+\\.?\\,?\\d*\\s?\\-?(ml|ul|µl|mL|mg)"; //list of volume units needed to amend the regular expression
									matchers=PatternUtils.getWordListAccordingToPattern(sentence, pattern);
									if(!matchers.equals("")){
										for(String m :matchers){
											for(String vv: listOfMeasures.keySet()){
												//it's deleting something, but idk how regex does stuff and dont wanna look it up
												if(m.replace(",", "").replaceAll("[-+]?[+-]?\\s?\\d+\\.?\\,?\\d*\\s?\\-?", "").trim().matches(vv)){
													//append to the stringbuilder volume the volume plus :: plus the id otherwise append the volume and replace any comma with an empty string
												volumes.append(m.replace(",", "")+"::"+ listOfMeasures.get(vv).replaceAll(",", "")).append("::");
												}
											
										//	volumes.append(m.replace(",","")).append("::");
											}
										}
										protocolItem.setVolume(volumes.toString());
									}
									if(protocolItem.getVolume().equals("")){
										//System.out.println("No volume specified for "+ protocolItem.getEntity());
										note.append("No volume specified for "+ protocolItem.getEntity()+"::");
										protocolItem.setVolume(null);
									}
								}else if(descriptors[j].equals("concentration")){
									concentrations=new StringBuilder();
									//find the corresponding concentration, if not found send a message
									pattern = "\\d+\\s?%|\\d+\\s?(X|x)|\\d+/\\d+";
									matchers=PatternUtils.getWordListAccordingToPattern(sentence, pattern);
									if(!matchers.equals("")){
										for(String m: matchers){	
											if(!m.equals("")){
												concentrations.append(m).append("::");
											}
										}
										protocolItem.setConcentration(concentrations.toString());
									}
									if(protocolItem.getConcentration().equals("")){
										//System.out.println("No concentration specified for "+ protocolItem.getEntity());
										note.append("No concentration specified for "+ protocolItem.getEntity()+"::");
										protocolItem.setConcentration(null);
									}
								}else if(descriptors[j].equals("period")){
									//find the corresponding period, if not found send a message
									pattern = "overnight|(\\d+)?\\-?\\d+?\\s?(hour|hr|h|minutes|min|m)";
									match=PatternUtils.getWordAccordingToPattern(sentence, pattern);
									if(!match.equals("")){
										protocolItem.setPeriod(match.replace("overnight", "16h"));	
									}else{
										//System.out.println("No period spcified for " + protocolItem.getAction());
										note.append("No period specified for "+ protocolItem.getAction()+"::");
										protocolItem.setPeriod(null);
									}
								}else if(descriptors[j].equals("speed")){
									//find the corresponding speed, if not found send a message
									pattern="\\d+\\,?\\d+\\s?(r\\.?\\s?p\\.?\\s?m\\.?\\s?|g)"; //need to add the other speed units
									match=PatternUtils.getWordAccordingToPattern(sentence, pattern);
									if(!match.equals("")){
										protocolItem.setSpeed(match.replace(",",""));
									}else{
										//System.out.println("no specified speed for action " + protocolItem.getAction());
										note.append("No speed specified for "+ protocolItem.getAction()+"::");
										protocolItem.setSpeed(null);
									}
								}else if(descriptors[j].equals("temperature")){
									//find the corresponding temperature, if not found send a message
									pattern="[+-]?\\d+\\s?°\\s?C|RT|room\\stemperature";
									match=PatternUtils.getWordAccordingToPattern(sentence, pattern);
									if(!match.equals("")){
										protocolItem.setTemperature(match.replace("RT", "18°C").replace("room temperature", "18°C").replace("°C","°C:UO:0000027").replace("° C","°C:UO:0000027"));
									}else{
										//System.out.println("no specified temperature for "+ protocolItem.getAction());
										note.append("No specified temperature for "+ protocolItem.getAction()+"::");
										protocolItem.setTemperature(null);
									}
								} else if(descriptors[j].equals("condition")){
									conditions=new StringBuilder();
									for(String condition: listOfConditions.split(",")){
										pattern="\\b"+condition.toLowerCase()+"\\s?((\\d+\\s?\\*?\\s?\\d+(<sup>\\d</sup>)?\\s?)\\-(\\d+\\s?\\*?\\s?\\d+(<sup>\\d</sup>)?\\s?)\\s?(cells)?\\/(cm)\\d?(<sup>\\d<sup>)?|(\\d+\\:\\d+)|\\d+|ca((<sup>2+</sup>)?(\\d+\\+)?)\\sand\\smg(<sup>2+</sup>)?(\\d+\\+)?|\\d+\\s?(nm)?|\\d+\\s?\\=\\~\\d+|\\=\\s?\\~\\d+\\.?\\d+)?";
										match=PatternUtils.getWordAccordingToPattern(sentence.toLowerCase(), pattern);
										if(!match.equals("")){ 								
											conditions.append(match+"::");										
										}
									}
									protocolItem.setCondition(conditions.toString());
									if(protocolItem.getCondition().equals("")){	
										//System.out.println("No condition specified for "+ protocolItem.getAction());
										note.append("No condition specified for "+ protocolItem.getAction()+"::");
										protocolItem.setCondition(null);
									}
								}else if(descriptors[j].equals("method")){

									for(String method: listOfMethods.split(",")){
										pattern="\\b"+method+"\\b";
										match=PatternUtils.getWordAccordingToPattern(sentence.toLowerCase(), pattern);
										if(!match.equals("")){
											protocolItem.setMethod(method);
											break;
										}
									}
									if(match.equals("")){
										note.append("No method specified for "+ protocolItem.getAction()+"::");
										protocolItem.setMethod(null);
									}
								}else if(descriptors[j].equals("density")){
									pattern="";
									match=PatternUtils.getWordAccordingToPattern(sentence, pattern);
									if(!match.equals("")){
										protocolItem.setDensity(match);
										break;
									}else{
										note.append("No density specified for "+ protocolItem.getAction()+"::");
										protocolItem.setDensity(null);                 
									}
								}
							}
							protocolItem.setNote(note);

							protocolItems.add(protocolItem);
						}
					}else{ //if there werent any actions detected
						protocolItem=new ProtocolBeans();
						protocolItem.setActionStatement(sentence.replaceAll(",", "")); //dunno why deleting the commas is needed
						note=new StringBuilder();
						//	System.out.println("need to add action " + sentence.split(" ")[0]+ " appeared in statement: " + sentence);
						note.append("need to add action " + sentence.split(" ")[0]+ " appeared in statement: " + sentence);
						protocolItem.setNote(note); //SB was pointless
						protocolItems.add(protocolItem);
					}


				}

				else{ //if starts with NB, critical thing, or whatever
					protocolItem=new ProtocolBeans();
					protocolItem.setActionStatement(protocol[i].replaceAll(","," ")); //why delete commas?
					protocolItems.add(protocolItem);
				}

			}
		}
			catch(FileNotFoundException e){
		}
	}

	public void printProtocol() throws IOException{
		StringBuilder fullProtocol = new StringBuilder();
		fullProtocol.append("Statement,Action,Entity,Subject,Temperature,Volume,Concentration,Equipment,Period,Speed,Condition,Method,Note\n");
		for(ProtocolBeans item: protocolItems){
			fullProtocol.append(item.printStructuredAction()).append("\n");
		}
		ProtocolBeans forTitle= protocolItems.get(0);
		FileWriter fw = new FileWriter("processed_"+ forTitle.getTitle() +".csv");
		fw.write(fullProtocol.toString());
		fw.close();
	}

	public static void main(String[] args) throws InvalidFormatException, IOException 
	{
		long start=System.currentTimeMillis();
		ProtocolsParser pp = new ProtocolsParser();
		pp.parseProtocol(new File("TP1_Bickles2006.txt"));
		pp.printProtocol();
		System.out.println((System.currentTimeMillis()-start)/1000l); //dont round this please
	}
}
