package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.PanelJudge;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.collections.map.ListOrderedMap;
import org.jdom.Document;
import org.jdom.Element;

public class ChangePanelGrade extends SyncHandler 
{
	public Object doWorks(Document doc) throws CustomException 
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String glassName = SMessageUtil.getBodyItemValue(doc, "GLASSNAME", true);		
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);		

		List<Element> panelElementList = SMessageUtil.getBodySequenceItemList(doc, "PANELLIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangePanelGrade", getEventUser(), getEventComment(), "", "");

		Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(glassName);
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(productData.getLotName());
		String ProductSpecName = lotData.getProductSpecName();
		
		//start modify by jhiying on20191016 mantis:5016
		if(StringUtil.equals(lotData.getLotState(),"Received"))
		 {
			throw new CustomException("LOT-4004", "");
		 }
		//end modify by jhiying on20191016 mantis:5016
		//validation 
		CommonValidation.checkAlreadyLotProcessStateTrackIn(lotData);
		
		if (panelElementList != null && panelElementList.size() > 0) {
			// ---------------------------------------------------------------------------------------------------------------------------------------------
			// Modified by smkang on 2018.11.18 - According to EDO's request, old panel grades and new panel grades are updated in Note of ProductHistory.
			//									  Because OPI sends panel list ordered by EDO's layout, it is necessary to be queried by order of panel names.
//			for (Element panelElement : panelElementList) {
//				String panelName = SMessageUtil.getChildText(panelElement, "PANELNAME", true);
//				String newPanelJudge = SMessageUtil.getChildText(panelElement, "NEWJUDGE", true);
//				
//				PanelJudge panelJudge = null;
//				
//				try {
//					panelJudge = ExtendedObjectProxy.getPanelJudgeService().selectByKey(false, new Object[] {panelName});
//				} catch(Throwable e) {
//					// CustomException...
//				}
//				
//				if (panelJudge == null)
//					panelJudge = new PanelJudge();
//				
//				String oldPanelJudge = panelJudge.getPanelJudge();
//				
//				if(!StringUtil.equals(oldPanelJudge, newPanelJudge)) {
//					panelJudge.setPanelJudge(newPanelJudge);
//					panelJudge.setLastEventName(eventInfo.getEventName());
//					panelJudge.setLastEventUser(eventInfo.getEventUser());
//					panelJudge.setLastEventTime(eventInfo.getEventTime());
//					panelJudge.setLastEventComment(eventInfo.getEventComment());
//					panelJudge.setMachineName("");
//					panelJudge.setProcessOperationName(lot.getProcessOperationName());
//					
//					ExtendedObjectProxy.getPanelJudgeService().modify(eventInfo, panelJudge);
//				}
//			}
//			
//			this.checkThresHoldRatio(productSpecName, factoryName, glassName, eventInfo);
			ListOrderedMap changedPanelJudgeInfoMap = new ListOrderedMap();
			List<PanelJudge> panelJudgeDataList = ExtendedObjectProxy.getPanelJudgeService().select("GLASSNAME = ? ORDER BY PANELNAME", new Object[] {glassName});
			for (PanelJudge panelJudgeData : panelJudgeDataList) {
				ChangedPanelJudgeInfo changedPanelJudgeInfo = new ChangedPanelJudgeInfo();
				changedPanelJudgeInfo.setPanelJudgeData(panelJudgeData);
				
				changedPanelJudgeInfoMap.put(panelJudgeData.getPanelName(), changedPanelJudgeInfo);
			}
			
			for (Element panelElement : panelElementList) {
				String panelName = SMessageUtil.getChildText(panelElement, "PANELNAME", true);
				String newPanelJudge = SMessageUtil.getChildText(panelElement, "NEWJUDGE", true);
				
				if (changedPanelJudgeInfoMap.containsKey(panelName)) {
					ChangedPanelJudgeInfo changedPanelJudgeInfo = (ChangedPanelJudgeInfo) changedPanelJudgeInfoMap.get(panelName);
					PanelJudge panelJudgeData = changedPanelJudgeInfo.getPanelJudgeData();
					changedPanelJudgeInfo.setOldPanelJudge(panelJudgeData.getPanelJudge());
					changedPanelJudgeInfo.setNewPanelJudge(newPanelJudge);
					
					if(!StringUtil.equals(panelJudgeData.getPanelJudge(), newPanelJudge)) {
						panelJudgeData.setProductSpecName(ProductSpecName);
						panelJudgeData.setPanelJudge(newPanelJudge);
						panelJudgeData.setLastEventName(eventInfo.getEventName());
						panelJudgeData.setLastEventUser(eventInfo.getEventUser());
						panelJudgeData.setLastEventTime(eventInfo.getEventTime());
						panelJudgeData.setLastEventComment(eventInfo.getEventComment());
						
						ExtendedObjectProxy.getPanelJudgeService().modify(eventInfo, panelJudgeData);
					}
				} else {
					eventLog.warn(panelName + " is not existed in CT_PANELJUDGE.");
				}
			}
			
			String oldPanelGrades = "";
			String newPanelGrades = "";
			
			for (int index = 0; index < changedPanelJudgeInfoMap.size(); index++) {
				ChangedPanelJudgeInfo changedPanelJudgeInfo = (ChangedPanelJudgeInfo) changedPanelJudgeInfoMap.getValue(index);
				oldPanelGrades = oldPanelGrades.concat(changedPanelJudgeInfo.getOldPanelJudge());
				newPanelGrades = newPanelGrades.concat(changedPanelJudgeInfo.getNewPanelJudge());
			}
			
			if (!oldPanelGrades.equals(newPanelGrades)) {
				// LotHistory 기록
				kr.co.aim.greentrack.lot.management.info.SetEventInfo setLotEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
				// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
				//lotData.getUdfs().put("NOTE", glassName);
				//setLotEventInfo.setUdfs(lotData.getUdfs());
				setLotEventInfo.getUdfs().put("NOTE", glassName);
				LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setLotEventInfo);
				
				// Modified by smkang on 2019.05.24 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//			    lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
//
//				lotData.getUdfs().put("NOTE", "");
//				LotServiceProxy.getLotService().update(lotData);
				Map<String, String> updateUdfs = new HashMap<String, String>();
				updateUdfs.put("NOTE", "");
				MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
				
				// ProductHistory 기록
				SetEventInfo setProductEventInfo = new SetEventInfo();
				// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
				//productData.getUdfs().put("NOTE", "Before[" + oldPanelGrades + "], After[" + newPanelGrades + "]");
				//setProductEventInfo.setUdfs(productData.getUdfs());
				setProductEventInfo.getUdfs().put("NOTE", "Before[" + oldPanelGrades + "], After[" + newPanelGrades + "]");
				ProductServiceProxy.getProductService().setEvent(productData.getKey(), eventInfo, setProductEventInfo);
				
				// Modified by smkang on 2019.05.28 - ProductServiceProxy.getProductService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//				productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productData.getKey().getProductName());
//				
//				productData.getUdfs().put("NOTE", "");
//				ProductServiceProxy.getProductService().update(productData);
				Map<String, String> updateUdfs2 = new HashMap<String, String>();
				updateUdfs2.put("NOTE", "");
				MESProductServiceProxy.getProductServiceImpl().updateProductWithoutHistory(productData, updateUdfs2);
				
				this.checkThresHoldRatio(productSpecName, factoryName, glassName, eventInfo);
			} else {
				eventLog.info("Any PanelGrade is not changed. Before[" + oldPanelGrades + "], After[" + newPanelGrades + "]");
			}
			// ---------------------------------------------------------------------------------------------------------------------------------------------
		}

		return doc;
	}

	private void checkThresHoldRatio(String productSpecName, String factoryName, String glassName, EventInfo eventInfo) throws CustomException 
	{
 		Double cut1ThresHoldRatio = 0.0;
		Double cut2ThresHoldRatio = 0.0;
		Double cut3ThresHoldRatio = 0.0;
		Double cut4ThresHoldRatio = 0.0;
		
		// 2019.03.19_hsryu_For Note.
		Double currentCut1ThresHoldRatio = 0.0;
		Double currentCut2ThresHoldRatio = 0.0;
		
		boolean holdFlag = true;

		ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(factoryName, productSpecName, "00001");
		
		int cutType = StringUtil.equals(productSpecData.getUdfs().get("CUTTYPE").toUpperCase(), "HALF") ? 2 : 4 ;
		
		if(Double.valueOf(productSpecData.getUdfs().get("CUT1THRESHOLDRATIO")) != null)
			cut1ThresHoldRatio = Double.valueOf(productSpecData.getUdfs().get("CUT1THRESHOLDRATIO"));
		
		if(Double.valueOf(productSpecData.getUdfs().get("CUT2THRESHOLDRATIO")) != null)
			cut2ThresHoldRatio = Double.valueOf(productSpecData.getUdfs().get("CUT2THRESHOLDRATIO"));

		if(StringUtil.equals(String.valueOf(cutType), "4")) {
			if(Double.valueOf(productSpecData.getUdfs().get("CUT3THRESHOLDRATIO")) != null)
				cut3ThresHoldRatio = Double.valueOf(productSpecData.getUdfs().get("CUT3THRESHOLDRATIO"));
			
			if(Double.valueOf(productSpecData.getUdfs().get("CUT4THRESHOLDRATIO")) != null)
				cut4ThresHoldRatio = Double.valueOf(productSpecData.getUdfs().get("CUT4THRESHOLDRATIO"));
		}

		for(int i = 1; i < cutType + 1; i++) {
			StringBuilder sql = new StringBuilder();

			sql.append(" select  PJ.PANELNAME, SUBSTR(PJ.PANELNAME,-5,1) CUTPOSITION, PJ.PANELJUDGE ");
			sql.append("   from CT_PANELJUDGE PJ ");
			sql.append("  WHERE     PJ.GLASSNAME = :GLASSNAME ");
			sql.append("        AND SUBSTR(PJ.PANELNAME,-5,1) = :CUTNUM ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("GLASSNAME", glassName);
			bindMap.put("CUTNUM", i);

			List<Map<String, Object>> sqlResult = new ArrayList<Map<String, Object>>();

			try {
				sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

				if(sqlResult.size() > 0) {
					double oNum = 0;
					double xNum = 0;
					double gNum = 0;
					double otherGradeNum = 0;

					for(int j = 0; j < sqlResult.size(); j++) {
						String panelJudge = sqlResult.get(j).get("PANELJUDGE").toString();

						if(StringUtil.equals(panelJudge, "O"))
							oNum++;
						else if(StringUtil.equals(panelJudge, "X"))
							xNum++;
						else if(StringUtil.equals(panelJudge, "G"))
							gNum++;
						else if((!StringUtil.equals(panelJudge, "G"))&&(!StringUtil.equals(panelJudge, "O")&&(!StringUtil.equals(panelJudge, "X"))))
							otherGradeNum ++;
					}

					Double cutXThresHoldRatio = 0.0;
					
					if(i==1) cutXThresHoldRatio = cut1ThresHoldRatio;
					else if(i==2) cutXThresHoldRatio = cut2ThresHoldRatio;
					else if(i==3) cutXThresHoldRatio = cut3ThresHoldRatio;
					else if(i==4) cutXThresHoldRatio = cut4ThresHoldRatio;

//					HQGlassJudge hqGlassJudge = new HQGlassJudge();
//
//					try {
//						hqGlassJudge = ExtendedObjectProxy.getHQGlassJudgeService().selectByKey(false, new Object[] {glassName+String.valueOf(i)});
//					} catch(Throwable e) {
//						//hqGlass is not exist..
//					}

//					if (hqGlassJudge != null&&cutXThresHoldRatio!= 0.0) {
					if( cutXThresHoldRatio != 0.0 ) {	
						if(oNum != 0) {
							Double ThresRatio = oNum/(oNum+xNum+otherGradeNum);
							if(ThresRatio<cutXThresHoldRatio) {
								
								//2019.03.19_hsryu_Insert Logic. For HoldNote. Mantis 0003148.
								if(i==1) currentCut1ThresHoldRatio = ThresRatio;
								if(i==2) currentCut2ThresHoldRatio = ThresRatio;
								eventLog.info("CUT" + i + ", "+ "CutThresHold : " + cutXThresHoldRatio + ", CurrentThresHold : " + ThresRatio);
								//holdFlag = true;

//								if(!StringUtil.equals(hqGlassJudge.getHQGlassJudge(), "N")) {
//									hqGlassJudge.setHQGlassJudge("N");
//									hqGlassJudge.setLastEventName(eventInfo.getEventName());
//									hqGlassJudge.setLastEventUser(eventInfo.getEventUser());
//									hqGlassJudge.setLastEventTime(eventInfo.getEventTime());
//									hqGlassJudge.setLastEventComment(eventInfo.getEventComment());
//
//									ExtendedObjectProxy.getHQGlassJudgeService().modify(eventInfo, hqGlassJudge);
//								}
							}
							else
							{
								//2019.03.19_hsryu_Insert Logic. For HoldNote. Mantis 0003148.
								if(i==1) currentCut1ThresHoldRatio = ThresRatio;
								if(i==2) currentCut2ThresHoldRatio = ThresRatio;
								eventLog.info("Cut Judge is Good.");
								holdFlag = false;
								
								/******** 2019.03.20_hsryu_Not Changed Judge. Requested by CIM. Mantis 3148. **********/
//								if(!StringUtil.equals(hqGlassJudge.getHQGlassJudge(), "G")) {
//									hqGlassJudge.setHQGlassJudge("G");
//									hqGlassJudge.setLastEventName(eventInfo.getEventName());
//									hqGlassJudge.setLastEventUser(eventInfo.getEventUser());
//									hqGlassJudge.setLastEventTime(eventInfo.getEventTime());
//									hqGlassJudge.setLastEventComment(eventInfo.getEventComment());
//
//									ExtendedObjectProxy.getHQGlassJudgeService().modify(eventInfo, hqGlassJudge);
//								}
							}
						}
						else if(gNum != 0) 
						{
							eventLog.info("PanelJudge 'O' Number is zero. ");
							
							Double ThresRatio = gNum/(gNum+xNum+otherGradeNum);
							
							if(ThresRatio < cutXThresHoldRatio) {
								
								//2019.03.19_hsryu_Insert Logic. For HoldNote. Mantis 0003148.
								if(i==1) currentCut1ThresHoldRatio = ThresRatio;
								if(i==2) currentCut2ThresHoldRatio = ThresRatio;
								eventLog.info("CUT" + i + ", "+ "CutThresHold : " + cutXThresHoldRatio + ", CurrentThresHold : " + ThresRatio);
								//holdFlag = true;
								
								/******** 2019.03.20_hsryu_Not Changed Judge. Requested by CIM. Mantis 3148. **********/
//								if(!StringUtil.equals(hqGlassJudge.getHQGlassJudge(), "N")) {
//									hqGlassJudge.setHQGlassJudge("N");
//									hqGlassJudge.setLastEventName(eventInfo.getEventName());
//									hqGlassJudge.setLastEventUser(eventInfo.getEventUser());
//									hqGlassJudge.setLastEventTime(eventInfo.getEventTime());
//									hqGlassJudge.setLastEventComment(eventInfo.getEventComment());
//
//									ExtendedObjectProxy.getHQGlassJudgeService().modify(eventInfo, hqGlassJudge);
//								}
							}
							else
							{
								//2019.03.19_hsryu_Insert Logic. For HoldNote. Mantis 0003148.
								if(i==1) currentCut1ThresHoldRatio = ThresRatio;
								if(i==2) currentCut2ThresHoldRatio = ThresRatio;
								eventLog.info("Cut Judge is Good.");
								holdFlag = false;
								
								/******** 2019.03.20_hsryu_Not Changed Judge. Requested by CIM. Mantis 3148. **********/
//								if(!StringUtil.equals(hqGlassJudge.getHQGlassJudge(), "G")) {
//									hqGlassJudge.setHQGlassJudge("G");
//									hqGlassJudge.setLastEventName(eventInfo.getEventName());
//									hqGlassJudge.setLastEventUser(eventInfo.getEventUser());
//									hqGlassJudge.setLastEventTime(eventInfo.getEventTime());
//									hqGlassJudge.setLastEventComment(eventInfo.getEventComment());
//
//									ExtendedObjectProxy.getHQGlassJudgeService().modify(eventInfo, hqGlassJudge);
//								}
							}
						}
						else
						{
							eventLog.info("'O' Judge Num, 'G' Judge Num all zero. ");
						}
					}
					// 2019.04.16_hsryu_Insert Logic. if set ThresRatioHold is 0, Not Check! 
					else
					{
						holdFlag = false;
					}
				}
			} catch (Exception ex) {
				throw new CustomException("CRATE-0003", factoryName, productSpecName);
			}
		}
		
//		List<HQGlassJudge> hqGlassJudgeList = new ArrayList<HQGlassJudge>();
//		
//		try {
//			hqGlassJudgeList = ExtendedObjectProxy.getHQGlassJudgeService().select("where glassname = ? ", new Object[]{glassName});
//		} catch(Throwable e) {
//			//HQGlassJudge List is not exist..
//		}
//		
//		if(hqGlassJudgeList.size() > 0) {
//			boolean ngFlag = true;
//			
//			for(int i=0; i<hqGlassJudgeList.size(); i++) {
//				HQGlassJudge hqGlassJudge = hqGlassJudgeList.get(i);
//				
//				if(!StringUtil.equals(hqGlassJudge.getHQGlassJudge(), "N"))
//					ngFlag = false;
//			}
			
//			if(ngFlag) {
			if(holdFlag)
			{
				/******** 2019.03.20_hsryu_Not Changed Judge. Requested by CIM. Mantis 3148. **********/
				Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(glassName);
				
//				if(!StringUtil.equals(productData.getProductGrade(), "N")) {
//					productData.setProductGrade("N");
//					ProductServiceProxy.getProductService().update(productData);
//					
//					// Added by smkang on 2018.11.21 - For avoid duplication of timekey.
//					Timestamp currentTimeStamp = TimeStampUtil.getCurrentTimestamp();
//					//2019.03.19_hsryu_Delete Change EventTime Logic. 
//					//eventInfo.setEventTime(currentTimeStamp);
//					eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(currentTimeStamp));
//					
//					SetEventInfo setEventInfo = new SetEventInfo();
//					ProductServiceProxy.getProductService().setEvent(productData.getKey(), eventInfo, setEventInfo);
//				}
				
				try {
					Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(productData.getLotName());
					String note = "ProductID:[" + productData.getKey().getProductName() + "], cut1SettingTheshold：[" + cut1ThresHoldRatio + "], cut1 Current Threshold:[" + currentCut1ThresHoldRatio + "], cut2SettingTheshold：[" + cut2ThresHoldRatio + "], cut2 Current Threshold:[ " +currentCut2ThresHoldRatio + "]";

					// 2019.06.21_hsryu_Delete Logic.  Move to logic.
					//lotData.getUdfs().put("NOTE",note);
					//LotServiceProxy.getLotService().update(lotData);
					
					//ProcessOperationSpec processOperation = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), processOperationName);

					EventInfo eventInfo1 = EventInfoUtil.makeEventInfo("ThresRatioHold", getEventUser(), getEventComment(), "", "");

					eventInfo1.setEventTime(eventInfo.getEventTime());
					eventInfo1.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
					eventInfo1.setReasonCode("TRHL");
					eventInfo1.setReasonCodeType(GenericServiceProxy.getConstantMap().REASONCODETYPE_HOLDLOT);
					// 2019.04.10_hsryu_Change EventComment. Mantis 0003462.
					//eventInfo1.setEventComment("Hold by CutThresHoldRatio");
					eventInfo1.setEventComment(note);
					
					if(!lotData.getLotHoldState().equals("Y")) {
						List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
						MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo(productUSequence);
						// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//						makeOnHoldInfo.setUdfs(lotData.getUdfs());
						makeOnHoldInfo.getUdfs().put("NOTE", note);
						LotServiceProxy.getLotService().makeOnHold(lotData.getKey(), eventInfo1, makeOnHoldInfo);
						
						try {
							lotData = MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(lotData.getKey().getLotName(), "TRHL", "INT","AHOLD", eventInfo1);
						} catch (Exception e) {
							eventLog.warn(e);
						}
					} else {	
						kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
						// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//						setEventInfo.setUdfs(lotData.getUdfs());
						setEventInfo.getUdfs().put("NOTE", note);
						LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo1, setEventInfo);
						
						try {
							lotData = MESLotServiceProxy.getLotServiceImpl().addMultiHoldLot(lotData.getKey().getLotName(), "TRHL", "INT","AHOLD", eventInfo1);
						} catch (Exception e) {
							eventLog.warn(e);
						}
						// -------------------------------------------------------------------------------------------------------------------------------------------
					}
					
					// Modified by smkang on 2019.05.24 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//					lotData.getUdfs().put("NOTE", "");
//					LotServiceProxy.getLotService().update(lotData);
					Map<String, String> updateUdfs = new HashMap<String, String>();
					updateUdfs.put("NOTE", "");
					MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
				} catch (Throwable ex) {
					eventLog.error(ex.getMessage());
				}
			}
			/******** 2019.03.20_hsryu_Not Changed Judge. Requested by CIM. Mantis 3148. **********/	
//			else
//			{
//				Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(glassName);
//				
//				if(StringUtil.equals(productData.getProductGrade(), "N")) {
//					productData.setProductGrade("G");
//
//					ProductServiceProxy.getProductService().update(productData);
//
//					// Added by smkang on 2018.11.21 - For avoid duplication of timekey.
//					Timestamp currentTimeStamp = TimeStampUtil.getCurrentTimestamp();
//					eventInfo.setEventTime(currentTimeStamp);
//					eventInfo.setEventTimeKey(TimeStampUtil.getEventTimeKeyFromTimestamp(currentTimeStamp));
//
//					SetEventInfo setEventInfo = new SetEventInfo();
//					ProductServiceProxy.getProductService().setEvent(productData.getKey(), eventInfo, setEventInfo);
//				}
//			}
	}
}

/**
 * @author smkang
 * @since 2018.11.18
 * @see According to EDO's request, old panel grades and new panel grades are updated in Note of ProductHistory.
 */
class ChangedPanelJudgeInfo {
	private PanelJudge panelJudgeData;
	private String oldPanelJudge;
	private String newPanelJudge;
	
	public PanelJudge getPanelJudgeData() {
		return panelJudgeData;
	}
	
	public void setPanelJudgeData(PanelJudge panelJudgeData) {
		this.panelJudgeData = panelJudgeData;
	}
	
	public String getOldPanelJudge() {
		return oldPanelJudge;
	}
	
	public void setOldPanelJudge(String oldPanelJudge) {
		this.oldPanelJudge = oldPanelJudge;
	}
	
	public String getNewPanelJudge() {
		return newPanelJudge;
	}
	
	public void setNewPanelJudge(String newPanelJudge) {
		this.newPanelJudge = newPanelJudge;
	}
	}