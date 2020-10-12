package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.DspAlternativeStocker;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DspAlternativeStockerService extends CTORMService<DspAlternativeStocker>{

	public static Log logger = LogFactory.getLog(DspAlternativeStocker.class);

	private final String historyEntity = "DspAlternativeStockerHist";

	public List<DspAlternativeStocker> select(String condition, Object[] bindSet)
			throws CustomException
	{
		List<DspAlternativeStocker> result = super.select(condition, bindSet, DspAlternativeStocker.class);

		return result;
	}

	public DspAlternativeStocker selectByKey(boolean isLock, Object[] keySet)
			throws CustomException
	{
		return super.selectByKey(DspAlternativeStocker.class, isLock, keySet);
	}

	public DspAlternativeStocker create(EventInfo eventInfo, DspAlternativeStocker dataInfo)
			throws CustomException
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, DspAlternativeStocker dataInfo)
			throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public DspAlternativeStocker modify(EventInfo eventInfo, DspAlternativeStocker dataInfo)
			throws CustomException
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
