package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.data.ProductSpecKey;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class CreateMissingPanel extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Release", getEventUser(), getEventComment(), "", "");
		
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", false);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		
		String insertSql = " INSERT INTO CT_PANELJUDGE "
				+ " (PANELNAME, PANELJUDGE, PANELGRADE, XAXIS1, YAXIS1, XAXIS2, YAXIS2, "
				+ " GLASSNAME, HQGLASSNAME, CUTTYPE, LASTEVENTNAME, LASTEVENTUSER, LASTEVENTTIME, LASTEVENTCOMMENT, PRODUCTSPECTYPE)"
				+ " VALUES "
				+ " (:PANELNAME, :PANELJUDGE, :PANELGRADE, NVL(:XAXIS1,0), NVL(:YAXIS1,0), NVL(:XAXIS2,0), NVL(:YAXIS2,0), "
				+ "  :GLASSNAME, :HQGLASSNAME, :CUTTYPE, :LASTEVENTNAME, :LASTEVENTUSER, TO_DATE(:LASTEVENTTIME,'yyyy-MM-dd HH24:mi:ss'), :LASTEVENTCOMMENT, :PRODUCTSPECTYPE)";
		
		String insertSqlToOLED = " INSERT INTO p2oledadm.CT_PANELJUDGE "
				+ " (PANELNAME, PANELJUDGE, PANELGRADE, XAXIS1, YAXIS1, XAXIS2, YAXIS2, "
				+ " GLASSNAME, HQGLASSNAME, CUTTYPE, LASTEVENTNAME, LASTEVENTUSER, LASTEVENTTIME, LASTEVENTCOMMENT, PRODUCTSPECTYPE)"
				+ " VALUES "
				+ " (:PANELNAME, :PANELJUDGE, :PANELGRADE, NVL(:XAXIS1,0), NVL(:YAXIS1,0), NVL(:XAXIS2,0), NVL(:YAXIS2,0), "
				+ "  :GLASSNAME, :HQGLASSNAME, :CUTTYPE, :LASTEVENTNAME, :LASTEVENTUSER, TO_DATE(:LASTEVENTTIME,'yyyy-MM-dd HH24:mi:ss'), :LASTEVENTCOMMENT, :PRODUCTSPECTYPE)";
		
//		String insHistSql = " INSERT INTO CT_PANELJUDGEHISTORY "
//				+ " (TIMEKEY, PANELNAME, PANELJUDGE, PANELGRADE, XAXIS1, YAXIS1, XAXIS2, YAXIS2, " 
//				+ " GLASSNAME, HQGLASSNAME, CUTTYPE, EVENTNAME, EVENTUSER, EVENTTIME, EVENTCOMMENT, PRODUCTSPECTYPE) "
//				+ " SELECT TO_CHAR(SYSDATE, 'YYYYMMDDHH24MISSsss'), PANELNAME, PANELJUDGE, PANELGRADE, XAXIS1, YAXIS1, XAXIS2, YAXIS2, " 
//				+ " GLASSNAME, HQGLASSNAME, CUTTYPE, LASTEVENTNAME, LASTEVENTUSER, LASTEVENTTIME, LASTEVENTCOMMENT, PRODUCTSPECTYPE "
//				+ " FROM CT_PANELJUDGE "
//				+ " WHERE PANELNAME = :PANELNAME ";
		
		List<Lot> lotDataList = new ArrayList<Lot>();
		ProductSpec productSpecData = null;
		if (StringUtils.isNotEmpty(lotName)) {
			Lot lotData = LotServiceProxy.getLotService().selectByKey(new LotKey(lotName));
			productSpecData = ProductServiceProxy.getProductSpecService().selectByKey(new ProductSpecKey("ARRAY", lotData.getProductSpecName(), "00001"));
			lotDataList.add(lotData);
		} else if (StringUtils.isNotEmpty(productSpecName)) {			
			productSpecData = ProductServiceProxy.getProductSpecService().selectByKey(new ProductSpecKey("ARRAY", productSpecName, "00001"));
			lotDataList = LotServiceProxy.getLotService().select("PRODUCTSPECNAME = ? AND LOTSTATE <> ?", new Object[] {productSpecName, GenericServiceProxy.getConstantMap().Lot_Created});
		} else {
			return doc;
		}
		
		for (Lot lotData : lotDataList) {
			List<Object[]> insertArgList = new ArrayList<Object[]>();
//			List<String> missingPanelNameList = new ArrayList<String>();
			
			try {
				List<Product> productDataList = ProductServiceProxy.getProductService().select("LOTNAME = ?", new Object[] {lotData.getKey().getLotName()});
				
				for (Product productData : productDataList) {
					String productName = productData.getKey().getProductName();
					
					int cut1XaxisCount = StringUtils.isNumeric(productSpecData.getUdfs().get("CUT1XAXISCOUNT")) ? Integer.parseInt(productSpecData.getUdfs().get("CUT1XAXISCOUNT")) : 0;
					int cut2XaxisCount = StringUtils.isNumeric(productSpecData.getUdfs().get("CUT2XAXISCOUNT")) ? Integer.parseInt(productSpecData.getUdfs().get("CUT2XAXISCOUNT")) : 0;
					int cut1YaxisCount = StringUtils.isNumeric(productSpecData.getUdfs().get("CUT1YAXISCOUNT")) ? Integer.parseInt(productSpecData.getUdfs().get("CUT1YAXISCOUNT")) : 0;
					int cut2YaxisCount = StringUtils.isNumeric(productSpecData.getUdfs().get("CUT2YAXISCOUNT")) ? Integer.parseInt(productSpecData.getUdfs().get("CUT2YAXISCOUNT")) : 0;
					
					for ( int i = 1; i < 3; i++ )
					{
						if(i == 1)
						{
							// for PanelJudge
							for(int x=0; x<cut1XaxisCount; x++)
							{
								for(int y=0; y< cut1YaxisCount; y++)
								{
									Object[] inbindSet = new Object[15];

									String panelName = productName + Integer.toString(i) + StringUtils.leftPad(Integer.toString(x+1), 2, "0") + StringUtils.leftPad(Integer.toString(y+1), 2, "0");
									try {
										ExtendedObjectProxy.getPanelJudgeService().selectByKey(false, new Object[] {panelName});
									} catch (Exception e) {
										eventLog.info(e);
										
										inbindSet[0] = panelName;
										inbindSet[1] = "G";
										inbindSet[2] = "G";
										inbindSet[3] = String.valueOf(cut1XaxisCount);
										inbindSet[4] = String.valueOf(cut1YaxisCount);
										inbindSet[5] = String.valueOf(cut2XaxisCount);
										inbindSet[6] = String.valueOf(cut2YaxisCount);
										inbindSet[7] = productName;
										inbindSet[8] = productName + Integer.toString(i);
										inbindSet[9] = productSpecData.getUdfs().get("CUTTYPE");
										inbindSet[10] = "Created";
										inbindSet[11] = eventInfo.getEventUser();
										inbindSet[12] = ConvertUtil.getCurrTime();
										inbindSet[13] = eventInfo.getEventComment();
										inbindSet[14] = productSpecData.getUdfs().get("PRODUCTSPECTYPE");
										
										insertArgList.add(inbindSet);
										
//										missingPanelNameList.add(panelName);
									}
								}
							}
						}
						else if(i == 2)
						{
							for(int x=0; x<cut2XaxisCount; x++)
							{
								for(int y=cut1YaxisCount; y<cut1YaxisCount+cut2YaxisCount; y++)
								{
									Object[] inbindSet = new Object[15];

									String panelName = productName + Integer.toString(i) + StringUtils.leftPad(Integer.toString(x+1), 2, "0") + StringUtils.leftPad(Integer.toString(y+1), 2, "0");
									try {
										ExtendedObjectProxy.getPanelJudgeService().selectByKey(false, new Object[] {panelName});
									} catch (Exception e) {
										eventLog.info(e);
										
										inbindSet[0] = panelName;
										inbindSet[1] = "G";
										inbindSet[2] = "G";
										inbindSet[3] = String.valueOf(cut1XaxisCount);
										inbindSet[4] = String.valueOf(cut1YaxisCount);
										inbindSet[5] = String.valueOf(cut2XaxisCount);
										inbindSet[6] = String.valueOf(cut2YaxisCount);
										inbindSet[7] = productName;
										inbindSet[8] = productName + Integer.toString(i);
										inbindSet[9] = productSpecData.getUdfs().get("CUTTYPE");
										inbindSet[10] = "Created";
										inbindSet[11] = eventInfo.getEventUser();
										inbindSet[12] = ConvertUtil.getCurrTime();
										inbindSet[13] = eventInfo.getEventComment();
										inbindSet[14] = productSpecData.getUdfs().get("PRODUCTSPECTYPE");
										
										insertArgList.add(inbindSet);
										
//										missingPanelNameList.add(panelName);
									}
								}
							}
						}
					}
				}
				
				try
				{
					GenericServiceProxy.getSqlMesTemplate().updateBatch(insertSql, insertArgList);
					GenericServiceProxy.getSqlMesTemplate().updateBatch(insertSqlToOLED, insertArgList);
					
//					for (String missingPanelName : missingPanelNameList) {
//						Map<String, Object> insHistbindSet = new HashMap<String, Object>();
//						insHistbindSet.put("PANELNAME", missingPanelName);
//						
//						GenericServiceProxy.getSqlMesTemplate().update(insHistSql, insHistbindSet);
//					}
				}
				catch (Exception ex)
				{
					eventLog.warn(String.format("Update Fail! CT_PANELJUDGE"));
				}
			} catch (Exception e) {
				eventLog.info(e);
			}
		}
		
		return doc;
	}
}