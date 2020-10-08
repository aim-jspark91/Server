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

public class DSPCapacityBalance extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		/**
		 * StockerRegion(103 Stock).ThresHoldCount = Current Count (Stock -> Stock)
		 * Transport To AlternativeStocker.ToStocker
		 */

		// 각 Stocker의 Stock(Region) 위치의 ThresholdCount를 알기 위한 쿼리
		String stokcerSql = " SELECT SR.STOCKERNAME, "
				+ "       SR.SETCOUNT, "
				+ "       SR.THRESHOLDCOUNT, "
				+ "       SR.GABAGETIME "
				+ "  FROM MACHINESPEC MS, CT_DSPSTOCKERREGION SR "
				+ " WHERE     MS.MACHINENAME = SR.STOCKERNAME "
				+ "       AND MS.MACHINETYPE = :MACHINETYPE "
				+ "       AND MS.DETAILMACHINETYPE = :DETAILMACHINETYPE "
				+ "       AND SR.STOCKERREGIONTYPE = :STOCKERREGIONTYPE "
				+ " ORDER BY SR.STOCKERNAME ASC ";
			
		Map<String, Object> stockerBind = new HashMap<String, Object>();
		stockerBind.put("MACHINETYPE", GenericServiceProxy.getConstantMap().Mac_StorageMachine);
		stockerBind.put("DETAILMACHINETYPE", GenericServiceProxy.getConstantMap().RECIPE_TYPE_MAIN);
		stockerBind.put("STOCKERREGIONTYPE", GenericServiceProxy.getConstantMap().STOCKERREGION_STOCK);

		List<Map<String, Object>> stockerResult = GenericServiceProxy.getSqlMesTemplate().queryForList(stokcerSql, stockerBind);

		if(stockerResult.size() > 0)
		{
			for ( int i = 0; i < stockerResult.size(); i++ )
			{
				String stockerName = (String)stockerResult.get(i).get("STOCKERNAME");
				BigDecimal thresholdCount = ((BigDecimal)stockerResult.get(i).get("THRESHOLDCOUNT"));

				// 해당 Stocker의 CST 갯수 확인
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
						+ "       D.REGION, "
						+ "       D.KANBAN "
						+ "  FROM DURABLE D "
						+ " WHERE D.MACHINENAME = :MACHINENAME " 
						+ "   AND D.REGION = :REGION "
						+ "   AND NVL(D.TRANSPORTLOCKFLAG, 'N') <> 'Y' "
						+ "   AND NVL(D.DURABLEHOLDSTATE, 'N') <> 'Y' "
						+ " ORDER BY LASTEVENTTIMEKEY ASC ";

				Map<String, Object> cstBind = new HashMap<String, Object>();
				cstBind.put("MACHINENAME", stockerName);
				cstBind.put("REGION", GenericServiceProxy.getConstantMap().STOCKERREGION_STOCK);

				List<Map<String, Object>> cstResult = GenericServiceProxy.getSqlMesTemplate().queryForList(cstSql, stockerBind);

				if(cstResult.size() > 0)
				{
					int cstInStock = cstResult.size();
					int iThresholdCount = thresholdCount.intValue();

					if ( cstInStock == iThresholdCount )
					{
						for ( int j = 0; j < cstResult.size(); j++ )
						{
							String carrierName = (String)cstResult.get(j).get("DURABLENAME");
							String sourceMachineName = (String)cstResult.get(j).get("MACHINENAME");
							String sourcePositionType = "SHELF";
							String sourcePositionName = (String)cstResult.get(j).get("POSITIONNAME");
							String sourceZoneName = (String)cstResult.get(j).get("ZONENAME");
							
							// AlterStockerList 중 UsedShlefCount 비율이 적은 Stocker's Zone 찾기
							String alterStockerSql = " WITH ALTERSTOCKERLIST "
								  + "    AS (SELECT * "
								  + "            FROM CT_DSPALTERNATIVESTOCKER "
								  + "           WHERE STOCKERNAME = :STOCKERNAME) "
								  + "   SELECT MIN (TMP.CAL) MIN_CAL, STOCKERNAME AS TOSTOCKER, ZONENAME AS TOZONENAME "
								  + "     FROM (SELECT ROUND (USEDSHELFCOUNT / (TOTALCAPACITY - (PROHIBITEDSHELFCOUNT + EMPTYSHELFCOUNT)) * 100, 2) CAL, A.* "
								  + "             FROM CT_DSPSTOCKERZONE A "
								  + "            WHERE STOCKERNAME IN (SELECT TOSTOCKERNAME FROM ALTERSTOCKERLIST)) TMP "
								  + " GROUP BY STOCKERNAME, ZONENAME "
								  + " ORDER BY MIN_CAL, STOCKERNAME, ZONENAME ";
							
							Map<String, Object> alterStockerBind = new HashMap<String, Object>();
							alterStockerBind.put("MACHINENAME", stockerName);
							alterStockerBind.put("REGION", GenericServiceProxy.getConstantMap().STOCKERREGION_STOCK);

							List<Map<String, Object>> alterStockerResult = GenericServiceProxy.getSqlMesTemplate().queryForList(alterStockerSql, alterStockerBind);

							if(alterStockerResult.size() > 0)
							{
								String destinationMachineName = (String)stockerResult.get(0).get("TOSTOCKER");
								String destinationPortName = "";
								String destinationZoneName = (String)stockerResult.get(0).get("TOZONENAME");
								
								// Make Transport Job Doc
								Document texDoc = this.writeTransportJob(doc, carrierName, sourceMachineName, sourcePositionType, sourcePositionName, sourceZoneName, 
										destinationMachineName, "", destinationPortName, destinationZoneName, "Stock", "");

								// Make Transport Job
								sendToTEM(texDoc);
								
								// 가장 오래된 CST 하나만 반송 처리
								break;
							}
						}
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