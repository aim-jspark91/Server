package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.DspStockerKanban;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DspStockerKanbanService extends CTORMService<DspStockerKanban>{

	public static Log logger = LogFactory.getLog(DspStockerKanban.class);

	private final String historyEntity = "DspStockerKanbanHist";

	public List<DspStockerKanban> select(String condition, Object[] bindSet)
			throws CustomException
	{
		List<DspStockerKanban> result = super.select(condition, bindSet, DspStockerKanban.class);

		return result;
	}

	public DspStockerKanban selectByKey(boolean isLock, Object[] keySet)
			throws CustomException
	{
		return super.selectByKey(DspStockerKanban.class, isLock, keySet);
	}

	public DspStockerKanban create(EventInfo eventInfo, DspStockerKanban dataInfo)
			throws CustomException
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, DspStockerKanban dataInfo)
			throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public DspStockerKanban modify(EventInfo eventInfo, DspStockerKanban dataInfo)
			throws CustomException
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
