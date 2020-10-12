package kr.co.aim.messolution.consumable.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

import org.jdom.Document;

public class CrateProcessCanceled extends AsyncHandler {

	@Override
	public void doWorks(Document doc)
		throws CustomException
	{
		//If Initial Cleaner PU port, Change Reserve Lot Info (CT_ReserveLot Reserve State : Executing > Reserved)
		
		//1. pre-processing for sync
	    String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
	    String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String crateName = SMessageUtil.getBodyItemValue(doc, "CRATENAME", true);
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "WORKORDERNAME", true);
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);
		String reasonCodeDescription = SMessageUtil.getBodyItemValue(doc, "REASONCODEDESCRIPTION", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Cancel", getEventUser(), getEventComment(), null, null);
		
		try
        {
    		/* 01. Machine existence validation */
    		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
    		
    		/* 02. Port existence validation */
    		Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);
    		
    		/* 03. ConsumalbeInfo validation */
            Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(crateName);            
        }
		catch (CustomException ce)
        {
		    throw ce;
        }
		
        if(CommonUtil.isInitialInput(machineName))
        {
            if(MESLotServiceProxy.getLotServiceUtil().validateReserveLot(machineName, lotName, productRequestName))
            {
            
                ReserveLot reserveLot = ExtendedObjectProxy.getReserveLotService().selectByKey(true,  new Object[] {machineName, lotName});
                reserveLot.setReserveState(GenericServiceProxy.getConstantMap().RESV_LOT_STATE_RESV);
    
                ExtendedObjectProxy.getReserveLotService().modify(eventInfo, reserveLot);
            }
        }
        
        /* 20181203, hhlee, delete, delete logic ProductRequestPlan State 'Released' ==>> */
        //if(CommonUtil.isInitialInput(machineName))
		//{
		//	if(findDense(carrierName))
		//	{
		//		
		//	  if(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PL"))
		//	  {
		//		//160604 by swcho : box loader condition added
		//		if (CommonUtil.getValue(portData.getUdfs(), "PORTUSETYPE").equals("OO"))
		//		{
		//			// Get Plan Data / Change W/O State
		//			ProductRequestPlan pPlanData = CommonUtil.getStartPlanByMachine(machineName);
		//			
		//			this.ChangeWorkOrder(pPlanData);
		//		}
		//	  }
		//	}
		//}
		/* <<== 20181203, hhlee, delete, delete logic ProductRequestPlan State 'Released' */
	}
	
	//2017-01-10 by zhanghao CheckDenseID
	private  boolean findDense(String CarrierName) throws CustomException
	{
		String sql = "SELECT CONSUMABLENAME FROM CONSUMABLE  WHERE CONSUMABLENAME=:CONSUMABLENAME ";
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("CONSUMABLENAME", CarrierName);
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = 
				GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		if(sqlResult.size()>0)
		{
			return true;
		}
		
		else
		{
			return false;
		}
			
	}
}