import java.io.*;

public class TerminalApp{

   // Data model
   private Chario chario;
   private Scanner scanner;
   private Parser parser;
   private String filename;
   private boolean include_r=false;//for role analysis
   private boolean include_s=false;//for scope analysis
   private boolean include_t=false;//for basic analysis

   public TerminalApp(String args[]){
      java.util.Scanner reader = new java.util.Scanner(System.in);
      this.filename = args[0];//first argument will be the filename
	  
	  for(int i=1;i<args.length;i++){//a forloop to fetch arguments from user input command
		  if(args[i]=="-r"){
			  this.include_r=true;
		  }else if(args[i]=="-s"){
			  this.include_s=true;
		  }else if(args[i]=="-t"){
			  this.include_t=true;
		  }
	  }
	  
      FileInputStream stream;
      try{
         stream = new FileInputStream(filename);
     }catch(IOException e){
         System.out.println("Error opening file.");
         return;
      }      
      chario = new Chario(stream);
      //testChario();
      scanner = new Scanner(chario);
      //testScanner();
      parser = new Parser(chario, scanner,this.include_r,this.include_s,this.include_t);
      testParser();
   }

   private void testChario(){
      char ch = chario.getChar();
      while (ch != Chario.EF)
         ch = chario.getChar();
      chario.reportErrors();
   }

   private void testScanner(){
      Token token = scanner.nextToken();
      while (token.code != Token.EOF){
         chario.println(token.toString());
         token = scanner.nextToken();
      }
      chario.reportErrors();
   }

   private void testParser(){
      try{
         parser.parse();
      }
      catch(Exception e){}
      chario.reportErrors();
   }

   public static void main(String args[]){
      new TerminalApp(args);
   }
}