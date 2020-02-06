public class SymbolEntry extends Object{

   public static final int NONE = 0;
   public static final int CONST = 1;
   public static final int PARAM = 2;
   public static final int PROC = 3;
   public static final int TYPE = 4;
   public static final int VAR = 5;
   private boolean include_r = false;
   private boolean include_s = false;
   private boolean include_t = false;

   private String name;
   public int role;
   public SymbolEntry next;

   public SymbolEntry(String id,boolean include_r,boolean include_s,boolean include_t){
      name = id;
      role = NONE;
      next = null;
	  this.include_r = include_r;
	  this.include_s = include_s;
	  this.include_t = include_t;
   }
   
   public String getName(){
      return name;
   }

   public String toString(){
	  if(this.include_r){
		return "Name: " + name + "\n" + "Role: " + roleToString();
	  }
	
	return "Name: " + name;  
	  
   }

   public void setRole(int r){
      role = r;
      if (next != null)
         next.setRole(r);
   }

   public void append(SymbolEntry entry){
      if (next == null)
         next = entry;
      else{
        // next.append(entry);
         SymbolEntry newOne = new SymbolEntry(getName(),this.include_r,this.include_s,this.include_t);
         next = newOne;
      }
   }  

   private String roleToString(){
      String s = "";
      switch (role){
         case NONE:  s = "None";      break;
         case CONST: s = "CONSTANT";  break;
         case PARAM: s = "PARAMETER"; break;
         case PROC:  s = "PROCEDURE"; break;
         case TYPE:  s = "TYPE";      break;
         case VAR:   s = "VARIABLE";  break;
         default:    s = "None";
      }
      return s;
   }

}
