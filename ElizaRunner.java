import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

class ElizaRunner {

	public static void main(String[] args) {
		ElizaBot bot = new ElizaBot();
		try {
			//File path
			Path path = Paths.get("ElizaScript.txt");
			BufferedReader br = new BufferedReader(new FileReader(new File(path.toString())));
			String line = br.readLine();
			String lastKeyWord = "";
			String lastDecompRule ="";
			while (line != null) {
				line=line.trim();
				String head = line.substring(0, line.indexOf(" "));
				String strippedString;
				switch (head) {
				case "quit:":
					bot.addQuit(line.substring(line.indexOf(" ")+1));
					//System.out.println(line.substring(line.indexOf(" ")+1));
					break;
				case "pre:":
					strippedString = line.substring(line.indexOf(" ")+1);
					bot.addPreSub(strippedString.substring(0,strippedString.indexOf(" ")), strippedString.substring(strippedString.indexOf(" ")+1));
					//System.out.println(strippedString.substring(0,strippedString.indexOf(" ")) + " " + strippedString.substring(strippedString.indexOf(" ")+1));
					break;
				case "post:":
					strippedString = line.substring(line.indexOf(" ")+1);
					bot.addPostSub(strippedString.substring(0,strippedString.indexOf(" ")), strippedString.substring(strippedString.indexOf(" ")+1));
					//System.out.println(strippedString.substring(0,strippedString.indexOf(" ")) + " " + strippedString.substring(strippedString.indexOf(" ")+1));
					break;
				case "synon:":
					bot.addSynonyms(line.substring(line.indexOf(" ")+1));
					//System.out.println(line.substring(line.indexOf(" ")+1));
					break;
				case "key:":
					String[] keydetails = line.substring(line.indexOf(" ") + 1).split(" ");
					int rating = 0;
					if (keydetails.length > 1){
						rating=Integer.parseInt(keydetails[1]);
					}
					bot.addKeyWord(keydetails[0], rating);
					lastKeyWord = keydetails[0];
					//System.out.println(lastKeyWord + " " + rating);
					break;
				case "decomp:":
					lastDecompRule = line.substring(line.indexOf(" ") + 1);
					bot.addDecomp(lastKeyWord, lastDecompRule);
					//System.out.println(lastDecompRule);
					break;
				case "reasmb:":
					bot.addAssembly(lastKeyWord, lastDecompRule, line.substring(line.indexOf(" ")+1));
					//System.out.println(line.substring(line.indexOf(" ")+1));
					break;
				default:
					break;
				}

				line = br.readLine();
			}
			System.out.println(bot.greet());
			br = new BufferedReader(new InputStreamReader(System.in));
			line = br.readLine();
			while(!bot.quit(line)){
				//bot processes input
				//generates responce
				bot.process(line);

				line = br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
