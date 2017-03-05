package android.microntek;

public class ProcessInfo
{
  private int memSize;
  private int pid;
  public String[] pkgnameList;
  private String processName;
  private int uid;
  
  public ProcessInfo() {}
  
  public String getProcessName()
  {
    return processName;
  }

  public int getUid() {
    return this.uid;
  }
  
  public void setMemSize(int paramInt)
  {
    memSize = paramInt;
  }
  
  public void setPid(int paramInt)
  {
    pid = paramInt;
  }
  
  public void setPocessName(String paramString)
  {
    processName = paramString;
  }
  
  public void setUid(int uid)
  {
    this.uid = uid;
  }
}
