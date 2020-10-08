package kr.co.aim.messolution.recipe.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.jdom.Document;

public class UnitRecipeListRequest extends AsyncHandler {

    @Override
    public void doWorks(Document doc) throws CustomException {
                
        String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME" , true);
        String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME" , true);
        Machine machineData = null;
    
        //get Unit machine
        machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
        
        SMessageUtil.setBodyItemValue(doc, "MACHINENAME", machineName, true);
        SMessageUtil.setBodyItemValue(doc, "UNITNAME", unitName, true);
                        
        String targetSubjectName = CommonUtil.getValue(machineData.getUdfs(), "MCSUBJECTNAME");
                
        GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "EISSender");
    }
}