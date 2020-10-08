package kr.co.aim.messolution.fgms.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.area.AreaServiceProxy;
import kr.co.aim.greentrack.area.management.data.Area;
import kr.co.aim.greentrack.area.management.data.AreaKey;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;

import org.jdom.Document;

public class CreateLocation extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {

		String superAreaName    = SMessageUtil.getBodyItemValue(doc, "SUPERAREANAME", true);
		String description  = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
		String capacity      = SMessageUtil.getBodyItemValue(doc, "CAPACITY", true);

		String F      = SMessageUtil.getBodyItemValue(doc, "F", true);
		String S      = SMessageUtil.getBodyItemValue(doc, "S", true);
		String X      = SMessageUtil.getBodyItemValue(doc, "X", true);
		String Y      = SMessageUtil.getBodyItemValue(doc, "Y", true);
		String Z      = SMessageUtil.getBodyItemValue(doc, "Z", true);
		String areaName = F + S + Z + X + Y;
		
		//SuperAreaData
		AreaKey aKey = new AreaKey();
		aKey.setAreaName(superAreaName);
		Area supAreaData = AreaServiceProxy.getAreaService().selectByKey(aKey);
		
		
		if(description.isEmpty())
		{
			description = supAreaData.getDescription() +"-ø‚Œª";
		}
		
		
		//check duplication
		List <Area> sqlResult = new ArrayList <Area>();
		try
		{
			sqlResult = AreaServiceProxy.getAreaService().select("AREANAME = ?", new Object[] { areaName });
			
			if (sqlResult.size() > 0)
				throw new CustomException("AREA-9001", areaName);
		}
		catch (NotFoundSignal ne)
		{
			sqlResult = new ArrayList <Area>();
		}
		catch(greenFrameDBErrorSignal de)
		{
			if(!de.getErrorCode().equals("NotFoundSignal"))
				throw new CustomException("SYS-8001",de.getSql()); 
		}
		
		//prepare data
		Map<String, String> udfs = new HashMap<String,String>();
		
		udfs.put("FULLSTATE", "N");
		udfs.put("DOMESTICEXPORT", supAreaData.getUdfs().get("DOMESTICEXPORT"));
		udfs.put("CAPACITY", capacity);
		udfs.put("PALLETQUANTITY", "0");
		udfs.put("OUTSOURCE", supAreaData.getUdfs().get("OUTSOURCE"));
		udfs.put("ORIGINALFACTORY", supAreaData.getUdfs().get("ORIGINALFACTORY"));
		
		AreaKey areaKey = new AreaKey();
		areaKey.setAreaName(areaName);
		
		Area area = new Area();
		area.setKey(areaKey);
		area.setDescription(description);
		area.setAreaType(GenericServiceProxy.getConstantMap().FGMS_AREATYPE_STORAGEAREA);
		area.setFactoryName("FGI");
		area.setSuperAreaName(superAreaName);
		area.setResourceState(GenericServiceProxy.getConstantMap().FGMS_RESOURCE_INSERVICE);
		
		area.setUdfs(udfs);
		
		//execute
		AreaServiceProxy.getAreaService().insert(area);
		
		return doc;
	}
	
}
