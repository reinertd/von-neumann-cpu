import java.lang.*;
import REGISTER;

class MEMORY
{
    
    /** Holds value of property mem. */
    private int[] mem;
    
    /** Utility field used by bound properties. */
    private java.beans.PropertyChangeSupport propertyChangeSupport =  new java.beans.PropertyChangeSupport(this);
    
  MEMORY()
    {
      mem = new int[8*1024];
    } 
    
  int saveMem(java.io.ObjectOutputStream out)
  {
      try{     
        out.writeObject(mem);
        return 0;
      } catch(java.io.IOException e)
      {
          return -1;
      }
  }
  int loadMem(java.io.ObjectInputStream in)
  {
      try{     
        mem=(int[])in.readObject();
        return 0;
      } catch(java.io.IOException e)
      {
          return -1;
      }
      catch(java.lang.ClassNotFoundException e)
      {
          return -1;
      }      
  }
  int Getmem(int addr)
    {
	return getMem(addr);
    }
  void Setmem(int addr,int val)
    {
	setMem(addr,val);
    }
  int Setmem(int addr,String s)
    {
        int val;
        int ret = 0;
	try{
	if(s.indexOf(' ') != -1){
	    if(REGISTER.ComtoInt(s.substring(0,s.indexOf(' ')).toUpperCase()) != -1)
		{ 
                    //System.out.println(s.substring(s.indexOf(' ')+1));
		    val = Integer.parseInt(s.substring(s.indexOf(' ')+1)) + (REGISTER.ComtoInt(s.substring(0,s.indexOf(' ')).toUpperCase()) << 13);
            }  else { val = 0; ret = -2; }
	} else val = Integer.parseInt(s);
        if(ret>=0) setMem(addr,val);
	} catch(NumberFormatException n)
	    {
		return -1;
	    }
	return ret;
    }
  void Showmem(int from, int to)
    { 
	int i;
	for(i=from;i<=to;i++)
	    {
		System.out.print(i);
		System.out.print(" : ");
		System.out.print(REGISTER.toCommand(REGISTER.Opcode(getMem(i))));
		System.out.print(" ");
		System.out.print(REGISTER.Addr(getMem(i)));
		System.out.print(" : ");
		System.out.println(getMem(i));
	    }
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
    
    /** Indexed getter for property mem.
     * @param index Index of the property.
     * @return Value of the property at <CODE>index</CODE>.
     */
    public int getMem(int index) {
        
        return mem[index];
    }

    /** Indexed getter for property mem.
     * @param index Index of the property.
     * @return Value of the property at <CODE>index</CODE> as Commandstring.
     */
   public String getMemAsCommand(int index) {
       return REGISTER.toCommand(REGISTER.Opcode(getMem(index)))+" "+REGISTER.Addr(getMem(index));
    }
     
    /** Indexed setter for property mem.
     * @param index Index of the property.
     * @param mem New value of the property at <CODE>index</CODE>.
     */
    public void setMem(int index, int mem) {
        this.mem[index] = mem;
        propertyChangeSupport.firePropertyChange("mem", new Integer(index), null );
    }
    
}
