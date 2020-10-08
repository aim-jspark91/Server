package kr.co.aim.messolution.fgms.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.area.AreaServiceProxy;
import kr.co.aim.greentrack.area.management.data.Area;
import kr.co.aim.greentrack.area.management.data.AreaKey;

import org.jdom.Document;

public class RemoveLocation extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {

		String areaName    = SMessageUtil.getBodyItemValue(doc, "AREANAME", true);

		AreaKey areaKey = new AreaKey();
		
		areaKey.setAreaName(areaName);
		
		Area areaData = null;
		
		areaData = AreaServiceProxy.getAreaService().selectByKey(areaKey);
		
		String palletQty = CommonUtil.getValue(areaData.getUdfs(), "PALLETQUANTITY"); 
		
		if(palletQty.equals("0")){
		
			AreaServiceProxy.getAreaService().delete(areaKey);
		}
		else {
			System.out.println("It can be removed only when LocationState=\"Empty\"");
			throw new CustomException("Customer-0001", "Empty");
		}
		return doc;
	}
}
