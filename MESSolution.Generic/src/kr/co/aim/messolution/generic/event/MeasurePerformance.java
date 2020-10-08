package kr.co.aim.messolution.generic.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;

import org.jdom.Document;

public class MeasurePerformance extends AsyncHandler {

	private long sleepSec = 30;

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		eventLog.debug(String.format("Sleeping while %d sec, processing count are %d", sleepSec, Thread.activeCount()));
		
		try
		{
			eventLog.debug(String.format("[%s][%s][%d]", GenericServiceProxy.getTxDataSourceManager().getIsolationLevel(),
														GenericServiceProxy.getTxDataSourceManager().getPropagationBehavior(),
														GenericServiceProxy.getTxDataSourceManager().getTransactionTimeout()));

			//if (GenericServiceProxy.getTxDataSourceManager().isAutoManaged())
			//	eventLog.debug("transaction manager is managed automatically");
			//else
			//	eventLog.debug("transaction manager is managed manually");
			
			//GenericServiceProxy.getTxDataSourceManager().beginTransaction();
			
			//String sqlText = "update MESFactory set DESCRIPTION = ? where FACTORYNAME = ?";
			//Object[] bindArray = new Object[] {"CSW TEST", "CF"};
			
			//kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().update(sqlText, bindArray);
			
			//GenericServiceProxy.getTxDataSourceManager().commitTransaction();
						
			Thread.sleep(sleepSec * 1000);
			
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		catch (FrameworkErrorSignal fe)
		{
			throw fe;
		}
				
		eventLog.debug(String.format("awake it, processing count are %d", Thread.activeCount()));
	}

}
