package kr.co.aim.messolution.machine.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ChamberIdleTime;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.jdom.Document;

/**
 * 
 * @author Administrator
 *
 */
public class ChamberIdleTimeReport extends AsyncHandler {

    @Override
    public void doWorks(Document doc) throws CustomException 
    {
        String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
        String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
        String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", true);
        String settingIdleTime = SMessageUtil.getBodyItemValue(doc, "SETTINGIDLETIME", true);
        String idleTimeResult = SMessageUtil.getBodyItemValue(doc, "IDLETIMERESULT", true);
        String mqcQuantity = SMessageUtil.getBodyItemValue(doc, "MQCQUANTITY", true);
        
        String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", true);
        
        EventInfo eventInfo = EventInfoUtil.makeEventInfo(SMessageUtil.getMessageName(doc), getEventUser(), getEventComment(), null, null);
        
        //check Machine Data            
        Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
        
        //check Unit Data            
        Machine unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
        
        //check SubUnit Data            
        Machine subUnitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(subUnitName);
        
        
        try
        {
            ChamberIdleTime chamberIdleTimeData = null;
            try
            {
                chamberIdleTimeData = ExtendedObjectProxy.getChamberIdleTimeService().selectByKey(false, new Object[] {machineName, unitName, subUnitName});
                
                chamberIdleTimeData.setMachineRecipeName(machineRecipeName);
                
                chamberIdleTimeData.setSettingIdleTime(Long.parseLong(settingIdleTime));
                chamberIdleTimeData.setIdleTimeResult(idleTimeResult);
                chamberIdleTimeData.setMqcQuantity(Long.parseLong(mqcQuantity));
                
                chamberIdleTimeData.setLastEventUser(eventInfo.getEventUser());
                chamberIdleTimeData.setLastEventTime(eventInfo.getEventTime());
                chamberIdleTimeData.setLastEventName(eventInfo.getEventName());
                chamberIdleTimeData.setLastEventComment(eventInfo.getEventComment());
                
                ExtendedObjectProxy.getChamberIdleTimeService().modify(eventInfo, chamberIdleTimeData);
            }
            catch (Exception ex)
            {
                chamberIdleTimeData = new ChamberIdleTime(machineName, unitName, subUnitName);
                
                chamberIdleTimeData.setMachineRecipeName(machineRecipeName);
                
                chamberIdleTimeData.setSettingIdleTime(Long.parseLong(settingIdleTime));
                chamberIdleTimeData.setIdleTimeResult(idleTimeResult);
                chamberIdleTimeData.setMqcQuantity(Long.parseLong(mqcQuantity));
                
                chamberIdleTimeData.setLastEventUser(eventInfo.getEventUser());
                chamberIdleTimeData.setLastEventTime(eventInfo.getEventTime());
                chamberIdleTimeData.setLastEventName(eventInfo.getEventName());
                chamberIdleTimeData.setLastEventComment(eventInfo.getEventComment());
                
                chamberIdleTimeData = ExtendedObjectProxy.getChamberIdleTimeService().create(eventInfo, chamberIdleTimeData);
            }                        
        }
        catch (Exception ex)
        {
            eventLog.error(ex.getMessage());            
        }
    }
}