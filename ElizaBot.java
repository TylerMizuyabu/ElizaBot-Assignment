import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
	private String inputSynonym = "";

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

	public void addPreSub(String key, String value) {
		this.preSub.put(key, value);
	}

	public void addPostSub(String key, String value) {
		this.postSub.put(key, value);
	}

	public void addSynonyms(String value) {
		this.synonyms.add(value);
	}

	public void addQuit(String value) {
		this.quit.add(value);
	}

	public void addKeyWord(String keyword, int keyValue) {
		this.keyWords.put(keyword, new KeyDetails(keyValue));
	}

	public void addDecomp(String keyword, String decompRule) {
		this.keyWords.get(keyword).addDecomp(decompRule);
	}

	public void addAssembly(String keyword, String decompRule, String assemblyRule) {
		this.keyWords.get(keyword).addAssembly(decompRule, assemblyRule);
	}

	public boolean quit(String input) {
		if (this.quit.contains(input)) {
			System.out.println(this.quit.get(((int) (Math.random() * this.quit.size()))).toLowerCase());
			return true;
		}
		return false;
	}

	public String[] convertToWords(String input) {
		String[] words = input.replaceAll("(?!')\\p{P}", "").toLowerCase().split(" ");
		for (int i = 0; i < words.length; i++) {
			if (preSub.containsKey(words[i])) {
				words[i] = preSub.get(words[i]);
			}

		}
		return Arrays.toString(words).replaceAll("\\p{P}", "").split(" ");
	}

	public ArrayList<String> sortedKeyWords(String[] words) {
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
							break;
						}
					}
				}
			}
		}
		return keywords;
	}

	public String findDecomp(String keyWord, String input) {
		HashMap<String, ArrayList<String>> rules = this.keyWords.get(keyWord).getRules();
		Iterator<String> decompIterator = rules.keySet().iterator();
		while (decompIterator.hasNext()) {
			String decompRule = decompIterator.next();
			// System.out.println("Decomp Rule: " + decompRule);
			if (decompRule.contains("@")) {
				Pattern p = Pattern.compile("@[\\w]+", Pattern.CASE_INSENSITIVE);
				Matcher m = p.matcher(decompRule);
				m.find();
				String synWrd = m.group(0).substring(1); // Retrieves the word
															// that is
															// synonymous
															// without the @
				String[] allSyns = findSyns(synWrd);
				for (int i = 0; i < allSyns.length; i++) {
					String tempRule = m.replaceAll(allSyns[i]);
					if (match(tempRule, input)) {
						this.inputSynonym = allSyns[i];
						return decompRule;
					}
				}
			} else {
				if (match(decompRule, input)) {
					return decompRule;
				}
			}
		}
		return null;
	}

	public boolean match(String decompRegEx, String text) {
		Pattern p = Pattern.compile(decompRegEx, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(text);
		return m.matches();
	}

	public String[] findSyns(String word) {
		for (int i = 0; i < this.synonyms.size(); i++) {
			String compWord = this.synonyms.get(i).split(" ")[0];
			if (compWord.equalsIgnoreCase(word)) {
				return this.synonyms.get(i).split(" ");
			}
		}
		return null;
	}

	// Method responsible for analyzing user input and generate a response
	public void process(String input) {
		// Breaks the input into words and does pre substitution
		String[] words = convertToWords(input);
		input = Arrays.toString(words).replaceAll("\\p{P}", "");
		// System.out.println(Arrays.toString(words));
		// Finds keywords and sorts in descending weight
		ArrayList<String> keywords = sortedKeyWords(words);
		String decompRule = null;
		Iterator<String> it = keywords.iterator();
		while (it.hasNext()) {
			String currentKey = it.next();
			decompRule = this.findDecomp(currentKey, input);
			if (decompRule != null) {
				String asmblyRule = chooseAssembly(this.keyWords.get(currentKey).getRules().get(decompRule));
				if (asmblyRule.split(" ")[0].equalsIgnoreCase("goto")) {
					keywords.add(keywords.indexOf(currentKey) + 1, asmblyRule.split(" ")[1]);
				} else {
					System.out.println(reAssemble(decompRule, input, asmblyRule).toLowerCase());
					return;
				}
			}
		}

		return;
	}

	public String reAssemble(String decRule, String input, String asmblyRule) {
		input = input.toLowerCase();

		String[] splitter = decRule.trim().split("\\(\\.\\*\\)");
		for (int i = 0; i < splitter.length; i++) {
			if (!splitter[i].equals("")) {
				if (!splitter[i].contains("@")) {
					input = input.replaceFirst(splitter[i], "|");
					//System.out.println(Arrays.toString(input.replaceFirst(splitter[i], "|").replaceFirst(inputSynonym, "|").split("\\|")));
				} else {
					input = input.replaceFirst(this.inputSynonym, "|"+this.inputSynonym+"|");
				}
			}
		} // End of Loop
		String[] content = input.split("\\|");
		//System.out.println(Arrays.toString(content));
		for (int i = 0; i < content.length; i++) {
			content[i] = content[i].trim();
			String[] temp = content[i].split(" ");
			for (int j = 0; j < temp.length; j++) {
				if(this.postSub.containsKey(temp[j])){
					temp[j] = this.postSub.get(temp[j]);
				}
			}
			content[i] = Arrays.toString(temp).replaceAll("(,|\\[|\\])", "");
		}
		String[] spltAssmbly = asmblyRule.split(" ");
		for (int i = 0; i < spltAssmbly.length; i++) {
			Pattern p = Pattern.compile("\\(\\d\\)");
			Matcher m = p.matcher(spltAssmbly[i]);
			if (m.find()) {
				int x = Integer.parseInt(Character.toString(m.group(0).charAt(1))) - 1;
				spltAssmbly[i] = m.replaceAll(content[x]);
			}
		}
		return Arrays.toString(spltAssmbly).replaceAll("(,|\\[|\\])", "");
	}

	public String chooseAssembly(ArrayList<String> rules) {
		int x = (int) (Math.random() * rules.size());
		return rules.get(x);
	}
}

class KeyDetails {

	private int rating;
	// HashMap of the form <RegEx Decomp Rule, ArrayList<Reassembly Rules>>
	private LinkedHashMap<String, ArrayList<String>> rules;

	/*
	 * if no rating is given the default rating is 0
	 */
	public KeyDetails() {
		this.rating = 0;
		this.rules = new LinkedHashMap<String, ArrayList<String>>();
	}

	public KeyDetails(int rating) {
		this.rating = rating;
		this.rules = new LinkedHashMap<String, ArrayList<String>>();
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
