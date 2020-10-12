package kr.co.aim.messolution.extended.object.management.impl;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.data.OperationMode;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OperationModeService extends CTORMService<OperationMode>{
	public static Log logger = LogFactory.getLog(OperationModeService.class);
	
	private final String historyEntity = "";
	
	public void create(EventInfo eventInfo, OperationMode dataInfo)
			throws CustomException, NotFoundSignal
		{
			try
			{
				super.insert(dataInfo);
				
				super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
				
				//return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
			}
			catch(greenFrameDBErrorSignal ne)
			{
				if (ne.getErrorCode().equals("NotFoundSignal"))
					throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
				else
					throw new CustomException("SYS-9999", "OperationMode", ne.getMessage());
			}
		}
	
	public OperationMode selectByKey(boolean isLock, Object[] keySet)
			throws CustomException, NotFoundSignal
		{
			try
			{
				return super.selectByKey(OperationMode.class, isLock, keySet);
			}
			catch(greenFrameDBErrorSignal ne)
			{
				if (ne.getErrorCode().equals("NotFoundSignal"))
					throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
				else
					throw new CustomException("SYS-9999", "OperationMode", ne.getMessage());
			}
		}
	
	public void modify(EventInfo eventInfo, OperationMode dataInfo)
			throws CustomException, NotFoundSignal
		{
			try
			{
				super.update(dataInfo);
				
				super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
				
				//return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
			}
			catch(greenFrameDBErrorSignal ne)
			{
				if (ne.getErrorCode().equals("NotFoundSignal"))
					throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
				else
					throw new CustomException("SYS-9999", "OperationMode", ne.getMessage());
			}
		}	
	
	public void ChangeData(EventInfo eventInfo, OperationMode dataInfo, String OLDsOperationMode)
			throws CustomException, NotFoundSignal
		{
			try
			{
				if(StringUtil.equals(OLDsOperationMode, "")) {
					ExtendedObjectProxy.getOperationModeService().create(eventInfo, dataInfo);
				}
				else {
					ExtendedObjectProxy.getOperationModeService().modify(eventInfo, dataInfo);
				}
				
				//return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
			}
			catch(greenFrameDBErrorSignal ne)
			{
				if (ne.getErrorCode().equals("NotFoundSignal"))
					throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
				else
					throw new CustomException("SYS-9999", "OperationMode", ne.getMessage());
			}
		}	
}
