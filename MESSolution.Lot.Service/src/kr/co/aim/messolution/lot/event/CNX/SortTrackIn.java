package kr.co.aim.messolution.lot.event.CNX;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SortJobCarrier;
import kr.co.aim.messolution.extended.object.management.data.SortJobProduct;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.MakeLoggedInInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.product.management.info.ext.ProductC;

import org.jdom.Document;
import org.jdom.Element;
public class SortTrackIn extends SyncHandler{
	@Override
	public Object doWorks(Document doc) throws CustomException {
		// ChangeOutTrackIn ( Source )
		// ChangeInTrackIn 
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME",true);		
		String eventname = "";
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(),getEventComment(), null, null);
		String JobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME",true);		
		String JobType = SMessageUtil.getBodyItemValue(doc, "JOBTYPE",true);		
		String recipeName = "";
		List<Element> LotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		
		for (Element Lot : LotList )
		{
			String CheckEmptyCST = SMessageUtil.getChildText(Lot, "CHECKEMPTYCST", true);
			String CarrierName = SMessageUtil.getChildText(Lot, "CARRIERNAME", true);
			String portName = SMessageUtil.getChildText(Lot, "PORTNAME", true);
			String Query = "SELECT LOTNAME, TRANSFERDIRECTION FROM CT_SORTJOBCARRIER WHERE JOBNAME = :JOBNAME AND CARRIERNAME = :CARRIERNAME";
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("JOBNAME", JobName);
			bindMap.put("CARRIERNAME", CarrierName);
    		@SuppressWarnings("unchecked")
    		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(Query, bindMap);

    		if(sqlResult.get(0).get("LOTNAME") != null)
    		{
        		if(!StringUtil.isEmpty(sqlResult.get(0).get("LOTNAME").toString()))
        		{
        			try
        			{
            			String lotName = sqlResult.get(0).get("LOTNAME").toString();
            			String TransfetDirection = sqlResult.get(0).get("TRANSFERDIRECTION").toString();
            			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
            			// Added by smkang on 2018.11.26 - According to Liu Hongwei's request, check possible to run again.
            			if (!MESLotServiceProxy.getLotServiceUtil().possibleToOPIRun(lotName, machineName))
            				throw new CustomException("COMMON-0001", "Impossible to run, MachineName[" + machineName + "] and ProcessOperationName[" + lotData.getProcessOperationName() + "]");
            			
            			Machine eqpData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
            			
        	            CommonValidation.checkMachineState(eqpData);	

        	    		// Recipe
        	    		if(StringUtil.isEmpty(recipeName))
        	    		{
        	    			recipeName = MESRecipeServiceProxy.getRecipeServiceUtil()
        	    					.getMachineRecipe(lotData.getFactoryName(),
        	    							lotData.getProductSpecName(),
        	    							lotData.getProcessFlowName(),
        	    							lotData.getProcessOperationName(), machineName,
        	    							lotData.getUdfs().get("ECCODE"));
        	    		}	
            			
        	    		// TrackIn
        	    		List<ProductC> productCSequence = MESLotServiceProxy.getLotInfoUtil().setProductCSequence(lotName);

        				Map<String, String> udfs = new HashMap<String, String>();
        				udfs.put("PORTNAME", portName);
//        				
        	    		MakeLoggedInInfo makeLoggedInInfo = MESLotServiceProxy.getLotInfoUtil().makeLoggedInInfo(machineName, recipeName, productCSequence, udfs);

        	    		if(StringUtil.equals(JobType.toUpperCase(),"CHANGE"))
        	    		{
        	        		if(StringUtil.equals(TransfetDirection.toUpperCase(),"SOURCE"))
        	        		{
        	        			eventname = "ChangeOutTrackIn";
        	        		} else if(StringUtil.equals(TransfetDirection.toUpperCase(),"TARGET"))
        	        		{
        	        			eventname = "ChangeInTrackIn";
        	        		} else {
        	        			eventname = "TrackIn";
        	        		}
        	    		}
        	    		
        	    		if(StringUtil.equals(JobType.toUpperCase(),"SCRAP"))
        	    		{
        	    			eventname = "ScrapTrackIn";
        	    		}

        	    		if(StringUtil.equals(JobType.toUpperCase(),"TURNSIDE"))
        	    		{
        	    			eventname = "TurnSideTrackIn";
        	    		}
        	    		
        	    		if(StringUtil.equals(JobType.toUpperCase(),"TURNOVER"))
        	    		{
        	    			eventname = "TurnOverTrackIn";
        	    		}
        	    		
        	    		if(StringUtil.equals(JobType.toUpperCase(),"SLOTMAPCHANGE"))
        	    		{
        	    			eventname = "SlotMapChgTrackIn";
        	    		}
        	    		
        	    		eventInfo.setEventComment(eventname);
        	    		eventInfo.setEventName(eventname);
        	    		Lot trackInLot = MESLotServiceProxy.getLotServiceImpl().SorttrackInLot(eventInfo, lotData, makeLoggedInInfo);	
        	    		
        	    		// ************* 2018.12.07 add *************
        	    		// Update ProductState
        	    		if( trackInLot != null)
        	    		{
        	    			String condition = "";
        	    			List<SortJobProduct> SortJobProductList = null;       			      			
        	        		if(StringUtil.equals(JobType.toUpperCase(),"CHANGE"))
        	        		{     
        	        			Object[] bindSet = new Object[] {JobName, CarrierName };         			
        	            		if(StringUtil.equals(TransfetDirection.toUpperCase(),"SOURCE"))
        	            		{
        	            			condition = "where jobname=?" + " and fromcarriername = ?";       					     					
        	            		}
        	            		else 
        	            		{
        	            			condition = "where jobname=?" + " and tocarriername = ?";
        	            		}
        	            		
        						SortJobProductList = ExtendedObjectProxy.getSortJobProductService().select(condition,bindSet);    								
        	        		}
        	        		else 
        	        		{
        	        			condition = "where jobname=?";
        	        			Object[] bindSet = new Object[] {JobName};
        	        			SortJobProductList = ExtendedObjectProxy.getSortJobProductService().select(condition,bindSet);  
        	        		}
        					for (SortJobProduct product : SortJobProductList) {
        						MESLotServiceProxy.getLotServiceImpl().updateSorterJobProduct(eventInfo, JobName, product.getProductName(), GenericServiceProxy.getConstantMap().SORT_SORTPRODUCTSTATE_EXECUTING);				
        					} 
        	    		}
        	    		// ************* 2018.12.07 add *************
        	    		
        	            try
        	            {
        	                SortJobCarrier sortJobCarrier = ExtendedObjectProxy.getSortJobCarrierService().selectByKey(false, new Object[] {JobName, CarrierName});
        	                if(StringUtil.equals(CheckEmptyCST.toUpperCase(), "TRUE"))
        	                {
        	                	sortJobCarrier.settrackflag("OUT");
        	                }
        	                else {
        	                	sortJobCarrier.settrackflag("IN");
        	                }
        	                
        	                ExtendedObjectProxy.getSortJobCarrierService().modify(eventInfo, sortJobCarrier);
        	            }
        	            catch (Exception ex)
        	            {
        	                eventLog.error(ex.getMessage());
        	            }
        	            	
        	            if(lotData!=null)
        	            {
        	        		lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getKey().getLotName());
        	        		MESLotServiceProxy.getLotServiceUtil().deletePermanentHoldInfo(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProcessFlowName(),lotData.getProcessOperationName(), eventInfo);
        	            }
        			}
        			catch (Exception ex)
        			{
        				throw new CustomException("LOT-0117");
        			}
        		}
    		}  		
		}
		
		String Query = "SELECT CARRIERNAME FROM CT_SORTJOBCARRIER WHERE JOBNAME = :JOBNAME AND TRACKFLAG IS NULL";
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("JOBNAME", JobName);
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(Query, bindMap);
		
		if(sqlResult != null && sqlResult.size() > 0)
		{
			for(int i=0; i<sqlResult.size(); i++)
			{
				String CSTName = sqlResult.get(i).get("CARRIERNAME").toString();
				SortJobCarrier sortJobCarrier = ExtendedObjectProxy.getSortJobCarrierService().selectByKey(false, new Object[] {JobName, CSTName});
				sortJobCarrier.settrackflag("IN");
				ExtendedObjectProxy.getSortJobCarrierService().modify(eventInfo, sortJobCarrier);
			}
		}
		
		return doc;
	}
}
