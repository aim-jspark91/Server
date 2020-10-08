package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

/* hhlee, 2018.03.31, Add*/
public class ChamberGroupInfo extends UdfAccessor{

    //MACHINEGROUPNAME  VARCHAR2(40)    Not Null
    //MACHINENAME   VARCHAR2(40)    Not Null
    
    @CTORMTemplate(seq = "1", name="chamberGroupName", type="Key", dataType="String", initial="", history="")
    private String chamberGroupName;
    
    @CTORMTemplate(seq = "2", name="machineName", type="Key", dataType="String", initial="", history="")
    private String machineName;
    
    @CTORMTemplate(seq = "3", name="portName", type="Key", dataType="String", initial="", history="")
    private String portName;
    
    @CTORMTemplate(seq = "4", name="recipeName", type="Column", dataType="String", initial="", history="")
    private String recipeName;
        
    public ChamberGroupInfo()
    {       
    }
    
    public ChamberGroupInfo(String chamberGroupName, String machineName, String portName)
    {
        this.chamberGroupName = chamberGroupName;
        this.machineName = machineName;
        this.portName = portName;
    }

    public String getChamberGroupName() {
        return chamberGroupName;
    }

    public void setChamberGroupName(String chamberGroupName) {
        this.chamberGroupName = chamberGroupName;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }
    
    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }
    
    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }
}

