// Role analysis partially completed

import java.util.*;

public class Parser extends Object{

   private Chario chario;
   private Scanner scanner;
   private Token token;
   private SymbolTable table;
   private boolean include_r=false;
   private boolean include_s=false;
   private boolean include_t=false;

   private Set<Integer> addingOperator,
                        multiplyingOperator,
                        relationalOperator,
                        basicDeclarationHandles,
                        statementHandles,
                        leftNames,                        // Sets of roles for names (see below)
                        rightNames;

   public Parser(Chario c, Scanner s,boolean include_r,boolean include_s,boolean include_t){
      this.include_r = include_r;
	  this.include_s = include_s;
	  this.include_t = include_t;
	  chario = c;
      scanner = s;
      initHandles();
      initTable();
      token = scanner.nextToken();
   }

   public void reset(){
      scanner.reset();
      initTable();
      token = scanner.nextToken();
   }

   private void initHandles(){
      addingOperator = new HashSet<Integer>();
      addingOperator.add(Token.PLUS);
      addingOperator.add(Token.MINUS);
      multiplyingOperator = new HashSet<Integer>();
      multiplyingOperator.add(Token.MUL);
      multiplyingOperator.add(Token.DIV);
      multiplyingOperator.add(Token.MOD);
      relationalOperator = new HashSet<Integer>();
      relationalOperator.add(Token.EQ);
      relationalOperator.add(Token.NE);
      relationalOperator.add(Token.LE);
      relationalOperator.add(Token.GE);
      relationalOperator.add(Token.LT);
      relationalOperator.add(Token.GT);
      basicDeclarationHandles = new HashSet<Integer>();
      basicDeclarationHandles.add(Token.TYPE);
      basicDeclarationHandles.add(Token.ID);
      basicDeclarationHandles.add(Token.PROC);
      statementHandles = new HashSet<Integer>();
      statementHandles.add(Token.EXIT);
      statementHandles.add(Token.ID);
      statementHandles.add(Token.IF);
      statementHandles.add(Token.LOOP);
      statementHandles.add(Token.NULL);
      statementHandles.add(Token.WHILE);
      leftNames = new HashSet<Integer>();                 // Name roles for targets of assignment statement
      leftNames.add(SymbolEntry.PARAM);
      leftNames.add(SymbolEntry.VAR);
      rightNames = new HashSet<Integer>(leftNames);       // Name roles for names in expressions
      rightNames.add(SymbolEntry.CONST);
   }

   /*
   Two new routines for role analysis.
   */

   private void acceptRole(SymbolEntry s, int expected, String errorMessage){
      if (s.role != SymbolEntry.NONE && s.role != expected)
         chario.putError(errorMessage);
   }

   private void acceptRole(SymbolEntry s, Set<Integer> expected, String errorMessage){
      if (s.role != SymbolEntry.NONE && ! (expected.contains(s.role)))
         chario.putError(errorMessage);
   }

   private void accept(int expected, String errorMessage){
      if (token.code != expected)
         fatalError(errorMessage);
      token = scanner.nextToken();
   }

   private void fatalError(String errorMessage){
      chario.putError(errorMessage);
      throw new RuntimeException("Fatal error");
   }

   /*
   Three new routines for scope analysis.
   */

   private void initTable(){
      table = new SymbolTable(chario,this.include_r,this.include_s,this.include_t);
      table.enterScope();
      SymbolEntry entry = table.enterSymbol("BOOLEAN");
      entry.setRole(SymbolEntry.TYPE);
      entry = table.enterSymbol("CHAR");
      entry.setRole(SymbolEntry.TYPE);
      entry = table.enterSymbol("INTEGER");
      entry.setRole(SymbolEntry.TYPE);
      entry = table.enterSymbol("TRUE");
      entry.setRole(SymbolEntry.CONST);
      entry = table.enterSymbol("FALSE");
      entry.setRole(SymbolEntry.CONST);
   }      

   private SymbolEntry enterId(){
	   	  

      SymbolEntry entry = null;
      if (token.code == Token.ID)
         entry = table.enterSymbol(token.string);
      else
         fatalError("identifier expected");
      token = scanner.nextToken();
      return entry;
   }

   private SymbolEntry findId(){
	   	  

      SymbolEntry entry = null;
      if (token.code == Token.ID)
         entry = table.findSymbol(token.string);
      else
         fatalError("identifier expected");
      token = scanner.nextToken();
      return entry;
   }

   public void parse(){

      subprogramBody();
      accept(Token.EOF, "extra symbols after logical end of program");
      table.exitScope();
   }

   /*
   subprogramBody =
         subprogramSpecification "is"
         declarativePart
         "begin" sequenceOfStatements
         "end" [ <procedure>identifier ] ";"
   */
   private void subprogramBody(){
	  
      subprogramSpecification();
      accept(Token.IS, "'is' expected");
      declarativePart();
      accept(Token.BEGIN, "'begin' expected");
      sequenceOfStatements();
      accept(Token.END, "'end' expected");
      table.exitScope();
      if (token.code == Token.ID){
         SymbolEntry entry = findId();
		 acceptRole(entry,SymbolEntry.PROC,"must be a procedure name");
      }
      accept(Token.SEMI, "semicolon expected");
   }

   /*
   subprogramSpecification = "procedure" identifier [ formalPart ]
   */
   private void subprogramSpecification(){
	   	  

      accept(Token.PROC, "'procedure' expected");
      SymbolEntry entry = enterId();
	  entry.setRole(SymbolEntry.PROC);
      table.enterScope();
      if (token.code == Token.L_PAR)
         formalPart();
   }

   /*
   formalPart = "(" parameterSpecification { ";" parameterSpecification } ")"
   */
   private void formalPart() {
	   	  

	   accept(Token.L_PAR, "'(' expected");
	   parameterSpecification();
	   
	   while(token.code == Token.SEMI)
	   {   
		   token = scanner.nextToken();
		   parameterSpecification(); 
	   }
	   
	   //token = scanner.nextToken();
	   accept(Token.R_PAR, "')' expected");
   }
   /*
   parameterSpecification = identifierList ":" mode <type>identifer
   */
   private void parameterSpecification(){
	  
      SymbolEntry list = identifierList();
	  list.setRole(SymbolEntry.PARAM);
      accept(Token.COLON, "':' expected");
	  mode();
      SymbolEntry entry = findId();
	  acceptRole(entry,SymbolEntry.TYPE,"must be a type name");
   }
   

// mode = ["in"] | "in" "out" | "out"
   private void mode(){
	   if(token.code == Token.IN){
		   accept(Token.IN,"'in' expected");
		   
		   if(token.code == Token.OUT){
			  accept(Token.OUT,"'out' expected");
		   }

	   }
	   
	   else if(token.code == Token.OUT){
		   accept(Token.OUT,"'out' expected");
	   }   
	   
	   
   }

   /*
   declarativePart = { basicDeclaration }
   */
   private void declarativePart(){
	  	  
      while (basicDeclarationHandles.contains(token.code))
         basicDeclaration();
   }

   /*
   basicDeclaration = objectDeclaration | numberDeclaration
                    | typeDeclaration | subprogramBody   
   */
   private void basicDeclaration(){
	  
      switch (token.code){
         case Token.ID:
            numberOrObjectDeclaration();
            break;
         case Token.TYPE:
            typeDeclaration();
            break;
         case Token.PROC:
            subprogramBody();
            break;
         default: fatalError("error in declaration part");
      }
   }

   /*
   objectDeclaration =
         identifierList ":" typeDefinition ";"

   numberDeclaration =
         identifierList ":" "constant" ":=" <static>expression ";"
   */
   private void numberOrObjectDeclaration(){
	  
      SymbolEntry list = identifierList();
      accept(Token.COLON, "':' expected");
      if (token.code == Token.CONST){
         list.setRole(SymbolEntry.CONST);
         token = scanner.nextToken();
         accept(Token.GETS, "':=' expected");
         expression();
      }
      else{
         list.setRole(SymbolEntry.VAR);
         typeDefinition();
      }
      accept(Token.SEMI, "semicolon expected");
   }

   /*
   typeDeclaration = "type" identifier "is" typeDefinition ";"
   */
   private void typeDeclaration(){
      accept(Token.TYPE, "'type' expected");
      SymbolEntry entry = enterId();
      entry.setRole(SymbolEntry.TYPE);
      accept(Token.IS, "'is' expected");
      typeDefinition();
      accept(Token.SEMI, "semicolon expected");
   }

   /*
   typeDefinition = enumerationTypeDefinition | arrayTypeDefinition
                  | range | <type>identifer
				  
   */
     private void typeDefinition() {
    	
    	 if(token.code == Token.L_PAR)
    	 {
    		 enumerationTypeDefinition();
    	 }
    	 
    	 else if(token.code == Token.ARRAY)
    	 {
    		 arrayTypeDefinition();
    	 }
    	 
    	 else if(token.code == Token.RANGE)
    	 {
    		 range();
    	 }
    	 
    	 else if(token.code == Token.ID)
    	 {
    		 SymbolEntry entry = findId();
			 acceptRole(entry,SymbolEntry.TYPE,"type name expected");
    	 }
    	 
    	 else {
    		 
    		 fatalError("error in typeDefinition part");
    		 
    	 }
     }
   /*
   enumerationTypeDefinition = "(" identifierList ")"
   */
   private void enumerationTypeDefinition(){
      accept(Token.L_PAR, "'(' expected");
      SymbolEntry list = identifierList();
      list.setRole(SymbolEntry.CONST);
      accept(Token.R_PAR, "')' expected");
   }

   /*
	arrayTypeDefinition = "array" "(" index { "," index } ")" "of" <type>identifier
   */
       private void arrayTypeDefinition() {
       
        accept(Token.ARRAY, "'array' expected");
        accept(Token.L_PAR, "'(' expected");
        index();
        
        while(token.code == Token.COMMA)
        {   
    	   token = scanner.nextToken();
    	   index();
        }
       
        accept(Token.R_PAR, "')' expected5");
        accept(Token.OF, "'of' expected");
        SymbolEntry entry = name();
		acceptRole(entry,SymbolEntry.TYPE,"type name expected");
       }
   /*
  index = range | <type>identifier
   */
   private void index(){
	  
      if (token.code == Token.RANGE)
         range();
      else if (token.code == Token.ID){
         SymbolEntry entry = findId();
         acceptRole(entry, SymbolEntry.TYPE, "type name expected");
      }
      else
         fatalError("error in index type");
   }

   /*
   range = "range " simpleExpression ".." simpleExpression
   */
        private void range() {
        	
        	accept(Token.RANGE, "'range' expected");
        	
        	simpleExpression();
        	
        	accept(Token.THRU, "'..' expected");
        	
        	simpleExpression();
        		
        	
        	
        }
   /*
   identifier { "," identifer }
   */
   private SymbolEntry identifierList(){
      SymbolEntry list = enterId();
      while (token.code == Token.COMMA){
         token = scanner.nextToken();
         list.append(enterId());
      }
      return list;
   }

   /*
   sequenceOfStatements = statement { statement }
   */
   private void sequenceOfStatements(){
      statement();
      while (statementHandles.contains(token.code))
         statement();
   }

   /*
   statement = simpleStatement | compoundStatement

   simpleStatement = nullStatement | assignmentStatement
                   | procedureCallStatement | exitStatement

   compoundStatement = ifStatement | loopStatement
   */
   private void statement(){
      switch (token.code){
         case Token.ID:
            assignmentOrCallStatement();
            break;
         case Token.EXIT:
            exitStatement();
            break;
         case Token.IF:
            ifStatement();
            break;
         case Token.NULL:
            nullStatement();
            break;
         case Token.WHILE:
         case Token.LOOP:
            //loopStatement();
        	 loopStatementOrIterationScheme();
            break;
         default: fatalError("error in statement");
      }
   }
   /*
   nullStatement = "null" ";"
   */
     private void nullStatement() {
    	 accept(Token.NULL, "'null' expected");
    	 accept(Token.SEMI, "';' expected nullStatement");
     }
   /*
   loopStatement =
         [ iterationScheme ] "loop" sequenceOfStatements "end" "loop" ";"

   iterationScheme = "while" condition
   */
     private void loopStatementOrIterationScheme() {
    	 
    	 if(token.code == Token.WHILE)
    	 {
    		 //accept(Token.WHILE, "'accept' expected");
    		 token = scanner.nextToken();
    		 condition();
    	 }
    	 
    	 accept(Token.LOOP, "'loop' expected");
    	 sequenceOfStatements();
    	 
    	 accept(Token.END, "'end' expected");
    	 accept(Token.LOOP, "'loop' expected");
    	 accept(Token.SEMI, "';' expected loopStatementOrIterationScheme");
     }
   /*
   ifStatement =
         "if" condition "then" sequenceOfStatements
         { "elsif" condition "then" sequenceOfStatements }
         [ "else" sequenceOfStatements ]
         "end" "if" ";"
   */
     public void ifStatement() {
    	 accept(Token.IF, "'if' expected");
    	 
    	 condition();
    	 
    	 accept(Token.THEN, "'then' expected");
    	 sequenceOfStatements();
    	 
    	
    	 while(token.code == Token.ELSIF)
    	 {
    		 token = scanner.nextToken();
    		 condition();
    		 accept(Token.THEN, "'then' expected");
    		 sequenceOfStatements();
    	 }
    	 
    	 if(token.code == Token.ELSE)
    	 {   
    		 token = scanner.nextToken();
    		 sequenceOfStatements();
    	 }
    	 
    	 accept(Token.END, "'end' expected");
    	 accept(Token.IF, "'if' expected");
    	 accept(Token.SEMI, "';' expected in ifstatement");
    	 
    	
     }
   /*
   exitStatement = "exit" [ "when" condition ] ";"
   */
     private void exitStatement() {
    	 accept(Token.EXIT, "'exit' expected");
    	 
    	 if(token.code == Token.WHEN)
    	 {
    		 scanner.nextToken();
    		 condition();
    	 }
    	 
    	 accept(Token.SEMI, "';' expected exitStatement");
     }
   /*
   assignmentStatement = <variable>name ":=" expression ";"

   procedureCallStatement = <procedure>name ";"
   
   */
   private void assignmentOrCallStatement(){
	  
      SymbolEntry entry = name();
      if (token.code == Token.GETS){
		 Set<Integer> temp = new HashSet<Integer>();
		 temp.addAll(Arrays.asList(SymbolEntry.VAR,SymbolEntry.PARAM));
		 acceptRole(entry,temp,"must be a variable or parameter name: "+token.code+" "+entry.role);
		 token = scanner.nextToken();
         expression();
      }else{
		  acceptRole(entry,SymbolEntry.PROC,"must be a procedure name");
	  }
      accept(Token.SEMI, "semicolon expected");
   }

   /*
   condition = <boolean>expression
   */
   private void condition(){
	  	  

      expression();
   }

   /*
   expression = relation [{ "and" relation } | {"or" relation}]
   */
   private void expression(){
	  	  

      relation();
      if (token.code == Token.AND)
         while (token.code == Token.AND){
            token = scanner.nextToken();
            relation();
         }
      else if (token.code == Token.OR)
         while (token.code == Token.OR){
            token = scanner.nextToken();
            relation();
         }
   }

   /*
   relation = simpleExpression [ relationalOperator simpleExpression ]
   */
   private void relation(){
	   	  

      simpleExpression();
      if (relationalOperator.contains(token.code)){
         token = scanner.nextToken();
         simpleExpression();
      }
   }

   /*
  simpleExpression =
         [ unaryAddingOperator ] term { binaryAddingOperator term }
   */
   private void simpleExpression(){
	   	  

      if (addingOperator.contains(token.code))
         token = scanner.nextToken();
      term();
      while (addingOperator.contains(token.code)){
         token = scanner.nextToken();
         term();
      }
   }

   /*
   term = factor { multiplyingOperator factor }
   */
      private void term() {
		  	  

    	  factor();
    	  
    	  while(multiplyingOperator.contains(token.code))
    	  {
    		  token = scanner.nextToken();
    		  factor();
    	  }
      }
   /*
   factor = primary [ "**" primary ] | "not" primary
   */
      private void factor() {
    	  	  

    	  if(token.code == Token.INT || token.code == Token.CHAR || token.code == Token.ID || token.code == Token.L_PAR)
    	 {
    	     primary();
    		  
    		 if(token.code == Token.EXPO)
    	    {  
    		  token = scanner.nextToken();
    		  primary(); 
    	    }
    		
    		 /*
    		 else if(token.code <= 46 && token.code >= 0 && token.code != Token.EXPO )
    		 {
    			 fatalError("error in factor1");
    		 }
    		 */
    	 }
    	  
    	  
    	  else if(token.code == Token.NOT)
    	  {
    		  token = scanner.nextToken();
    		  primary(); 
    	  }
    	  
    	  else
    	  {
    		  fatalError("error in factor2");
    	  }
      }
   /*
   primary = numericLiteral | name | "(" expression ")"
   */
   void primary(){
	   
      switch (token.code){
         case Token.INT:
         case Token.CHAR:
			token = scanner.nextToken();
			break;
         case Token.ID:
            SymbolEntry entry = name();
			Set<Integer> temp = new HashSet<Integer>();
			temp.addAll(Arrays.asList(SymbolEntry.VAR,SymbolEntry.PARAM,SymbolEntry.CONST));
			acceptRole(entry,temp,"must be a variable,constant,or parameter name");

            break;
         case Token.L_PAR:
            token = scanner.nextToken();
            expression();
            accept(Token.R_PAR, "')' expected");
            break;
         default: fatalError("error in primary");
      }
   }

   /*
   name = identifier [ indexedComponent | selectedComponent ]
   */
   private SymbolEntry name(){
	  
      SymbolEntry entry = findId();
      if (token.code == Token.L_PAR)
         indexedComponent();
      return entry;
   }

   /*
   indexedComponent = "(" expression  { "," expression } ")"
   */
   private void indexedComponent() {
	   	  

	   accept(Token.L_PAR, "'(' expected");
	   expression();
	   
	   while(token.code == Token.COMMA)
	   {
		   token = scanner.nextToken();
		   expression();
	   }
	   
	   accept(Token.R_PAR, "')' expected2 " + token.code);
   }
}
