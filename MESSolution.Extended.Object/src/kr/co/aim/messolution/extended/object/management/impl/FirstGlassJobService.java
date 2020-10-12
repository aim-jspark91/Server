package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.FirstGlassJob;
import kr.co.aim.messolution.extended.object.management.data.FirstGlassLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.processoperationspec.ProcessOperationSpecServiceProxy;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpecKey;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FirstGlassJobService extends CTORMService<FirstGlassJob> {
	
	public static Log logger = LogFactory.getLog(FirstGlassJobService.class);
	
	private final String historyEntity = "FirstGlassJobHist";
	
	public List<FirstGlassJob> select(String condition, Object[] bindSet)
		throws CustomException, NotFoundSignal
	{
		try
		{
			List<FirstGlassJob> result = super.select(condition, bindSet, FirstGlassJob.class);
			
			return result;
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "FirstGlassJob", ne.getMessage());
		}
	}
	
	public FirstGlassJob selectByKey(boolean isLock, Object[] keySet)
		throws CustomException, NotFoundSignal
	{
		try
		{
			return super.selectByKey(FirstGlassJob.class, isLock, keySet);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "FirstGlassJob", ne.getMessage());
		}
	}
	
	public FirstGlassJob create(EventInfo eventInfo, FirstGlassJob dataInfo)
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
				throw new CustomException("SYS-9999", "FirstGlassJob", ne.getMessage());
		}
	}
	
	public void remove(EventInfo eventInfo, FirstGlassJob dataInfo)
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
				throw new CustomException("SYS-9999", "FirstGlassJob", ne.getMessage());
		}
	}
	
	public FirstGlassJob modify(EventInfo eventInfo, FirstGlassJob dataInfo)
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
				throw new CustomException("SYS-9999", "FirstGlassJob", ne.getMessage());
		}
	}
	
	/**
	 * create pilot job
	 * @author swcho
	 * @since 2016.09.01
	 * @param eventInfo
	 * @param lotData
	 * @param machineName
	 * @param targetOperationName
	 * @param jobName
	 * @return
	 * @throws CustomException
	 */
	public FirstGlassJob createJob(EventInfo eventInfo, Lot lotData, String machineName, String targetOperationName, String jobName)
		throws CustomException
	{
		
		//String jobName = "FGJ-" + lotData.getCarrierName() + "-" + new SimpleDateFormat("yyyyMMddss").format(new java.util.Date());
		
		FirstGlassJob jobData = new FirstGlassJob(jobName);
		{
			jobData.setFactoryName(lotData.getFactoryName());
			jobData.setProductSpecName(lotData.getProductSpecName());
			jobData.setProcessFlowName(lotData.getProcessFlowName());
			jobData.setProcessOperationName(lotData.getProcessOperationName());
			
			//destination
			jobData.setTargetOperationName(targetOperationName);
			
			//default state
			jobData.setJobState("Created");
			jobData.setJobProcessState("Waiting");
			
			jobData.setMachineName(machineName);
			
			jobData.setReturnFlowName("");
			jobData.setReturnOperationName("");
			
			//event info
			jobData.setLastEventName(eventInfo.getEventName());
			jobData.setLastEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			jobData.setLastEventTime(eventInfo.getEventTime());
			jobData.setLastEventUser(eventInfo.getEventUser());
			jobData.setLastEventComment(eventInfo.getEventComment());
		}
		
		jobData = ExtendedObjectProxy.getFirstGlassJobService().create(eventInfo, jobData);
		
		return jobData;
	}
	
	/**
	 * create pilot job latest
	 * @author swcho
	 * @since 2016.12.27
	 * @param eventInfo
	 * @param lotData
	 * @param machineName
	 * @param targetOperationName
	 * @param jobName
	 * @param inspectFlowName
	 * @return
	 * @throws CustomException
	 */
	public FirstGlassJob createJob(EventInfo eventInfo, Lot lotData, String machineName, String targetOperationName, String jobName, String inspectFlowName)
		throws CustomException
	{
		
		FirstGlassJob jobData = new FirstGlassJob(jobName);
		{
			jobData.setFactoryName(lotData.getFactoryName());
			jobData.setProductSpecName(lotData.getProductSpecName());
			jobData.setProcessFlowName(lotData.getProcessFlowName());
			jobData.setProcessOperationName(lotData.getProcessOperationName());
			
			//destination
			jobData.setTargetOperationName(targetOperationName);
			
			//process flow for inspection
			jobData.setInspectFlowName(inspectFlowName);
			
			//default state
			jobData.setJobState("Created");
			jobData.setJobProcessState("Waiting");
			
			jobData.setMachineName(machineName);
			
			jobData.setReturnFlowName("");
			jobData.setReturnOperationName("");
			
			//event info
			jobData.setLastEventName(eventInfo.getEventName());
			jobData.setLastEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			jobData.setLastEventTime(eventInfo.getEventTime());
			jobData.setLastEventUser(eventInfo.getEventUser());
			jobData.setLastEventComment(eventInfo.getEventComment());
		}
		
		jobData = ExtendedObjectProxy.getFirstGlassJobService().create(eventInfo, jobData);
		
		return jobData;
	}
	
	/**
	 * validate wheather Lot is clear
	 * @author swcho
	 * @since 2016.09.01
	 * @param lotName
	 * @throws CustomException
	 */
	public void validateOccupiedJob4Lot(String lotName) throws CustomException
	{
		/*SELECT J.jobName, J.factoryName, J.processFlowName, J.processOperationName, J.machineName,
		       J.targetOperationName,
		       J.jobState, J.jobProcessState,
		       L.lotName, L.carrierName, L.parentLotName
		    FROM CT_FirstGlassJob J, CT_FirstGlassLot L
		WHERE J.jobName = L.jobName
		    AND L.lotName = ?
		    AND J.jobState NOT IN (?, ?)*/
		
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("SELECT J.jobName, J.factoryName, J.processFlowName, J.processOperationName, J.machineName,").append("\n")
				.append("       J.targetOperationName,").append("\n")
				.append("       J.jobState, J.jobProcessState,").append("\n")
				.append("       L.lotName, L.carrierName, L.parentLotName").append("\n")
				.append("    FROM CT_FirstGlassJob J, CT_FirstGlassLot L").append("\n")
				.append("WHERE J.jobName = L.jobName").append("\n")
				.append("    AND L.lotName = ?").append("\n")
				.append("    AND J.jobState NOT IN (?, ?)").append("\n");
		
		Object[] bindList = new Object[] {lotName, "Completed", "Canceled"};
		
		try
		{
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bindList);
			
			if (result.size() > 0)
				throw new CustomException("SYS-9999", "FirstGlassLot", String.format("Lot[%s] is occupied by any job already", lotName)); 
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "FirstGlassLot", fe.getMessage());
		}
	}
	
	/**
	 * validate wheather Machine is clear
	 * @author swcho
	 * @since 2016.09.05
	 * @param machineName
	 * @throws CustomException
	 */
	public void validateOccupiedJob4Machine(String machineName) throws CustomException
	{
		/*SELECT J.jobName, J.factoryName, J.processFlowName, J.processOperationName, J.machineName,
		       J.targetOperationName,
		       J.jobState, J.jobProcessState,
		       L.lotName, L.carrierName, L.parentLotName
		    FROM CT_FirstGlassJob J, CT_FirstGlassLot L
		WHERE J.jobName = L.jobName
		    AND L.lotName = ?
		    AND J.jobState NOT IN (?, ?)*/
		
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("SELECT J.jobName, J.factoryName, J.processFlowName, J.processOperationName, J.machineName,").append("\n")
				.append("       J.targetOperationName,").append("\n")
				.append("       J.jobState, J.jobProcessState").append("\n")
				.append("    FROM CT_FirstGlassJob J").append("\n")
				.append("WHERE 1=1").append("\n")
				.append("    AND J.machineName = ?").append("\n")
				.append("    AND J.jobState NOT IN (?, ?, ?)").append("\n");
		
		//170119 by swcho : multi-reserve pilot job enable
		Object[] bindList = new Object[] {machineName, "Created", "Completed", "Canceled"};
		
		try
		{
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bindList);
			
			if (result.size() > 0)
				throw new CustomException("SYS-9999", "FirstGlassJob", String.format("Machine[%s] is occupied by any job already", machineName)); 
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "FirstGlassJob", fe.getMessage());
		}
	}
	
	
	/**
	 * judge Pilot job
	 * @author swcho
	 * @since 2016.09.10
	 * @param eventInfo
	 * @param jobName
	 * @param judge
	 * @param actionName
	 * @return
	 * @throws CustomException
	 */
	public FirstGlassJob changeJudge(EventInfo eventInfo, String jobName, String judge, String actionName)
		throws CustomException
	{
		FirstGlassJob jobData = ExtendedObjectProxy.getFirstGlassJobService().selectByKey(false, new Object[] {jobName});
		
		jobData.setJudge(judge);
		
		//set history
		jobData.setLastEventName(eventInfo.getEventName());
		jobData.setLastEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		jobData.setLastEventTime(eventInfo.getEventTime());
		jobData.setLastEventUser(eventInfo.getEventUser());
		jobData.setLastEventComment(eventInfo.getEventComment());
				
		jobData = ExtendedObjectProxy.getFirstGlassJobService().modify(eventInfo, jobData);
		
		return jobData;
	}
	
	/**
	 * change job state or process state
	 * @author swcho
	 * @since 2016.09.01
	 * @param eventInfo
	 * @param jobName
	 * @param jobState
	 * @param jobProcessState
	 * @return
	 * @throws CustomException
	 */
	public FirstGlassJob changeJobState(EventInfo eventInfo, String jobName, String jobState, String jobProcessState)
		throws CustomException
	{
		//Select JobData
		FirstGlassJob jobData = ExtendedObjectProxy.getFirstGlassJobService().selectByKey(false, new Object[] {jobName});
				
		if (!jobState.isEmpty())
			jobData.setJobState(jobState);
		
		if (!jobProcessState.isEmpty())
			jobData.setJobProcessState(jobProcessState);
		
		//set history
		jobData.setLastEventName(eventInfo.getEventName());
		jobData.setLastEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		jobData.setLastEventTime(eventInfo.getEventTime());
		jobData.setLastEventUser(eventInfo.getEventUser());
		jobData.setLastEventComment(eventInfo.getEventComment());
				
		jobData = ExtendedObjectProxy.getFirstGlassJobService().modify(eventInfo, jobData);
		
		return jobData;
	}
	
	/**
	 * job state validation
	 * @author swcho
	 * @since 2016.09.01
	 * @param jobData
	 * @param stats
	 * @throws CustomException
	 */
	public void validateJobState(FirstGlassJob jobData, String... stats)
		throws CustomException
	{
		boolean pass = false;
		
		for (String state : stats)
		{
			if (jobData.getJobState().equals(state))
			{
				pass = true;
			}
		}
		
		if (!pass)
		{
			StringBuilder states = new StringBuilder();
			for (String stat : stats)
			{
				states.append(stat).append(" ");
			}
			
			throw new CustomException("SYS-9999", "FirstGlassJob", String.format("Job state is not [%s]", states.toString()));
		}
			
	}
	
	/**
	 * job process state validation
	 * @author swcho
	 * @since 2016.09.01
	 * @param jobData
	 * @param processStats
	 * @throws CustomException
	 */
	public void validateJobProcessState(FirstGlassJob jobData, String... processStats)
		throws CustomException
	{
		boolean pass = false;
		
		for (String state : processStats)
		{
			if (jobData.getJobProcessState().equals(state))
			{
				pass = true;
			}
		}
		
		if (!pass)
		{
			StringBuilder states = new StringBuilder();
			for (String stat : processStats)
			{
				states.append(stat).append(" ");
			}
			
			throw new CustomException("SYS-9999", "FirstGlassJob", String.format("Job process state is not [%s]", states.toString()));
		}
			
	}
	
	/**
	 * active job from target EQP
	 * @author swcho
	 * @since 2016.09.10
	 * @param factoryName
	 * @param machineName
	 * @return
	 * @throws CustomException
	 */
	public String getActiveJobByMachine(String factoryName, String machineName) throws CustomException
	{
		String query = "machineName = ? AND jobState IN (?, ?) ";
		Object[] bindList = new Object[] {machineName, "Created", "Released"};
		
		List<FirstGlassJob> result;
		try
		{
			result = ExtendedObjectProxy.getFirstGlassJobService().select(query, bindList);
			
			//active job is only one per Machine
			String jobName = result.get(0).getJobName();
			
			return jobName;
		}
		catch (NotFoundSignal ne)
		{
			result = new ArrayList<FirstGlassJob>();
			return "";
		}
	}
	
	/**
	 * get verified PRD spec changed latest
	 * @author swcho
	 * @since 2016.11.01
	 * @param factoryName
	 * @param processFlowName
	 * @param processOperationName
	 * @param machineName
	 * @return
	 * @throws CustomException
	 */
	public String getJobSpecName(String factoryName, String processFlowName, String processOperationName, String machineName)
		throws CustomException
	{
		//161130 by swcho : last passed job search
		String query = "jobState = ? AND factoryName = ? AND processFlowName = ? AND processOperationName = ? AND machineName = ? AND judge = ? ORDER BY lastEventTime DESC ";
		Object[] bindList = new Object[] {"Completed", factoryName, processFlowName, processOperationName, machineName, "OK" };
		
		List<FirstGlassJob> result;
		try
		{
			result = ExtendedObjectProxy.getFirstGlassJobService().select(query, bindList);
			
			//last verified job changed PRD spec
			return result.get(0).getProductSpecName();
		}
		catch (NotFoundSignal ne)
		{
			result = new ArrayList<FirstGlassJob>();
			return "";
		}
	}
	
	/**
	 * pilot job by model, step and tool
	 * @author swcho
	 * @since 2016.01.17
	 * @param factoryName
	 * @param productSpecName
	 * @param processFlowName
	 * @param processOperationName
	 * @param machineName
	 * @return
	 * @throws CustomException
	 */
	public List<FirstGlassJob> getPilotJob(String factoryName, String productSpecName, String processFlowName, String processOperationName, String machineName)
		throws CustomException
	{
		String query = "jobState IN (?, ?) AND factoryName = ? AND productSpecName = ? AND processFlowName = ? AND processOperationName = ? AND machineName = ? ORDER BY lastEventTime DESC ";
		Object[] bindList = new Object[] {"Released", "Completed", factoryName, productSpecName, processFlowName, processOperationName, machineName };
		
		List<FirstGlassJob> result;
		try
		{
			result = ExtendedObjectProxy.getFirstGlassJobService().select(query, bindList);
		}
		catch (NotFoundSignal ne)
		{
			result = new ArrayList<FirstGlassJob>();
		}
		
		return result;
	}	
	
	/**
	 * pilot job by model, step and tool but last verified
	 * @author swcho
	 * @since 2017.02.14
	 * @param factoryName
	 * @param productSpecName
	 * @param processFlowName
	 * @param processOperationName
	 * @param machineName
	 * @return
	 * @throws CustomException
	 */
	public List<FirstGlassJob> getPilotJobV2(String factoryName, String productSpecName, String processFlowName, String processOperationName, String machineName) throws CustomException
	{
		//170216 by swcho : trace double-confirm done
		String query = "jobState IN (?, ?) AND factoryName = ? AND productSpecName = ? AND processFlowName = ? AND processOperationName = ? AND machineName = ? AND judge = ? ORDER BY lastEventTimeKey DESC ";
		Object[] bindList = new Object[] {"Released", "Completed", factoryName, productSpecName, processFlowName, processOperationName, machineName, "OK" };
		
		List<FirstGlassJob> result;
		try
		{
			result = ExtendedObjectProxy.getFirstGlassJobService().select(query, bindList);
		}
		catch (NotFoundSignal ne)
		{
			result = new ArrayList<FirstGlassJob>();
		}
		
		return result;
	}	
	
	/**
	 * Created job validation on pilot
	 * @author yudan
	 * @since 2017.02.28
	 * @param factoryName
	 * @param productSpecName
	 * @param processFlowName
	 * @param processOperationName
	 * @param lotName
	 * @return
	 * @throws CustomException
	 */
	public String checkCreatedPilotJob(String lotName) throws CustomException
	{
		
		/*SELECT J.jobName, J.factoryName, J.machineName,
	       J.jobState, J.jobProcessState, L.activeState,
	       L.lotName, L.carrierName, L.parentLotName
	       L.lotName, L.parentLotName
	FROM CT_FirstGlassJob J, CT_FirstGlassLot L
	WHERE J.jobName = L.jobName
	AND L.lotName = :LOTNAME
	AND J.jobState = :JOBSTATE*/ 
		
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("SELECT J.jobName, J.factoryName, J.machineName, ").append("\n")
				.append("       J.jobState, J.jobProcessState, L.activeState, ").append("\n")
				.append("       L.lotName, L.carrierName, L.parentLotName ").append("\n")
				.append("    FROM CT_FirstGlassJob J, CT_FirstGlassLot L ").append("\n")
				.append("WHERE J.jobName = L.jobName ").append("\n")
				.append("    AND L.lotName = :LOTNAME ").append("\n")
				.append("    AND J.jobState = :JOBSTATE ");
		
		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("JOBSTATE", "Created");
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
	 * OnJudge job validation on pilot
	 * @author yudan
	 * @since 2017.08.30
	 * @param factoryName
	 * @param productSpecName
	 * @param processFlowName
	 * @param processOperationName
	 * @param lotData
	 * @return
	 * @throws CustomException
	 */
	public String getPilotLotTargetOper(Lot lotData) throws CustomException
	{
		
		/*SELECT P.JOBNAME, P.FACTORYNAME, P.PROCESSFLOWNAME,
       P.PROCESSOPERATIONNAME,P.TARGETOPERATIONNAME,
       P.JOBSTATE,P.MACHINENAME
  FROM CT_FIRSTGLASSLOT L, CT_FIRSTGLASSJOB P
 WHERE L.LOTNAME = :lotName 
 AND L.JOBNAME = P.JOBNAME*/ 
		
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("SELECT P.JOBNAME, P.FACTORYNAME, P.PROCESSFLOWNAME, ").append("\n")
				.append("       P.PROCESSOPERATIONNAME,P.TARGETOPERATIONNAME, ").append("\n")
				.append("       P.JOBSTATE,P.MACHINENAME ").append("\n")
				.append("    FROM CT_FIRSTGLASSLOT L, CT_FIRSTGLASSJOB P ").append("\n")
				.append("WHERE L.LOTNAME = :lotName ").append("\n")
				.append("    AND L.JOBNAME = P.JOBNAME ").append("\n");
		
		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		
		bindMap.put("lotName", lotData.getKey().getLotName());
		
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
				return CommonUtil.getValue(result.get(0), "TARGETOPERATIONNAME");
			}
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "FirstGlassLot", fe.getMessage());
		}
	}
	
	/**
	 * post validation on pilot
	 * @author swcho
	 * @since 2017.01.24
	 * @param lotData
	 * @param machineName
	 * @throws CustomException
	 */
	public void validateLotProcessingPost(Lot lotData, String machineName) throws CustomException
	{
		//post validation for pilot Lot
		String jobName = ExtendedObjectProxy.getFirstGlassLotService().getActiveJobNameByLotName(lotData.getKey().getLotName());
		
		if (!jobName.isEmpty())
		{
			FirstGlassJob jobData = ExtendedObjectProxy.getFirstGlassJobService().selectByKey(false, new Object[] {jobName});
			
			FirstGlassLot onLotData = ExtendedObjectProxy.getFirstGlassLotService().selectByKey(false, new Object[] {jobName, lotData.getKey().getLotName()});
			
			//inactive Lot interlock
			if (!onLotData.getActiveState().equals("Active"))
			{
				throw new CustomException("SYS-9999", "FirstGlassJob", String.format("Lot[%s] is not valid for current First Glass job[%s] in ProcessState[%s]",
											lotData.getKey().getLotName(), jobName, jobData.getJobProcessState()));
			}
			
			//primary step tool check
			if (lotData.getProcessFlowName().equals(jobData.getProcessFlowName())
					&& lotData.getProcessOperationName().equals(jobData.getProcessOperationName()))
			{
				if (!jobData.getMachineName().equals(machineName))
				{
					throw new CustomException("SYS-9999", "FirstGlassJob", String.format("Machine[%s] is not enable on First Glass Job processing for Lot[%s]",
												machineName, lotData.getKey().getLotName()));
				}
			}
		}
	}
	
	/**
	 * judge Carrier whether is on pilot job
	 * @author swcho
	 * @since 2016.11.17
	 * @param carrierName
	 * @return
	 * @throws CustomException
	 */
	public boolean isOnPilotJob(String carrierName) throws CustomException
	{
		String pilotLotName = ExtendedObjectProxy.getFirstGlassLotService().getActiveLotNameByCarrier(carrierName);
		List<Lot> lotList = CommonUtil.getLotListByCarrier(carrierName, false);
		
		if (!pilotLotName.isEmpty())
		{
			return true;
		}		
		
		//Multi Lot in Bank case Check		
		if(lotList != null && lotList.size() > 1)
		{			
			for (Lot lotData : lotList) 
			{
				ProcessOperationSpecKey poKey = new ProcessOperationSpecKey(lotData.getFactoryName(), lotData.getProcessOperationName(),lotData.getProcessOperationVersion());
				ProcessOperationSpec poSpec = ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(poKey);
				
				if(poSpec.getDetailProcessOperationType().equals("BANK"))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * judge whether job is on inspection
	 * @author swcho
	 * @since 2016.12.27
	 * @param jobData
	 * @return
	 */
	public boolean isOnPilotInspection(FirstGlassJob jobData, Lot lotData)
	{
		String factoryName = jobData.getFactoryName();
		String productSpecName = jobData.getProductSpecName();
		String processFlowName = jobData.getProcessFlowName();
		//String processOperationName = jobData.getProcessOperationName();
		//170210 by swcho : check all sampling reservation on pilot at current location
		String processOperationName = lotData.getProcessOperationName();
		String inspectFlowName = jobData.getInspectFlowName();
		String sampleFlag = "Y";
		
		StringBuffer sqlBuffer = new StringBuffer()
										.append("SELECT DISTINCT factoryName, toProcessFlowName").append("\n")
										.append("    FROM CT_FlowSampleLot").append("\n")
										.append(" WHERE factoryName = ?").append("\n")
										.append("    AND productSpecName = ?").append("\n")
										.append("    AND processFlowName = ?").append("\n")
										.append("    AND processOperationName = ?").append("\n")
										.append("    AND toProcessFlowName = ?").append("\n")
										.append("    AND lotName = ?").append("\n")
										.append("    AND lotSampleFlag = ?");
		
		Object[] bindList = new Object[] {factoryName, productSpecName, processFlowName, processOperationName, inspectFlowName, lotData.getKey().getLotName(), sampleFlag};
		
		try
		{
			/** 20180212 NJPARK 
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bindList);
			
			if (result.size() > 0)
				return true;
			else 
				return false;
				*/
			return false;
		}
		catch (FrameworkErrorSignal fe)
		{
			logger.error(fe);
			return false;
		}
	}
}
