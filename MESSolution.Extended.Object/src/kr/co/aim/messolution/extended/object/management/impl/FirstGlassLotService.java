package kr.co.aim.messolution.extended.object.management.impl;

import java.util.HashMap;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.FirstGlassJob;
import kr.co.aim.messolution.extended.object.management.data.FirstGlassLot;
import kr.co.aim.messolution.extended.object.management.data.FirstGlassProduct;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FirstGlassLotService extends CTORMService<FirstGlassLot> {
	
	public static Log logger = LogFactory.getLog(FirstGlassLotService.class);
	
	private final String historyEntity = "FirstGlassLotHist";
	
	//core exception type
	/*catch(greenFrameDBErrorSignal ne)
	{
		if (!ne.getErrorCode().equals("NotFoundSignal"))
			throw ne;
	}*/
	
	public List<FirstGlassLot> select(String condition, Object[] bindSet)
		throws CustomException, NotFoundSignal
	{
		try
		{
			List<FirstGlassLot> result = super.select(condition, bindSet, FirstGlassLot.class);
			
			return result;
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "FirstGlassLot", ne.getMessage());
		}
	}
	
	public FirstGlassLot selectByKey(boolean isLock, Object[] keySet)
		throws CustomException, NotFoundSignal
	{
		try
		{
			return super.selectByKey(FirstGlassLot.class, isLock, keySet);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "FirstGlassLot", ne.getMessage());
		}
	}
	
	public FirstGlassLot create(EventInfo eventInfo, FirstGlassLot dataInfo)
		throws CustomException, NotFoundSignal
	{
		try
		{
			super.insert(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "FirstGlassLot", ne.getMessage());
		}
	}
	
	public void remove(EventInfo eventInfo, FirstGlassLot dataInfo)
		throws CustomException, NotFoundSignal
	{
		try
		{
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			super.delete(dataInfo);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "FirstGlassLot", ne.getMessage());
		}
	}
	
	public FirstGlassLot modify(EventInfo eventInfo, FirstGlassLot dataInfo)
		throws CustomException, NotFoundSignal
	{
		try
		{
			super.update(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "FirstGlassLot", ne.getMessage());
		}
	}
	
	/**
	 * change active state for job processing Lot
	 * @author swcho
	 * @since 2016.09.09
	 * @param eventInfo
	 * @param jobName
	 * @param lotName
	 * @param activeState
	 * @return
	 * @throws CustomException
	 */
	public FirstGlassLot changeActiveState(EventInfo eventInfo, String jobName, String lotName, String activeState)
		throws CustomException
	{
		FirstGlassLot lotData = ExtendedObjectProxy.getFirstGlassLotService().selectByKey(false, new Object[] {jobName, lotName});
		
		lotData.setActiveState(activeState);
		
		lotData = ExtendedObjectProxy.getFirstGlassLotService().modify(eventInfo, lotData);
		
		return lotData;
	}
	
	/**
	 * Carrier loading sync
	 * @author swcho
	 * @since 2017.02.10
	 * @param eventInfo
	 * @param jobName
	 * @param lotName
	 * @param carrierName
	 * @return
	 * @throws CustomException
	 */
	public FirstGlassLot adjustCarrier(EventInfo eventInfo, String jobName, String lotName, String carrierName)
		throws CustomException
	{
		FirstGlassLot lotData = ExtendedObjectProxy.getFirstGlassLotService().selectByKey(false, new Object[] {jobName, lotName});
		
		//lotData.setActiveState(activeState);
		lotData.setCarrierName(carrierName);
		
		lotData = ExtendedObjectProxy.getFirstGlassLotService().modify(eventInfo, lotData);
		
		return lotData;
	}
	
	/**
	 * Carrier loading sync
	 * @author dmlee
	 * @since 2017.03.30
	 * @param eventInfo
	 * @param jobName
	 * @param lotName
	 * @param carrierName
	 * @param unloaderFlag
	 * @return
	 * @throws CustomException
	 */
	public FirstGlassLot adjustCarrier(EventInfo eventInfo, String jobName, String lotName, String carrierName, boolean unloaderFlag)
		throws CustomException
	{
		FirstGlassLot lotData = ExtendedObjectProxy.getFirstGlassLotService().selectByKey(false, new Object[] {jobName, lotName});
		
		//lotData.setActiveState(activeState);
		lotData.setCarrierName(carrierName);
		
		if(unloaderFlag)
		{
			lotData.setParentLotName("");
		}
		
		lotData = ExtendedObjectProxy.getFirstGlassLotService().modify(eventInfo, lotData);
		
		return lotData;
	}
	
	/**
	 * register original Lot
	 * @author swcho
	 * @since 2016.09.01
	 * @param eventInfo
	 * @param jobData
	 * @param lotName
	 * @param carrierName
	 * @return
	 * @throws CustomException
	 */
	public FirstGlassLot createParentLot(EventInfo eventInfo, FirstGlassJob jobData, String lotName, String carrierName)
		throws CustomException
	{
		FirstGlassLot lotData = new FirstGlassLot(jobData.getJobName(), lotName);
		{
			lotData.setParentLotName("");
			lotData.setCarrierName(carrierName);
			lotData.setFactoryName(jobData.getFactoryName());
			lotData.setActiveState("Inactive");
			
			//event info
			lotData.setLastEventName(eventInfo.getEventName());
			lotData.setLastEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			lotData.setLastEventTime(eventInfo.getEventTime());
			lotData.setLastEventUser(eventInfo.getEventUser());
			lotData.setLastEventComment(eventInfo.getEventComment());
		}
		
		return ExtendedObjectProxy.getFirstGlassLotService().create(eventInfo, lotData);
	}
	
	/**
	 * register pilot Lot
	 * @author swcho
	 * @since 2016.09.01
	 * @param eventInfo
	 * @param jobData
	 * @param lotName
	 * @param carrierName
	 * @return
	 * @throws CustomException
	 */
	public FirstGlassLot createChildLot(EventInfo eventInfo, FirstGlassJob jobData, String lotName, String carrierName, String parentLotName)
		throws CustomException
	{
		FirstGlassLot lotData = new FirstGlassLot(jobData.getJobName(), lotName);
		{
			lotData.setParentLotName(parentLotName);
			lotData.setCarrierName(carrierName);
			lotData.setFactoryName(jobData.getFactoryName());
			lotData.setActiveState("Inactive");
				
			//event info
			lotData.setLastEventName(eventInfo.getEventName());
			lotData.setLastEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			lotData.setLastEventTime(eventInfo.getEventTime());
			lotData.setLastEventUser(eventInfo.getEventUser());
			lotData.setLastEventComment(eventInfo.getEventComment());
		}
			
		return ExtendedObjectProxy.getFirstGlassLotService().create(eventInfo, lotData);
	}
	
	/**
	 * get Lot list in processing job
	 * @author swcho
	 * @since 2016.09.05
	 * @param jobName
	 * @throws CustomException
	 */
	public List<ListOrderedMap> getProcessingLotList(String jobName) throws CustomException
	{                                                     
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("SELECT J.jobName,                                                                                  ").append("\n")
				.append("        J.factoryName, J.productSpecName, J.processFlowName, J.processOperationName, J.machineName,").append("\n")
				.append("        J.targetOperationName,                                                                     ").append("\n")
				.append("        J.jobState, J.jobProcessState,                                                             ").append("\n")
				.append("        L.lotName, L.carrierName, L.parentLotName, L.activeState                                   ").append("\n")
				.append("  FROM CT_FirstGlassJob J                                                                          ").append("\n")
				.append("    LEFT OUTER JOIN                                                                                ").append("\n")
				.append("        (SELECT F.jobName, F.activeState,                                                          ").append("\n")
				.append("               F.lotName, F.carrierName, F.parentLotName,                                          ").append("\n")
				.append("               L.factoryName, L.productSpecName, L.processFlowName, L.processOperationName,        ").append("\n")
				.append("               L.machineName,                                                                      ").append("\n")
				.append("               L.lotGrade,                                                                         ").append("\n")
				.append("               L.productQuantity, L.subProductQuantity,                                            ").append("\n")
				.append("               L.lotState, L.lotProcessState, L.lotHoldState                                       ").append("\n")
				.append("         FROM CT_FirstGlassLot F, Lot L                                                            ").append("\n")
				.append("         WHERE F.lotName = L.lotName                                                               ").append("\n")
				.append("            AND L.lotState IN (:LOTSTATE)                                                         ").append("\n")
				.append("        ) L                                                                                        ").append("\n")
				.append("    ON L.jobName = J.jobName                                                                       ").append("\n")
				.append("WHERE 1=1                                                                                          ").append("\n")
				.append("    AND J.jobState IN (:JOBSTATE)                                                                  ").append("\n")
				.append("    AND J.jobName = :JOBNAME                                                               ");
		
		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("JOBSTATE", "Released");
		bindMap.put("JOBNAME", jobName);
		bindMap.put("LOTSTATE", GenericServiceProxy.getConstantMap().Lot_Released);
		
		try
		{
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bindMap);
			
			return result;
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "FirstGlassLot", fe.getMessage());
		}
	}
	
	/**
	 * ongoing pilot job by Lot
	 * @author swcho
	 * @since 2016.09.09
	 * @param lotName
	 * @return
	 * @throws CustomException
	 */
	public String getActiveJobNameByLotName(String lotName) throws CustomException
	{
		/*select J.jobName, J.factoryName, J.machineName,
		       J.jobState, J.jobProcessState,
		       J.carrierName,
		       L.lotName, L.parentLotName
		from CT_FirstGlassJob J, CT_FirstGlassLot L
		where J.jobName = L.jobName
		and L.lotName = :LOTNAME
		and J.jobState = :JOBSTATE*/                                                          
		
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("SELECT J.jobName, J.factoryName, J.machineName, ").append("\n")
				.append("       J.jobState, J.jobProcessState, L.activeState, ").append("\n")
				.append("       L.lotName, L.carrierName, L.parentLotName ").append("\n")
				.append("    FROM CT_FirstGlassJob J, CT_FirstGlassLot L ").append("\n")
				.append("WHERE J.jobName = L.jobName ").append("\n")
				.append("    AND L.lotName = :LOTNAME ").append("\n")
				//.append("    AND L.activeState = :ACTIVESTATE ").append("\n")
				.append("    AND J.jobState = :JOBSTATE ");
		
		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("JOBSTATE", "Released");
		bindMap.put("LOTNAME", lotName);
		//bindMap.put("ACTIVESTATE", "Active");
		
		try
		{
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bindMap);
			
			if (result.size() < 1)
			{
				//normal process end case
				return "";
			}
			else
			{
				//only one
				return CommonUtil.getValue(result.get(0), "JOBNAME");
			}
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "FirstGlassLot", fe.getMessage());
		}
	}
	
	/**
	 * ongoing pilot job by Lot(Not Judge OK)
	 * @author yudan
	 * @since 2017.02.28
	 * @param lotName
	 * @return
	 * @throws CustomException
	 */
	public String getActiveNotOkJobNameByLotName(String lotName) throws CustomException
	{
		/*select J.jobName, J.factoryName, J.machineName,
		       J.jobState, J.jobProcessState,
		       J.carrierName,
		       L.lotName, L.parentLotName
		from CT_FirstGlassJob J, CT_FirstGlassLot L
		where J.jobName = L.jobName
		and L.lotName = :LOTNAME
		AND (J.judge NOT IN ('OK') OR J.judge IS NULL)
		and J.jobState = :JOBSTATE*/                                                          
		
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("SELECT J.jobName, J.factoryName, J.machineName, ").append("\n")
				.append("       J.jobState, J.jobProcessState, L.activeState, ").append("\n")
				.append("       L.lotName, L.carrierName, L.parentLotName ").append("\n")
				.append("    FROM CT_FirstGlassJob J, CT_FirstGlassLot L ").append("\n")
				.append("WHERE J.jobName = L.jobName ").append("\n")
				.append("    AND L.lotName = :LOTNAME ").append("\n")
				.append("    AND (J.judge NOT IN (:OK) OR J.judge IS NULL)").append("\n")
				.append("    AND J.jobState = :JOBSTATE ");
		
		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("JOBSTATE", "Released");
		bindMap.put("LOTNAME", lotName);
		bindMap.put("OK", "OK");
		
		try
		{
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bindMap);
			
			if (result.size() < 1)
			{
				//normal process end case
				return "";
			}
			else
			{
				//only one
				return CommonUtil.getValue(result.get(0), "JOBNAME");
			}
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "FirstGlassLot", fe.getMessage());
		}
	}
	
	/**
	 * active processing Lot from ongoing pilot job by carrier
	 * @author swcho
	 * @since 201.09.09
	 * @param carrierName
	 * @return
	 * @throws CustomException
	 */
	public String getActiveLotNameByCarrier(String carrierName) throws CustomException
	{
		/*SELECT J.jobName, J.factoryName, J.machineName,
			   J.jobState, J.jobProcessState,
			   FL.lotName, FL.parentLotName, FL.activeState,
		       OL.carrierName, OL.productQuantity,
		       OL.lotState, OL.lotHoldState, OL.lotProcessState, OL.reworkState
		   FROM CT_FirstGlassJob J, CT_FirstGlassLot FL, Lot OL
		 WHERE J.jobName = OL.jobName
		   AND J.jobState = :JOBSTATE
		   AND FL.activeState = :ACTIVESTATE
		   AND OL.lotName = L.lotName
		   AND L.lotState = :LOTSTATE
		   AND L.carrierName = :CARRIERNAME*/
		
		//active pilot job is only one
		//active Lot is only one in single Lot
		
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("SELECT J.jobName, J.factoryName, J.machineName, ").append("\n")
				.append("	   J.jobState, J.jobProcessState, ").append("\n")
				.append("	   FL.lotName, FL.parentLotName, FL.activeState, ").append("\n")
				.append("      OL.carrierName, OL.productQuantity, ").append("\n")
				.append("      OL.lotState, OL.lotHoldState, OL.lotProcessState, OL.reworkState ").append("\n")
				.append("  FROM CT_FirstGlassJob J, CT_FirstGlassLot FL, Lot OL ").append("\n")
				.append("WHERE J.jobName = FL.jobName ").append("\n")
				.append("  AND J.jobState = :JOBSTATE ").append("\n")
				.append("  AND FL.activeState = :ACTIVESTATE ").append("\n")
				.append("  AND FL.lotName = OL.lotName ").append("\n")
				.append("  AND FL.carrierName = OL.carrierName ").append("\n")
				.append("  AND OL.lotState = :LOTSTATE ").append("\n")
				.append("  AND OL.carrierName = :CARRIERNAME ");
		
		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("JOBSTATE", "Released");
		bindMap.put("ACTIVESTATE", "Active");
		bindMap.put("LOTSTATE", GenericServiceProxy.getConstantMap().Lot_Released);
		bindMap.put("CARRIERNAME", carrierName);
		
		try
		{
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bindMap);
			
			if (result.size() < 1)
			{
				//normal process end case
				return "";
			}
			else
			{
				//only one
				return CommonUtil.getValue(result.get(0), "LOTNAME");
			}
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "FirstGlassLot", fe.getMessage());
		}
	}
	
	/**
	 * purge pilot Lot & Product
	 * @author swcho
	 * @since 2016.09.26
	 * @param eventInfo
	 * @param jobName
	 * @param lotName
	 * @throws CustomException
	 */
	public void purgeFirstGlassLot(EventInfo eventInfo, String jobName, String lotName) throws CustomException
	{
		FirstGlassLot gLotData = ExtendedObjectProxy.getFirstGlassLotService().selectByKey(false, new Object[] {jobName, lotName});
		ExtendedObjectProxy.getFirstGlassLotService().remove(eventInfo, gLotData);
		
		//cascading purge
		try
		{
			List<FirstGlassProduct> productList = ExtendedObjectProxy.getFirstGlassProductService().select("jobName = ? AND lotName = ?", new Object[] {jobName, lotName}); 
			
			for (FirstGlassProduct productData : productList)
			{
				ExtendedObjectProxy.getFirstGlassProductService().remove(eventInfo, productData);
			}
		}
		catch (NotFoundSignal ne)
		{
			logger.error("No pilot product data");
		}
		catch (FrameworkErrorSignal fe)
		{
			logger.error("pilot product data removal failed");
		}
	}
	
	/**
	 * purge all pilot Lot & Product
	 * @author yudan
	 * @since 2017.05.27
	 * @param eventInfo
	 * @param jobName
	 * @throws CustomException
	 */
	public void purgeAllFirstGlassLot(EventInfo eventInfo, String jobName) throws CustomException
	{
		List<FirstGlassLot> onLotList = ExtendedObjectProxy.getFirstGlassLotService().select("jobName = ?", new Object[] {jobName});
		
		for (FirstGlassLot onLotData : onLotList)
		{			
			ExtendedObjectProxy.getFirstGlassLotService().remove(eventInfo, onLotData);
			
			//cascading purge
			try
			{
				List<FirstGlassProduct> productList = ExtendedObjectProxy.getFirstGlassProductService().select("jobName = ? AND lotName = ?", new Object[] {jobName, onLotData.getLotName()}); 
				
				for (FirstGlassProduct productData : productList)
				{
					ExtendedObjectProxy.getFirstGlassProductService().remove(eventInfo, productData);
				}
			}
			catch (NotFoundSignal ne)
			{
				logger.error("No pilot product data");
			}
			catch (FrameworkErrorSignal fe)
			{
				logger.error("pilot product data removal failed");
			}
		}			
		
	}
	
	/**
	 * CheckPilotLot
	 * @author yudan
	 * @since 2017.09.08
	 * @param lotName
	 * @return
	 * @throws CustomException
	 */
	public boolean CheckPilotLot(String lotName) throws CustomException
	{
		/*select J.jobName, J.factoryName, J.machineName,
		       J.jobState, J.jobProcessState,
		       J.carrierName,
		       L.lotName, L.parentLotName
		from CT_FirstGlassJob J, CT_FirstGlassLot L
		where J.jobName = L.jobName
		and L.lotName = :LOTNAME*/                                                          
		
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("SELECT J.jobName, J.factoryName, J.machineName, ").append("\n")
				.append("       J.jobState, J.jobProcessState, L.activeState, ").append("\n")
				.append("       L.lotName, L.carrierName, L.parentLotName ").append("\n")
				.append("    FROM CT_FirstGlassJob J, CT_FirstGlassLot L ").append("\n")
				.append("WHERE J.jobName = L.jobName ").append("\n")
				.append("    AND L.lotName = :LOTNAME ").append("\n");
		
		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("LOTNAME", lotName);
		
		try
		{
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bindMap);
			
			if (result.size() < 1)
			{
				//normal process end case
				return false;
			}
			else
			{
				//only one
				return true;
			}
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "FirstGlassLot", fe.getMessage());
		}
	}
}
