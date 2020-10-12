package kr.co.aim.messolution.fgms.event;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.fgms.FGMSServiceProxy;
import kr.co.aim.messolution.fgms.management.data.PackingSTDDef;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class RemovePackingSTDDef extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {

		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		
		//check productSpec
		CommonValidation.checkExistProductSpec(productSpecName, "FGI");
		
		//check existence
		List <PackingSTDDef> sqlResult = new ArrayList <PackingSTDDef>();
		try
		{
			sqlResult = FGMSServiceProxy.getPackingSTDDefService().select("PRODUCTSPECNAME = ?", new Object[] { productSpecName });
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("WEIGHT-9000", productSpecName);
		}
		catch(greenFrameDBErrorSignal de)
		{
			if(de.getErrorCode().equals("NotFoundSignal"))
			{
				throw new CustomException("WEIGHT-9000", productSpecName);
			}else 
			{
				throw new CustomException("SYS-8001",de.getSql()); 
			}
		}
		
		//prepare
		PackingSTDDef packingSTDDef = new PackingSTDDef(productSpecName);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Delete", getEventUser(), getEventComment(), "", "");
		
		//execute
		FGMSServiceProxy.getPackingSTDDefService().remove(eventInfo, packingSTDDef);
	
		return doc;
	}
}
