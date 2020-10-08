package kr.co.aim.messolution.consumable.event;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.IncrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.TransitionInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;


public class ChangeMaterialQuantity extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		/*============= Set event =============*/
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeQuantity", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		List<Element> materialList = SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", true);
	
		if (materialList != null)
		{
			for(Element materialE : materialList)
			{
				String materialName = SMessageUtil.getChildText(materialE, "MATERIALNAME", true);
				String changeQuantity = SMessageUtil.getChildText(materialE, "CHANGEQUANTITY", true);
				
				double changedQuantity = Double.parseDouble(changeQuantity);
	
				/*============= Validation ChangedQuantity =============*/
				if(changedQuantity < 0)
				{
					throw new CustomException("MATERIAL-0026", materialName);
				}
				
				Consumable materialData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);
				
				/*============= Change Quantity =============*/
				changeMaterialQuantity(eventInfo,materialData,changedQuantity);		
			}
		}
		return doc;		
	}
	
	
	private void changeMaterialQuantity(EventInfo eventInfo, Consumable materialData, double changedQuantity) 
			throws CustomException
	{
		/*============= Set ConsumableQuantity =============*/
		
		double oldQuantity = materialData.getQuantity();
		Map<String, String> udfs = materialData.getUdfs();
		
		if(changedQuantity < oldQuantity)
		{	
			TransitionInfo transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().decrementQuantityInfo("", "", "", "",
					eventInfo.getEventTimeKey(),(oldQuantity - changedQuantity), udfs);

			MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(materialData,
                                              (DecrementQuantityInfo) transitionInfo, eventInfo);
		}
		else // changedQuantity >= oldQuantity
		{
			TransitionInfo transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().incrementQuantityInfo(changedQuantity - oldQuantity, udfs);
			
			MESConsumableServiceProxy.getConsumableServiceImpl().incrementQuantity(materialData, 
											  (IncrementQuantityInfo)transitionInfo, eventInfo);
		}
		
		
		
		//kr.co.aim.greentrack.consumable.management.info.SetEventInfo setEventInfo = MESConsumableServiceProxy.getConsumableInfoUtil().setEventInfo(materialData, materialData.getAreaName());
		//MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(materialData.getKey().getConsumableName(), setEventInfo, eventInfo);
		
		/*============= After Change Qty, if quantity == 0, Make NotAvaliable =============*/
		if(materialData.getQuantity() == 0 && StringUtil.equals(materialData.getConsumableState(), "Available"))
		{
			eventInfo.setEventName("ChangeState");
			MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();
			makeNotAvailableInfo.setUdfs(materialData.getUdfs());
			MESConsumableServiceProxy.getConsumableServiceImpl().makeNotAvailable(materialData, makeNotAvailableInfo, eventInfo);
		}
	}
}
	
