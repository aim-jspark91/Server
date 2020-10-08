package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LotAction;
import kr.co.aim.messolution.extended.object.management.data.NonSortJobProduct;
import kr.co.aim.messolution.extended.object.management.data.SortJob;
import kr.co.aim.messolution.extended.object.management.data.SortJobCarrier;
import kr.co.aim.messolution.extended.object.management.data.SortJobProduct;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.product.management.data.Product;

import org.jdom.Document;
import org.jdom.Element;

public class CreateSortJob extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateSortJob", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));	
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		
		//2019.01.16_hsryu
		List<String> allCarrierList = new ArrayList<String>();
		List<String> allProductList = new ArrayList<String>();
		
		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("MACHINENAME", machineName.substring(0, 5));
		
		String jobName = CommonUtil.generateNameByNamingRule("ArraySortNamingRule", nameRuleAttrMap, Integer.parseInt("1")).get(0);
		
		String jobType = SMessageUtil.getBodyItemValue(doc, "JOBTYPE", false);
		String processFlow = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String operationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String DetailJobType = SMessageUtil.getBodyItemValue(doc, "DETAILJOBTYPE", true);
		StringBuilder strComment = new StringBuilder();
		
		SortJob sortJob = new SortJob(jobName);
		SortJobCarrier sortCST;
		SortJobProduct sortPrt;
		
		String PortFlag = "";
		String Tcount = SMessageUtil.getBodyItemValue(doc, "TOTALCOUNT", true);
		
		
		// modify by jhiying on 20191006 mantis :4887 start
		List<Element> ValidationList = SMessageUtil.getBodySequenceItemList(doc, "VALIDATIONLIST", true);
		for(Element ValidationLot : ValidationList)
		{
		
		String CheckLot ="SELECT A.JOBNAME,A.CARRIERNAME,A.TRACKFLAG FROM CT_SORTJOBCARRIER A ,CT_SORTJOB B WHERE A.JOBNAME = B.JOBNAME AND A.CARRIERNAME = :CARRIERNAME AND NVL(TRACKFLAG, ' ') != 'OUT'  AND B.JOBSTATE NOT IN ('CANCELED','ENDED','ABORT')";	
		Map<String, String> CHECKMAP = new HashMap<String, String>();
		CHECKMAP.put("CARRIERNAME", ValidationLot.getChild("CARRIERNAME").getText());
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> CHECKResult = GenericServiceProxy.getSqlMesTemplate().queryForList(CheckLot,CHECKMAP);
		if(CHECKResult.size() > 0)
		 {
			throw new CustomException("SORT-0005", CHECKResult.get(0).get("CARRIERNAME").toString(),CHECKResult.get(0).get("JOBNAME").toString());
		 }
		}
					
		// modify by jhiying on 20191006 mantis :4887 end

		// Machine Seq setting;
		String sql = "SELECT SEQ FROM CT_SORTJOB WHERE MACHINENAME = :MACHINENAME AND JOBSTATE = :JOBSTATE ORDER BY SEQ DESC";
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("MACHINENAME", machineName);
		bindMap.put("JOBSTATE", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_WAIT);
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> SEQsqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		if( SEQsqlResult.size() > 0 )
		{	
			sortJob.setSeq(Integer.parseInt(SEQsqlResult.get(0).get("seq").toString()) + 1);
		}
		else {
			// port update
			sortJob.setSeq(1);
		}
		
		sortJob.setJobName(jobName);
		sortJob.setJobState(GenericServiceProxy.getConstantMap().SORT_JOBSTATE_WAIT);
		sortJob.setJobType(jobType);
		sortJob.setMachineName(machineName);
		sortJob.setTimeKey(eventInfo.getEventTimeKey());
		sortJob.setEventTime(eventInfo.getEventTime());
		sortJob.setEventName(eventInfo.getEventName());
		sortJob.setEventUser(eventInfo.getEventUser());
		sortJob.setEventComment(eventInfo.getEventComment());
		sortJob.setCreateTime(eventInfo.getEventTime());
		sortJob.setProcessFlowName(processFlow);
		sortJob.setProcessOperationName(operationName);
		sortJob.setDetailJobType(DetailJobType); // 2018.08.09 ADD
		
		ExtendedObjectProxy.getSortJobService().create(eventInfo, sortJob);
		
		/**** End Create SortJob & Create SortJobCarrierInfo, SortJobProductInfo ****/

		List<Element> fromLotList = SMessageUtil.getBodySequenceItemList(doc, "FROMLOTLIST", true);
		
		List<String> insertedCarrierList = new ArrayList<String>();
						
		for(Element fromLotE : fromLotList)
		{
			String fromLotName = fromLotE.getChild("LOTNAME").getText();
			String fromCarrierName = fromLotE.getChild("CARRIERNAME").getText();
			String fromPortName = fromLotE.getChild("PORTNAME").getText();			
			String SorterOutHold = "";
						
			if(!insertedCarrierList.contains(fromCarrierName))
			{
				//2019.01.16_hsryu
				allCarrierList.add(fromCarrierName);
				
				for( Element ValidationLot : ValidationList)
				{
					String SorterHoldCSTName = ValidationLot.getChild("CARRIERNAME").getText();
					if(StringUtil.equals(fromCarrierName,SorterHoldCSTName))
					{
						SorterOutHold = ValidationLot.getChild("HOLDFLAG").getText();
					}
				}
				
				sortCST = new SortJobCarrier(jobName,fromCarrierName);
				
				sortCST.setLotName(fromLotName);
				sortCST.setMachineName(machineName);
				sortCST.setPortName(fromPortName);
				sortCST.setoutholdflag(SorterOutHold); // 2018.08.17
				
				if (StringUtil.equals(jobType.toUpperCase(), "CHANGE")) 
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

			
			boolean MQCflag = false;
			for(Element ValidationLot : ValidationList)
			{
				String CHECKLOT = "SELECT PRODUCTIONTYPE, LOTNAME FROM LOT WHERE CARRIERNAME = :CARRIERNAME";
	    		Map<String, String> CHECKMAP = new HashMap<String, String>();
	    		CHECKMAP.put("CARRIERNAME", ValidationLot.getChild("CARRIERNAME").getText());
	    		@SuppressWarnings("unchecked")
	    		List<Map<String, Object>> CHECKResult = GenericServiceProxy.getSqlMesTemplate().queryForList(CHECKLOT,CHECKMAP);
	    		if(CHECKResult.size() > 0)
	    		{
	    			// Mentis 3372
	    			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(CHECKResult.get(0).get("LOTNAME").toString());	    			
	    			if(StringUtil.equals(lotData.getLotHoldState(), "Y"))
	    			{
	    				throw new CustomException("SORT-0010", CHECKResult.get(0).get("LOTNAME").toString());
	    			}
	    			
	    			// Add Validation about dual user of OPI request by Hongwei.
	    			if(!StringUtil.equals(lotData.getProcessFlowName(),processFlow))
	    			{
	    				throw new CustomException("SORT-0100", CHECKResult.get(0).get("LOTNAME").toString());
	    			}
	    			
	    			if(!StringUtil.equals(CHECKResult.get(0).get("PRODUCTIONTYPE").toString(),"MQCA"))
	    			{
	    				MQCflag = true;
	    			}
	    			else
	    			{
	    				// 2019.03.06 Mentis 2921 
	    				if (StringUtil.equals(jobType.toUpperCase(), "CHANGE") || StringUtil.equals(jobType.toUpperCase(), "SLOTMAPCHANGE")) 
	    				{
	    					if(MESLotServiceProxy.getLotServiceUtil().CheckMQCState(CHECKResult.get(0).get("LOTNAME").toString()))
	    					{
	    						throw new CustomException("MQC-0055");
	    					}
	    				}
	    			}
	    		}
			}
			
			List<Element> sorterProductList = SMessageUtil.getSubSequenceItemList(fromLotE, "SORTERPRODUCTLIST", true);

			List<Lot> compareData = new ArrayList<Lot>();
			for( Element ValidationLot : ValidationList)
			{
				String pass_validation = ValidationLot.getChild("PASSVALIDATION").getText();
				
				if(!StringUtil.equals(pass_validation,"Y"))
				{
					
					String CHECKLOT = "SELECT LOTNAME FROM LOT WHERE CARRIERNAME = :CARRIERNAME";
		    		Map<String, String> CHECKMAP = new HashMap<String, String>();
		    		CHECKMAP.put("CARRIERNAME", ValidationLot.getChild("CARRIERNAME").getText());
		    		@SuppressWarnings("unchecked")
		    		List<Map<String, Object>> CHECKResult = GenericServiceProxy.getSqlMesTemplate().queryForList(CHECKLOT,CHECKMAP);
		    		if( CHECKResult.size() > 0)
		    		{			
						Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(CHECKResult.get(0).get("LOTNAME").toString());
						if(compareData == null || compareData.size() < 1)
						{
							compareData.add(lotData);
						}
						
						if(!StringUtil.equals(compareData.get(0).getProductSpecName(), lotData.getProductSpecName()))
						{
							throw new CustomException("SYS-9999", "Lot", "Different ProductSpec[" + ValidationLot.getChild("CARRIERNAME").getText() + " / " + CHECKResult.get(0).get("LOTNAME").toString() + "]");
						}
						//2019.02.26_hsryu_Modify compare ProductionType -> WorkOrderType. 
						else if(!StringUtil.equals(CommonUtil.getWorkOrderType(compareData.get(0)), CommonUtil.getWorkOrderType(lotData)))
						{
							throw new CustomException("SYS-9999", "Lot", "Different WorkOrderType[" + ValidationLot.getChild("CARRIERNAME").getText() + " / " + CHECKResult.get(0).get("LOTNAME").toString() + "]");
						}
						else if(!StringUtil.equals(compareData.get(0).getUdfs().get("DEPARTMENTNAME"), lotData.getUdfs().get("DEPARTMENTNAME")))
						{
							throw new CustomException("SYS-9999", "Lot", "Different DEPARTMENTNAME[" + ValidationLot.getChild("CARRIERNAME").getText() + " / " + CHECKResult.get(0).get("LOTNAME").toString() + "]");
						}

						if(MQCflag)
						{
							// Added by smkang on 2019.01.01 - According to Liu Hongwei's request, BeforeOperationName of Lot should be same.
							if(!StringUtil.equals(compareData.get(0).getUdfs().get("BEFOREOPERATIONNAME"), lotData.getUdfs().get("BEFOREOPERATIONNAME")))
							{
								throw new CustomException("SYS-9999", "Lot", "Different BEFOREOPERATIONNAME[" + ValidationLot.getChild("CARRIERNAME").getText() + " / " + CHECKResult.get(0).get("LOTNAME").toString() + "]");
							}
							if(!StringUtil.equals(compareData.get(0).getUdfs().get("BEFOREFLOWNAME"), lotData.getUdfs().get("BEFOREFLOWNAME")))
							{
								throw new CustomException("SYS-9999", "Lot", "Different BEFOREFLOWNAME[" + ValidationLot.getChild("CARRIERNAME").getText() + " / " + CHECKResult.get(0).get("LOTNAME").toString() + "]");
							}
							//2019.02.26_hsryu_Insert Logic. Compare ECCode.
							if(!StringUtil.equals(compareData.get(0).getUdfs().get("ECCODE"), lotData.getUdfs().get("ECCODE")))
							{
								throw new CustomException("SYS-9999", "Lot", "Different ECCode[" + ValidationLot.getChild("CARRIERNAME").getText() + " / " + CHECKResult.get(0).get("LOTNAME").toString() + "]");
							}
						}
		    		} 		
				}
				
			}

			//2019.01.16_hsryu_Insert Logic.
			List<String> productList = new ArrayList<String>();
			
			for(Element sorterProductE : sorterProductList)
			{
				String toCarrierName = sorterProductE.getChild("TOCARRIERNAME").getText();
				String toPortName = sorterProductE.getChild("TOPORTNAME").getText();
				String tolotName = sorterProductE.getChild("TOLOTNAME").getText();
				
				sortPrt = new SortJobProduct(jobName, sorterProductE.getChild("PRODUCTNAME").getText());
				sortPrt.setMachineName(machineName);
				sortPrt.setFromLotName(fromLotName);
				sortPrt.setFromCarrierName(fromCarrierName);
				sortPrt.setFromPortName(fromPortName);
				sortPrt.setFromPosition(sorterProductE.getChild("FROMPOSITION").getText());
				sortPrt.setToLotName(sorterProductE.getChild("TOLOTNAME").getText());
				sortPrt.setToCarrierName(toCarrierName);
				sortPrt.setToPortName(sorterProductE.getChild("TOPORTNAME").getText());
				sortPrt.setToPosition(sorterProductE.getChild("TOPOSITION").getText());
				sortPrt.setSortProductState(GenericServiceProxy.getConstantMap().SORT_SORTPRODUCTSTATE_READY);
				
				if (StringUtil.equals(jobType.toUpperCase(), "CHANGE"))  {
				}else if(StringUtil.equals(jobType.toUpperCase(), "SCRAP")) {
					sortPrt.setScrapFlag(sorterProductE.getChild("SCRAPFLAG").getText());
					sortPrt.setOutStageFlag(sorterProductE.getChild("OUTSTAGEFLAG").getText());
				}else if(StringUtil.equals(jobType.toUpperCase(), "TURNOVER")) {
					sortPrt.setTurnOverFlag(sorterProductE.getChild("TURNOVERFLAG").getText());
				}else if(StringUtil.equals(jobType.toUpperCase(), "TURNSIDE")) {
					sortPrt.setTurnSideFlag(sorterProductE.getChild("TURNSIDEFLAG").getText());
				}
				
				if(!insertedCarrierList.contains(toCarrierName))
				{
					for( Element ValidationLot : ValidationList)
					{
						String SorterHoldCSTName = ValidationLot.getChild("CARRIERNAME").getText();
						if(StringUtil.equals(toCarrierName,SorterHoldCSTName))
						{
							SorterOutHold = ValidationLot.getChild("HOLDFLAG").getText();
						}
					}
					
					sortCST = new SortJobCarrier(jobName,toCarrierName);
					
					sortCST.setLotName(tolotName);
					sortCST.setMachineName(machineName);
					sortCST.setPortName(toPortName);
					sortCST.setoutholdflag(SorterOutHold); // 2018.08.17
					
					if (StringUtil.equals(jobType.toUpperCase(), "CHANGE")) 
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

					ExtendedObjectProxy.getSortJobCarrierService().create(eventInfo, sortCST);				
					insertedCarrierList.add(toCarrierName);
					
					if(!allCarrierList.contains(toCarrierName))
						allCarrierList.add(toCarrierName);
				}
				
				if(!allProductList.contains(sorterProductE.getChild("PRODUCTNAME").getText()))
					allProductList.add(sorterProductE.getChild("PRODUCTNAME").getText());
				
				ExtendedObjectProxy.getSortJobProductService().create(eventInfo, sortPrt);
			}
		}
		
		//2019.01.16_hsryu_Insert Logic. 
		for(String carrierName : allCarrierList)
		{
			List<Product> productListInDurable = MESProductServiceProxy.getProductServiceUtil().getProductListByDurable(carrierName);
			
			if(productListInDurable!=null)
			{
				for(Product productData : productListInDurable)
				{
					String productName = productData.getKey().getProductName();
					
					if(!allProductList.contains(productName))
					{
						NonSortJobProduct nonSortJobProduct = new NonSortJobProduct(jobName, productName);
						nonSortJobProduct.setMachineName(machineName);
						nonSortJobProduct.setFromLotName(productData.getLotName());
						nonSortJobProduct.setFromCarrierName(carrierName);
						nonSortJobProduct.setFromPortName("");
						nonSortJobProduct.setFromPosition(String.valueOf(productData.getPosition()));
						nonSortJobProduct.setToLotName(productData.getLotName());
						nonSortJobProduct.setToCarrierName(carrierName);
						nonSortJobProduct.setToPortName("");
						nonSortJobProduct.setToPosition(String.valueOf(productData.getPosition()));
						
						ExtendedObjectProxy.getNonSortJobProductService().create(eventInfo, nonSortJobProduct);
					}
				}
			}
		}

		/* 2019.08.27 dmlee : Request by cim Mantis 4654
		eventInfo = EventInfoUtil.makeEventInfo("AssignPort",getEventUser(), getEventComment(), "", "");
		eventInfo.setCheckTimekeyValidation(false);
        eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
        eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
        
		// change port
		String tsql = "SELECT PT.PORTNAME";
		tsql += " FROM PORT P, PORTSPEC PT";
		tsql += " WHERE PT.MACHINENAME = P.MACHINENAME";
		tsql += " AND P.PORTNAME = PT.PORTNAME";
		tsql += " AND P.FACTORYNAME = PT.FACTORYNAME";
		tsql += " AND PT.MACHINENAME = :MACHINENAME";	
		tsql += " AND PT.PORTNAME NOT IN (SELECT CC.PORTNAME FROM CT_SORTJOB C JOIN CT_SORTJOBCARRIER CC ON C.JOBNAME = CC.JOBNAME WHERE CC.MACHINENAME = :MACHINENAME AND C.JOBSTATE IN (:JOBSTATE1, :JOBSTATE2, :JOBSTATE3))";
		tsql += " AND P.PORTTYPE = :PORTTYPE";
		tsql += " AND P.PORTSTATENAME != :PORTSTATENAME";
		
		// Added by smkang on 2019.02.25 - Need to check PortTransferState and PortInInhibitFlag.
		tsql += " AND P.TRANSFERSTATE = :TRANSFERSTATE";
		tsql += " AND (P.PORTININHIBITFLAG IS NULL OR P.PORTININHIBITFLAG = :PORTININHIBITFLAG)";
		
		tsql += " ORDER BY PT.PORTNAME";
		
		Map<String, String> tbindMap = new HashMap<String, String>();
		tbindMap.put("MACHINENAME", machineName);
		tbindMap.put("JOBSTATE1", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_WAIT);
		tbindMap.put("JOBSTATE2", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_STARTED);
		tbindMap.put("JOBSTATE3", GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CONFIRMED);
		tbindMap.put("PORTTYPE", "PS");
		tbindMap.put("PORTSTATENAME", GenericServiceProxy.getConstantMap().Port_DOWN);
		
		// Added by smkang on 2019.02.25 - Need to check PortTransferState and PortInInhibitFlag.
		tbindMap.put("TRANSFERSTATE", GenericServiceProxy.getConstantMap().Port_ReadyToLoad);
		tbindMap.put("PORTININHIBITFLAG", "N");
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(tsql, tbindMap);
		
		if(Integer.parseInt(Tcount) >  sqlResult.size())
		{
			PortFlag = "TRUE";
		}		
		else
		{
			PortFlag = "FALSE";
		}
		
		
		
		// 2018.11.19 
		// Mentis 1386
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		String MachineStateName = machineData.getMachineStateName();
		String MachineCommunicationState = machineData.getCommunicationState();
		
		if(MachineStateName.equals(GenericServiceProxy.getConstantMap().MACHINE_STATE_DOWN))
		{
			PortFlag = "TRUE";
		}
		// 2018.12.10
		if(MachineCommunicationState.equals(GenericServiceProxy.getConstantMap().Mac_OffLine))
		{
			PortFlag = "TRUE";
		}
		
		if (StringUtil.equals(jobType.toUpperCase(), "CHANGE"))
		{
			String Ccondition = "where jobname=?";
			Object[] CbindSet = new Object[] {jobName};
			List<SortJobCarrier> SortJobCarrier = ExtendedObjectProxy.getSortJobCarrierService().select(Ccondition,CbindSet);
			
			int portnumber = 0;
			String transferdiraction = "";
			String carriername = "";
			for (SortJobCarrier Carrier : SortJobCarrier)
			{
				// Update new port to the carrier DB
				transferdiraction = Carrier.getTransferDirection();
				carriername = Carrier.getCarrierName();
				if (StringUtil.equals(PortFlag, "TRUE")) {
					Carrier.setPortName("Auto");
				}
				else {
					Carrier.setPortName(sqlResult.get(portnumber).get("portname").toString());
				}
				ExtendedObjectProxy.getSortJobCarrierService().modify(eventInfo, Carrier);
				
				if(StringUtil.equals(transferdiraction, "TARGET"))
				{					
					eventInfo.setCheckTimekeyValidation(false);
					eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
					eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
					// Update port in the 'TOCARRIERNAME';
					String condition = "where jobname=?" + " and tocarriername = ?";
					Object[] bindSet = new Object[] {jobName, carriername };
					List<SortJobProduct> SortJobProductList = ExtendedObjectProxy.getSortJobProductService().select(condition,bindSet);
					for (SortJobProduct product : SortJobProductList) {
						if (StringUtil.equals(PortFlag, "TRUE")) {
							product.setToPortName("Auto");
						} else {
							product.setToPortName(sqlResult.get(portnumber).get("portname").toString());
						}
						ExtendedObjectProxy.getSortJobProductService().modify(eventInfo, product);
					}
				}
				else
				{
					eventInfo.setCheckTimekeyValidation(false);
			        eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
			        eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
					// Update port in the 'FROMCARRIERNAME';
					String condition = "where jobname=?" + " and fromcarriername = ?";
					Object[] bindSet = new Object[] {jobName, carriername };
					List<SortJobProduct> SortJobProductList = ExtendedObjectProxy.getSortJobProductService().select(condition,bindSet);
					for (SortJobProduct product : SortJobProductList) {
						if (StringUtil.equals(PortFlag, "TRUE")) {
							product.setFromPortName("Auto");
						} else {
							product.setFromPortName(sqlResult.get(portnumber).get("portname").toString());
						}
						ExtendedObjectProxy.getSortJobProductService().modify(eventInfo, product);
					}
				}
				portnumber++;
			}
		}
		else
		{
			// only one port case
			String NewPort = (StringUtil.equals(PortFlag, "TRUE")) ? "Auto" : sqlResult.get(0).get("portname").toString();
			String Ccondition = "where jobname=?";
			Object[] CbindSet = new Object[] {jobName};
			List<SortJobCarrier> SortJobCarrier = ExtendedObjectProxy.getSortJobCarrierService().select(Ccondition, CbindSet);

			for (SortJobCarrier Carrier : SortJobCarrier)
			{
				Carrier.setPortName(NewPort);
				ExtendedObjectProxy.getSortJobCarrierService().modify(eventInfo, Carrier);
			}
			String condition = "where jobname=?";
			Object[] bindSet = new Object[] {jobName};
			List<SortJobProduct> SortJobProductList = ExtendedObjectProxy.getSortJobProductService().select(condition, bindSet);
				for (SortJobProduct product : SortJobProductList)
			{
				product.setToPortName(NewPort);
				product.setFromPortName(NewPort);
				ExtendedObjectProxy.getSortJobProductService().modify(eventInfo, product);
			}
		}
		2019.08.27 dmlee : Request by cim Mantis 4654 */
		
		strComment.append("SortJobName").append("[").append(jobName).append("]").append("\n");
		
		setNextInfo(doc, strComment);
		
		return doc;
	}
	private void copyFutureAction(Lot parent, Lot child, EventInfo eventInfo)
	{
		
		List<LotAction> sampleActionList = new ArrayList<LotAction>();
		long lastPosition = 0;
		
		String condition = " WHERE lotName = ? AND factoryName = ? AND actionState = ? ";
		Object[] bindSet = new Object[]{ parent.getKey().getLotName(), parent.getFactoryName(), "Created" };

		try
		{
			sampleActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);
			
			for(int i=0; i<sampleActionList.size();i++)
			{
				LotAction lotaction = new LotAction();
				lotaction = sampleActionList.get(i);
				
				lastPosition = Integer.parseInt(MESLotServiceProxy.getLotServiceUtil().getLastPositionOfLotAction(child,lotaction.getProcessFlowName(),lotaction.getProcessOperationName()));
				
				lotaction.setLotName(child.getKey().getLotName());
				lotaction.setPosition(lastPosition+1);
				lotaction.setLastEventTime(eventInfo.getEventTime());

				// Modified by smkang on 2019.05.17 - EventInfo.LastEventTimekey is null.
//				lotaction.setLastEventTimeKey(eventInfo.getLastEventTimekey());
				lotaction.setLastEventTimeKey(eventInfo.getEventTimeKey());
				
				lotaction.setLastEventName(eventInfo.getEventName());
				lotaction.setLastEventUser(eventInfo.getEventUser());
				lotaction.setLastEventComment(eventInfo.getEventComment());
				
				ExtendedObjectProxy.getLotActionService().create(eventInfo, lotaction);

			}
		}
		catch (Throwable e)
		{
			return;
		}
	}

	private void setNextInfo(Document doc, StringBuilder strComment)
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
	
}
