import java.io.*;
import java.lang.*;
import MEMORY;
import CPU;
import CPUFrame;
import java.util.ResourceBundle;

public class usecpu implements java.beans.PropertyChangeListener
{
    private MEMORY mem;
    private CPU cpu;
    private CPUFrame gui;
    private boolean showgui,showtext;
    private String prompt;
    private String filename;
    
  public usecpu(boolean sg,boolean st,String file)
  {
       mem = new MEMORY();
       cpu = new CPU(mem);
       filename=file;
       if(filename==null)
           filename="config.cps";
       cpu.loadState(filename);
       gui = new CPUFrame(this);
       cpu.addPropertyChangeListener(this);
       showgui=sg;
       showtext=st;
       prompt="";
  }
    
  public void run()
  {
       if(showgui) gui.show();
       if(showtext) TextInputLoop();
  }
  
  public void dispose()
  {
      cpu.saveState("config.cps");
      gui.dispose();
      if(showtext) System.out.println(ResourceBundle.getBundle("Messages").getString("msg_goodby"));
      System.exit(0);
  }
  
  public CPU getCpu()
  {
      return cpu;
  }
  
  public void TextInputLoop()
  {
        int i=-1;
	int l=0;
        int ret;
	String s;
	char c; /* Naechster Befehl */
	int a1; /* Erstes Argument */
	int a2; /* Zweites Argument */

	try {
	BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        
        System.out.println(ResourceBundle.getBundle("Messages").getString("msg_hello"));
	do
	    {
                prompt="USECPU> ";
		System.out.print(prompt);
		s = in.readLine(); /* Lese Befehl */
                prompt="";
		/* Parse Befehl */
		try{
		if(s.indexOf(' ') != -1)
		    {
			c = s.charAt(0);
			if(s.indexOf(',') != -1)
			    {
				a1 = Integer.parseInt(s.substring(s.indexOf(' ')+1,s.indexOf(',')));
				a2 = Integer.parseInt(s.substring(s.indexOf(',')+1));
			    }
			else { a1 = Integer.parseInt(s.substring(s.indexOf(' ')+1)); a2 = -1; }
		    }
		else { c = s.charAt(0); a1 = -1; a2 = -1; }
		}catch(StringIndexOutOfBoundsException e){ c = ' '; a1=-1; a2=-1; }
		catch(NumberFormatException e2){ c = ' '; a1=-1; a2 = -1; };

		switch(c)
		    {
		    case 'e':
			if(a1 == -1) a1 = 0;
			i = a1-1;
			System.out.println(ResourceBundle.getBundle("Messages").getString("msg_type_in_commands"));
			do {
			    i++;
                            prompt=Integer.toString(i)+" : ";
			    System.out.print(prompt);
			    s= in.readLine();
                            prompt="";
                            ret = mem.Setmem(i,s);

                            if((ret == -1) & !(s.equals("") | s.equals("END")) )
                                {
                                  System.out.println(ResourceBundle.getBundle("Messages").getString("msg_error_wrong_com"));
                                  i--; ret = 0;
                                }
			} while(ret != -1);
			if(s.equals("END"))
			    {
				System.out.println(ResourceBundle.getBundle("Messages").getString("msg_adding_end"));
				mem.Setmem(i,"JUMP "+Integer.toString(i));
			    }
			break;
		    case 'm':
			if(a1 == -1) a1 = 0;
			if(a2 == -1) a2 = i;
			System.out.println(ResourceBundle.getBundle("Messages").getString("msg_memory"));
			mem.Showmem(a1,a2);
			break;
		    case 'c':
		    case 'v':
		    case 's':
			if(a1 == -1) a1 = l;
			if(a2 == -1) a2 = 0;
			System.out.print(ResourceBundle.getBundle("Messages").getString("msg_start_at_line")); System.out.println(a1);
			l = cpu.run(c,a1,a2);
			System.out.println(ResourceBundle.getBundle("Messages").getString("msg_stopped_at_line")); System.out.println(l);
			break;
		    case 'b':
			if(a1 == -1) System.out.println(ResourceBundle.getBundle("Messages").getString("msg_error_no_line"));
			else
			    {
				cpu.insertBreakpoint(a1);
			    }
			break;
		    case 'd':
			if(a1 == -1) System.out.println(ResourceBundle.getBundle("Messages").getString("msg_error_no_line"));
			else
			    {
				cpu.removeBreakpoint(a1);
			    }
			break;
                    case 'r': cpu.ShowRegs();
                        break;
		    case 'q': 
			break;
		    default:
                        System.out.println(ResourceBundle.getBundle("Messages").getString("msg_help_commands"));
                        break;
		    }
                //gui.invalidate();
	    } while(  c != 'q' );
            this.dispose();
	} catch (IOException e)
	    {
		System.out.println(ResourceBundle.getBundle("Messages").getString("msg_error_stream_input"));
	    }
  }
  
  public static void main(String args[])
    {
        String file=null;
        if(args.length > 0)
            file=args[0];
        usecpu u=new usecpu(
             ResourceBundle.getBundle("Config").getString("ShowGui").equals("1"),
             ResourceBundle.getBundle("Config").getString("ShowText").equals("1"),
             file
         );
        u.run();
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent propertyChangeEvent) {
        if(propertyChangeEvent.getPropertyName().equals("status"))
            if(showtext) System.out.print((String)(propertyChangeEvent.getNewValue())+"\n"+prompt);
    }
    
}
