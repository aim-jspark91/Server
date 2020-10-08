package kr.co.aim.messolution.product.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ProductQueueTime;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
 
public class QTimeResolve extends SyncHandler 
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		Element lotList = SMessageUtil.getBodySequenceItem(doc, "LOTLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), "", "");
		
		if(lotList != null)
		{
			for(Object obj : lotList.getChildren())
			{
				Element element = (Element)obj;
				String lotName = SMessageUtil.getChildText(element, "LOTNAME", false);
				String processFlowName = SMessageUtil.getChildText(element, "PROCESSFLOWNAME", false);
				String processFlowVersion = SMessageUtil.getChildText(element, "PROCESSFLOWVERSION", false);
				String processOperationName = SMessageUtil.getChildText(element, "PROCESSOPERATIONNAME", false);
				String processOperationVersion = SMessageUtil.getChildText(element, "PROCESSOPERATIONVERSION", false);
				String toFactoryName = SMessageUtil.getChildText(element, "TOFACTORYNAME", false);
				String toProcessFlowName = SMessageUtil.getChildText(element, "TOPROCESSFLOWNAME", false);
				String toProcessFlowVersion = SMessageUtil.getChildText(element, "TOPROCESSFLOWVERSION", false);
				String toProcessOperationName = SMessageUtil.getChildText(element, "TOPROCESSOPERATIONNAME", false);
				String toProcessOperationVersion = SMessageUtil.getChildText(element, "TOPROCESSOPERATIONVERSION", false);
				String queueTimeType = SMessageUtil.getChildText(element, "QUEUETIMETYPE", false);

				Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
				
				List<Product> productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
				
				if(productList != null)
				{
					for(Product productData : productList)
					{
						ProductQueueTime qTimeData = null;
						
						try
						{
							qTimeData = ExtendedObjectProxy.getProductQueueTimeService().selectByKey(false, new Object[] {productData.getKey().getProductName(), 
																															factoryName, 
																															processFlowName, 
																															processFlowVersion, 
																															processOperationName, 
																															processOperationVersion, 
																															toFactoryName,
																															toProcessFlowName,
																															toProcessFlowVersion,
																															toProcessOperationName,
																															toProcessOperationVersion,
																															queueTimeType});
						}
						catch (Exception ex)
						{
							qTimeData = null;
						}
						
						//2018.12.06_hsryu_add tryCatch Logic.
						try
						{
							if(qTimeData == null)
							{
								throw new CustomException("QUEUE-0004", "");
							}
						}
						catch(Throwable e)
						{
							//qTimeData is null!
						}

						//2018.12.06_hsryu_add tryCatch Logic. because Split & Merge product QTime..
						try
						{
							if(!StringUtils.equals(qTimeData.getqueueTimeState(), "Interlocked"))
							{
								throw new CustomException("QUEUE-0001", "");
							}
							else
							{
								MESProductServiceProxy.getProductServiceImpl().ResolvedQTime(eventInfo, qTimeData);
							}
						}
						catch(Throwable e)
						{
							eventLog.info("Not Interlocked State! " +  productData.getKey().getProductName() + processFlowName + " " + processOperationName + " "
									+ toFactoryName + " " +  toProcessFlowName + " " + toProcessOperationName);
						}
					}
					
					Lot lotdata=MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
					eventInfo.setEventName("QtimeResolve");
					//productUSequence
					List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotdata);
					//List<ProductU> productUSequence2 = new List<ProductU>();
					SetEventInfo setEventInfo = MESLotServiceProxy.getLotInfoUtil().setEventInfo(lotdata, lotdata.getProductQuantity(),productUSequence);
					//Add lothistory
					LotServiceProxy.getLotService().setEvent(lotdata.getKey(), eventInfo, setEventInfo);
				}
			}
		}
		
		return doc;
	}
}
