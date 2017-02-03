import java.io.*;

class ElizaRunner {

  public static String inputFile = "ElizaScript";

  public static void main(String[]args){
    try{
      BufferedReader br = new BufferedReader(new FileReader(new File(inputFile)));
      String line = br.readLine();
      
    }catch(IOException e){
      e.printStackTrace();
    }
  }
}
