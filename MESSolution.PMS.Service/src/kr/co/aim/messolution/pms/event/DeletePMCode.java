package kr.co.aim.messolution.pms.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.Maintenance;
import kr.co.aim.messolution.pms.management.data.PMCode;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class DeletePMCode extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String PMCode 		 = SMessageUtil.getBodyItemValue(doc, "PMCODE", true);
		String MachineGruopName  = SMessageUtil.getBodyItemValue(doc, "MACHINEGROUPNAME", false);
		String MachineName   = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String StartFlag   = SMessageUtil.getBodyItemValue(doc, "STARTFLAG", true);	
		
		
		EventInfo eventInfo  = EventInfoUtil.makeEventInfo("DeletePMCode", getEventUser(), getEventComment(), null, null);
		
		//Delete PMCode
		try
		{						
			PMCode pmData = PMSServiceProxy.getPMCodeService().selectByKey(true, new Object[]{PMCode});
			PMSServiceProxy.getPMCodeService().remove(eventInfo, pmData);		
		}
		catch(Exception ex)
		{
			throw new CustomException("PMS-0095", PMCode);
		}	
		
	//Delete Maintenance	
	   
	    if(StartFlag.equals("Y"))
		{
		    String MaintenanceID  = SMessageUtil.getBodyItemValue(doc, "MAINTENANCEID", true);
			String MaintStatus  = SMessageUtil.getBodyItemValue(doc, "MAINTSTATUS", false);				
		      try
		     {	
	           Maintenance MaintenanceData = PMSServiceProxy.getMaintenanceService().selectByKey(true, new Object[]{MaintenanceID});
	           
	           PMSServiceProxy.getMaintenanceService().remove(eventInfo, MaintenanceData);	
			 }	

	        catch (Exception ex)
              {
		         throw new CustomException("PMS-0096", MaintenanceID);
	          }    
		}	
	     // InserBoard(PMCode,getEventUser()); // Inser Modify data to Board.
	    
		return doc;		
	}	

}
