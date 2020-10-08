package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.DspStockerRegion;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DspStockerRegionService extends CTORMService<DspStockerRegion>{

	public static Log logger = LogFactory.getLog(DspStockerRegion.class);

	private final String historyEntity = "DspStockerRegionHist";

	public List<DspStockerRegion> select(String condition, Object[] bindSet)
			throws CustomException
	{
		List<DspStockerRegion> result = super.select(condition, bindSet, DspStockerRegion.class);

		return result;
	}

	public DspStockerRegion selectByKey(boolean isLock, Object[] keySet)
			throws CustomException
	{
		return super.selectByKey(DspStockerRegion.class, isLock, keySet);
	}

	public DspStockerRegion create(EventInfo eventInfo, DspStockerRegion dataInfo)
			throws CustomException
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, DspStockerRegion dataInfo)
			throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public DspStockerRegion modify(EventInfo eventInfo, DspStockerRegion dataInfo)
			throws CustomException
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
