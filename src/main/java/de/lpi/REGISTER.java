package de.lpi;

class REGISTER
{
   static int LOAD = 0;
   static int STORE = 1;
   static int ADD = 2;
   static int AND = 3;
   static int JUMP = 4;
   static int JUMPZ = 5;
   static int COMP = 6;
   static int RSHIFT = 7;   

   private int DR;
   private int IR;
   private int PC;
   private int AR;
   private int AC; 
   
   /** Utility field used by bound properties. */
   private java.beans.PropertyChangeSupport propertyChangeSupport =  new java.beans.PropertyChangeSupport(this);
   
   static String toCommand(int c)
    {
	if(c==LOAD)      { return "LOAD"; }
	else if(c==STORE){return "STORE"; }
	else if(c==ADD) { return "ADD"; }
	else if(c==AND){return "AND"; }
	else if(c==JUMP) { return "JUMP"; }
	else if(c==JUMPZ){return "JUMPZ"; }
	else if(c==COMP) { return "COMP"; }
	else if(c==RSHIFT){return "RSHIFT"; }
	else return "NOCommand";
    }
   static int ComtoInt(String c)
    {
	if(c.equals("LOAD"))      { return LOAD; }
	else if(c.equals("STORE")){return STORE; }
	else if(c.equals("ADD")) { return ADD; }
	else if(c.equals("AND")){return AND; }
	else if(c.equals("JUMP")) { return JUMP; }
	else if(c.equals("JUMPZ")){return JUMPZ; }
	else if(c.equals("COMP")) { return COMP; }
	else if(c.equals("RSHIFT")){return RSHIFT; }
	else return -1;
    }	
	
   public String Show()
    {
        String ret="";
	ret=ret+"DR: ";
        ret=ret+Integer.toString(DR);
	ret=ret+" DR Opcode: ";
        ret=ret+toCommand(DR_Opcode());
	ret=ret+ " DR Addr: ";
	ret=ret+DR_Addr();
        ret=ret+" IR: ";
	ret=ret+toCommand(IR);
	ret=ret+" PC: ";
        ret=ret+Integer.toString(PC);
        ret=ret+" AR: ";
	ret=ret+Integer.toString(AR);
	ret=ret+" AC: ";
	ret=ret+Integer.toString(AC);
        return ret;
    }
   int DR_Opcode()
    {
	return (DR >>> 13) & 7; 
    }
   int DR_Addr()
    {
	return (DR & 8191);
    }
   static int Opcode(int v)
    {
	return (v >>> 13) & 7;
    } 
   static int Addr(int v)
    {
	return (v & 8191);
    }
   boolean Equal(REGISTER r)
    {
	return (DR == r.DR) && (IR == r.IR) && (PC == r.PC) && (AR == r.AR) && (AC == r.AC);
    }
   void Set(REGISTER r)
    {
       DR = r.DR;
       IR = r.IR;
       PC = r.PC;
       AR = r.AR;
       AC = r.AC;
    }
    
    /** Add a PropertyChangeListener to the listener list.
     * @param l The listener to add.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }
    
    /** Removes a PropertyChangeListener from the listener list.
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }
    
    /** Getter for property DR.
     * @return Value of property DR.
     */
    public int getDR() {
        return DR;
    }
    
    /** Setter for property DR.
     * @param DR New value of property DR.
     */
    public void setDR(int DR) {
        int oldDR = this.DR;
        this.DR = DR;
        propertyChangeSupport.firePropertyChange("DR", new Integer(oldDR), new Integer(DR));
    }
    
    /** Getter for property IR.
     * @return Value of property IR.
     */
    public int getIR() {
        return IR;
    }
    
    /** Setter for property IR.
     * @param IR New value of property IR.
     */
    public void setIR(int IR) {
        int oldIR = this.IR;
        this.IR = IR;
        propertyChangeSupport.firePropertyChange("IR", new Integer(oldIR), new Integer(IR));
    }
    
    /** Getter for property PC.
     * @return Value of property PC.
     */
    public int getPC() {
        return PC;
    }
    
    /** Setter for property PC.
     * @param PC New value of property PC.
     */
    public void setPC(int PC) {
        if(PC<0) PC=0;
        PC=(PC & 8191);
        int oldPC = this.PC;
        this.PC = PC;
        propertyChangeSupport.firePropertyChange("PC", new Integer(oldPC), new Integer(PC));
    }
    
    /** Getter for property AR.
     * @return Value of property AR.
     */
    public int getAR() {
        return AR;
    }
    
    /** Setter for property AR.
     * @param AR New value of property AR.
     */
    public void setAR(int AR) {
        if(AR<0)AR=0;
        AR=(AR & 8191);
        int oldAR = this.AR;
        this.AR = AR;
        propertyChangeSupport.firePropertyChange("AR", new Integer(oldAR), new Integer(AR));
    }
    
    /** Getter for property AC.
     * @return Value of property AC.
     */
    public int getAC() {
        return AC;
    }
    
    /** Setter for property AC.
     * @param AC New value of property AC.
     */
    public void setAC(int AC) {
        AC=(AC & 65535);
        int oldAC = this.AC;
        this.AC = AC;
        propertyChangeSupport.firePropertyChange("AC", new Integer(oldAC), new Integer(AC));
    }
    
   REGISTER()
    {
       DR = 0;
       IR = 0;
       PC = 0;
       AR = 0;
       AC = 0; 
    }
   REGISTER(REGISTER r)
    { 
      Set(r);
    }
    int saveRegister(java.io.ObjectOutputStream out)
  {
      try{     
        out.writeInt(DR);
        out.writeInt(IR);
        out.writeInt(PC);
        out.writeInt(AR);
        out.writeInt(AC);
        return 0;
      } catch(java.io.IOException e)
      {
          return -1;
      }
  }
  int loadRegister(java.io.ObjectInputStream in)
  {
      try{     
        DR=in.readInt();
        IR=in.readInt();
        PC=in.readInt();
        AR=in.readInt();
        AC=in.readInt();
        return 0;
      } catch(java.io.IOException e)
      {
          return -1;
      }
  }
} 
