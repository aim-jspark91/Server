package kr.co.aim.messolution.fgms.event;

import java.sql.Timestamp;
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

public class ModifyPackingSTDDef extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {

		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
		String panelWeight = SMessageUtil.getBodyItemValue(doc, "PANELWEIGHT", true);
		String boxWeight = SMessageUtil.getBodyItemValue(doc, "BOXWEIGHT", true);
		String emptyBoxWeight = SMessageUtil.getBodyItemValue(doc, "EMPTYBOXWEIGHT", true);
		String palletWeight = SMessageUtil.getBodyItemValue(doc, "PALLETWEIGHT", true);
		String width = SMessageUtil.getBodyItemValue(doc, "WIDTH", true);
		String length = SMessageUtil.getBodyItemValue(doc, "LENGTH", true);
		String height1 = SMessageUtil.getBodyItemValue(doc, "HEIGHT1", true);
		String height2 = SMessageUtil.getBodyItemValue(doc, "HEIGHT2", true);
		String height3 = SMessageUtil.getBodyItemValue(doc, "HEIGHT3", true);
		String lastEventUser = getEventUser();
		
		//check productSpec existence
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
		
		Timestamp lastEventTime = new Timestamp(System.currentTimeMillis());

		PackingSTDDef packingSTDDef = new PackingSTDDef(productSpecName);

		packingSTDDef.setDescription(description);
		packingSTDDef.setPanelWeight(Long.parseLong(panelWeight));
		packingSTDDef.setBoxWeight(Long.parseLong(boxWeight));
		packingSTDDef.setEmptyBoxWeight(Long.parseLong(emptyBoxWeight));
		packingSTDDef.setPalletWeight(Long.parseLong(palletWeight));
		packingSTDDef.setWidth(Long.parseLong(width));
		packingSTDDef.setLength(Long.parseLong(length));
		packingSTDDef.setHeight1(Long.parseLong(height1));
		packingSTDDef.setHeight2(Long.parseLong(height2));
		packingSTDDef.setHeight3(Long.parseLong(height3));
		packingSTDDef.setLastEventTime(lastEventTime);
		packingSTDDef.setLastEventUser(lastEventUser);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Modify", getEventUser(), getEventComment(), "", "");
		FGMSServiceProxy.getPackingSTDDefService().modify(eventInfo, packingSTDDef);
	
		return doc;
	}
}
