import REGISTER;

class ALU 
{
   private REGISTER r;
   ALU(REGISTER regs)
     {
       r = regs; 
     }
   void rshift()
    {
      r.setAC(((r.getAC() >>> 1) | ( r.getAC() << 15)) & 65535);
    }
    void lshift()
    {
      r.setAC(((r.getAC() << 1) | ( r.getAC() >> 15)) & 65535);
    }
   void add()
   {
     r.setAC((r.getAC() + r.getDR()) & 65535);
   }
   void comp()
   {
     r.setAC((r.getAC() ^ 65535) & 65535);
   }
   void and()
   {
     r.setAC((r.getAC() & r.getDR()) & 65535);
   }  
}
