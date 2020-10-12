package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.DspReserveProduct;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DspReserveProductService extends CTORMService<DspReserveProduct>{

	public static Log logger = LogFactory.getLog(DspReserveProduct.class);

	private final String historyEntity = "DspReserveProductHist";

	public List<DspReserveProduct> select(String condition, Object[] bindSet)
			throws CustomException
	{
		List<DspReserveProduct> result = super.select(condition, bindSet, DspReserveProduct.class);

		return result;
	}

	public DspReserveProduct selectByKey(boolean isLock, Object[] keySet)
			throws CustomException
	{
		return super.selectByKey(DspReserveProduct.class, isLock, keySet);
	}

	public DspReserveProduct create(EventInfo eventInfo, DspReserveProduct dataInfo)
			throws CustomException
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, DspReserveProduct dataInfo)
			throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public DspReserveProduct modify(EventInfo eventInfo, DspReserveProduct dataInfo)
			throws CustomException
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
