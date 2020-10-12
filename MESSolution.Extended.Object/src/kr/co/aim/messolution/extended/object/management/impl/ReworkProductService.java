package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ReworkProduct;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReworkProductService extends CTORMService<ReworkProduct>{
	
	public static Log logger = LogFactory.getLog(ReworkProduct.class);
	
	private final String historyEntity = "ReworkProductHist";
	
	public List<ReworkProduct> select(String condition, Object[] bindSet)
			throws CustomException
		{
			List<ReworkProduct> result = super.select(condition, bindSet, ReworkProduct.class);
			
			return result;
		}
		
		public ReworkProduct selectByKey(boolean isLock, Object[] keySet)
			throws CustomException
		{
			return super.selectByKey(ReworkProduct.class, isLock, keySet);
		}
		
		public ReworkProduct create(EventInfo eventInfo, ReworkProduct dataInfo)
			throws CustomException
		{
			super.insert(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		
		public void remove(EventInfo eventInfo, ReworkProduct dataInfo)
			throws CustomException
		{
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			super.delete(dataInfo);
		}
		
		public ReworkProduct modify(EventInfo eventInfo, ReworkProduct dataInfo)
			throws CustomException
		{
			super.update(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
}
