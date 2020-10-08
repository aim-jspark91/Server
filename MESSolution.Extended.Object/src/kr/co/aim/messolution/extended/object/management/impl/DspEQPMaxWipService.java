package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.DspEQPMaxWip;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DspEQPMaxWipService extends CTORMService<DspEQPMaxWip>{

	public static Log logger = LogFactory.getLog(DspEQPMaxWip.class);

	private final String historyEntity = "DspEQPMaxWipHist";

	public List<DspEQPMaxWip> select(String condition, Object[] bindSet)
			throws CustomException
	{
		List<DspEQPMaxWip> result = super.select(condition, bindSet, DspEQPMaxWip.class);

		return result;
	}

	public DspEQPMaxWip selectByKey(boolean isLock, Object[] keySet)
			throws CustomException
	{
		return super.selectByKey(DspEQPMaxWip.class, isLock, keySet);
	}

	public DspEQPMaxWip create(EventInfo eventInfo, DspEQPMaxWip dataInfo)
			throws CustomException
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, DspEQPMaxWip dataInfo)
			throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public DspEQPMaxWip modify(EventInfo eventInfo, DspEQPMaxWip dataInfo)
			throws CustomException
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
