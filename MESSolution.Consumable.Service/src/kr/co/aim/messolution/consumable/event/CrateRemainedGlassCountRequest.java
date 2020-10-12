package kr.co.aim.messolution.consumable.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

import org.jdom.Document;
import org.jdom.Element;

public class CrateRemainedGlassCountRequest extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String crateName = SMessageUtil.getBodyItemValue(doc, "CRATENAME", true);

		/* 20181119, hhlee, modify , add CrateSpecName ==>> */
		//this.generateBodyTemplate(SMessageUtil.getBodyElement(doc), machineName, portName, "", crateName, "0");
		this.generateReplyTemplate(doc, machineName, portName, "", crateName, "0", StringUtil.EMPTY);
		/* <<== 20181119, hhlee, modify , add CrateSpecName */
		
		/* MachineInfo */
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

		/* PortInfo */
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		SMessageUtil.setBodyItemValue(doc, "PORTTYPE", portData.getUdfs().get("PORTTYPE"));

		/* ConsumalbeInfo */
		Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(crateName);
		if(consumableData.getQuantity() <= 0)
		{
		    throw new CustomException("CRATE-0006", crateName);
		}
		SMessageUtil.setBodyItemValue(doc, "REMAINCRATEGLASSQUANTITY", String.valueOf((int) consumableData.getQuantity()));

		/* 20181119, hhlee, modify , add CrateSpecName ==>> */
		SMessageUtil.setBodyItemValue(doc, "CRATESPECNAME",consumableData.getConsumableSpecName());
		/* <<== 20181119, hhlee, modify , add CrateSpecName */		
		
		//MESConsumableServiceProxy.getConsumableServiceUtil().crateRemainedCountReply(machineName, portName, crateName, doc);

		/* 20181122, hhlee, delete, Because validate at EAP(Requested by Guishi) ==>> */
		///* Use Crate Info validation */
        //this.workAvailableCrateCheck(machineName, portName, consumableData);
        /* <<== 20181122, hhlee, delete, Because validate at EAP(Requested by Guishi) */
        
		return doc;
	}

	private Document generateReplyTemplate(Document doc, String machineName, String portName,
            String portType, String crateName, String remainCrateGlassQuantity, String crateSpecName )
            throws CustomException
	{

		/* Send message only in " Online Initial ". Add, hhlee, 20180327 */
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_CrateRemainedGlassCountReply");
		SMessageUtil.setHeaderItemValue(doc, "EVENTCOMMENT", "CrateRemainedGlassCountReply");

		Element bodyElement = SMessageUtil.getBodyElement(doc);

		bodyElement.removeContent();

		Element machineNameElement = new Element("MACHINENAME");
		machineNameElement.setText(machineName);
		bodyElement.addContent(machineNameElement);

		Element portNameElement = new Element("PORTNAME");
		portNameElement.setText(portName);
		bodyElement.addContent(portNameElement);

		/* 2018.04.02, hhlee, porttype element Add */
		Element portTypeElement = new Element("PORTTYPE");
		portTypeElement.setText(portType);
		bodyElement.addContent(portTypeElement);

		Element crateNameElement = new Element("CRATENAME");
		crateNameElement.setText(crateName);
		bodyElement.addContent(crateNameElement);
		
		/* 20181119, hhlee, modify , add CrateSpecName ==>> */
		Element crateSpecNameElement = new Element("CRATESPECNAME");
		crateSpecNameElement.setText(crateSpecName);
        bodyElement.addContent(crateSpecNameElement);
        /* <<== 20181119, hhlee, modify , add CrateSpecName */
        
		Element remainCrateGlassQuantityElement = new Element("REMAINCRATEGLASSQUANTITY");
		remainCrateGlassQuantityElement.setText(remainCrateGlassQuantity);
		bodyElement.addContent(remainCrateGlassQuantityElement);

		return doc;

		/*  Return boby sample  */
		/* Send message only in " Online Initial ". Add, hhlee, 20180327
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_CrateRemainedGlassCountReply");
		SMessageUtil.setHeaderItemValue(doc, "EVENTCOMMENT", "CrateRemainedGlassCountReply");

		SMessageUtil.getBodyElement(doc).removeContent();

		Element machineNameElement = new Element("MACHINENAME");
		machineNameElement.setText(machineName);
		SMessageUtil.getBodyElement(doc).addContent(machineNameElement);

		Element portNameElement = new Element("PORTNAME");
		portNameElement.setText(portName);
		SMessageUtil.getBodyElement(doc).addContent(portNameElement);

		 2018.04.02, hhlee, porttype element Add
		Element portTypeElement = new Element("PORTTYPE");
		portTypeElement.setText(portType);
		SMessageUtil.getBodyElement(doc).addContent(portTypeElement);

		Element crateNameElement = new Element("CRATENAME");
		crateNameElement.setText(crateName);
		SMessageUtil.getBodyElement(doc).addContent(crateNameElement);

		Element remainCrateGlassQuantityElement = new Element("REMAINCRATEGLASSQUANTITY");
		remainCrateGlassQuantityElement.setText(remainCrateGlassQuantity);
		SMessageUtil.getBodyElement(doc).addContent(remainCrateGlassQuantityElement);

		return doc;*/
	}

	private Element generateBodyTemplate(Element bodyElement, String machineName, String portName,
			                                         String portType, String crateName, String remainCrateGlassQuantity )
			                                         throws CustomException
	{

		bodyElement.removeContent();

		Element machineNameElement = new Element("MACHINENAME");
		machineNameElement.setText(machineName);
		bodyElement.addContent(machineNameElement);

		Element portNameElement = new Element("PORTNAME");
		portNameElement.setText(portName);
		bodyElement.addContent(portNameElement);

		/* 2018.04.02, hhlee, porttype element Add */
		Element portTypeElement = new Element("PORTTYPE");
		portTypeElement.setText(portType);
		bodyElement.addContent(portTypeElement);

		Element crateNameElement = new Element("CRATENAME");
		crateNameElement.setText(crateName);
		bodyElement.addContent(crateNameElement);

		Element remainCrateGlassQuantityElement = new Element("REMAINCRATEGLASSQUANTITY");
		remainCrateGlassQuantityElement.setText(remainCrateGlassQuantity);
		bodyElement.addContent(remainCrateGlassQuantityElement);

		return bodyElement;
	}

	/**
	 * @Name     workAvailableCrateCheck
	 * @since    2018. 5. 22.
	 * @author   hhlee
	 * @contents Work Available Crate Check
	 * @param machinename
	 * @param portname
	 * @param cratedata
	 * @throws CustomException
	 */
	private void workAvailableCrateCheck(String machinename, String portname, Consumable cratedata) throws CustomException
	{
        String strSql = StringUtil.EMPTY;
        strSql = strSql + " SELECT SQ.PRODUCTREQUESTNAME,                          \n";
        strSql = strSql + "        SQ.PRODUCTSPECNAME,                             \n";
        strSql = strSql + "        PR.CRATESPECNAME                                \n";
        strSql = strSql + "   FROM PRODUCTREQUEST PR,                              \n";
        strSql = strSql + "        (                                               \n";
        strSql = strSql + "         SELECT CL.MACHINENAME,                         \n";
        strSql = strSql + "                CL.PRODUCTSPECNAME,                     \n";
        strSql = strSql + "                CL.PRODUCTREQUESTNAME,                  \n";
        strSql = strSql + "                CL.PLANRELEASEDTIME                     \n";
        strSql = strSql + "           FROM CT_RESERVELOT CL,                       \n";
        strSql = strSql + "                LOT L                                   \n";
        strSql = strSql + "          WHERE 1=1                                     \n";
        strSql = strSql + "            AND CL.LOTNAME = L.LOTNAME                  \n";
        strSql = strSql + "            AND CL.PRODUCTSPECNAME = L.PRODUCTSPECNAME  \n";
        strSql = strSql + "            AND CL.MACHINENAME = :MACHINENAME           \n";
        strSql = strSql + "            AND CL.RESERVESTATE = :RESERVESTATE         \n";
        strSql = strSql + "          ORDER BY L.PRIORITY,                          \n";
        strSql = strSql + "                   CL.PLANRELEASEDTIME,                 \n";
        strSql = strSql + "                   CL.RESERVETIMEKEY                    \n";
        strSql = strSql + "         )SQ                                            \n";
        strSql = strSql + " WHERE PR.PRODUCTREQUESTNAME = SQ.PRODUCTREQUESTNAME    \n";
        strSql = strSql + "   AND PR.PRODUCTSPECNAME = SQ.PRODUCTSPECNAME          \n";
        strSql = strSql + "   AND PR.CRATESPECNAME <> :CRATESPECNAME               \n";
        strSql = strSql + " GROUP BY SQ.PRODUCTREQUESTNAME,                        \n";
        strSql = strSql + "          SQ.PRODUCTSPECNAME,                           \n";
        strSql = strSql + "          PR.CRATESPECNAME                              \n";

        Map<String, Object> bindMap = new HashMap<String, Object>();

        bindMap.put("MACHINENAME", machinename);
        bindMap.put("RESERVESTATE", GenericServiceProxy.getConstantMap().Prq_Started);
        bindMap.put("CRATESPECNAME", cratedata.getConsumableSpecName());

        List<Map<String, Object>> workcrateList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);

        if(workcrateList.size() > 0)
        {
            /* 20181119, hhlee, modify , modify validation ==>> */
            if(!StringUtil.equals(cratedata.getConsumableSpecName(), workcrateList.get(0).get("CRATESPECNAME").toString()))
            {  
                //eventLog.warn(String.format("Consumable Spec Missmatched. EQP[{%s}] / MES[{%s}]", cratedata.getConsumableSpecName(), workcrateList.get(0).get("CRATESPECNAME").toString()));
                throw new CustomException("CRATE-0004", cratedata.getConsumableSpecName(), workcrateList.get(0).get("CRATESPECNAME").toString());                
            }
            /* <<== 20181119, hhlee, modify , modify validation */
        }
	}
}
