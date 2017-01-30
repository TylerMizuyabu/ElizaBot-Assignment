import java.util.ArrayList;
import java.util.HashMap;

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
	private HashMap<String,String> preSub;
	private HashMap<String,String> postSub;
	private ArrayList<String> synonyms;
	private HashMap<String,KeyDetails> keyWords;
	

	public ElizaBot(){
		
	}
	
}

class KeyDetails{
	
	private int rating;
	private HashMap<String, ArrayList<String>> rules;
	
	/*
	 * if no rating is given the default rating is 0
	 */
	public KeyDetails(){
		this.rating = 0;
	}
	
	public KeyDetails(int rating){
		this.rating = rating;
	}
	
	/*
	 * Sets the rating ofa keyword
	 */
	public int setRating(){
		return this.rating;
	}
	
	/*
	 * Method adds a decomposition pattern to the key word
	 */
	public void addDecomp(String key){
		this.rules.put(key, new ArrayList<String>());
	}
	
	/*
	 * Method adds an assembly rule to the key word
	 */
	public void addAssembly(String key, String asmblyRule){
		if (!this.rules.containsKey(key)){
			this.addDecomp(key);
		}
		this.rules.get(key).add(asmblyRule);
	}
	
	/*
	 * Returns the rules hash map for the key word
	 */
	public HashMap<String, ArrayList<String>> getRules(){
		return this.rules;
	}
}
