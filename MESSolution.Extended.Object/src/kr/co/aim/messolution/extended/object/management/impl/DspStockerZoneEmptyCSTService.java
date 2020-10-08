package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.DspStockerZoneEmptyCST;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DspStockerZoneEmptyCSTService extends CTORMService<DspStockerZoneEmptyCST>{

	public static Log logger = LogFactory.getLog(DspStockerZoneEmptyCST.class);

	private final String historyEntity = "DspStockerZoneEmptyCSTHist";

	public List<DspStockerZoneEmptyCST> select(String condition, Object[] bindSet)
			throws CustomException
	{
		List<DspStockerZoneEmptyCST> result = super.select(condition, bindSet, DspStockerZoneEmptyCST.class);

		return result;
	}

	public DspStockerZoneEmptyCST selectByKey(boolean isLock, Object[] keySet)
			throws CustomException
	{
		return super.selectByKey(DspStockerZoneEmptyCST.class, isLock, keySet);
	}

	public DspStockerZoneEmptyCST create(EventInfo eventInfo, DspStockerZoneEmptyCST dataInfo)
			throws CustomException
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, DspStockerZoneEmptyCST dataInfo)
			throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public DspStockerZoneEmptyCST modify(EventInfo eventInfo, DspStockerZoneEmptyCST dataInfo)
			throws CustomException
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
