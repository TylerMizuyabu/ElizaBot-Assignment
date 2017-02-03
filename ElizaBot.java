import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Processing consists of the following steps.
 *
 * First the sentence broken down into words, separated by spaces.  All further
 * processing takes place on these words as a whole, not on the individual
 * characters in them.
 *
 * Second, a set of pre-substitutions takes place.
 *
 * Third, Eliza takes all the words in the sentence and makes a list of all
 * keywords it finds.  It sorts this keyword list in descending weight.  It
 * process these keywords until it produces an output.
 *
 * Fourth, for the given keyword, a list of decomposition patterns is searched.
 * The first one that matches is selected.  If no match is found, the next keyword
 * is selected instead.
 *
 * Fifth, for the matching decomposition pattern, a reassembly pattern is
 * selected.  There may be several reassembly patterns, but only one is used
 * for a given sentence.  If a subsequent sentence selects the same decomposition
 * pattern, the next reassembly pattern in sequence is used, until they have all
 * been used, at which point Eliza starts over with the first reassembly pattern.
 *
 * Sixth, a set of post-substitutions takes place.
 *
 * Finally, the resulting sentence is displayed as output.
 */
public class ElizaBot {

	private final String GREETING = "Hello. How are you feeling today?";
	private final String BYE = "Goodbye. Thank you for talking to me.";
	private ArrayList<String> quit;
	private HashMap<String, String> preSub;
	private HashMap<String, String> postSub;
	private ArrayList<String> synonyms;
	private HashMap<String, KeyDetails> keyWords;

	public ElizaBot() {
		this.quit = new ArrayList<String>();
		this.preSub = new HashMap<String, String>();
		this.postSub = new HashMap<String, String>();
		this.synonyms = new ArrayList<String>();
		this.keyWords = new HashMap<String, KeyDetails>();
	}

	public String greet() {
		return this.GREETING;
	}

	public void addPreSub(String key, String value){
		this.preSub.put(key,value);
	}

	public void addPostSub(String key, String value){
		this.preSub.put(key,value);
	}

	public void addSynonyms(String value){
		this.synonyms.add(value);
	}

	public void addQuit(String value){
		this.quit.add(value);
	}

	public void addKeyWord(String key, String value){

	}

	//Method responsible for analyzing user input and returning associated assembly rules to use
	public ArrayList<String> process(String input) {
		//Breaks the input into words and does pre substitution
		String[] words = input.split(" ");
		for (int i = 0; i < words.length; i++) {
			if (preSub.containsKey(words[i])) {
				words[i] = preSub.get(words[i]);
			}
		}
		//Finds keywords and sorts in descending weight
		ArrayList<String> keywords = new ArrayList<String>();
		for (int i = 0; i < words.length; i++) {
			if (keyWords.containsKey(words[i])) {
				if (keywords.size() == 0) {
					keywords.add(words[i]);
				} else {
					for (int j = 0; j < keywords.size(); j++) {
						if (keyWords.get(keywords.get(j)).getRating() < keyWords.get(words[i]).getRating()) {
							keywords.add(j, words[i]);
							break;
						} else if (j == keywords.size() - 1) {
							keywords.add(words[i]);
						}
					}
				}
			}
		}
		Iterator<String> it = keywords.iterator();
		while (it.hasNext()) {
			HashMap<String, ArrayList<String>> rules = keyWords.get(it.next()).getRules();
			Iterator<String> decompIt = rules.keySet().iterator();
			while (decompIt.hasNext()) {
				String decompRule = decompIt.next();
				if (decompRule.contains("@")) {
					Pattern p = Pattern.compile("@[^\\s]+", Pattern.CASE_INSENSITIVE);
					Matcher m = p.matcher(decompRule);
					String synWrd = m.group(0).substring(1); // Retrieves the word that is synonymous without the @
					for (int i = 0; i < synonyms.size(); i++) {
						if (synonyms.get(i).contains(synWrd)) {
							String[] wrds = synonyms.get(i).split(" ");
							for (int j = 0; j < wrds.length; j++) {
								//Make decomp rule
								//See if there is a match with input text
								//If there use that decomp rule, no need to continue looking for another decomp rule
								String tempRule = m.replaceAll(wrds[j]);
								Pattern pRule = Pattern.compile(tempRule,Pattern.CASE_INSENSITIVE);
								Matcher mRule = pRule.matcher(input);
								if (mRule.matches()){
									return rules.get(decompRule);
								}
							}
						}
					}

				}else{
					//There are no synonyms then ...
					Pattern pRule = Pattern.compile(decompRule, Pattern.CASE_INSENSITIVE);
					Matcher mRule = pRule.matcher(input);
					if (mRule.matches()){
						return rules.get(decompRule);
					}
					//Check if the decomp rule matches the text input
					//If yes use this decomp rule, no need to continue looking for another decomp rule
				}
			}
		}
		//Nothing matches then ...
		return keyWords.get("xnone").getRules().get(".*");
	}
}

class KeyDetails {

	private int rating;
	// HashMap of the form <RegEx Decomp Rule, ArrayList<Reassembly Rules>>
	private HashMap<String, ArrayList<String>> rules;

	/*
	 * if no rating is given the default rating is 0
	 */
	public KeyDetails() {
		this.rating = 0;
	}

	public KeyDetails(int rating) {
		this.rating = rating;
	}

	/*
	 * Gets the rating of a keyword
	 */
	public int getRating() {
		return this.rating;
	}

	/*
	 * Method adds a decomposition pattern to the key word
	 */
	public void addDecomp(String key) {
		this.rules.put(key, new ArrayList<String>());
	}

	/*
	 * Method adds an assembly rule to the key word
	 */
	public void addAssembly(String key, String asmblyRule) {
		if (!this.rules.containsKey(key)) {
			this.addDecomp(key);
		}
		this.rules.get(key).add(asmblyRule);
	}

	/*
	 * Returns the rules hash map for the key word
	 */
	public HashMap<String, ArrayList<String>> getRules() {
		return this.rules;
	}
}
