package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LocalRunException;
import kr.co.aim.messolution.extended.object.management.data.LotAction;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.jdom.Document;
import org.jdom.Element;

public class DeleteLocalRunException extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Deleted", this.getEventUser(), this.getEventComment(), "", "");
		
		String userDepartment = SMessageUtil.getBodyItemValue(doc, "USERDEPARTMENT", true);
		List<Element> exceptionList = SMessageUtil.getBodySequenceItemList(doc, "EXCEPTIONLIST", true);
		
		for(Element elexception : exceptionList)
		{
			String lotName = SMessageUtil.getChildText(elexception, "LOTNAME", true);
			String processFlowName = SMessageUtil.getChildText(elexception, "PROCESSFLOWNAME", true);
			String processOperName = SMessageUtil.getChildText(elexception, "PROCESSOPERATIONNAME", true);
			String machineName = SMessageUtil.getChildText(elexception, "MACHINENAME", true);
			String recipeID = SMessageUtil.getChildText(elexception, "RECIPENAME", false);
			String department = SMessageUtil.getChildText(elexception, "DEPARTMENT", false);
			
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			try{
			 List<LotAction> localRunActionList = new ArrayList<LotAction>();
			 
			//Remove CT_LOTACTION
			String condition = " WHERE LOTNAME = ?  AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ?" +
					" AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ?  AND ACTIONNAME = ?" +
					" AND HOLDCODE = ? ";
            Object[] bindSet = new Object[]{ lotData.getKey().getLotName(), processFlowName, "00001",
                    		processOperName, "00001", GenericServiceProxy.getConstantMap().ACTIONNAME_HOLD, "RLRE"};
           
        	   
        	   localRunActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);
				
				LotAction lotAction = new LotAction();
				
				lotAction = localRunActionList.get(0);
    			
    			lotAction.setActionState(GenericServiceProxy.getConstantMap().ACTIONSTATE_CANCELED);
    			ExtendedObjectProxy.getLotActionService().remove(eventInfo, lotAction);
			}
    			catch(Exception ex)
    			{
    				//throw new CustomException("ACTION-0001",lotName, processFlowName, processOperName, GenericServiceProxy.getConstantMap().ACTIONNAME_HOLD); 
    			}
    			
			
				LocalRunException localRunExceptionData = ExtendedObjectProxy.getLocalRunExceptionService().selectByKey(false, new Object[] {lotName,processFlowName,processOperName,machineName});
				
				localRunExceptionData.setActionState(GenericServiceProxy.getConstantMap().ACTIONSTATE_CANCELED);
					
					ExtendedObjectProxy.getLocalRunExceptionService().remove(eventInfo, localRunExceptionData);
         
			
		}
           
		return doc;
	}
}
