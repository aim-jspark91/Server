package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.DspMachineDispatch;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DspMachineDispatchService extends CTORMService<DspMachineDispatch>{

	public static Log logger = LogFactory.getLog(DspMachineDispatch.class);

	private final String historyEntity = "DspMachineDispatchHist";

	public List<DspMachineDispatch> select(String condition, Object[] bindSet)
			throws CustomException
	{
		List<DspMachineDispatch> result = super.select(condition, bindSet, DspMachineDispatch.class);

		return result;
	}

	public DspMachineDispatch selectByKey(boolean isLock, Object[] keySet)
			throws CustomException
	{
		return super.selectByKey(DspMachineDispatch.class, isLock, keySet);
	}

	public DspMachineDispatch create(EventInfo eventInfo, DspMachineDispatch dataInfo)
			throws CustomException
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, DspMachineDispatch dataInfo)
			throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public DspMachineDispatch modify(EventInfo eventInfo, DspMachineDispatch dataInfo)
			throws CustomException
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
