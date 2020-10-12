package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.DspConnectedStocker;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DspConnectedStockerService extends CTORMService<DspConnectedStocker>{

	public static Log logger = LogFactory.getLog(DspConnectedStocker.class);

	private final String historyEntity = "DspConnectedStockerHist";

	public List<DspConnectedStocker> select(String condition, Object[] bindSet)
			throws CustomException
	{
		List<DspConnectedStocker> result = super.select(condition, bindSet, DspConnectedStocker.class);

		return result;
	}

	public DspConnectedStocker selectByKey(boolean isLock, Object[] keySet)
			throws CustomException
	{
		return super.selectByKey(DspConnectedStocker.class, isLock, keySet);
	}

	public DspConnectedStocker create(EventInfo eventInfo, DspConnectedStocker dataInfo)
			throws CustomException
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, DspConnectedStocker dataInfo)
			throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public DspConnectedStocker modify(EventInfo eventInfo, DspConnectedStocker dataInfo)
			throws CustomException
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
