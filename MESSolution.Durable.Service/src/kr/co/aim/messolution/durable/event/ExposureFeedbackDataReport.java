package kr.co.aim.messolution.durable.event;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ExposureFeedBack;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.product.management.data.Product;

import org.jdom.Document;

public class ExposureFeedbackDataReport  extends AsyncHandler{

	@Override
	public void doWorks(Document doc) throws CustomException {

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ExposureDataReport", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
        eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String sUnitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String sLotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String sProductName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String sMaskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String sExposurrecipeName = SMessageUtil.getBodyItemValue(doc, "EXPOSURERECIPENAME", true);

		try
		{
    		//Get Lot Data
            Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(sLotName);

    		//Get Glass Data
    		Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(sProductName);

    		//Map<String, String> productUdfs = productData.getUdfs();
    		//kr.co.aim.greentrack.product.management.info.SetEventInfo setProductEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
    		//setProductEventInfo.setUdfs(productUdfs);
    		//MESProductServiceProxy.getProductServiceImpl().setEvent(productData, setProductEventInfo, eventInfo);

    		//Get Mask Data
    		Durable maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sMaskName);

    		//Map<String, String> maskUdfs = maskData.getUdfs();
    		//SetEventInfo setMaskEventInfo = new SetEventInfo();
    		//setMaskEventInfo.setUdfs(maskUdfs);
    		//MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskData, setMaskEventInfo, eventInfo);

    		//Get Machine Data
    		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sMachineName);

    		//Get Unit Data
    		Machine unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sUnitName);

    		ExposureFeedBack exposureFeedBack = null;
    		
    		try
            {
    		    exposureFeedBack = ExtendedObjectProxy.getExposureFeedBackService().selectByKey(false, 
    		            new Object[] {productData.getKey().getProductName(), lotData.getKey().getLotName(),
    		                          machineData.getKey().getMachineName(), unitData.getKey().getMachineName(),
    		                          lotData.getProcessOperationName(), productData.getProductSpecName(), 
    		                          maskData.getKey().getDurableName()});
            }
            catch (Exception ex)
            {
                //eventLog.error(ex.getStackTrace());
            }    		
    		
    		if(exposureFeedBack != null)
    		{
    		    exposureFeedBack.setCarrierName(lotData.getCarrierName());
                exposureFeedBack.setMachineRecipeName(lotData.getMachineRecipeName());
                exposureFeedBack.setExposureRecipeName(sExposurrecipeName);
    
                exposureFeedBack.setLastEvnetName(eventInfo.getEventName());
                exposureFeedBack.setLastEventTimeKey(eventInfo.getEventTimeKey());
                exposureFeedBack.setLastEventTime(eventInfo.getEventTime());
                exposureFeedBack.setLastEventUser(eventInfo.getEventUser());
                exposureFeedBack.setLastEventComment(eventInfo.getEventComment());
                
                exposureFeedBack = ExtendedObjectProxy.getExposureFeedBackService().modify(eventInfo, exposureFeedBack);
    		}
    		else
    		{
        		exposureFeedBack = new ExposureFeedBack(productData.getKey().getProductName(), lotData.getKey().getLotName(),
        		        machineData.getKey().getMachineName(), unitData.getKey().getMachineName(),
        		        lotData.getProcessOperationName(), productData.getProductSpecName(), maskData.getKey().getDurableName());
    
        		exposureFeedBack.setCarrierName(lotData.getCarrierName());
        		exposureFeedBack.setMachineRecipeName(lotData.getMachineRecipeName());
        		exposureFeedBack.setExposureRecipeName(sExposurrecipeName);
    
        		exposureFeedBack.setLastEvnetName(eventInfo.getEventName());
        		exposureFeedBack.setLastEventTimeKey(eventInfo.getEventTimeKey());
        		exposureFeedBack.setLastEventTime(eventInfo.getEventTime());
        		exposureFeedBack.setLastEventUser(eventInfo.getEventUser());
        		exposureFeedBack.setLastEventComment(eventInfo.getEventComment());
    
        		exposureFeedBack = ExtendedObjectProxy.getExposureFeedBackService().create(eventInfo, exposureFeedBack);
    		}
		}
		catch(CustomException ex)
        {
		    eventLog.error(ex.getStackTrace());
        }
	}
}
