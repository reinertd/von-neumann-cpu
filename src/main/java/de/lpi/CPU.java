package de.lpi;

import java.util.*;
import java.lang.*;

class CPU implements java.lang.Runnable
{  
  private REGISTER reg;
  private REGISTER oldreg;
  private MEMORY mem;
  private ALU alu;
  private ArrayList Breakpoints;
  private char mode;
  private long speed;
  private Object ModeLock;
  private Object SpeedLock;
  private boolean running;
  
  /** Holds value of property status. */
  private String status;
  
  /** Utility field used by bound properties. */
  private java.beans.PropertyChangeSupport propertyChangeSupport =  new java.beans.PropertyChangeSupport(this);
  
  /** Holds value of property startpos. */
  private int startpos;
  
  /** Holds value of property endpos. */
  private int endpos;
  
    CPU(MEMORY m)
    {
	mem = m;
        reg = new REGISTER();
        oldreg = new REGISTER(reg);
        alu = new ALU(reg);
	Breakpoints = new ArrayList();
        mode = 's';  
        speed=0;
        ModeLock = new Object();
        SpeedLock = new Object();
        running=false;
        setStatus(java.util.ResourceBundle.getBundle("Messages").getString("msg_stopped"));
        setStartpos(0);
        setEndpos(8191);
    }
    
    synchronized int saveState(String file)
  
    {
        setStatus(java.util.ResourceBundle.getBundle("Messages").getString("msg_saving"));
        int ret=0;
        try{
            java.io.FileOutputStream ostream = new java.io.FileOutputStream(file);
            java.io.ObjectOutputStream p = new java.io.ObjectOutputStream(ostream);
            if(reg.saveRegister(p)==-1)
                ret=-1;
            if(ret!=-1)
                if(mem.saveMem(p)==-1)
                    ret=-1;
            p.writeObject(new Long(getSpeed()));
            p.writeObject(new Integer(getStartpos()));
            p.writeObject(new Integer(getEndpos()));
            p.writeObject(Breakpoints);
            p.flush();
            ostream.close();
            if(ret!=-1)
                setStatus(java.util.ResourceBundle.getBundle("Messages").getString("msg_saved"));
            else
                setStatus(java.util.ResourceBundle.getBundle("Messages").getString("msg_error_on_save"));
            return ret;
        } catch(java.io.FileNotFoundException e) { setStatus(java.util.ResourceBundle.getBundle("Messages").getString("msg_error_on_save_not_found")); return -1; }
        catch(java.io.IOException e) { setStatus(java.util.ResourceBundle.getBundle("Messages").getString("msg_error_on_save_io")); return -1; }   
    }

    synchronized int loadState(String file)
    {
        setStatus(java.util.ResourceBundle.getBundle("Messages").getString("msg_loading"));
        int ret=0;
        try{
            java.io.FileInputStream istream = new java.io.FileInputStream(file);
            java.io.ObjectInputStream p = new java.io.ObjectInputStream(istream);
            if(reg.loadRegister(p)==-1)
                ret=-1;
            if(ret!=-1)
                if(mem.loadMem(p)==-1)
                    ret=-1;
            setSpeed(((Long)p.readObject()).intValue());
            setStartpos(((Integer)p.readObject()).intValue());
            setEndpos(((Integer)p.readObject()).intValue());
            Breakpoints=(ArrayList)p.readObject();
            istream.close();
            if(ret!=-1)
                setStatus(java.util.ResourceBundle.getBundle("Messages").getString("msg_loadet"));
            else
                setStatus(java.util.ResourceBundle.getBundle("Messages").getString("msg_error_on_load"));
            return ret;
        } catch(java.io.FileNotFoundException e) { setStatus(java.util.ResourceBundle.getBundle("Messages").getString("msg_error_on_load_not_found")); return -1; }
        catch(java.io.IOException e) { setStatus(java.util.ResourceBundle.getBundle("Messages").getString("msg_error_on_load_io")); return -1; }     
        catch(java.lang.ClassNotFoundException e) { setStatus(java.util.ResourceBundle.getBundle("Messages").getString("msg_error_on_load_class")); return -1; }     
    }
    
    void ShowRegs()
    {
      reg.Show();  
    }   
    
    public MEMORY getMem()
    {
        return mem;
    }
    
    public REGISTER getRegister()
    {
        return reg;
    }
    
    boolean isBreakpoint(int i)
    {
     Integer h = new Integer(i); 
     return (Breakpoints.indexOf(h)!=-1);
    }
    
    void insertBreakpoint(int i)
    {
        synchronized(Breakpoints)
        {
            Integer h = new Integer(i);
            if(Breakpoints.indexOf(h)==-1)
            {
                Breakpoints.add(h);
                setStatus(java.util.ResourceBundle.getBundle("Messages").getString("msg_break_set")+" "+Integer.toString(i)+" .");
                propertyChangeSupport.firePropertyChange("breakpoints", h, null);
            }
            else
                 setStatus(java.util.ResourceBundle.getBundle("Messages").getString("msg_break_is")+" "+Integer.toString(i)+" .");
        }
    }  
    void removeBreakpoint(int i)
    {
        synchronized(Breakpoints)
        {
            Integer h = new Integer(i);
            try
                { Breakpoints.remove(Breakpoints.indexOf(h)); 
                  propertyChangeSupport.firePropertyChange("breakpoints", h, null);
                  setStatus(java.util.ResourceBundle.getBundle("Messages").getString("msg_break_del")+" "+Integer.toString(i)+" .");                  
            }
            catch(IndexOutOfBoundsException e)
                { setStatus(java.util.ResourceBundle.getBundle("Messages").getString("msg_error_no_breakpoint"));}
        }
    }

    /* Startet die CPU: mode = c fuer Continue, mode = v fuer Continue und Zeige Register, mode = s fuer Step */
    /* line = Startzeile , Speed = Zeit zwischen 2 Zeilen */

  int run(char m,int line,long s)
  {
      Thread t;
      synchronized(this)
      {
        reg.setPC(line & 8191);
        setMode(m);
        setSpeed(s);
        t=new Thread(this);
      }
      t.start();
      try{
      t.join();
      } catch(java.lang.InterruptedException e) {}
      return reg.getPC();
  }
  
  void run_async()
  {
      Thread t=new Thread (this);
      t.start();
  }
  
  public void setSpeed(long s)
  {
      synchronized(SpeedLock)
      {
        long oldspeed = this.speed;
        speed=s;
        propertyChangeSupport.firePropertyChange("speed", new Long(oldspeed), new Long(speed));        
      }
  }
  
  public long getSpeed()
  {
      return speed;
  }
  
  public boolean isRunning()
  {
      return running;
  }
  
  public void setMode(char m)
  {
      synchronized(ModeLock)
      {
        mode=m;
      }
  }
    
  public void run()
  {
      synchronized(this) {
	long i,ii;
	double a,b;
        running=true;
        setStatus(java.util.ResourceBundle.getBundle("Messages").getString("msg_running"));
        try
        {
	do
	    {   
                oldreg.Set(reg); 
		reg.setAR(reg.getPC()); Thread.sleep(speed);
		reg.setDR(mem.Getmem(reg.getAR())); Thread.sleep(speed);
		reg.setPC((reg.getPC() +1) & 8191); Thread.sleep(speed);
		reg.setIR(reg.DR_Opcode()); Thread.sleep(speed);
		if     (reg.getIR() == reg.RSHIFT){ alu.rshift();}
		else if(reg.getIR() == reg.COMP)  { alu.comp();}
		else if(reg.getIR() == reg.JUMP)  { reg.setPC(reg.DR_Addr());}
		else if(reg.getIR() == reg.JUMPZ) { if( (reg.getAC() == 0) ) reg.setPC(reg.DR_Addr());}
		else {
		    reg.setAR(reg.DR_Addr()); Thread.sleep(speed);
		    if(reg.getIR() == reg.STORE){
                          reg.setDR(reg.getAC()); Thread.sleep(speed);
                          mem.Setmem(reg.getAR(),reg.getDR()); }
		    else {
			reg.setDR(mem.Getmem(reg.getAR())); Thread.sleep(speed);
			if     (reg.getIR() == reg.LOAD){ reg.setAC(reg.getDR()); }
			else if(reg.getIR() == reg.ADD) { alu.add(); }
			else if(reg.getIR() == reg.AND) { alu.and(); }
			else setStatus(java.util.ResourceBundle.getBundle("Messages").getString("msg_error_wrong_com"));
		    }
		}
                Thread.sleep(speed);
		Integer h = new Integer(reg.getPC());
                Integer oldpc = new Integer(oldreg.getPC());
                if(oldpc.intValue()==getEndpos())
                    reg.setPC(oldpc.intValue());
		if(oldreg.Equal(reg) || (Breakpoints.indexOf(h)!=-1)||(oldpc.intValue()==getEndpos())) 
		    /* Pruefe ob sich keines der Register geaendert hat oder ein Breakpoint erreicht wurde und stoppe in diesem Fall */
		    { 
			mode='s';
		    }
		
		
		if(mode == 'v')      /* Zeige Registerinhalt */
		    {
			setStatus(java.util.ResourceBundle.getBundle("Messages").getString("msg_register")+"\n"+reg.Show());
		    }
	    }
	while( (mode == 'c') || (mode=='v') );
        } catch(java.lang.InterruptedException e)
        { }
        running=false;
        setStatus(java.util.ResourceBundle.getBundle("Messages").getString("msg_stopped"));
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
 
 /** Getter for property status.
  * @return Value of property status.
  */
 public String getStatus() {
     return status;
 }
 
 /** Setter for property status.
  * @param status New value of property status.
  */
 public void setStatus(String status) {
     String oldStatus = this.status;
     this.status = status;
     propertyChangeSupport.firePropertyChange("status", oldStatus, status);
 }
 
 /** Getter for property startpos.
  * @return Value of property startpos.
  */
 public int getStartpos() {
     return startpos;
 }
 
 /** Setter for property startpos.
  * @param startpos New value of property startpos.
  */
 public void setStartpos(int startpos) {
     int oldStartpos = this.startpos;
     this.startpos = startpos;
     propertyChangeSupport.firePropertyChange("startpos", new Integer(oldStartpos), new Integer(startpos));
 }
 
 /** Getter for property endpos.
  * @return Value of property endpos.
  */
 public int getEndpos() {
     return endpos;
 }
 
 /** Setter for property endpos.
  * @param endpos New value of property endpos.
  */
 public void setEndpos(int endpos) {
     int oldEndpos = this.endpos;
     this.endpos = endpos;
     propertyChangeSupport.firePropertyChange("endpos", new Integer(oldEndpos), new Integer(endpos));
 }
 
}