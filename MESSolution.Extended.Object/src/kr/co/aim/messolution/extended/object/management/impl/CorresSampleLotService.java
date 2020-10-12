package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.CorresSampleLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CorresSampleLotService extends CTORMService<CorresSampleLot>{
	
	public static Log logger = LogFactory.getLog(CorresSampleLot.class);
	
	private final String historyEntity = "CorresSampleLotHist";
	
	public List<CorresSampleLot> select(String condition, Object[] bindSet)
			throws CustomException
		{
			List<CorresSampleLot> result = super.select(condition, bindSet, CorresSampleLot.class);
			
			return result;
		}
		
		public CorresSampleLot selectByKey(boolean isLock, Object[] keySet)
			throws CustomException
		{
			return super.selectByKey(CorresSampleLot.class, isLock, keySet);
		}
		
		public CorresSampleLot create(EventInfo eventInfo, CorresSampleLot dataInfo)
			throws CustomException
		{
			super.insert(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		
		public void remove(EventInfo eventInfo, CorresSampleLot dataInfo)
			throws CustomException
		{
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			super.delete(dataInfo);
		}
		
		public CorresSampleLot modify(EventInfo eventInfo, CorresSampleLot dataInfo)
			throws CustomException
		{
			super.update(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
}
