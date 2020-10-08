package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ScrapProduct;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ScrapProductService  extends CTORMService<ScrapProduct> {
	
	public static Log logger = LogFactory.getLog(ScrapProduct.class);
	
	private final String historyEntity = "ScrapProductHist";
	
	public List<ScrapProduct> select(String condition, Object[] bindSet)
			throws CustomException
		{
			List<ScrapProduct> result = super.select(condition, bindSet, ScrapProduct.class);
			
			return result;
		}
		
		public ScrapProduct selectByKey(boolean isLock, Object[] keySet)
			throws CustomException
		{
			return super.selectByKey(ScrapProduct.class, isLock, keySet);
		}
		
		public ScrapProduct create(EventInfo eventInfo, ScrapProduct dataInfo)
			throws CustomException
		{
			super.insert(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		
		public void remove(EventInfo eventInfo, ScrapProduct dataInfo)
			throws CustomException
		{
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			super.delete(dataInfo);
		}
		
		public ScrapProduct modify(EventInfo eventInfo, ScrapProduct dataInfo)
			throws CustomException
		{
			super.update(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
}
