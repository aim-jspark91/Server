package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.DspReserveProductNon;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DspReserveProductNonService extends CTORMService<DspReserveProductNon>{

	public static Log logger = LogFactory.getLog(DspReserveProductNon.class);

	private final String historyEntity = "DspReserveProductNonHist";

	public List<DspReserveProductNon> select(String condition, Object[] bindSet)
			throws CustomException
	{
		List<DspReserveProductNon> result = super.select(condition, bindSet, DspReserveProductNon.class);

		return result;
	}

	public DspReserveProductNon selectByKey(boolean isLock, Object[] keySet)
			throws CustomException
	{
		return super.selectByKey(DspReserveProductNon.class, isLock, keySet);
	}

	public DspReserveProductNon create(EventInfo eventInfo, DspReserveProductNon dataInfo)
			throws CustomException
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, DspReserveProductNon dataInfo)
			throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public DspReserveProductNon modify(EventInfo eventInfo, DspReserveProductNon dataInfo)
			throws CustomException
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
