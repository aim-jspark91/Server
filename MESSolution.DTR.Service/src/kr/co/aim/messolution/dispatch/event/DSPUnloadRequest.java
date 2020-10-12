package kr.co.aim.messolution.dispatch.event;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.port.management.data.PortSpec;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

public class DSPUnloadRequest extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		/**
		 * UnloadRequest : Port -> ConnectedStocker's Stock(Region)
		 */
		
		// Unload Machine, Port List
		String unloadSql = " SELECT M.MACHINENAME, "
				+ " P.PORTNAME, "
				+ " P.CARRIERNAME, "
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
				+ " AND P.CARRIERNAME IS NOT NULL "
				+ " ORDER BY M.MACHINENAME, P.PORTNAME ASC ";				
				
		Map<String, Object> unloadBind = new HashMap<String, Object>();
		unloadBind.put("RESOURCESTATE", GenericServiceProxy.getConstantMap().Rsc_InService);
		unloadBind.put("COMMUNICATIONSTATE", GenericServiceProxy.getConstantMap().Mac_OffLine);
		unloadBind.put("ACCESSMODE", GenericServiceProxy.getConstantMap().Port_Auto);
		unloadBind.put("TRANSFERSTATE", GenericServiceProxy.getConstantMap().Port_ReadyToUnload);
		unloadBind.put("PORTSTATENAME", GenericServiceProxy.getConstantMap().Port_UP);
		
		List<Map<String, Object>> unloadResult = GenericServiceProxy.getSqlMesTemplate().queryForList(unloadSql, unloadBind);

		if(unloadResult.size() > 0)
		{
			for ( int i = 0; i < unloadResult.size(); i++ )
			{
				String sourceMachineName = (String)unloadResult.get(i).get("MACHINENAME");
				String sourcePortName = (String)unloadResult.get(i).get("PORTNAME");
				String carrierName = (String)unloadResult.get(i).get("CARRIERNAME");
				
				String plDispatchFlag = (String)unloadResult.get(i).get("PLDISPATCHFLAG");
				String puDispatchFlag = (String)unloadResult.get(i).get("PUDISPATCHFLAG");
				
				PortSpec portSpecData = MESPortServiceProxy.getPortServiceUtil().getPortSpecInfo(sourceMachineName, sourcePortName);
				String portType = portSpecData.getPortType();
				
				if (StringUtil.equals(portType, GenericServiceProxy.getConstantMap().PORT_TYPE_PL) ||
					StringUtil.equals(portType, GenericServiceProxy.getConstantMap().PORT_TYPE_PB))
				{
					// PL, PB
					if (StringUtil.equals(plDispatchFlag, "Y"))
					{
						// Unload 대상 설비의 근접 스토커 확인
						String connStkSql = " SELECT C.MACHINENAME, C.PORTNAME, C.LUDIRECTION, C.STOCKERNAME, C.ZONENAME, C.POSITION " 
								 + " FROM CT_DSPCONNECTEDSTOCKER C "
								 + " WHERE C.MACHINENAME = :MACHINENAME "
								 + " AND C.PORTNAME = :PORTNAME "
								 + " AND C.LUDIRECTION = :LUDIRECTION "
								 + " ORDER BY C.POSITION ASC ";
						
						Map<String, Object> connStkBind = new HashMap<String, Object>();
						connStkBind.put("MACHINENAME", sourceMachineName);
						connStkBind.put("PORTNAME", sourcePortName);
						connStkBind.put("LUDIRECTION", "UnloadTo");
						
						List<Map<String, Object>> connStkResult = GenericServiceProxy.getSqlMesTemplate().queryForList(connStkSql, connStkBind);

						if(connStkResult.size() > 0)
						{
							for ( int j = 0; j < connStkResult.size(); j++ )
							{
								String sourcePositionType = "PORT";
								String sourcePositionName = sourcePortName;
								String sourceZoneName = "";
								String destinationMachineName = (String)connStkResult.get(j).get("STOCKERNAME");
								String destinationPositionType = "SHELF";
								String destinationPositionName = "";
								String destinationZoneName = (String)connStkResult.get(j).get("ZONENAME");
								String region = "Stock";
								String kanban = "";
								
								// Unload 대상 CST 이지만, 대기해야 하는 경우는 제외해야 함
								String unloadTimeSql = "SELECT ROUND((SYSDATE - NVL(UNLOADTIME, SYSDATE)) * 60 * 24) UNLOADTIME FROM PORT "
										+ " WHERE MACHINENAME = :MACHINENAME "
										+ " AND PORTNAME = :PORTNAME ";
								
								Map<String, Object> unloadTimeBind = new HashMap<String, Object>();
								unloadTimeBind.put("MACHINENAME", sourceMachineName);
								unloadTimeBind.put("PORTNAME", sourcePortName);
								
								List<Map<String, Object>> unloadTimeResult = GenericServiceProxy.getSqlMesTemplate().queryForList(unloadTimeSql, unloadTimeBind);

								if(unloadTimeResult.size() > 0)
								{
									BigDecimal unloadTime = ((BigDecimal)unloadTimeResult.get(0).get("UNLOADTIME"));
									BigDecimal plWaitTimeE2E = (BigDecimal)unloadResult.get(i).get("PLWAITTIMEE2E");
									BigDecimal plWaitTimePush = (BigDecimal)unloadResult.get(i).get("PLWAITTIMEPUSH");
									
									int iUnloadTime = unloadTime.intValue();
									int iPlWaitTimeE2E = plWaitTimeE2E.intValue();
									int iPlWaitTimePush = plWaitTimePush.intValue();
									
									if ( iUnloadTime < iPlWaitTimeE2E || iUnloadTime < iPlWaitTimePush )
									{
										// Make Transport Job Doc
										Document texDoc = this.writeTransportJob(doc, carrierName, sourceMachineName, sourcePositionType, sourcePositionName, sourceZoneName, 
												destinationMachineName, destinationPositionType, destinationPositionName, destinationZoneName, region, kanban);
										
										// Make Transport Job
										sendToTEM(texDoc);
									}
								}
							}
						}
					}
				}
				else
				{
					// PU
					if (StringUtil.equals(puDispatchFlag, "Y"))
					{
						// Unload 대상 설비의 근접 스토커 확인
						String connStkSql = " SELECT C.MACHINENAME, C.PORTNAME, C.LUDIRECTION, C.STOCKERNAME, C.ZONENAME, C.POSITION " 
								 + " FROM CT_DSPCONNECTEDSTOCKER C "
								 + " WHERE C.MACHINENAME = :MACHINENAME "
								 + " AND C.PORTNAME = :PORTNAME "
								 + " AND C.LUDIRECTION = :LUDIRECTION "
								 + " ORDER BY C.POSITION ASC ";
						
						Map<String, Object> connStkBind = new HashMap<String, Object>();
						unloadBind.put("MACHINENAME", sourceMachineName);
						unloadBind.put("PORTNAME", sourcePortName);
						unloadBind.put("LUDIRECTION", "UnloadTo");
						
						List<Map<String, Object>> connStkResult = GenericServiceProxy.getSqlMesTemplate().queryForList(connStkSql, connStkBind);

						if(connStkResult.size() > 0)
						{
							for ( int j = 0; j < connStkResult.size(); j++ )
							{
								String sourcePositionType = "PORT";
								String sourcePositionName = sourcePortName;
								String sourceZoneName = "";
								String destinationMachineName = (String)connStkResult.get(j).get("STOCKERNAME");
								String destinationPositionType = "SHELF";
								String destinationPositionName = "";
								String destinationZoneName = (String)connStkResult.get(j).get("ZONENAME");
								String region = "Stock";
								String kanban = "";
								
								// Unload 대상 CST 이지만, 대기해야 하는 경우는 제외해야 함
								String unloadTimeSql = "SELECT ROUND((SYSDATE - NVL(UNLOADTIME, SYSDATE)) * 60 * 24) UNLOADTIME FROM PORT "
										+ " WHERE MACHINENAME = :MACHINENAME "
										+ " AND PORTNAME = :PORTNAME ";
								
								Map<String, Object> unloadTimeBind = new HashMap<String, Object>();
								unloadTimeBind.put("MACHINENAME", sourceMachineName);
								unloadTimeBind.put("PORTNAME", sourcePortName);
								
								List<Map<String, Object>> unloadTimeResult = GenericServiceProxy.getSqlMesTemplate().queryForList(unloadTimeSql, unloadTimeBind);

								if(unloadTimeResult.size() > 0)
								{
									String unloadTime = (String)unloadTimeResult.get(0).get("UNLOADTIME");
									String puWaitTimeE2E = (String)unloadResult.get(i).get("PUWAITTIMEE2E");
									String puWaitTimePush = (String)unloadResult.get(i).get("PUWAITTIMEPUSH");

									int iUnloadTime = Integer.parseInt(unloadTime);
									int iPuWaitTimeE2E = Integer.parseInt(puWaitTimeE2E);
									int iPuWaitTimePush = Integer.parseInt(puWaitTimePush);
									
									if ( iUnloadTime < iPuWaitTimeE2E || iUnloadTime < iPuWaitTimePush )
									{
										// Make Transport Job Doc
										Document texDoc = this.writeTransportJob(doc, carrierName, sourceMachineName, sourcePositionType, sourcePositionName, sourceZoneName, 
												destinationMachineName, destinationPositionType, destinationPositionName, destinationZoneName, region, kanban);
										
										// Make Transport Job
										sendToTEM(texDoc);
									}
								}
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