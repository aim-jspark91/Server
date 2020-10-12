package kr.co.aim.messolution.dispatch.event;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

public class DSPEmptyCassetteCollector extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		/**
		 * StockerZone.MinEmptyCarrierCount = Current Count (Stock -> Stock)
		 * Transport From StockerZoneEmptyCarrier.FromStocker/Zone
		 */

		// EmptyShelfCount와 MINEmptyCarrierCount와 같은 Stocker 찾기 (DestinationStocker 찾기) 
		String destStkSql = " SELECT SZ.STOCKERNAME DESTSTOKCERNAME, "
			 + "       SZ.ZONENAME DESTZONENAME, "
			 + "       SZ.EMPTYSHELFCOUNT, "
			 + "       SZ.MINEMPTYCARRIERCOUNT "
			 + "  FROM CT_DSPSTOCKERZONE SZ "
			 + " WHERE SZ.EMPTYSHELFCOUNT = SZ.MINEMPTYCARRIERCOUNT "
			 + " ORDER BY SZ.STOCKERNAME, SZ.ZONENAME ";
			
		Map<String, Object> destStkBind = new HashMap<String, Object>();

		List<Map<String, Object>> destStkResult = GenericServiceProxy.getSqlMesTemplate().queryForList(destStkSql, destStkBind);

		if(destStkResult.size() > 0)
		{
			for ( int i = 0; i < destStkResult.size(); i++ )
			{
				String destStockerName = (String)destStkResult.get(i).get("DESTSTOKCERNAME");
				String destZoneName = (String)destStkResult.get(i).get("DESTZONENAME");
				
				// DestStocker로 가져와야 할 SourceStocker 찾기 (해당 SourceStocker의 Zone 중에서 비율이 적은 것 찾기) 
				String sourceStkSql = " WITH SOURCESTOCKERLIST "
					  + "   AS (SELECT DISTINCT FROMSTOCKERNAME SOURCESTOCKERNAME "
					  + "           FROM CT_DSPSTOCKERZONEEMPTYCST "
					  + "          WHERE STOCKERNAME = :DESTSTOCKERNAME "
					  + "            AND ZONENAME = :DESTZONENAME) "
					  + "  SELECT SZ.STOCKERNAME SOURCESTOCKERNAME, "
					  + "         SZ.ZONENAME SOURCEZONENAME, "
					  + "         SZ.EMPTYSHELFCOUNT, "
					  + "         SZ.MINEMPTYCARRIERCOUNT, "
					  + "         ROUND ( (SZ.MINEMPTYCARRIERCOUNT / SZ.EMPTYSHELFCOUNT) * 100, 2) RATE "
					  + "    FROM MACHINESPEC MS, CT_DSPSTOCKERZONE SZ "
					  + "   WHERE MS.MACHINENAME = SZ.STOCKERNAME "
					  + "     AND SZ.STOCKERNAME IN "
					  + "            (SELECT SOURCESTOCKERNAME FROM SOURCESTOCKERLIST) "
					  + "     AND MS.MACHINETYPE = :MACHINETYPE "
					  + "     AND MS.DETAILMACHINETYPE = :DETAILMACHINETYPE "
					  + "     AND SZ.EMPTYSHELFCOUNT > SZ.MINEMPTYCARRIERCOUNT "
					  + "ORDER BY RATE, SZ.STOCKERNAME ASC ";
				
				Map<String, Object> sourceStkBind = new HashMap<String, Object>();
				sourceStkBind.put("DESTSTOCKERNAME", destStockerName);
				sourceStkBind.put("DESTZONENAME", destZoneName);
				sourceStkBind.put("MACHINETYPE", GenericServiceProxy.getConstantMap().Mac_StorageMachine);
				sourceStkBind.put("DETAILMACHINETYPE", GenericServiceProxy.getConstantMap().RECIPE_TYPE_MAIN);

				List<Map<String, Object>> sourceStkResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sourceStkSql, sourceStkBind);

				if(sourceStkResult.size() > 0)
				{
					String sourceStockerName = (String)sourceStkResult.get(0).get("SOURCESTOCKERNAME");
					String sourceZoneName = (String)sourceStkResult.get(0).get("SOURCEZONENAME");
					
					// 해당 SourceStocker/Zone의 CST 중 가장 오래된 반송 대상 CST 찾기 
					String cstSql = " SELECT D.DURABLENAME, "
							+ "       D.DURABLETYPE, "
							+ "       D.DURABLESTATE, "
							+ "       D.DURABLEHOLDSTATE, "
							+ "       D.DURABLECLEANSTATE, "
							+ "       NVL(D.TRANSPORTLOCKFLAG, 'N') TRANSPORTLOCKFLAG, "
							+ "       D.MACHINENAME, "
							+ "       D.ZONENAME, "
							+ "       D.PORTNAME, "
							+ "       D.POSITIONNAME, "
							+ "       D.POSITIONTYPE, "
							+ "       D.REGION, "
							+ "       D.KANBAN "
							+ "  FROM DURABLE D "
							+ " WHERE D.MACHINENAME = :MACHINENAME "
							+ "   AND D.ZONENAME = :ZONENAME "
							+ "   AND D.REGION = :REGION "
							+ "   AND NVL(D.TRANSPORTLOCKFLAG, 'N') <> 'Y' "
							+ "   AND NVL(D.DURABLEHOLDSTATE, 'N') <> 'Y' "
							+ " ORDER BY LASTEVENTTIMEKEY ASC ";
					
					Map<String, Object> cstBind = new HashMap<String, Object>();
					cstBind.put("MACHINENAME", sourceStockerName);
					cstBind.put("ZONENAME", sourceZoneName);
					cstBind.put("REGION", GenericServiceProxy.getConstantMap().STOCKERREGION_STOCK);

					List<Map<String, Object>> cstResult = GenericServiceProxy.getSqlMesTemplate().queryForList(cstSql, cstBind);

					if(cstResult.size() > 0)
					{
						String carrierName = (String)cstResult.get(0).get("DURABLENAME");
						String sourcePositionType = (String)cstResult.get(0).get("POSITIONTYPE");
						String sourcePositionName = (String)cstResult.get(0).get("POSITIONNAME");
						
						// Make Transport Job Doc
						Document texDoc = this.writeTransportJob(doc, carrierName, sourceStockerName, sourcePositionType, sourcePositionName, sourceZoneName, 
								destStockerName, "", "", destZoneName, "Stock", "");

						// Make Transport Job
						sendToTEM(texDoc);
						
						// 가장 오래된 CST 하나만 반송 처리
						break;
					}
				}
			}
		}
	}

	private void sendToTEM(Document doc)
	{
		// send to TEMsvr
		try
		{
			String targetSubject = GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEXsvr");

			GenericServiceProxy.getESBServive().sendBySender(targetSubject, doc, "TEXSender");
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