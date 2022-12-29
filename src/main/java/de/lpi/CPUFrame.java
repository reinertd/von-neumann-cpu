package de.lpi;/*
 * de.reinert.CPUFrame.java
 *
 * Created on 23. Oktober 2001, 14:10
 */
import javax.swing.table.AbstractTableModel;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.net.*;
import java.io.*;
/**
 *
 * @author  root
 */
public class CPUFrame extends javax.swing.JFrame implements java.beans.PropertyChangeListener {
    
    class MyModel extends AbstractTableModel implements java.beans.PropertyChangeListener
    {
            private int anf;
            private int len = 12;
            private boolean asstr;
            private Component owner;
            MyModel(boolean str,Component aowner)
            {
                asstr=str;
                owner=aowner;
                cpu.getMem().addPropertyChangeListener(this);
                cpu.getRegister().addPropertyChangeListener(this);
                cpu.addPropertyChangeListener(this);
            }
            public String getColumnName(int col) {
                String title;
                switch(col) {
                    case 0:  
                    case 1: title=" "; break;
                    case 2:  title=java.util.ResourceBundle.getBundle("Messages").getString("msg_value"); break;
                    default: title="";
                }
                return title;
            }
            
            public int getLen()
            {
                return len;
            }
            
            public void refreshAll()
            {
                for(int i=0;i<len;i++)
                {
                   fireTableCellUpdated(i,0); 
                   fireTableCellUpdated(i,1); 
                   fireTableCellUpdated(i,2); 
                }
            }
            
            public void propertyChange(java.beans.PropertyChangeEvent evt)
            {
                String property=evt.getPropertyName();
                if(property.equals("mem"))
                {
                    int index=((Integer)evt.getOldValue()).intValue();
                    if((index>=getAnf())&&(index<=getEnd()))
                        fireTableCellUpdated(index-getAnf(),2);
                } else
                if(asstr&&property.equals("PC"))
                {
                   int a=cpu.getRegister().getPC()-(len/2)+1;
                   setAnf(a);
                   PCMemTable.setRowSelectionInterval(cpu.getRegister().getPC()-getAnf(),
                                                      cpu.getRegister().getPC()-getAnf());
                } else
                if((!asstr)&&property.equals("AR"))
                {
                   int a=cpu.getRegister().getAR()-(len/2)+1;
                   setAnf(a);
                   ARMemTable.setRowSelectionInterval(cpu.getRegister().getAR()-getAnf(),
                                                      cpu.getRegister().getAR()-getAnf());
                }
                if(property.equals("startpos")||property.equals("endpos"))
                {
                    fireTableCellUpdated(((Integer)evt.getOldValue()).intValue()-getAnf(),1);
                    fireTableCellUpdated(((Integer)evt.getNewValue()).intValue()-getAnf(),1);
                }  
                if(property.equals("breakpoints"))
                {
                    fireTableCellUpdated(((Integer)evt.getOldValue()).intValue()-getAnf(),1);
                }
            }
            
            public int getAnf()
            {
               return anf;
            }
            public void setAnf(int a)
            {
                if(a<0) a=0;
                if(a>((8*1024)-len)) a=(8*1024)-len;
                if(anf!=a)
                {
                    anf=a;
                    refreshAll();
                }
            }
            public int getEnd()
            {
                return anf+len-1;
            }
            public int getRowCount() {
                return len;
            }
            public int getColumnCount() {
                return 3;
            }
            public Object getValueAt(int row,int col) {
                int a;
                if(asstr)
                    a=cpu.getRegister().getPC()-(len/2)+1;
                else
                    a=cpu.getRegister().getAR()-(len/2)+1;
                setAnf(a);
                Object val;
                switch(col) {
                    case 0:  val=new Integer(getAnf()+row); break;
                    case 1: if(cpu.getStartpos()==(getAnf()+row))
                                val=run_smallImage;
                            else if(cpu.getEndpos()==(getAnf()+row))
                                val=stop_smallImage; 
                            else if(cpu.isBreakpoint(getAnf()+row))
                                val=break_smallImage;                     
                            else val=empty_smallImage; break;
                    case 2:  if(asstr)
                                val=cpu.getMem().getMemAsCommand(getAnf()+row); 
                             else
                                val=new Integer(cpu.getMem().getMem(getAnf()+row)); 
                            break;
                    default: val=new Integer(0);
                }
                return val;
            }
            
            public Class getColumnClass(int col)
            {
               switch(col) {
                    case 0:  return Integer.class;
                    case 1:  return ImageIcon.class;
                    case 2:  if(asstr)
                                return String.class; 
                             else
                                return Integer.class; 
                            
                    default: return Integer.class;
                } 
            }
            
            public boolean isCellEditable(int row,int col) {
                if(col==0)
                    return false;
                else
                    return true;
            }
            
            public void setValueAt(Object value,int row, int col) {
                if(col==2)
                {
                    if(asstr)
                    {
                    if(cpu.getMem().Setmem(row+getAnf(),(String)value)!=0)
                        JOptionPane.showMessageDialog(owner, 
                          ResourceBundle.getBundle("Messages").getString("msg_error_wrong_com"),
                          ResourceBundle.getBundle("Messages").getString("msg_error"),
                          JOptionPane.ERROR_MESSAGE);
                    }
                    else cpu.getMem().Setmem(row+getAnf(),((Integer)value).intValue());
                }
                if(col==1)
                {
                    if(value.equals(empty_smallImage))
                    {
                        if(cpu.isBreakpoint(row+getAnf()))
                        {
                            cpu.removeBreakpoint(row+getAnf());
                        }
                    }
                    if(value.equals(break_smallImage))
                    {
                        if(!cpu.isBreakpoint(row+getAnf()))
                        {
                            cpu.insertBreakpoint(row+getAnf());
                        }
                    }
                    if(value.equals(run_smallImage))
                    {
                         cpu.setStartpos(row+getAnf());
                    }
                    if(value.equals(stop_smallImage))
                    {
                         cpu.setEndpos(row+getAnf());
                    }
                }
            }
        }

    javax.swing.table.TableModel ARMemModel;
    javax.swing.table.TableModel PCMemModel;
    CPU cpu;
    usecpu mycontrol;
    javax.swing.Action saveAction,loadAction,quitAction,runAction,stepAction,
                        stopAction,contAction,breakAction,unbreakAction,pcUpAction,
                        pcDownAction,arUpAction,arDownAction;
    ImageIcon          saveImage,loadImage,               runImage,stepImage,
                        stopImage,contImage,  breakImage,unbreakImage,upImage,
                        downImage,run_smallImage,stop_smallImage,break_smallImage,
                        empty_smallImage,cpuImage;
    String JarPath;
    
    /** Creates new form CPUFrame */
    public CPUFrame(usecpu acpu) {
        mycontrol=acpu;
        cpu=acpu.getCpu();
        ARMemModel = new MyModel(false,this);
        PCMemModel = new MyModel(true,this);
        cpu.getRegister().addPropertyChangeListener(this);
        cpu.addPropertyChangeListener(this);
        JarPath="./";
        findJarPath();
        loadImages();
        initActions();
        if(cpu.isRunning())
        {
                runAction.setEnabled(false);
                stepAction.setEnabled(false);
                stopAction.setEnabled(true);
                contAction.setEnabled(false);
        }
        else
        {
                runAction.setEnabled(true);
                stepAction.setEnabled(true);
                stopAction.setEnabled(false);
                contAction.setEnabled(true);
        }
        initComponents();
        ARMemTable.getColumnModel().getColumn(0).setPreferredWidth(35);
        PCMemTable.getColumnModel().getColumn(0).setPreferredWidth(35);
        ARMemTable.getColumnModel().getColumn(1).setPreferredWidth(17);
        PCMemTable.getColumnModel().getColumn(1).setPreferredWidth(17);
        JComboBox StartStopBreakSelector = new JComboBox();
        StartStopBreakSelector.addItem(empty_smallImage);
        StartStopBreakSelector.addItem(run_smallImage);
        StartStopBreakSelector.addItem(break_smallImage);
        StartStopBreakSelector.addItem(stop_smallImage);
        ARMemTable.getColumnModel().getColumn(1).setCellEditor(
           new DefaultCellEditor(StartStopBreakSelector));
        PCMemTable.getColumnModel().getColumn(1).setCellEditor(
           new DefaultCellEditor(StartStopBreakSelector));
        PCUpButton.setText(null);
        PCDownButton.setText(null);
        ARUpButton.setText(null);
        ARDownButton.setText(null);
        speedSlider.setValue((int)(100-(((cpu.getSpeed()<=1000)?cpu.getSpeed():1000)/10)));
        loadMenuItem.setIcon(null);
        saveMenuItem.setIcon(null);
        runMenuItem.setIcon(null);
        stopMenuItem.setIcon(null);
        stepMenuItem.setIcon(null);
        contMenuItem.setIcon(null);
        breakMenuItem.setIcon(null);
        unbreakMenuItem.setIcon(null);
        installFileChooserText();
    }
    
    void findJarPath()
    {
        String Classpath=System.getProperty("java.class.path",".");
        String seperator=System.getProperty("path.separator",":");
        do {
            String aktpath;
            if(Classpath.indexOf(seperator)!=-1)
            {
                int i=Classpath.indexOf(seperator);
                aktpath=Classpath.substring(0,i);
                Classpath=Classpath.substring(i+1);
            } else aktpath=Classpath;
            File test=new File(aktpath+"/"+ResourceBundle.getBundle("Config").getString("IconPath")+"run.gif");
            if(test.canRead())
                JarPath=aktpath+"/";
            else
            {
                if(aktpath.endsWith(ResourceBundle.getBundle("Config").getString("JarFile")))
                {
                    test=new File(aktpath);
                    if(test.canRead())
                        JarPath=aktpath.substring(0,
                          aktpath.indexOf(ResourceBundle.getBundle("Config").getString("JarFile")));
                }
            }
            //System.out.println(JarPath);
        } while(Classpath.indexOf(seperator)!=-1);
    }
    
    ImageIcon loadImageIcon(String name)
    {
        try{
        if((new File(JarPath+ResourceBundle.getBundle("Config").getString("IconPath")+name)).canRead())
        {
            return new ImageIcon(JarPath+ResourceBundle.getBundle("Config").getString("IconPath")+name);
        } else
        {
            try{
                ImageIcon retimage=new ImageIcon(
                           new URL("jar:file:"+JarPath+
                           ResourceBundle.getBundle("Config").getString("JarFile")+"!/"
                           +ResourceBundle.getBundle("Config").getString("IconPath")+name));
                return retimage;
            } catch(MalformedURLException e)
            {
               return null; 
            }
        }   
        }catch(Exception e)
        {
            return null;
        }
    }
    
    void loadImages()
    {        
       saveImage=loadImageIcon("save.gif");
       loadImage=loadImageIcon("load.gif");               
       runImage=loadImageIcon("run.gif");
       stepImage=loadImageIcon("step.gif");
       stopImage=loadImageIcon("stop.gif");
       contImage=loadImageIcon("cont.gif");
       breakImage=loadImageIcon("break.gif");
       unbreakImage=loadImageIcon("unbreak.gif");
       empty_smallImage=loadImageIcon("empty_small.gif");
       run_smallImage=loadImageIcon("run_small.gif");
       break_smallImage=loadImageIcon("break_small.gif");
       stop_smallImage=loadImageIcon("stop_small.gif");
       upImage=loadImageIcon("up.gif");
       downImage=loadImageIcon("down.gif");
       cpuImage=loadImageIcon("cpu.gif");
    }
    
    class cpsFileFilter extends javax.swing.filechooser.FileFilter
    {
       public boolean accept(java.io.File f) 
       {
           return (f.toString().endsWith(".cps"))||(f.isDirectory());
       }
       
        public String getDescription() 
        {
            return ResourceBundle.getBundle("Messages").getString("msg_filetype_cps");
        }
    }
    
    void installFileChooserText()
    {
	UIManager.put("FileChooser.acceptAllFileFilterText",
               ResourceBundle.getBundle("Messages").getString("FileChooser.acceptAllFileFilterText"));
        UIManager.put("FileChooser.cancelButtonText",
               ResourceBundle.getBundle("Messages").getString("FileChooser.cancelButtonText"));
        UIManager.put("FileChooser.saveButtonText",
               ResourceBundle.getBundle("Messages").getString("FileChooser.saveButtonText"));
        UIManager.put("FileChooser.openButtonText",
               ResourceBundle.getBundle("Messages").getString("FileChooser.openButtonText"));
        UIManager.put("FileChooser.updateButtonText",
               ResourceBundle.getBundle("Messages").getString("FileChooser.updateButtonText"));
        UIManager.put("FileChooser.helpButtonText",
               ResourceBundle.getBundle("Messages").getString("FileChooser.helpButtonText"));
        UIManager.put("FileChooser.pathLabelText",
               ResourceBundle.getBundle("Messages").getString("FileChooser.pathLabelText"));
        UIManager.put("FileChooser.filterLabelText",
               ResourceBundle.getBundle("Messages").getString("FileChooser.filterLabelText"));
        UIManager.put("FileChooser.foldersLabelText",
               ResourceBundle.getBundle("Messages").getString("FileChooser.foldersLabelText"));
        UIManager.put("FileChooser.filesLabelText",
               ResourceBundle.getBundle("Messages").getString("FileChooser.filesLabelText"));
        UIManager.put("FileChooser.enterFileNameLabelText",
               ResourceBundle.getBundle("Messages").getString("FileChooser.enterFileNameLabelText"));
        UIManager.put("FileChooser.cancelButtonToolTipText",
               ResourceBundle.getBundle("Messages").getString("FileChooser.cancelButtonToolTipText"));
        UIManager.put("FileChooser.saveButtonToolTipText",
               ResourceBundle.getBundle("Messages").getString("FileChooser.saveButtonToolTipText"));
        UIManager.put("FileChooser.openButtonToolTipText",
               ResourceBundle.getBundle("Messages").getString("FileChooser.openButtonToolTipText"));
        UIManager.put("FileChooser.updateButtonToolTipText",
               ResourceBundle.getBundle("Messages").getString("FileChooser.updateButtonToolTipText"));
        UIManager.put("FileChooser.helpButtonToolTipText",
               ResourceBundle.getBundle("Messages").getString("FileChooser.helpButtonToolTipText"));
        UIManager.put("FileChooser.lookInLabelText",
               ResourceBundle.getBundle("Messages").getString("FileChooser.lookInLabelText"));
        UIManager.put("FileChooser.fileNameLabelText",
               ResourceBundle.getBundle("Messages").getString("FileChooser.fileNameLabelText"));
        UIManager.put("FileChooser.filesOfTypeLabelText",
               ResourceBundle.getBundle("Messages").getString("FileChooser.filesOfTypeLabelText"));
        UIManager.put("FileChooser.upFolderToolTipText",
               ResourceBundle.getBundle("Messages").getString("FileChooser.upFolderToolTipText"));
        UIManager.put("FileChooser.upFolderAccessibleName",
               ResourceBundle.getBundle("Messages").getString("FileChooser.upFolderAccessibleName"));
        UIManager.put("FileChooser.homeFolderToolTipText",
               ResourceBundle.getBundle("Messages").getString("FileChooser.homeFolderToolTipText"));
        UIManager.put("FileChooser.homeFolderAccessibleName",
               ResourceBundle.getBundle("Messages").getString("FileChooser.homeFolderAccessibleName"));
        UIManager.put("FileChooser.newFolderToolTipText",
               ResourceBundle.getBundle("Messages").getString("FileChooser.newFolderToolTipText"));
        UIManager.put("FileChooser.newFolderAccessibleNam",
               ResourceBundle.getBundle("Messages").getString("FileChooser.newFolderAccessibleNam"));
        UIManager.put("FileChooser.listViewButtonToolTipText",
               ResourceBundle.getBundle("Messages").getString("FileChooser.listViewButtonToolTipText"));
        UIManager.put("FileChooser.listViewButtonAccessibleName",
               ResourceBundle.getBundle("Messages").getString("FileChooser.listViewButtonAccessibleName"));
        UIManager.put("FileChooser.detailsViewButtonToolTipText",
               ResourceBundle.getBundle("Messages").getString("FileChooser.detailsViewButtonToolTipText"));
        UIManager.put("FileChooser.detailsViewButtonAccessibleName",
               ResourceBundle.getBundle("Messages").getString("FileChooser.detailsViewButtonAccessibleName"));
    }
    
    class cpsFileChooser extends javax.swing.JFileChooser
    {
        cpsFileChooser()
        {

            super(".");
            cpsFileFilter myfilter=new cpsFileFilter();
            this.addChoosableFileFilter(myfilter); 
            this.setFileFilter(myfilter);
            this.removeChoosableFileFilter(this.getAcceptAllFileFilter()); 
        }
        
    }

   void initActions()
   {
       final java.awt.Component parent=this;
       saveAction=new javax.swing.AbstractAction(java.util.ResourceBundle.getBundle("Messages").getString("com_save"),
                                                  saveImage)
                    {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            cpsFileChooser fc=new cpsFileChooser();
                            int returnVal = fc.showSaveDialog(parent);
                            if (returnVal == javax.swing.JFileChooser.APPROVE_OPTION) {
                              java.io.File file = fc.getSelectedFile();
                              String name=file.toString();
                              if(!(name.endsWith(".cps"))) name=name+".cps";
                              cpu.saveState(name);
                            } else {
                                
                            }
                        }
                    };
      saveAction.putValue(Action.SHORT_DESCRIPTION,ResourceBundle.getBundle("Messages").getString("msg_tooltip_save"));
      loadAction=new javax.swing.AbstractAction(java.util.ResourceBundle.getBundle("Messages").getString("com_load"),
                                                loadImage)
                    {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            cpsFileChooser fc=new cpsFileChooser();
                            int returnVal = fc.showOpenDialog(parent);
                            if (returnVal == javax.swing.JFileChooser.APPROVE_OPTION) {
                              java.io.File file = fc.getSelectedFile();
                              cpu.loadState(file.toString());
                              ((CPUFrame)parent).loadAllNew();
                            } else {
                                
                            }
                        }
                    };
      loadAction.putValue(Action.SHORT_DESCRIPTION,ResourceBundle.getBundle("Messages").getString("msg_tooltip_load"));
      quitAction=new javax.swing.AbstractAction(java.util.ResourceBundle.getBundle("Messages").getString("com_quit"),null)
                    {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                             mycontrol.dispose();
                        }
                    };                    
      runAction=new javax.swing.AbstractAction(java.util.ResourceBundle.getBundle("Messages").getString("com_run"),
                                                runImage)
                    {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                             cpu.setMode('c');
                             cpu.getRegister().setPC(cpu.getStartpos());
                             cpu.run_async();
                        }
                    };
      runAction.putValue(Action.SHORT_DESCRIPTION,ResourceBundle.getBundle("Messages").getString("msg_tooltip_run"));
      stepAction=new javax.swing.AbstractAction(java.util.ResourceBundle.getBundle("Messages").getString("com_step"),
                                                stepImage)
                    {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                             cpu.setMode('s');
                             cpu.run_async();
                        }
                    };
      stepAction.putValue(Action.SHORT_DESCRIPTION,ResourceBundle.getBundle("Messages").getString("msg_tooltip_step"));
      stopAction=new javax.swing.AbstractAction(java.util.ResourceBundle.getBundle("Messages").getString("com_stop"),
                                                stopImage)
                    {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                             cpu.setMode('s');
                        }
                    };
      stopAction.putValue(Action.SHORT_DESCRIPTION,ResourceBundle.getBundle("Messages").getString("msg_tooltip_stop"));
      contAction=new javax.swing.AbstractAction(java.util.ResourceBundle.getBundle("Messages").getString("com_cont"),
                                                contImage)
                    {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                             cpu.setMode('c');
                             cpu.run_async();
                        }
                    };
      contAction.putValue(Action.SHORT_DESCRIPTION,ResourceBundle.getBundle("Messages").getString("msg_tooltip_cont"));
      breakAction=new javax.swing.AbstractAction(java.util.ResourceBundle.getBundle("Messages").getString("com_break"),
                                                breakImage)
                    {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                           String number=JOptionPane.showInputDialog(parent,
                                ResourceBundle.getBundle("Messages").getString("msg_type_in_break"),
                                ResourceBundle.getBundle("Messages").getString("com_break"),
                                JOptionPane.PLAIN_MESSAGE);
                            if(number!=null)
                           {
                               try
                               {
                                   int i=Integer.parseInt(number);
                                   cpu.insertBreakpoint(i);
                               }
                                catch(java.lang.NumberFormatException ex)
                                {
                                    javax.swing.JOptionPane.showMessageDialog(parent,
                                        ResourceBundle.getBundle("Messages").getString("msg_error_number_input"),
                                        ResourceBundle.getBundle("Messages").getString("msg_error"),
                                        javax.swing.JOptionPane.ERROR_MESSAGE);
                                }
                           }       
                        }
                    };
      breakAction.putValue(Action.SHORT_DESCRIPTION,ResourceBundle.getBundle("Messages").getString("msg_tooltip_break"));
      unbreakAction=new javax.swing.AbstractAction(java.util.ResourceBundle.getBundle("Messages").getString("com_unbreak"),
                                                unbreakImage)
                    {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                           String number=JOptionPane.showInputDialog(parent,
                                ResourceBundle.getBundle("Messages").getString("msg_type_in_break"),
                                ResourceBundle.getBundle("Messages").getString("com_unbreak"),
                                JOptionPane.PLAIN_MESSAGE);
                           if(number!=null)
                           {
                               try
                               {
                                   int i=Integer.parseInt(number);
                                   cpu.removeBreakpoint(i);
                               }
                                catch(java.lang.NumberFormatException ex)
                                {
                                    javax.swing.JOptionPane.showMessageDialog(parent,
                                        ResourceBundle.getBundle("Messages").getString("msg_error_number_input"),
                                        ResourceBundle.getBundle("Messages").getString("msg_error"),
                                        javax.swing.JOptionPane.ERROR_MESSAGE);
                                }
                           }
                           
                        }
                    };
      unbreakAction.putValue(Action.SHORT_DESCRIPTION,ResourceBundle.getBundle("Messages").getString("msg_tooltip_unbreak"));
      pcUpAction=new javax.swing.AbstractAction(java.util.ResourceBundle.getBundle("Messages").getString("com_up"),
                                                upImage)
                    {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            cpu.getRegister().setPC(
                              cpu.getRegister().getPC()-((MyModel)PCMemModel).getLen());
                        }
                    };
       pcUpAction.putValue(Action.SHORT_DESCRIPTION,ResourceBundle.getBundle("Messages").getString("msg_tooltip_pc_up"));
       pcDownAction=new javax.swing.AbstractAction(java.util.ResourceBundle.getBundle("Messages").getString("com_down"),
                                                downImage)
                    {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            cpu.getRegister().setPC(
                              cpu.getRegister().getPC()+((MyModel)PCMemModel).getLen());
                        }
                    };
       pcDownAction.putValue(Action.SHORT_DESCRIPTION,ResourceBundle.getBundle("Messages").getString("msg_tooltip_pc_down"));
      arUpAction=new javax.swing.AbstractAction(java.util.ResourceBundle.getBundle("Messages").getString("com_up"),
                                                upImage)
                    {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            cpu.getRegister().setAR(
                              cpu.getRegister().getAR()-((MyModel)PCMemModel).getLen());
                        }
                    };
       arUpAction.putValue(Action.SHORT_DESCRIPTION,ResourceBundle.getBundle("Messages").getString("msg_tooltip_ar_up"));
       arDownAction=new javax.swing.AbstractAction(java.util.ResourceBundle.getBundle("Messages").getString("com_down"),
                                                downImage)
                    {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            cpu.getRegister().setAR(
                              cpu.getRegister().getAR()+((MyModel)PCMemModel).getLen());
                        }
                    };
       arDownAction.putValue(Action.SHORT_DESCRIPTION,ResourceBundle.getBundle("Messages").getString("msg_tooltip_ar_down"));

   }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
   
   private void initComponents() {//GEN-BEGIN:initComponents
       jMenuBar1 = new javax.swing.JMenuBar();
       Dateimenu = new javax.swing.JMenu();
       loadMenuItem = new javax.swing.JMenuItem();
       saveMenuItem = new javax.swing.JMenuItem();
       jSeparator1 = new javax.swing.JSeparator();
       exitMenuItem = new javax.swing.JMenuItem();
       RunMenu = new javax.swing.JMenu();
       runMenuItem = new javax.swing.JMenuItem();
       stepMenuItem = new javax.swing.JMenuItem();
       contMenuItem = new javax.swing.JMenuItem();
       stopMenuItem = new javax.swing.JMenuItem();
       DebugMenu = new javax.swing.JMenu();
       breakMenuItem = new javax.swing.JMenuItem();
       unbreakMenuItem = new javax.swing.JMenuItem();
       StatusPanel = new javax.swing.JPanel();
       StatusLabel = new javax.swing.JLabel();
       MainToolbar = new javax.swing.JToolBar();
       MainToolbar.add(loadAction);
       MainToolbar.add(saveAction);
       MainToolbar.add(new JToolBar.Separator());
       MainToolbar.add(runAction);
       MainToolbar.add(stepAction);
       MainToolbar.add(contAction);
       MainToolbar.add(stopAction);
       MainToolbar.add(new JToolBar.Separator());
       MainToolbar.add(breakAction);
       MainToolbar.add(unbreakAction);
       MainToolbar.add(new JToolBar.Separator());
       MainPanel = new javax.swing.JPanel();
       CPUPanel = new javax.swing.JPanel();
       jScrollPane1 = new javax.swing.JScrollPane();
       MemoryPanel = new javax.swing.JPanel();
       PCMem = new javax.swing.JPanel();
       PCMemLeft = new javax.swing.JPanel();
       PCUpButton = new javax.swing.JButton();
       jPanel22 = new javax.swing.JPanel();
       PCMemLab = new javax.swing.JLabel();
       PCMemVal = new javax.swing.JTextField();
       PCDownButton = new javax.swing.JButton();
       PCMemRight = new javax.swing.JScrollPane();
       PCMemTable = new javax.swing.JTable();
       ARMem = new javax.swing.JPanel();
       ARMemLeft = new javax.swing.JPanel();
       ARUpButton = new javax.swing.JButton();
       jPanel21 = new javax.swing.JPanel();
       ARMemLab = new javax.swing.JLabel();
       ARMemVal = new javax.swing.JTextField();
       ARDownButton = new javax.swing.JButton();
       ARMemRight = new javax.swing.JScrollPane();
       ARMemTable = new javax.swing.JTable();
       jScrollPane3 = new javax.swing.JScrollPane();
       RegisterPanel = new javax.swing.JPanel();
       PCPan = new javax.swing.JPanel();
       PCLabel = new javax.swing.JLabel();
       PCVal = new javax.swing.JTextField();
       PCValDez = new javax.swing.JTextField();
       ACPan = new javax.swing.JPanel();
       ACLabel = new javax.swing.JLabel();
       ACVal = new javax.swing.JTextField();
       ACValDez = new javax.swing.JTextField();
       ARPan = new javax.swing.JPanel();
       ARLabel = new javax.swing.JLabel();
       ARVal = new javax.swing.JTextField();
       ARValDez = new javax.swing.JTextField();
       DRPan = new javax.swing.JPanel();
       DRLabel = new javax.swing.JLabel();
       DRVal = new javax.swing.JTextField();
       DRValDez = new javax.swing.JTextField();
       DRValCom = new javax.swing.JTextField();
       IRPan = new javax.swing.JPanel();
       IRLabel = new javax.swing.JLabel();
       IRVal = new javax.swing.JTextField();
       IRValCom = new javax.swing.JTextField();
       jScrollPane2 = new javax.swing.JScrollPane();
       PropertyPanel = new javax.swing.JPanel();
       jPanel3 = new javax.swing.JPanel();
       SpeedLabel = new javax.swing.JLabel();
       slowLabel = new javax.swing.JLabel();
       speedSlider = new javax.swing.JSlider();
       fastLabel = new javax.swing.JLabel();
       jPanel5 = new javax.swing.JPanel();
       jLabel1 = new javax.swing.JLabel();
       StartVal = new javax.swing.JTextField();
       jLabel2 = new javax.swing.JLabel();
       EndVal = new javax.swing.JTextField();
       
       Dateimenu.setMnemonic('d');
       Dateimenu.setText(java.util.ResourceBundle.getBundle("Messages").getString("com_file"));
       loadMenuItem.setToolTipText(java.util.ResourceBundle.getBundle("Messages").getString("msg_tooltip_load"));
       loadMenuItem.setMnemonic('l');
       loadMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
       loadMenuItem.setText(java.util.ResourceBundle.getBundle("Messages").getString("com_load"));
       loadMenuItem.setAction(loadAction);
       Dateimenu.add(loadMenuItem);
       saveMenuItem.setToolTipText(java.util.ResourceBundle.getBundle("Messages").getString("msg_tooltip_save"));
       saveMenuItem.setMnemonic('s');
       saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
       saveMenuItem.setText(java.util.ResourceBundle.getBundle("Messages").getString("com_save"));
       saveMenuItem.setAction(saveAction);
       Dateimenu.add(saveMenuItem);
       Dateimenu.add(jSeparator1);
       exitMenuItem.setMnemonic('b');
       exitMenuItem.setText(java.util.ResourceBundle.getBundle("Messages").getString("com_quit"));
       exitMenuItem.setAction(quitAction);
       Dateimenu.add(exitMenuItem);
       jMenuBar1.add(Dateimenu);
       RunMenu.setMnemonic('a');
       RunMenu.setText(java.util.ResourceBundle.getBundle("Messages").getString("com_programm"));
       runMenuItem.setToolTipText(java.util.ResourceBundle.getBundle("Messages").getString("msg_tooltip_run"));
       runMenuItem.setMnemonic('r');
       runMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F8, 0));
       runMenuItem.setText(java.util.ResourceBundle.getBundle("Messages").getString("com_run"));
       runMenuItem.setAction(runAction);
       RunMenu.add(runMenuItem);
       stepMenuItem.setToolTipText(java.util.ResourceBundle.getBundle("Messages").getString("msg_tooltip_step"));
       stepMenuItem.setMnemonic('s');
       stepMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F7, 0));
       stepMenuItem.setText(java.util.ResourceBundle.getBundle("Messages").getString("com_step"));
       stepMenuItem.setAction(stepAction);
       RunMenu.add(stepMenuItem);
       contMenuItem.setToolTipText(java.util.ResourceBundle.getBundle("Messages").getString("msg_tooltip_cont"));
       contMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
       contMenuItem.setText(java.util.ResourceBundle.getBundle("Messages").getString("com_cont"));
       contMenuItem.setAction(contAction);
       RunMenu.add(contMenuItem);
       stopMenuItem.setToolTipText(java.util.ResourceBundle.getBundle("Messages").getString("msg_tooltip_stop"));
       stopMenuItem.setMnemonic('t');
       stopMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F6, 0));
       stopMenuItem.setText(java.util.ResourceBundle.getBundle("Messages").getString("com_stop"));
       stopMenuItem.setAction(stopAction);
       RunMenu.add(stopMenuItem);
       jMenuBar1.add(RunMenu);
       DebugMenu.setMnemonic('e');
       DebugMenu.setText(java.util.ResourceBundle.getBundle("Messages").getString("com_debug"));
       breakMenuItem.setToolTipText(java.util.ResourceBundle.getBundle("Messages").getString("msg_tooltip_break"));
       breakMenuItem.setMnemonic('s');
       breakMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, 0));
       breakMenuItem.setText(java.util.ResourceBundle.getBundle("Messages").getString("com_break"));
       breakMenuItem.setAction(breakAction);
       DebugMenu.add(breakMenuItem);
       unbreakMenuItem.setToolTipText(java.util.ResourceBundle.getBundle("Messages").getString("msg_tooltip_unbreak"));
       unbreakMenuItem.setMnemonic('a');
       unbreakMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0));
       unbreakMenuItem.setText(java.util.ResourceBundle.getBundle("Messages").getString("com_unbreak"));
       unbreakMenuItem.setAction(unbreakAction);
       DebugMenu.add(unbreakMenuItem);
       jMenuBar1.add(DebugMenu);
       
       setTitle("Von-Neumann-CPU-Simulator");
       setIconImage(cpuImage.getImage());
       addWindowListener(new java.awt.event.WindowAdapter() {
           public void windowClosing(java.awt.event.WindowEvent evt) {
               exitForm(evt);
           }
       });
       
       StatusPanel.setLayout(new java.awt.BorderLayout());
       
       StatusPanel.setBorder(new javax.swing.border.EtchedBorder());
       StatusLabel.setText(java.util.ResourceBundle.getBundle("Messages").getString("msg_state")+cpu.getStatus());
       StatusPanel.add(StatusLabel, java.awt.BorderLayout.CENTER);
       
       getContentPane().add(StatusPanel, java.awt.BorderLayout.SOUTH);
       
       getContentPane().add(MainToolbar, java.awt.BorderLayout.NORTH);
       
       MainPanel.setLayout(new java.awt.BorderLayout());
       
       CPUPanel.setLayout(new java.awt.GridLayout(1, 0));
       
       CPUPanel.setPreferredSize(new java.awt.Dimension(600, 500));
       CPUPanel.setMinimumSize(new java.awt.Dimension(100, 50));
       MemoryPanel.setBorder(new javax.swing.border.TitledBorder(java.util.ResourceBundle.getBundle("Messages").getString("msg_memory")));
       MemoryPanel.setPreferredSize(new java.awt.Dimension(300, 480));
       PCMem.setLayout(new java.awt.GridLayout(1, 0));
       
       PCMem.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.RAISED));
       PCMemLeft.setLayout(new java.awt.GridBagLayout());
       java.awt.GridBagConstraints gridBagConstraints2;
       
       PCMemLeft.setPreferredSize(new java.awt.Dimension(150, 200));
       PCUpButton.setAction(pcUpAction);
       gridBagConstraints2 = new java.awt.GridBagConstraints();
       PCMemLeft.add(PCUpButton, gridBagConstraints2);
       
       jPanel22.setLayout(new java.awt.GridLayout(2, 1));
       
       jPanel22.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.LOWERED));
       PCMemLab.setText(" PC:                      ");
       PCMemLab.setForeground(java.awt.Color.black);
       jPanel22.add(PCMemLab);
       
       PCMemVal.setText(Integer.toString(cpu.getRegister().getPC()));
       PCMemVal.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
       PCMemVal.addFocusListener(new java.awt.event.FocusAdapter() {
           public void focusLost(java.awt.event.FocusEvent evt) {
               PCMemValFocusLost(evt);
           }
       });
       
       PCMemVal.addKeyListener(new java.awt.event.KeyAdapter() {
           public void keyPressed(java.awt.event.KeyEvent evt) {
               PCMemValKeyPressed(evt);
           }
       });
       
       jPanel22.add(PCMemVal);
       
       gridBagConstraints2 = new java.awt.GridBagConstraints();
       gridBagConstraints2.gridx = 0;
       gridBagConstraints2.gridy = 1;
       PCMemLeft.add(jPanel22, gridBagConstraints2);
       
       PCDownButton.setAction(pcDownAction);
       gridBagConstraints2 = new java.awt.GridBagConstraints();
       gridBagConstraints2.gridx = 0;
       gridBagConstraints2.gridy = 2;
       PCMemLeft.add(PCDownButton, gridBagConstraints2);
       
       PCMem.add(PCMemLeft);
       
       PCMemTable.setToolTipText(java.util.ResourceBundle.getBundle("Messages").getString("msg_tooltip_edit_mem"));
       PCMemTable.setModel(PCMemModel);
       PCMemTable.setPreferredSize(new java.awt.Dimension(150, 200));
       PCMemTable.setPreferredScrollableViewportSize(new java.awt.Dimension(150, 200));
       PCMemRight.setViewportView(PCMemTable);
       
       PCMem.add(PCMemRight);
       
       MemoryPanel.add(PCMem);
       
       ARMem.setLayout(new java.awt.GridLayout(1, 0));
       
       ARMem.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.RAISED));
       ARMemLeft.setLayout(new java.awt.GridBagLayout());
       java.awt.GridBagConstraints gridBagConstraints1;
       
       ARMemLeft.setPreferredSize(new java.awt.Dimension(150, 200));
       ARUpButton.setAction(arUpAction);
       gridBagConstraints1 = new java.awt.GridBagConstraints();
       ARMemLeft.add(ARUpButton, gridBagConstraints1);
       
       jPanel21.setLayout(new java.awt.GridLayout(2, 1));
       
       jPanel21.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.LOWERED));
       ARMemLab.setText(" AR:                      ");
       ARMemLab.setForeground(java.awt.Color.black);
       jPanel21.add(ARMemLab);
       
       ARMemVal.setText(Integer.toString(cpu.getRegister().getAR()));
       ARMemVal.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
       ARMemVal.addFocusListener(new java.awt.event.FocusAdapter() {
           public void focusLost(java.awt.event.FocusEvent evt) {
               ARMemValFocusLost(evt);
           }
       });
       
       ARMemVal.addKeyListener(new java.awt.event.KeyAdapter() {
           public void keyPressed(java.awt.event.KeyEvent evt) {
               ARMemValKeyPressed(evt);
           }
       });
       
       jPanel21.add(ARMemVal);
       
       gridBagConstraints1 = new java.awt.GridBagConstraints();
       gridBagConstraints1.gridx = 0;
       gridBagConstraints1.gridy = 2;
       ARMemLeft.add(jPanel21, gridBagConstraints1);
       
       ARDownButton.setAction(arDownAction);
       gridBagConstraints1 = new java.awt.GridBagConstraints();
       gridBagConstraints1.gridx = 0;
       gridBagConstraints1.gridy = 4;
       ARMemLeft.add(ARDownButton, gridBagConstraints1);
       
       ARMem.add(ARMemLeft);
       
       ARMemTable.setToolTipText(java.util.ResourceBundle.getBundle("Messages").getString("msg_tooltip_edit_mem"));
       ARMemTable.setModel(ARMemModel);
       ARMemTable.setPreferredSize(new java.awt.Dimension(150, 200));
       ARMemTable.setPreferredScrollableViewportSize(new java.awt.Dimension(150, 200));
       ARMemTable.setMinimumSize(new java.awt.Dimension(20, 20));
       ARMemRight.setViewportView(ARMemTable);
       
       ARMem.add(ARMemRight);
       
       MemoryPanel.add(ARMem);
       
       jScrollPane1.setViewportView(MemoryPanel);
       
       CPUPanel.add(jScrollPane1);
       
       RegisterPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 20, 20));
       
       RegisterPanel.setBorder(new javax.swing.border.TitledBorder(java.util.ResourceBundle.getBundle("Messages").getString("msg_register")));
       RegisterPanel.setPreferredSize(new java.awt.Dimension(300, 320));
       PCPan.setLayout(new java.awt.GridLayout(3, 1));
       
       PCPan.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.RAISED));
       PCPan.setPreferredSize(new java.awt.Dimension(130, 70));
       PCLabel.setText(" PC:                             ");
       PCLabel.setForeground(java.awt.Color.black);
       PCPan.add(PCLabel);
       
       PCVal.setEditable(false);
       PCVal.setText(fill(13,Integer.toBinaryString(cpu.getRegister().getPC())));
       PCVal.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
       PCPan.add(PCVal);
       
       PCValDez.setEditable(false);
       PCValDez.setText(Integer.toString(cpu.getRegister().getPC()));
       PCValDez.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
       PCPan.add(PCValDez);
       
       RegisterPanel.add(PCPan);
       
       ACPan.setLayout(new java.awt.GridLayout(3, 1));
       
       ACPan.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.RAISED));
       ACPan.setPreferredSize(new java.awt.Dimension(130, 70));
       ACLabel.setText(" AC:                             ");
       ACLabel.setForeground(java.awt.Color.black);
       ACPan.add(ACLabel);
       
       ACVal.setEditable(false);
       ACVal.setText(fill(16,Integer.toBinaryString(cpu.getRegister().getAC())));
       ACVal.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
       ACPan.add(ACVal);
       
       ACValDez.setText(Integer.toString(cpu.getRegister().getAC()));
       ACValDez.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
       ACValDez.addFocusListener(new java.awt.event.FocusAdapter() {
           public void focusLost(java.awt.event.FocusEvent evt) {
               ACValDezFocusLost(evt);
           }
       });
       
       ACValDez.addKeyListener(new java.awt.event.KeyAdapter() {
           public void keyPressed(java.awt.event.KeyEvent evt) {
               ACValDezKeyPressed(evt);
           }
       });
       
       ACPan.add(ACValDez);
       
       RegisterPanel.add(ACPan);
       
       ARPan.setLayout(new java.awt.GridLayout(3, 1));
       
       ARPan.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.RAISED));
       ARPan.setPreferredSize(new java.awt.Dimension(130, 70));
       ARLabel.setText(" AR:                             ");
       ARLabel.setForeground(java.awt.Color.black);
       ARPan.add(ARLabel);
       
       ARVal.setEditable(false);
       ARVal.setText(fill(13,Integer.toBinaryString(cpu.getRegister().getAR())));
       ARVal.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
       ARPan.add(ARVal);
       
       ARValDez.setEditable(false);
       ARValDez.setText(Integer.toString(cpu.getRegister().getAR()));
       ARValDez.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
       ARPan.add(ARValDez);
       
       RegisterPanel.add(ARPan);
       
       DRPan.setLayout(new java.awt.GridLayout(4, 1));
       
       DRPan.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.RAISED));
       DRPan.setPreferredSize(new java.awt.Dimension(130, 85));
       DRLabel.setText(" DR:                             ");
       DRLabel.setForeground(java.awt.Color.black);
       DRPan.add(DRLabel);
       
       DRVal.setEditable(false);
       DRVal.setText(fill(16,Integer.toBinaryString(cpu.getRegister().getDR())));
       DRVal.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
       DRPan.add(DRVal);
       
       DRValDez.setEditable(false);
       DRValDez.setText(Integer.toString(cpu.getRegister().getDR()));
       DRValDez.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
       DRPan.add(DRValDez);
       
       DRValCom.setEditable(false);
       DRValCom.setText(REGISTER.toCommand(REGISTER.Opcode(cpu.getRegister().getDR()))+" "+REGISTER.Addr(cpu.getRegister().getDR()));
       DRPan.add(DRValCom);
       
       RegisterPanel.add(DRPan);
       
       IRPan.setLayout(new java.awt.GridLayout(3, 1));
       
       IRPan.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.RAISED));
       IRPan.setPreferredSize(new java.awt.Dimension(65, 70));
       IRLabel.setText(" IR:   ");
       IRLabel.setForeground(java.awt.Color.black);
       IRPan.add(IRLabel);
       
       IRVal.setEditable(false);
       IRVal.setText(fill(3,Integer.toBinaryString(cpu.getRegister().getIR())));
       IRVal.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
       IRVal.setPreferredSize(new java.awt.Dimension(85, 21));
       IRPan.add(IRVal);
       
       IRValCom.setEditable(false);
       IRValCom.setText(REGISTER.toCommand(cpu.getRegister().getIR()));
       IRPan.add(IRValCom);
       
       RegisterPanel.add(IRPan);
       
       jScrollPane3.setViewportView(RegisterPanel);
       
       CPUPanel.add(jScrollPane3);
       
       MainPanel.add(CPUPanel, java.awt.BorderLayout.CENTER);
       
       PropertyPanel.setBorder(new javax.swing.border.TitledBorder(java.util.ResourceBundle.getBundle("Messages").getString("msg_proprties")));
       jPanel3.setBorder(new javax.swing.border.EtchedBorder());
       jPanel3.setPreferredSize(new java.awt.Dimension(400, 35));
       SpeedLabel.setText(java.util.ResourceBundle.getBundle("Messages").getString("msg_speed"));
       jPanel3.add(SpeedLabel);
       
       slowLabel.setText(java.util.ResourceBundle.getBundle("Messages").getString("msg_slow"));
       slowLabel.setPreferredSize(new java.awt.Dimension(10, 17));
       slowLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
       jPanel3.add(slowLabel);
       
       speedSlider.addChangeListener(new javax.swing.event.ChangeListener() {
           public void stateChanged(javax.swing.event.ChangeEvent evt) {
               speedSliderStateChanged(evt);
           }
       });
       
       jPanel3.add(speedSlider);
       
       fastLabel.setText(java.util.ResourceBundle.getBundle("Messages").getString("msg_fast"));
       fastLabel.setPreferredSize(new java.awt.Dimension(10, 17));
       fastLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
       jPanel3.add(fastLabel);
       
       PropertyPanel.add(jPanel3);
       
       jPanel5.setBorder(new javax.swing.border.EtchedBorder());
       jPanel5.setPreferredSize(new java.awt.Dimension(250, 35));
       jLabel1.setText(java.util.ResourceBundle.getBundle("Messages").getString("msg_begin"));
       jPanel5.add(jLabel1);
       
       StartVal.setText(Integer.toString(cpu.getStartpos()));
       StartVal.setPreferredSize(new java.awt.Dimension(50, 21));
       StartVal.setMinimumSize(new java.awt.Dimension(78, 21));
       StartVal.addFocusListener(new java.awt.event.FocusAdapter() {
           public void focusLost(java.awt.event.FocusEvent evt) {
               StartValFocusLost(evt);
           }
       });
       
       StartVal.addKeyListener(new java.awt.event.KeyAdapter() {
           public void keyPressed(java.awt.event.KeyEvent evt) {
               StartValKeyPressed(evt);
           }
       });
       
       jPanel5.add(StartVal);
       
       jLabel2.setText(java.util.ResourceBundle.getBundle("Messages").getString("msg_end"));
       jPanel5.add(jLabel2);
       
       EndVal.setText(Integer.toString(cpu.getEndpos()));
       EndVal.setPreferredSize(new java.awt.Dimension(50, 21));
       EndVal.setMinimumSize(new java.awt.Dimension(78, 21));
       EndVal.addFocusListener(new java.awt.event.FocusAdapter() {
           public void focusLost(java.awt.event.FocusEvent evt) {
               EndValFocusLost(evt);
           }
       });
       
       EndVal.addKeyListener(new java.awt.event.KeyAdapter() {
           public void keyPressed(java.awt.event.KeyEvent evt) {
               EndValKeyPressed(evt);
           }
       });
       
       jPanel5.add(EndVal);
       
       PropertyPanel.add(jPanel5);
       
       jScrollPane2.setViewportView(PropertyPanel);
       
       MainPanel.add(jScrollPane2, java.awt.BorderLayout.SOUTH);
       
       getContentPane().add(MainPanel, java.awt.BorderLayout.CENTER);
       
       setJMenuBar(jMenuBar1);
       pack();
   }//GEN-END:initComponents

   private void EndValFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_EndValFocusLost
       // Add your handling code here:
       updateVal(EndVal.getText(),"endpos");
   }//GEN-LAST:event_EndValFocusLost

   private void EndValKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_EndValKeyPressed
       // Add your handling code here:
         if(evt.getKeyCode()==evt.VK_ENTER)
       {
           updateVal(EndVal.getText(),"endpos");
       }
   }//GEN-LAST:event_EndValKeyPressed

   private void StartValFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_StartValFocusLost
       // Add your handling code here:
       updateVal(StartVal.getText(),"startpos");
   }//GEN-LAST:event_StartValFocusLost

   private void StartValKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_StartValKeyPressed
       // Add your handling code here:
         if(evt.getKeyCode()==evt.VK_ENTER)
       {
           updateVal(StartVal.getText(),"startpos");
       }
   }//GEN-LAST:event_StartValKeyPressed

   private void ACValDezKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ACValDezKeyPressed
       // Add your handling code here:
        if(evt.getKeyCode()==evt.VK_ENTER)
       {
           updateVal(ACValDez.getText(),"AC");
       }
   }//GEN-LAST:event_ACValDezKeyPressed

   private void ACValDezFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_ACValDezFocusLost
       // Add your handling code here:
       updateVal(ACValDez.getText(),"AC");
   }//GEN-LAST:event_ACValDezFocusLost

   private void ARMemValKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ARMemValKeyPressed
       // Add your handling code here:
        if(evt.getKeyCode()==evt.VK_ENTER)
       {
           updateVal(ARMemVal.getText(),"AR");
       }
   }//GEN-LAST:event_ARMemValKeyPressed

   private void ARMemValFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_ARMemValFocusLost
       // Add your handling code here:
       updateVal(ARMemVal.getText(),"AR");
   }//GEN-LAST:event_ARMemValFocusLost

   private void PCMemValFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_PCMemValFocusLost
       // Add your handling code here:
       updateVal(PCMemVal.getText(),"PC");
   }//GEN-LAST:event_PCMemValFocusLost

   private void PCMemValKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PCMemValKeyPressed
       // Add your handling code here:
       if(evt.getKeyCode()==evt.VK_ENTER)
       {
           updateVal(PCMemVal.getText(),"PC");
       }
   }//GEN-LAST:event_PCMemValKeyPressed

   private void speedSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_speedSliderStateChanged
       // Add your handling code here:
       cpu.setSpeed((100-speedSlider.getValue())*10);
   }//GEN-LAST:event_speedSliderStateChanged

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        mycontrol.dispose();
    }//GEN-LAST:event_exitForm

    public static String fill(int num,String s)
    {
        String r=s;
        while(r.length()<num) r="0"+r;
        return r;
    }
    
    public void updateVal(String text,String Reg)
    {
        try{
            int newVal=Integer.parseInt(text);
            int oldVal=0;
            if(Reg.equals("PC")) oldVal=cpu.getRegister().getPC();
            else if(Reg.equals("AC")) oldVal=cpu.getRegister().getAC();
            else if(Reg.equals("AR")) oldVal=cpu.getRegister().getAR();
            else if(Reg.equals("startpos")) oldVal=cpu.getStartpos();
            else if(Reg.equals("endpos")) oldVal=cpu.getEndpos();
            if(newVal!=oldVal)
            {
                if(Reg.equals("PC")) cpu.getRegister().setPC(newVal);
                else if(Reg.equals("AC")) cpu.getRegister().setAC(newVal);
                else if(Reg.equals("AR")) cpu.getRegister().setAR(newVal);
                else if(Reg.equals("startpos")) cpu.setStartpos(newVal);
                else if(Reg.equals("endpos")) cpu.setEndpos(newVal);
            }
        } catch(java.lang.NumberFormatException e)
        {
            if(Reg.equals("PC")) PCMemVal.setText(Integer.toString(cpu.getRegister().getPC()));
            else if(Reg.equals("AC")) ACValDez.setText(Integer.toString(cpu.getRegister().getAC()));
            else if(Reg.equals("AR")) ARMemVal.setText(Integer.toString(cpu.getRegister().getAR()));
            else if(Reg.equals("startpos")) StartVal.setText(Integer.toString(cpu.getStartpos()));
            else if(Reg.equals("endpos")) EndVal.setText(Integer.toString(cpu.getEndpos()));
            
            javax.swing.JOptionPane.showMessageDialog(this,
                    ResourceBundle.getBundle("Messages").getString("msg_error_number_input"),
                    ResourceBundle.getBundle("Messages").getString("msg_error"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent propertyChangeEvent) {
        String property=propertyChangeEvent.getPropertyName();
        if(property.equals("status"))
        {
            StatusLabel.setText(java.util.ResourceBundle.getBundle("Messages").getString("msg_state")+cpu.getStatus());
            if(cpu.isRunning())
            {
                runAction.setEnabled(false);
                stepAction.setEnabled(false);
                stopAction.setEnabled(true);
            }
            else
            {
                runAction.setEnabled(true);
                stepAction.setEnabled(true);
                stopAction.setEnabled(false);
            }
        }
        if(property.equals("PC"))
        {
            PCMemVal.setText(propertyChangeEvent.getNewValue().toString());
            PCValDez.setText(propertyChangeEvent.getNewValue().toString());
            PCVal.setText(fill(13,Integer.toBinaryString(((Integer)propertyChangeEvent.getNewValue()).intValue())));
        }
        if(property.equals("AR"))
        {
            ARMemVal.setText(propertyChangeEvent.getNewValue().toString());
            ARValDez.setText(propertyChangeEvent.getNewValue().toString());
            ARVal.setText(fill(13,Integer.toBinaryString(((Integer)propertyChangeEvent.getNewValue()).intValue())));
        }
        if(property.equals("DR"))
        {
            DRValDez.setText(propertyChangeEvent.getNewValue().toString());
            DRVal.setText(fill(16,Integer.toBinaryString(((Integer)propertyChangeEvent.getNewValue()).intValue())));
            DRValCom.setText(REGISTER.toCommand(REGISTER.Opcode(cpu.getRegister().getDR()))+" "+REGISTER.Addr(cpu.getRegister().getDR()));
        }
        if(property.equals("IR"))
        {
            IRValCom.setText(REGISTER.toCommand(((Integer)propertyChangeEvent.getNewValue()).intValue()));
            IRVal.setText(fill(3,Integer.toBinaryString(((Integer)propertyChangeEvent.getNewValue()).intValue())));
        }
        if(property.equals("AC"))
        {
            ACValDez.setText(propertyChangeEvent.getNewValue().toString());
            ACVal.setText(fill(16,Integer.toBinaryString(((Integer)propertyChangeEvent.getNewValue()).intValue())));
        }
        if(property.equals("startpos"))
        {
            StartVal.setText(Integer.toString(cpu.getStartpos()));
        }
        if(property.equals("endpos"))
        {
            EndVal.setText(Integer.toString(cpu.getEndpos()));
        }
        if(property.equals("speed"))
        {
           speedSlider.setValue((int)(100-(((cpu.getSpeed()<=1000)?cpu.getSpeed():1000)/10)));
        }

    }    
    
    void loadAllNew()
    {
            PCMemVal.setText(Integer.toString(cpu.getRegister().getPC()));
            PCValDez.setText(Integer.toString(cpu.getRegister().getPC()));
            PCVal.setText(fill(13,Integer.toBinaryString(cpu.getRegister().getPC())));
            ARMemVal.setText(Integer.toString(cpu.getRegister().getAR()));
            ARValDez.setText(Integer.toString(cpu.getRegister().getAR()));
            ARVal.setText(fill(13,Integer.toBinaryString(cpu.getRegister().getAR())));
            DRValDez.setText(Integer.toString(cpu.getRegister().getDR()));
            DRVal.setText(fill(16,Integer.toBinaryString(cpu.getRegister().getDR())));
            DRValCom.setText(REGISTER.toCommand(REGISTER.Opcode(cpu.getRegister().getDR()))+" "+REGISTER.Addr(cpu.getRegister().getDR()));
            IRValCom.setText(REGISTER.toCommand(cpu.getRegister().getIR()));
            IRVal.setText(fill(3,Integer.toBinaryString(cpu.getRegister().getIR())));      
            ACValDez.setText(Integer.toString(cpu.getRegister().getAC()));
            ACVal.setText(fill(16,Integer.toBinaryString(cpu.getRegister().getAC())));
            StartVal.setText(Integer.toString(cpu.getStartpos()));
            EndVal.setText(Integer.toString(cpu.getEndpos()));
            speedSlider.setValue((int)(100-(((cpu.getSpeed()<=1000)?cpu.getSpeed():1000)/10)));
            ((MyModel)ARMemModel).refreshAll();
            ((MyModel)ARMemModel).refreshAll();
            
            
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu Dateimenu;
    private javax.swing.JMenuItem loadMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu RunMenu;
    private javax.swing.JMenuItem runMenuItem;
    private javax.swing.JMenuItem stepMenuItem;
    private javax.swing.JMenuItem contMenuItem;
    private javax.swing.JMenuItem stopMenuItem;
    private javax.swing.JMenu DebugMenu;
    private javax.swing.JMenuItem breakMenuItem;
    private javax.swing.JMenuItem unbreakMenuItem;
    private javax.swing.JPanel StatusPanel;
    private javax.swing.JLabel StatusLabel;
    private javax.swing.JToolBar MainToolbar;
    private javax.swing.JPanel MainPanel;
    private javax.swing.JPanel CPUPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel MemoryPanel;
    private javax.swing.JPanel PCMem;
    private javax.swing.JPanel PCMemLeft;
    private javax.swing.JButton PCUpButton;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JLabel PCMemLab;
    private javax.swing.JTextField PCMemVal;
    private javax.swing.JButton PCDownButton;
    private javax.swing.JScrollPane PCMemRight;
    private javax.swing.JTable PCMemTable;
    private javax.swing.JPanel ARMem;
    private javax.swing.JPanel ARMemLeft;
    private javax.swing.JButton ARUpButton;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JLabel ARMemLab;
    private javax.swing.JTextField ARMemVal;
    private javax.swing.JButton ARDownButton;
    private javax.swing.JScrollPane ARMemRight;
    private javax.swing.JTable ARMemTable;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPanel RegisterPanel;
    private javax.swing.JPanel PCPan;
    private javax.swing.JLabel PCLabel;
    private javax.swing.JTextField PCVal;
    private javax.swing.JTextField PCValDez;
    private javax.swing.JPanel ACPan;
    private javax.swing.JLabel ACLabel;
    private javax.swing.JTextField ACVal;
    private javax.swing.JTextField ACValDez;
    private javax.swing.JPanel ARPan;
    private javax.swing.JLabel ARLabel;
    private javax.swing.JTextField ARVal;
    private javax.swing.JTextField ARValDez;
    private javax.swing.JPanel DRPan;
    private javax.swing.JLabel DRLabel;
    private javax.swing.JTextField DRVal;
    private javax.swing.JTextField DRValDez;
    private javax.swing.JTextField DRValCom;
    private javax.swing.JPanel IRPan;
    private javax.swing.JLabel IRLabel;
    private javax.swing.JTextField IRVal;
    private javax.swing.JTextField IRValCom;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel PropertyPanel;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel SpeedLabel;
    private javax.swing.JLabel slowLabel;
    private javax.swing.JSlider speedSlider;
    private javax.swing.JLabel fastLabel;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField StartVal;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField EndVal;
    // End of variables declaration//GEN-END:variables

}
