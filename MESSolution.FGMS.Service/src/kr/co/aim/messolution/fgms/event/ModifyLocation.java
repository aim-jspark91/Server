package kr.co.aim.messolution.fgms.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.area.AreaServiceProxy;
import kr.co.aim.greentrack.area.management.data.Area;
import kr.co.aim.greentrack.area.management.data.AreaKey;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;

import org.jdom.Document;

public class ModifyLocation extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {

		String oldAreaName    = SMessageUtil.getBodyItemValue(doc, "AREANAME", true);
		String superAreaName    = SMessageUtil.getBodyItemValue(doc, "SUPERAREANAME", true);
		String resourceState   = SMessageUtil.getBodyItemValue(doc, "RESOURCESTATE", true);
		String description  = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
		String capacity      = SMessageUtil.getBodyItemValue(doc, "CAPACITY", true);

		//SuperAreaData
		AreaKey aKey = new AreaKey();
		aKey.setAreaName(superAreaName);
		Area supAreaData = AreaServiceProxy.getAreaService().selectByKey(aKey);
		
		if(description.isEmpty())
		{
			description = supAreaData.getDescription() +"-ø‚Œª";
		}
		
		//check existence
		List <Area> sqlResult = new ArrayList <Area>();
		try
		{
			sqlResult = AreaServiceProxy.getAreaService().select("AREANAME = ?", new Object[] { oldAreaName });
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("AREA-9006", oldAreaName);
		}
		catch(greenFrameDBErrorSignal de)
		{
			if(de.getErrorCode().equals("NotFoundSignal"))
			{
				throw new CustomException("AREA-9006", oldAreaName);
			}
			else 
			{
				throw new CustomException("SYS-8001",de.getSql()); 
			}
		}
		
		Map<String, String> udfs = new HashMap<String,String>();
		udfs.put("CAPACITY", capacity);
		udfs.put("DOMESTICEXPORT", supAreaData.getUdfs().get("DOMESTICEXPORT"));
		udfs.put("OUTSOURCE", supAreaData.getUdfs().get("OUTSOURCE"));
		udfs.put("ORIGINALFACTORY", supAreaData.getUdfs().get("ORIGINALFACTORY"));
		
		
		AreaKey areaKey = new AreaKey();
		areaKey.setAreaName(oldAreaName);
		
		Area areaData = null;
		areaData = AreaServiceProxy.getAreaService().selectByKey(areaKey);
		
		areaData.setSuperAreaName(superAreaName);
		areaData.setResourceState(resourceState);
		areaData.setDescription(description);
		areaData.setUdfs(udfs);
		
		AreaServiceProxy.getAreaService().update(areaData);
		return doc;
	}
}
