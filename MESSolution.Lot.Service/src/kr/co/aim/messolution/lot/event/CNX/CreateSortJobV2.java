package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SortJob;
import kr.co.aim.messolution.extended.object.management.data.SortJobCarrier;
import kr.co.aim.messolution.extended.object.management.data.SortJobProduct;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.util.sys.SystemPropHelper;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.collections.map.ListOrderedMap;
import org.jdom.Document;
import org.jdom.Element;

public class CreateSortJobV2 extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String fromcst = SMessageUtil.getBodyItemValue(doc, "FROMCST", true);
		String tocst = SMessageUtil.getBodyItemValue(doc, "TOCST", true);
		String toProcessFlowName = SMessageUtil.getBodyItemValue(doc, "TOPROCESSFLOWNAME", true);
		String toProcessOperationName = SMessageUtil.getBodyItemValue(doc, "TOPROCESSOPERATIONNAME", true);
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		
		String jobType = SMessageUtil.getBodyItemValue(doc, "JOBTYPE", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String jobName = machineName.substring(0, 5) + eventInfo.getEventTimeKey().substring(2, 14);
		//String Validationflag = SMessageUtil.getBodyItemValue(doc, "VALIDATIONFLAG", true);
		List<Element> fromLotList = SMessageUtil.getBodySequenceItemList(doc, "FROMLOTLIST", true);
				
		this.CarrierValidation(fromcst);
		
		this.CarrierValidation(tocst);
		
//		if(StringUtil.equals(Validationflag.toUpperCase(), "Y"))
//		{
//			this.checkMixValidation(lotList);
//		}
		this.checkMixValidation(lotList);
		
		eventInfo.setEventComment("SortJobBatchAction");
		
		this.ChangeOperationForSort(eventInfo, lotList, toProcessFlowName, toProcessOperationName);
		
		this.CreateSorterJob(eventInfo, doc, machineName, jobName, jobType, toProcessFlowName, toProcessOperationName, fromLotList);
		
		this.ReserveSortJob(eventInfo, machineName, jobName, jobType);
		
		try
		{
			//downloadFirstJob(eventInfo, doc, machineName);
		}
		catch (Exception ex)
		{
			eventLog.warn("First job download failed");
		}
		
		return doc;
	}
	
	private void CarrierValidation(String CarrierName)
					throws CustomException
	{
		DurableKey durableKey = new DurableKey();	
		durableKey.setDurableName(CarrierName);	
		Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);		
		if(StringUtil.equals(durableData.getUdfs().get("DURABLEHOLDSTATE").toUpperCase(), "Y"))
			throw new CustomException("CST-0005", CarrierName);
		
		if(StringUtil.equals(durableData.getDurableState().toUpperCase(), "SCRAPPED"))
			throw new CustomException("CST-0007", CarrierName);
		
	}
	
	
	private void ChangeOperationForSort(EventInfo eventInfo, List<Element> lotList,
			String toProcessFlowName, String toProcessOperationName)
					throws CustomException
	{
		eventInfo.setEventName("ChangeOper");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		List<String> compareData = new ArrayList<String>();
		
		for ( Element lotE : lotList)
		{
			List<Element> sorterLotList = SMessageUtil.getSubSequenceItemList(lotE, "SORTLOTLIST", false);
			
			for (Element eleLot : sorterLotList) 
			{
				String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
				Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
				
				if(compareData != null && compareData.size() > 0 && compareData.contains(lotName))
				{
					continue;					
				}
				else
				{
					compareData.add(lotName);
				}
				
				List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotName);
				
				//2017.7.20 zhongsl  when Operation Changed, Update Product ProcessingInfo to N
				productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfsProcessingInfo(productUdfs,"");
				
				//2018.11.01 Modify, hsryu, lotData.getProductRequestName -> "", Because WorkOrder of Product must not be changed.
				ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotName,
												lotData.getProductionType(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProductSpec2Name(), lotData.getProductSpec2Version(),
												"", lotData.getSubProductUnitQuantity1(), lotData.getSubProductQuantity2(), lotData.getDueDate(), lotData.getPriority(),
												lotData.getFactoryName(), lotData.getAreaName(), lotData.getLotState(), lotData.getLotProcessState(), lotData.getLotHoldState(),
												lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
												toProcessFlowName, toProcessOperationName, lotData.getProcessFlowName(), lotData.getProcessOperationName(), "",
												lotData.getUdfs(), productUdfs,
												true,true);
				
				lotData = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
			}
		}
	}

	private void CreateSorterJob(EventInfo eventInfo, Document doc, String machineName, String jobName,
			String jobType, String processFlowName, String processOperationName, List<Element> fromLotList)
			throws CustomException
	{
		eventInfo.setEventName("CreateSortJob");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		StringBuilder strComment = new StringBuilder();
		
		SortJob sortJob = new SortJob(jobName);
		SortJobCarrier sortCST;
		SortJobProduct sortPrt;
		
		sortJob.setJobName(jobName);
		sortJob.setJobState(GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CREATE);
		sortJob.setJobType(jobType);
		sortJob.setMachineName(machineName);
		sortJob.setTimeKey(eventInfo.getEventTimeKey());
		sortJob.setEventTime(eventInfo.getEventTime());
		sortJob.setEventName(eventInfo.getEventName());
		sortJob.setEventUser(eventInfo.getEventUser());
		sortJob.setEventComment(eventInfo.getEventComment());
		sortJob.setCreateTime(eventInfo.getEventTime());
		sortJob.setProcessFlowName(processFlowName);
		sortJob.setProcessOperationName(processOperationName);
		
		ExtendedObjectProxy.getSortJobService().create(eventInfo, sortJob);
		
		List<String> insertedCarrierList = new ArrayList<String>();
		
		for(Element fromLotE : fromLotList)
		{
			String fromLotName = fromLotE.getChild("LOTNAME").getText();
			String fromCarrierName = fromLotE.getChild("CARRIERNAME").getText();
			String fromPortName = fromLotE.getChild("PORTNAME").getText();			

			if(!insertedCarrierList.contains(fromCarrierName))
			{
				sortCST = new SortJobCarrier(jobName,fromCarrierName);
				
				sortCST.setLotName(fromLotName);
				sortCST.setMachineName(machineName);
				sortCST.setPortName(fromPortName);
				if(StringUtil.equals(jobType.toUpperCase(), "SPLIT") || StringUtil.equals(jobType.toUpperCase(), "MERGE"))
				{
					sortCST.setTransferDirection(GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_SOURCE);
				}
				else if(StringUtil.equals(jobType.toUpperCase().substring(0, 4), "SLOT"))
				{
					sortCST.setTransferDirection(GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_BOTH);
				}
				else
				{
					sortCST.setTransferDirection(GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_ALONE);
				}
				sortCST.setLoadFlag("");
				sortCST.setLoadTimeKey("");				
				
				ExtendedObjectProxy.getSortJobCarrierService().create(eventInfo, sortCST);
				
				insertedCarrierList.add(fromCarrierName);
			}
			
			List<Element> sorterProductList = SMessageUtil.getSubSequenceItemList(fromLotE, "SORTERPRODUCTLIST", true);
			
			for(Element sorterProductE : sorterProductList)
			{
				String toCarrierName = sorterProductE.getChild("TOCARRIERNAME").getText();
				String toPortName = sorterProductE.getChild("TOPORTNAME").getText();
				
				sortPrt = new SortJobProduct(jobName, sorterProductE.getChild("PRODUCTNAME").getText());
				sortPrt.setMachineName(machineName);
				sortPrt.setFromLotName(fromLotName);
				sortPrt.setFromCarrierName(fromCarrierName);
				sortPrt.setFromPortName(fromPortName);
				sortPrt.setFromPosition(sorterProductE.getChild("FROMPOSITION").getText());
				sortPrt.setToLotName("");
				sortPrt.setToCarrierName(toCarrierName);
				sortPrt.setToPortName(sorterProductE.getChild("TOPORTNAME").getText());
				sortPrt.setToPosition(sorterProductE.getChild("TOPOSITION").getText());
				sortPrt.setSortProductState(GenericServiceProxy.getConstantMap().SORT_SORTPRODUCTSTATE_READY);
				sortPrt.setTurnSideFlag(sorterProductE.getChild("TURNSIDEFLAG").getText());
				sortPrt.setScrapFlag(sorterProductE.getChild("SCRAPFLAG").getText());
				sortPrt.setTurnOverFlag(sorterProductE.getChild("TURNOVERFLAG").getText());
				sortPrt.setOutStageFlag(sorterProductE.getChild("OUTSTAGEFLAG").getText());
				
				if(!insertedCarrierList.contains(toCarrierName))
				{
					sortCST = new SortJobCarrier(jobName,toCarrierName);
					
					sortCST.setLotName("");
					sortCST.setMachineName(machineName);
					sortCST.setPortName(toPortName);
					if(StringUtil.equals(jobType.toUpperCase(), "SPLIT") || StringUtil.equals(jobType.toUpperCase(), "MERGE"))
					{
						sortCST.setTransferDirection(GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_TARGET);
					}
					else if(StringUtil.equals(jobType.toUpperCase().substring(0, 4), "SLOT"))
					{
						sortCST.setTransferDirection(GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_BOTH);
					}
					else
					{
						sortCST.setTransferDirection(GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_ALONE);
					}
					sortCST.setLoadFlag("");
					sortCST.setLoadTimeKey("");				
					
					ExtendedObjectProxy.getSortJobCarrierService().create(eventInfo, sortCST);
					
					insertedCarrierList.add(toCarrierName);
				}
				
				ExtendedObjectProxy.getSortJobProductService().create(eventInfo, sortPrt);
			}
			
			strComment.append("SortJobName").append("[").append(jobName).append("]").append("\n");
			
			this.setNextInfo(doc, strComment);
		}
	}

	private void setNextInfo(Document doc, StringBuilder strComment)
			throws CustomException
	{
		try
		{			
			SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, strComment.toString());
		}
		catch (Exception ex)
		{
			eventLog.warn("Note after SortJob is nothing");
		}
	}
	
	private void checkMixValidation(List<Element> lotList) throws CustomException
	{	
		
		for (Element eleSort : lotList) 
		{
			List<Lot> compareData = new ArrayList<Lot>();
			
			List<Element> sorterLotList = SMessageUtil.getSubSequenceItemList(eleSort, "SORTLOTLIST", false);
			
			for (Element eleLot : sorterLotList) 
			{				
				String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
				String cstName = SMessageUtil.getChildText(eleLot, "CARRIERNAME", true);
				Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
				
				//validation 
				CommonValidation.checkAlreadyLotProcessStateTrackIn(lotData);
				
				if(compareData == null || compareData.size() < 1)
				{
					compareData.add(lotData);
				}
				
				if(!StringUtil.equals(compareData.get(0).getProductSpecName(), lotData.getProductSpecName()))
				{
					throw new CustomException("SYS-9999", "Lot", "Different ProductSpec[" + cstName + " / " + lotName + "]");
				}
				else if(!StringUtil.equals(compareData.get(0).getProductRequestName(), lotData.getProductRequestName()))
				{
					throw new CustomException("SYS-9999", "Lot", "Different WorkOrder[" + cstName + " / " + lotName + "]");
				}
				else if(!StringUtil.equals(compareData.get(0).getProcessFlowName(), lotData.getProcessFlowName()))
				{
					throw new CustomException("SYS-9999", "Lot", "Different ProcessFlow[" + cstName + " / " + lotName + "]");
				}
				else if(!StringUtil.equals(compareData.get(0).getProcessOperationName(), lotData.getProcessOperationName()))
				{
					throw new CustomException("SYS-9999", "Lot", "Different ProcessOperation[" + cstName + " / " + lotName + "]");
				}
			}
		}
	}

	private void ReserveSortJob(EventInfo eventInfo, String machineName, String jobName, String jobType)
			throws CustomException
	{
		eventInfo.setEventName("ReserveSortJob");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		SortJob sortJob;
		
		String sequence = this.getlastSortJobSequence(machineName);			
		sortJob = ExtendedObjectProxy.getSortJobService().selectByKey(false, new Object[] {jobName});			
		sortJob.setSeq(Integer.valueOf(sequence) + 1);
		sortJob.setJobState(GenericServiceProxy.getConstantMap().SORT_JOBSTATE_WAIT);
		sortJob.setEventTime(eventInfo.getEventTime());
		sortJob.setEventName(eventInfo.getEventName());
		sortJob.setEventUser(eventInfo.getEventUser());
		sortJob.setEventComment(eventInfo.getEventComment());			
		ExtendedObjectProxy.getSortJobService().modify(eventInfo, sortJob);
		
		String NewPort = "";
		String toNewPort = "";
		String sql = "SELECT PT.PORTNAME";
		sql += " FROM PORT P, PORTSPEC PT";
		sql += " WHERE PT.MACHINENAME = P.MACHINENAME";
		sql += " AND P.PORTNAME = PT.PORTNAME";
		sql += " AND P.FACTORYNAME = PT.FACTORYNAME";
		sql += " AND PT.MACHINENAME = :MACHINENAME";
		sql += " ORDER BY PT.PORTNAME";
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("MACHINENAME", machineName);
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> NewsqlResult = GenericServiceProxy
				.getSqlMesTemplate().queryForList(sql, bindMap);
		
		if (StringUtil.equals(jobType.toUpperCase(), "SPLIT")
				|| StringUtil.equals(jobType.toUpperCase(), "MERGE")) {
			if (NewsqlResult.size() > 1) {
				NewPort = NewsqlResult.get(0).get("portname")
						.toString();

				// from port name
				String Ccondition = "where jobname=?"
						+ " and transferdirection = ?";
				Object[] CbindSet = new Object[] {
						jobName,"TARGET" };
				List<SortJobCarrier> SortJobCarrier = ExtendedObjectProxy
						.getSortJobCarrierService().select(Ccondition,
								CbindSet);

				for (SortJobCarrier Carrier : SortJobCarrier) {
					Carrier.setPortName(NewPort);
					ExtendedObjectProxy.getSortJobCarrierService()
							.modify(eventInfo, Carrier);
				}

				String condition = "where jobname=?";
				Object[] bindSet = new Object[] {jobName};
				List<SortJobProduct> SortJobProductList = ExtendedObjectProxy
						.getSortJobProductService().select(condition,
								bindSet);

				// to port name
				toNewPort = NewsqlResult.get(1).get("portname")
						.toString();

				Object[] CCbindSet = new Object[] {
						jobName,"SOURCE" };
				SortJobCarrier = ExtendedObjectProxy
						.getSortJobCarrierService().select(Ccondition,
								CCbindSet);

				for (SortJobCarrier Carrier : SortJobCarrier) {
					Carrier.setPortName(toNewPort);
					ExtendedObjectProxy.getSortJobCarrierService()
							.modify(eventInfo, Carrier);
				}

				for (SortJobProduct product : SortJobProductList) {
					product.setToPortName(toNewPort);
					product.setFromPortName(NewPort);
					ExtendedObjectProxy.getSortJobProductService()
							.modify(eventInfo, product);
				}
			}

			else {
				throw new CustomException("PORT-9004");
			}	
		}
		
		else {
			// only one port case
			if (NewsqlResult.size() > 0) {
				NewPort = NewsqlResult.get(0).get("portname")
						.toString();

				String Ccondition = "where jobname=?";
				Object[] CbindSet = new Object[] {jobName};
				List<SortJobCarrier> SortJobCarrier = ExtendedObjectProxy
						.getSortJobCarrierService().select(Ccondition,CbindSet);

				for (SortJobCarrier Carrier : SortJobCarrier) {
					Carrier.setPortName(NewPort);
					ExtendedObjectProxy.getSortJobCarrierService()
							.modify(eventInfo, Carrier);
				}

				String condition = "where jobname=?";
				Object[] bindSet = new Object[] {jobName};
				List<SortJobProduct> SortJobProductList = ExtendedObjectProxy
						.getSortJobProductService().select(condition,bindSet);

				for (SortJobProduct product : SortJobProductList) {
					product.setToPortName(NewPort);
					product.setFromPortName(NewPort);
					ExtendedObjectProxy.getSortJobProductService()
							.modify(eventInfo, product);
				}
			} else {
				throw new CustomException("PORT-9004");
			}
		}
		
	}
	
	private String getlastSortJobSequence(String machineName)
			throws CustomException
	{
		String NEWLINE = SystemPropHelper.CR;
		
		StringBuffer sqlBuffer = new StringBuffer("")
			.append(" SELECT NVL(MAX(SEQ), 0) AS SEQ").append(NEWLINE)
			.append("   FROM CT_SORTJOB ").append(NEWLINE)
			.append("  WHERE 1=1 ").append(NEWLINE)
			.append("	 AND JOBSTATE = ? ").append(NEWLINE)
			.append("    AND MACHINENAME = ? ").append(NEWLINE)
			.append("").append(NEWLINE);
		
		String sqlStmt = sqlBuffer.toString();
		
		Object[] bindSet = new String[]{GenericServiceProxy.getConstantMap().SORT_JOBSTATE_WAIT, machineName};
		try
		{
			List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmt, bindSet);
		
			if(sqlResult.size() > 0)
			{
				return sqlResult.get(0).get("SEQ").toString();
			}
			else
			{
				throw new CustomException("SYS-9001", "Error");
			}
		}
		catch (FrameworkErrorSignal de)
		{
			throw new CustomException("SYS-9999", "Error", de.getMessage());
		}
	}
	
	private void downloadFirstJob(EventInfo eventInfo, Document doc, String machineName) throws CustomException
	{
		List<SortJob> result = ExtendedObjectProxy.getSortJobService().getReservedJobList(machineName);
		
		if (result.size() == 1)
		{
			SortJob jobData = result.get(0);
			
			if (jobData.getJobState().equals(GenericServiceProxy.getConstantMap().SORT_JOBSTATE_WAIT)
					|| jobData.getJobState().equals(GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CANCELED))
			{
				eventLog.info("Download first job");
				
				ExtendedObjectProxy.getSortJobService().AutoDownloadSortJob(eventInfo, doc, machineName);
			}
		}
	}
}

