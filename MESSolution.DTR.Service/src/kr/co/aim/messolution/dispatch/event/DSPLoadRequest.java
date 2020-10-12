package kr.co.aim.messolution.dispatch.event;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;

import org.jdom.Document;
import org.jdom.Element;

public class DSPLoadRequest extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		/**
		 * LoadRequest : Stocker's Pull or Push(Region) -> Port 
		 */
		
		// load Machine, Port List
		String loadSql = " SELECT M.MACHINENAME, "
				+ " P.PORTNAME, "
				+ " MD.PLDISPATCHFLAG, "
				+ " NVL(MD.PLWAITTIMEE2E, 0) PLWAITTIMEE2E, "
				+ " NVL(MD.PLWAITTIMEPUSH, 0) PLWAITTIMEPUSH, "
				+ " MD.PLPULLFLAG, "
				+ " MD.PUDISPATCHFLAG, "
				+ " NVL(MD.PUWAITTIMEE2E, 0) PUWAITTIMEE2E, "
				+ " NVL(MD.PUWAITTIMEPUSH, 0) PUWAITTIMEPUSH "
				+ " FROM MACHINE M, PORT P, CT_DSPMACHINEDISPATCH MD "
				+ " WHERE M.MACHINENAME = P.MACHINENAME "
				+ " AND P.MACHINENAME = MD.MACHINENAME "
				+ " AND M.MACHINENAME = MD.MACHINENAME(+) "
				+ " AND M.RESOURCESTATE = :RESOURCESTATE "
				+ " AND M.COMMUNICATIONSTATE <> :COMMUNICATIONSTATE "
				+ " AND P.RESOURCESTATE = :RESOURCESTATE "
				+ " AND P.ACCESSMODE = :ACCESSMODE "
				+ " AND P.TRANSFERSTATE = :TRANSFERSTATE "
				+ " AND P.PORTSTATENAME = :PORTSTATENAME "
				+ " AND P.CARRIERNAME IS NULL "
				+ " ORDER BY M.MACHINENAME, P.PORTNAME ASC ";

		Map<String, Object> loadBind = new HashMap<String, Object>();
		loadBind.put("RESOURCESTATE", GenericServiceProxy.getConstantMap().Rsc_InService);
		loadBind.put("COMMUNICATIONSTATE", GenericServiceProxy.getConstantMap().Mac_OffLine);
		loadBind.put("ACCESSMODE", GenericServiceProxy.getConstantMap().Port_Auto);
		loadBind.put("TRANSFERSTATE", GenericServiceProxy.getConstantMap().Port_ReadyToLoad);
		loadBind.put("PORTSTATENAME", GenericServiceProxy.getConstantMap().Port_UP);

		List<Map<String, Object>> loadResult = GenericServiceProxy.getSqlMesTemplate().queryForList(loadSql, loadBind);

		if(loadResult.size() > 0)
		{
			for ( int i = 0; i < loadResult.size(); i++ )
			{
				String destinationMachineName = (String)loadResult.get(i).get("MACHINENAME");
				String destinationPortName = (String)loadResult.get(i).get("PORTNAME");
				
				String reserveLotSql = " SELECT MACHINENAME, CARRIERNAME, LOTNAME, RESERVESTATE, POSITION FROM CT_DSPRESERVELOT "
						+ " WHERE MACHINENAME = :MACHINENAME "
						+ " AND RESERVESTATE = :RESERVESTATE "
						+ " ORDER BY POSITION ASC ";
				
				Map<String, Object> reserveLotBind = new HashMap<String, Object>();
				reserveLotBind.put("MACHINENAME", destinationMachineName);
				reserveLotBind.put("RESERVESTATE", GenericServiceProxy.getConstantMap().DSPSTATUS_RESERVED);
				
				List<Map<String, Object>> reserveLotResult = GenericServiceProxy.getSqlMesTemplate().queryForList(reserveLotSql, reserveLotBind);

				if(reserveLotResult.size() > 0)
				{
					String carrierName = (String)reserveLotResult.get(0).get("CARRIERNAME");
					Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
					String sourceMachineName = durableData.getUdfs().get("MACHINENAME");
					String sourcePositionType = "SHELF";
					String sourcePositionName = durableData.getUdfs().get("POSITIONNAME");
					String sourceZoneName = durableData.getUdfs().get("ZONENAME");
					
					// Make Transport Job Doc
					Document texDoc = this.writeTransportJob(doc, carrierName, sourceMachineName, sourcePositionType, sourcePositionName, sourceZoneName, 
							destinationMachineName, "PORT", destinationPortName, "", "", "");
					
					// Make Transport Job
					sendToTEM(texDoc);
				}
			}
		}
	}

	private void sendToTEM(Document doc)
	{
		// send to TEMsvr
		try
		{
//			String targetSubject = GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEXsvr");
//
//			GenericServiceProxy.getESBServive().sendBySender(targetSubject, doc, "TEXSender");

			String targetSubject = GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEMsvr");

			GenericServiceProxy.getESBServive().sendBySender(targetSubject, doc, "GenericSender");

		}
		catch (Exception e)
		{
			eventLog.error("sending to TEMsvr is failed");
		}
	}

	private Document writeTransportJob(Document doc, String carrierName, String sourceMachineName, String sourcePositionType, String sourcePositionName, String sourceZoneName, 
			String destinationMachineName, String destinationPositionType, String destinationPositionName, String destinationZoneName, String region, String kanban)
			throws CustomException
	{
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "RequestTransportJobRequest");

		boolean result = doc.getRootElement().removeChild(SMessageUtil.Body_Tag);

		Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);

		Element element1 = new Element("CARRIERNAME");
		element1.setText(carrierName);
		eleBodyTemp.addContent(element1);

		Element element2 = new Element("SOURCEMACHINENAME");
		element2.setText(sourceMachineName);
		eleBodyTemp.addContent(element2);

		Element element3 = new Element("SOURCEZONENAME");
		element3.setText(sourceZoneName);
		eleBodyTemp.addContent(element3);

		Element element4 = new Element("SOURCEPOSITIONTYPE");
		element4.setText(sourcePositionType);
		eleBodyTemp.addContent(element4);

		Element element5 = new Element("SOURCEPOSITIONNAME");
		element5.setText(sourcePositionName);
		eleBodyTemp.addContent(element5);

		Element element6 = new Element("DESTINATIONMACHINENAME");
		element6.setText(destinationMachineName);
		eleBodyTemp.addContent(element6);

		Element element7 = new Element("DESTINATIONZONENAME");
		element7.setText(destinationZoneName);
		eleBodyTemp.addContent(element7);

		Element element8 = new Element("DESTINATIONPOSITIONTYPE");
		element8.setText(destinationPositionType);
		eleBodyTemp.addContent(element8);

		Element element9 = new Element("DESTINATIONPOSITIONNAME");
		element9.setText(destinationPositionName);
		eleBodyTemp.addContent(element9);

		Element element10 = new Element("LOTNAME");
		element10.setText("");
		eleBodyTemp.addContent(element10);

		Element element11 = new Element("PRODUCTQUANTITY");
		element11.setText("0");
		eleBodyTemp.addContent(element11);

		Element element12 = new Element("CARRIERSTATE");
		element12.setText("");
		eleBodyTemp.addContent(element12);

		Element element13 = new Element("PRIORITY");
		element13.setText("0");
		eleBodyTemp.addContent(element13);
		
		Element element14 = new Element("REGION");
		element14.setText(region);
		eleBodyTemp.addContent(element14);

		Element element15 = new Element("KANBAN");
		element15.setText(kanban);
		eleBodyTemp.addContent(element15);
		
		eventLog.debug("Make Transport Job");

		//overwrite
		doc.getRootElement().addContent(eleBodyTemp);

		return doc;
	}
}