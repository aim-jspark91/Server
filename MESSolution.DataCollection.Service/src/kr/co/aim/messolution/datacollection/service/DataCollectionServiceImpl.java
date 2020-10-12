package kr.co.aim.messolution.datacollection.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.datacollection.MESEDCServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.greentrack.datacollection.DataCollectionServiceProxy;
import kr.co.aim.greentrack.datacollection.management.data.DCData;
import kr.co.aim.greentrack.datacollection.management.data.DCDataItem;
import kr.co.aim.greentrack.datacollection.management.data.DCDataKey;
import kr.co.aim.greentrack.datacollection.management.data.DCDataResult;
import kr.co.aim.greentrack.datacollection.management.data.DCDataSample;
import kr.co.aim.greentrack.datacollection.management.data.DCSpec;
import kr.co.aim.greentrack.datacollection.management.data.DCSpecItem;
import kr.co.aim.greentrack.datacollection.management.data.DCSpecKey;
import kr.co.aim.greentrack.datacollection.management.info.CollectDataInfo;
import kr.co.aim.greentrack.datacollection.management.info.ext.ResultData;
import kr.co.aim.greentrack.datacollection.management.info.ext.SampleData;
import kr.co.aim.greentrack.datacollection.management.sql.SqlStatement;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DataCollectionServiceImpl implements ApplicationContextAware {
	/**
	 * @uml.property name="applicationContext"
	 * @uml.associationEnd
	 */
	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog(DataCollectionServiceImpl.class);

	/**
	 * @param arg0
	 * @throws BeansException
	 * @uml.property name="applicationContext"
	 */
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		applicationContext = arg0;
	}

	/**
	 * DCOL
	 * @since 2016.06.22
	 * @author swcho
	 * @param DCSpecData
	 * @param factoryName
	 * @param machineName
	 * @param machineRecipeName
	 * @param materialName
	 * @param productSpecName
	 * @param processFlowName
	 * @param processOperationName
	 * @param sds
	 * @param udfs
	 * @param eventUser
	 * @param eventComment
	 * @return
	 * @throws CustomException
	 */
	public long collectData(DCSpec DCSpecData,
							  String factoryName, String machineName, String machineRecipeName,
							  String materialName, String productSpecName, String processFlowName, String processOperationName,
							  List<SampleData> sds, List<DCSpecItem> DCSpecItemList,
							  Map<String, String> udfs,
							  String eventUser, String eventComment)
		throws CustomException
	{
		CollectDataInfo collectDataInfo = new CollectDataInfo();

		collectDataInfo.setNewDataFlag("Y");
		collectDataInfo.setDCSpecName(DCSpecData.getKey().getDCSpecName());
		collectDataInfo.setDCSpecVersion(DCSpecData.getKey().getDCSpecVersion());
		collectDataInfo.setMaterialType(DCSpecData.getMaterialType());

		collectDataInfo.setMaterialName(materialName);
		collectDataInfo.setFactoryName(factoryName);
		collectDataInfo.setProductSpecName(productSpecName);
		collectDataInfo.setProductSpecVersion("00001");
		collectDataInfo.setProcessFlowName(processFlowName);
		collectDataInfo.setProcessFlowVersion("00001");
		collectDataInfo.setProcessOperationName(processOperationName);
		collectDataInfo.setProcessOperationVersion("00001");

		collectDataInfo.setMachineName(machineName);
		collectDataInfo.setMachineRecipeName(machineRecipeName);
		collectDataInfo.setSds(sds);
		collectDataInfo.setUdfs(udfs);
		collectDataInfo.setCreateUser(eventUser);
		collectDataInfo.setCreateTime(TimeUtils.getCurrentTimestamp());
		collectDataInfo.setCreateComment(eventComment);

		long result = 0;
		try
		{
			result = MESEDCServiceProxy.getDataCollectionServiceImpl().collectData(collectDataInfo, DCSpecItemList);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "DCOL", fe.getMessage());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("SYS-9999", "DCOL", ne.getMessage());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("SYS-9999", "DCOL", de.getMessage());
		}

		return result;
	}
	
	public long collectData(CollectDataInfo collectDataInfo, List<DCSpecItem> dcSpecItemList)
		throws NotFoundSignal, DuplicateNameSignal, FrameworkErrorSignal
	{
		if (log.isInfoEnabled())
			log.info(String.format("¢º START %s.%s", this.getClass().getSimpleName(), "collectData"));

		try
		{
			// 1. Set DCData
			DCData data = new DCData();

			data.setDCSpecName(collectDataInfo.getDCSpecName());
			data.setDCSpecVersion(collectDataInfo.getDCSpecVersion());

			data.setMaterialType(collectDataInfo.getMaterialType());
			data.setMaterialName(collectDataInfo.getMaterialName());

			data.setFactoryName(collectDataInfo.getFactoryName());
			data.setProductSpecName(collectDataInfo.getProductSpecName());
			data.setProductSpecVersion(collectDataInfo.getProductSpecVersion());

			data.setProcessFlowName(collectDataInfo.getProcessFlowName());
			data.setProcessFlowVersion(collectDataInfo.getProcessFlowVersion());

			data.setProcessOperationName(collectDataInfo.getProcessOperationName());
			data.setProcessOperationVersion(collectDataInfo.getProcessOperationVersion());

			data.setMachineName(collectDataInfo.getMachineName());
			data.setMachineRecipeName(collectDataInfo.getMachineRecipeName());

			if (collectDataInfo.getCreateTime() == null || collectDataInfo.getCreateTime().getTime() == 0)
			{
				collectDataInfo.setCreateTime(TimeUtils.getCurrentTimestamp());
			}

			data.setCreateTime(collectDataInfo.getCreateTime());
			data.setCreateUser(collectDataInfo.getCreateUser());
			data.setCreateComment(collectDataInfo.getCreateComment());
			data.setUdfs(collectDataInfo.getUdfs());

			// 2. Check if DCData exists already
			if ("N".equalsIgnoreCase(collectDataInfo.getNewDataFlag()) && getDCData(data))
			{}
			else
			{
				// 2-1 Get New DCDataId
				getNextDCDataId(data);

				// 2-2 Create DCData
				DataCollectionServiceProxy.getDCDataService().insert(data);
			}

			// 3. Select DCData
			DataCollectionServiceProxy.getDCDataService().selectByKeyForUpdate(data.getKey());

			// 4. select DCSpec
			DCSpecKey specKey = new DCSpecKey();
			specKey.setDCSpecName(data.getDCSpecName());
			specKey.setDCSpecVersion(data.getDCSpecVersion());

			DCSpec DCSpec = DataCollectionServiceProxy.getDCSpecService().selectByKey(specKey);

			// 6. Create DCDataSample
			createDCDataSample(data, collectDataInfo.getSds());

			// 7. Create DCDataItem
			createDCDataItem(data, DCSpec, dcSpecItemList);

			Map<String, DCSpecItem> dcSpecItemsMap = new HashMap<String, DCSpecItem>(dcSpecItemList.size());
			for (DCSpecItem specItem : dcSpecItemList)
			{
				dcSpecItemsMap.put(specItem.getKey().getItemName(), specItem);
			}

			// 8. Create DCDataResult
			createDCDataResult(data, DCSpec, dcSpecItemsMap, collectDataInfo.getSds());

			if (log.isInfoEnabled())
				log.info(String.format("¢º ENDOK %s.%s", this.getClass().getSimpleName(), "collectData"));

			return data.getKey().getDCDataId();
		}
		catch (Exception e)
		{
			if (log.isInfoEnabled())
				log.info(String.format("¢º ENDNG %s.%s", this.getClass().getSimpleName(), "collectData"));

			//throw ExceptionNotify.getNotifyException(e);
			return -1;
		}
	}
	
	private boolean getDCData(DCData DCData) throws FrameworkErrorSignal
	{
		Object bindSet[] = new Object[7];
		bindSet[0] = DCData.getDCSpecName();
		bindSet[1] = DCData.getDCSpecVersion();
		bindSet[2] = DCData.getMaterialType();
		bindSet[3] = DCData.getMaterialName();
		bindSet[4] = DCData.getFactoryName();
		bindSet[5] = DCData.getProcessOperationName();
		bindSet[6] = DCData.getProcessOperationVersion();

		try
		{
			List<DCData> DCDatas =
					DataCollectionServiceProxy.getDCDataService().select(SqlStatement.SELECT_DCDATA, bindSet);

			DCData.setKey(DCDatas.get(0).getKey());

			return true;
		} catch (NotFoundSignal e)
		{
			return false;
		}
	}

	private void getNextDCDataId(DCData DCData) throws FrameworkErrorSignal
	{
		List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList("SELECT DCDATAID.NEXTVAL FROM DUAL", new Object[0]);

		if (result != null & result.size() != 0)
		{
			String sSeq = CommonUtil.getValue(result.get(0), "NEXTVAL");
			
			DCData.getKey().setDCDataId(Long.valueOf(sSeq).longValue());
		}
	}
	
	private void createDCDataSample(DCData DCData, List<SampleData> sampleDataSeq)
		throws NotFoundSignal, FrameworkErrorSignal
	{
		for (SampleData sampleData : sampleDataSeq)
		{
			DCDataSample data = new DCDataSample();
			data.setSampleMaterialType(sampleData.getSampleMaterialType());
			data.setSampleNo(sampleData.getSampleNo());
			data.setCreateTime(DCData.getCreateTime());
			data.setCreateUser(DCData.getCreateUser());
			data.setCreateComment(DCData.getCreateComment());

			try
			{
				addDCDataSample(DCData.getKey(), sampleData.getSampleMaterialName(), data);
			}
			catch (DuplicateNameSignal e)
			{
				setDCDataSample(DCData.getKey(), sampleData.getSampleMaterialName(), data);
			}
		}
	}
	
	public void addDCDataSample(DCDataKey key, String sampleName, DCDataSample DCDataSample)
		throws NotFoundSignal, DuplicateNameSignal, FrameworkErrorSignal
	{
		DCDataSample.getKey().setDCDataId(key.getDCDataId());
		DCDataSample.getKey().setSampleMaterialName(sampleName);

		DataCollectionServiceProxy.getDCDataSampleService().insert(DCDataSample);
	}
	
	public void setDCDataSample(DCDataKey key, String sampleName, DCDataSample DCDataSample)
		throws NotFoundSignal, FrameworkErrorSignal
	{
		DCDataSample.getKey().setDCDataId(key.getDCDataId());
		DCDataSample.getKey().setSampleMaterialName(sampleName);

		DataCollectionServiceProxy.getDCDataSampleService().update(DCDataSample);
	}
	
	private void createDCDataItem(DCData DCData, DCSpec DCSpec, List<DCSpecItem> DCSpecItemSeq)
		throws NotFoundSignal, FrameworkErrorSignal
	{
		for (DCSpecItem specItem : DCSpecItemSeq)
		{
			DCDataItem data = new DCDataItem();
			data.setTarget(specItem.getTarget());
			data.setLowerSpecLimit(specItem.getLowerSpecLimit());
			data.setUpperSpecLimit(specItem.getUpperSpecLimit());

			try
			{
				addDCDataItem(DCData.getKey(), specItem.getKey().getItemName(), data);
			}
			catch (DuplicateNameSignal e)
			{
				setDCDataItem(DCData.getKey(), specItem.getKey().getItemName(), data);
			}
		}
	}
	
	public void addDCDataItem(DCDataKey key, String itemName, DCDataItem DCDataItem)
		throws NotFoundSignal, DuplicateNameSignal, FrameworkErrorSignal
	{
		DCDataItem.getKey().setDCDataId(key.getDCDataId());
		DCDataItem.getKey().setItemName(itemName);

		DataCollectionServiceProxy.getDCDataItemService().insert(DCDataItem);
	}
	
	public void setDCDataItem(DCDataKey key, String itemName, DCDataItem DCDataItem)
		throws NotFoundSignal, FrameworkErrorSignal
	{
		DCDataItem.getKey().setDCDataId(key.getDCDataId());
		DCDataItem.getKey().setItemName(itemName);

		DataCollectionServiceProxy.getDCDataItemService().update(DCDataItem);
	}
	
	private void createDCDataResult(DCData DCData, DCSpec DCSpec, Map<String, DCSpecItem> dcSpecItemsMap, List<SampleData> sampleDataSeq)
		throws NotFoundSignal, FrameworkErrorSignal
	{
		for (SampleData sampleData : sampleDataSeq)
		{
			for (ResultData resultData : sampleData.getRds())
			{
				DCDataResult data = new DCDataResult();
				
				DCSpecItem dcSpecItem = dcSpecItemsMap.get(resultData.getItemName());
				if (dcSpecItem != null)
				data.setDataType(dcSpecItem.getDataType());
				
				data.setResult(resultData.getResult());
				
				String siteName = StringUtils.isEmpty(resultData.getSiteName()) ? "-" : resultData.getSiteName();
				
				try
				{
					addDCDataResult(DCData.getKey(), sampleData.getSampleMaterialName(), resultData.getItemName(), siteName, "-", data);
				}
				catch (DuplicateNameSignal e)
				{
					setDCDataResult(DCData.getKey(), sampleData.getSampleMaterialName(), resultData.getItemName(), siteName, "-", data);
				}
			}
		}
	}
	
	public void addDCDataResult(DCDataKey key, String sampleMaterialName, String itemName, String siteName, String derivedItemName, DCDataResult DCDataResult)
		throws NotFoundSignal, DuplicateNameSignal, FrameworkErrorSignal
	{
		DCDataResult.getKey().setDCDataId(key.getDCDataId());
		DCDataResult.getKey().setSampleMaterialName(sampleMaterialName);
		DCDataResult.getKey().setItemName(itemName);
		DCDataResult.getKey().setSiteName(siteName);
		DCDataResult.getKey().setDerivedItemName(derivedItemName);
		
		DataCollectionServiceProxy.getDCDataResultService().insert(DCDataResult);
	}
	
	public void setDCDataResult(DCDataKey key, String sampleMaterialName, String itemName, String siteName, String derivedItemName, DCDataResult DCDataResult)
		throws NotFoundSignal, FrameworkErrorSignal
	{
		DCDataResult.getKey().setDCDataId(key.getDCDataId());
		DCDataResult.getKey().setSampleMaterialName(sampleMaterialName);
		DCDataResult.getKey().setItemName(itemName);
		DCDataResult.getKey().setSiteName(siteName);
		DCDataResult.getKey().setDerivedItemName(derivedItemName);
		
		DataCollectionServiceProxy.getDCDataResultService().update(DCDataResult);
	}
}
