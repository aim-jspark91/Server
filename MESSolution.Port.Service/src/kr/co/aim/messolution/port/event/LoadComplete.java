package kr.co.aim.messolution.port.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DspMachineDispatch;
import kr.co.aim.messolution.extended.object.management.data.SortJobCarrier;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.event.SorterJobStartCommand;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greenframe.util.support.InvokeUtils;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class LoadComplete extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
		String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		String portAccessMode = SMessageUtil.getBodyItemValue(doc, "PORTACCESSMODE", false);
		
		//20.11.06 DMLee (Add Item)
		String carrierSettingCode = SMessageUtil.getBodyItemValue(doc, "CARRIERSETTINGCODE", false);
		String portStateName = SMessageUtil.getBodyItemValue(doc, "PORTSTATENAME", false);
		String reserveProductID = SMessageUtil.getBodyItemValue(doc, "REVERSEPRODUCTID", false);

		Port portData = CommonUtil.getPortInfo(machineName, portName);

		if(!StringUtils.equals(portData.getTransferState(), GenericServiceProxy.getConstantMap().Port_ReadyToProcess))
		{
			eventInfo.setEventName("ChangeTransferState");

			MakeTransferStateInfo makeTransferStateInfo = new MakeTransferStateInfo();
			makeTransferStateInfo.setTransferState( GenericServiceProxy.getConstantMap().Port_ReadyToProcess );
			makeTransferStateInfo.setValidateEventFlag( "N" );
			// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//			Map<String,String> udfs = portData.getUdfs();
//			udfs.put("FULLSTATE", GenericServiceProxy.getConstantMap().Port_FULL);
//			udfs.put("CARRIERNAME", carrierName);
//			makeTransferStateInfo.setUdfs(udfs);
			makeTransferStateInfo.getUdfs().put("FULLSTATE", GenericServiceProxy.getConstantMap().Port_FULL);
			makeTransferStateInfo.getUdfs().put("CARRIERNAME", carrierName);

			MESPortServiceProxy.getPortServiceImpl().makeTransferState(portData, makeTransferStateInfo, eventInfo);
		}

		if(!StringUtils.equals(CommonUtil.getValue(portData.getUdfs(), "FULLSTATE"), GenericServiceProxy.getConstantMap().Port_FULL))
		{
			eventInfo.setEventName("ChangeFullState");

			SetEventInfo setEventInfo = new SetEventInfo();

			// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//			Map<String,String> udfs = portData.getUdfs();
//			udfs.put("FULLSTATE", GenericServiceProxy.getConstantMap().Port_FULL);
//			udfs.put("CARRIERNAME", carrierName);
//			setEventInfo.setUdfs(udfs);
			setEventInfo.getUdfs().put("FULLSTATE", GenericServiceProxy.getConstantMap().Port_FULL);
			setEventInfo.getUdfs().put("CARRIERNAME", carrierName);

			MESPortServiceProxy.getPortServiceImpl().setEvent(portData, setEventInfo, eventInfo);
		}
		
		//20.11.06 DMLee (Update CST Setting Code)
		if(!StringUtils.isEmpty(carrierSettingCode) && !StringUtils.equals(portData.getUdfs().get("CSTSETTINGCODE"), carrierSettingCode))
		{
			eventInfo.setEventName("ChangeCSTSettingCode");
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("CSTSETTINGCODE", carrierSettingCode);
			
			MESPortServiceProxy.getPortServiceImpl().setEvent(portData, setEventInfo, eventInfo);
		}

		/* 20180602, Carrier Location Update(Machine, PortName) ==>> */
        //cleaning EQP
        MachineSpec machineSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);
        
        // Modified by smkang on 2018.11.28 - ConstructType of cassette cleaner is changed to CCLN.
//        if (CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals("CCLN"))
        if (CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals(GenericServiceProxy.getConstantMap().ConstructType_CassetteClenaer))
        {
            eventInfo.setEventName("ChangePositionState");
            Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

            kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();

            // 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//            Map<String,String> udfs = durableData.getUdfs();
//            udfs.put("MACHINENAME", machineName);
//            udfs.put("PORTNAME", portData.getKey().getPortName());
//            setEventInfo.setUdfs(udfs);
            setEventInfo.getUdfs().put("MACHINENAME", machineName);
            setEventInfo.getUdfs().put("PORTNAME", portData.getKey().getPortName());

            MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
        }
        /* <<== 20180602, Carrier Location Update(Machine, PortName) */

        /* 20181214, hhlee, modify ==>> */
        //sorterJobReply
        if(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PS"))
        {
            SorterJobStartCommand(eventInfo, doc, getSortJobName(machineName,carrierName,portName), carrierName);
        }
        /* <<== 20181214, hhlee, modify */
        
		//carrier handling
		if (!carrierName.isEmpty())
		{
		    /* 20190422, hhlee, modify, Changed PP, BL, BU, PG Type LoadComplete Update Logic ==>> */
		    if(StringUtil.equals(portData.getAccessMode(), GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL))
		    {
    		    /* 2181030, hhlee, add, Current Load CarrierName Update, Befor CarrierName remove portName, machineName ==>> */
    		    MESDurableServiceProxy.getDurableServiceImpl().checkLoadedCarrier(eventInfo, portData.getFactoryName(), carrierName, machineName, portName);
    		    /* <<== 2181030, hhlee, add, Current Load CarrierName Update, Befor CarrierName remove portName, machineName */
    		    		    
    		    MESDurableServiceProxy.getDurableServiceImpl().load(eventInfo, carrierName, machineName, portName, machineSpecData.getAreaName());
		    }
		    /* <<== 20190422, hhlee, modify, Changed PP, BL, BU, PG Type LoadComplete Update Logic */
		}

		//success then report to FMC
		try
		{
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");
		}
		catch(Exception ex)
		{
			eventLog.warn("FMC Report Failed!");
		}
		
		/* 20181214, hhlee, modify ==>> */
        ////sorterJobReply
        //if(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PS"))
        //{
        //    SorterJobStartCommand(eventInfo, doc, getSortJobName(machineName,carrierName,portName), carrierName);
        //}
        /* <<== 20181214, hhlee, modify */
		
		//add by wghuang 20181204       
		 //send to DSP with PullFlag
        DspMachineDispatch DSPMachineDisPatchData = null;
        try
        {
        	DSPMachineDisPatchData = ExtendedObjectProxy.getDspMachineDispatchService().selectByKey(true, new Object[]{machineName});
        }
        catch(Exception ce)
        {       	
        }
        
        if(DSPMachineDisPatchData != null && StringUtil.equals(DSPMachineDisPatchData.getPlPullFlag(), "Y"))
        {
        	if(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals(GenericServiceProxy.getConstantMap().PORT_TYPE_PB) ||
     		   CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals(GenericServiceProxy.getConstantMap().PORT_TYPE_PL))   
     		   {
        		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "Common_PullTrigger");
        		sendToDSP(doc);
     		   }
        }
	}
	
	private void sendToDSP(Document doc)
	{
		// send to DSPsvr
		try
		{
			String replySubject = GenericServiceProxy.getESBServive().getSendSubject("DSPsvr");
			GenericServiceProxy.getESBServive().sendReplyBySender(replySubject, doc, "DSPSender");
		}
		catch (Exception e)
		{
			eventLog.error("sending to DSPsvr is failed");
		}
	}

	/**
	 * bypass for certain type
	 * @author wghuang
	 * @since 2018.04.04
	 * @param doc
	 */
	private void SorterJobStartCommand(EventInfo eventInfo, Document doc,String sortJobName, String carrierName) throws CustomException
	{
		Document copyDoc = (Document)doc.clone();

	    SMessageUtil.setBodyItemValue(copyDoc, "JOBNAME", sortJobName,true);

		SMessageUtil.setHeaderItemValue(copyDoc, "MESSAGENAME", "A_SorterJobStartCommand");

//		try
//		{
//			//GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("PEXsvr"), copyDoc, "LocalSender");
//		}
//		catch(Exception ex)
//		{
//			eventLog.warn("PEX Report [SorterJobStartCommand] Failed!");
//		}

		try
        {
            Object result = InvokeUtils.invokeMethod(InvokeUtils.newInstance(SorterJobStartCommand.class.getName(), null, null), "execute", new Object[] {copyDoc});
        }
        catch (Exception ex)
        {
            eventLog.error(ex.getMessage());
        }
		
		try
        {
    		SortJobCarrier sortJobCarrier = ExtendedObjectProxy.getSortJobCarrierService().selectByKey(false, new Object[] {sortJobName, carrierName});
            
    		sortJobCarrier.setLoadFlag(GenericServiceProxy.getConstantMap().Flag_Y);
    		sortJobCarrier.setLoadTimeKey(eventInfo.getEventTimeKey());
    		        
            ExtendedObjectProxy.getSortJobCarrierService().modify(eventInfo, sortJobCarrier);
        }
		catch (Exception ex)
        {
            eventLog.error(ex.getMessage());
        }
	}

	private String getSortJobName(String machineName,String carrierName, String portName) throws CustomException
	{
		List<ListOrderedMap> sortJobList = new ArrayList<ListOrderedMap>();

		try
		{
			//String strSql = "SELECT J.JOBNAME " +
			//				"  FROM CT_SORTJOB J, CT_SORTJOBCARRIER C " +
			//				" WHERE 1 = 1 " +
			//				"   AND J.JOBNAME = C.JOBNAME " +
			//				"   AND C.MACHINENAME = :MACHINENAME " +
			//				"   AND C.CARRIERNAME = :CARRIERNAME " +
			//				"   AND C.PORTNAME = :PORTNAME " +
			//				"   AND J.SEQ = (SELECT MIN(SEQ) FROM CT_SORTJOB WHERE MACHINENAME =?  AND JOBSTATE IN(?,?,?)) " +
			//				"   AND J.JOBSTATE IN(?,?,?) " ;

		    //String strSql = "SELECT J.JOBNAME " +
            //        "  FROM CT_SORTJOB J, CT_SORTJOBCARRIER C " +
            //        " WHERE 1 = 1 " +
            //        "   AND J.JOBNAME = C.JOBNAME " +
            //        "   AND C.MACHINENAME = ? " +
            //        "   AND C.CARRIERNAME = ? " +
            //        "   AND C.PORTNAME = ? " +
            //        "   AND J.SEQ = (SELECT MIN(SEQ) FROM CT_SORTJOB WHERE MACHINENAME =?  AND JOBSTATE IN(?,?,?)) " +
            //        "   AND J.JOBSTATE IN(?,?,?) " ;
		    
		    //Object[] bindList = new Object[] { machineName, carrierName,portName,machineName,
            //        GenericServiceProxy.getConstantMap().SORT_JOBSTATE_WAIT
            //        ,GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CONFIRMED
            //        ,GenericServiceProxy.getConstantMap().SORT_JOBSTATE_STARTED
            //        ,GenericServiceProxy.getConstantMap().SORT_JOBSTATE_WAIT
            //        ,GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CONFIRMED
            //        ,GenericServiceProxy.getConstantMap().SORT_JOBSTATE_STARTED};
		    
		    String strSql = "SELECT J.JOBNAME   " +
                    "  FROM CT_SORTJOB J, CT_SORTJOBCARRIER C   " +
                    " WHERE 1 = 1   " +
                    "   AND J.JOBSTATE NOT IN(?,?,?)  " +
                    //"   AND J.SEQ = (SELECT MIN(SEQ) FROM CT_SORTJOB WHERE MACHINENAME =?  AND JOBSTATE IN(?,?)) " +
                    "   AND J.JOBNAME = C.JOBNAME   " +
                    "   AND C.MACHINENAME = :MACHINENAME   " +
                    "   AND C.CARRIERNAME = :CARRIERNAME   " +
                    "   AND C.PORTNAME = :PORTNAME " ;

             Object[] bindList = new Object[] {GenericServiceProxy.getConstantMap().SORT_JOBSTATE_ABORT,
                                      GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CANCELED,
                                      GenericServiceProxy.getConstantMap().SORT_JOBSTATE_ENDED,
                                      machineName, carrierName,portName};
		    
			

			sortJobList = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindList);

			return CommonUtil.getValue(sortJobList.get(0), "JOBNAME");
		}
		catch(Exception ex)
		{
			/* Not used ==> */
		    //eventLog.debug("No sorter job");
			//throw new CustomException("SYS-9999", "SortJob", "No job for Product");
			/* <== Not used */

		    eventLog.warn("[SorterJobStartCommand] No sorter job");
		    return  GenericServiceProxy.getConstantMap().INSERT_LOG_NONE;
		}
		/* Not used ==> */
		//return CommonUtil.getValue(sortJobList.get(0), "JOBNAME");
		/* <== Not used */
	}
}