package kr.co.aim.messolution.datacollection.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.datacollection.management.info.CollectDataInfo;
import kr.co.aim.greentrack.datacollection.management.info.ext.ResultData;
import kr.co.aim.greentrack.datacollection.management.info.ext.SampleData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;

public class DataCollectionInfoUtil {

	private static Log log = LogFactory.getLog(DataCollectionInfoUtil.class);
	
	/*
	* Name : collectDataInfo.
	* Desc : This function is CollectDataInfo
	* Author : AIM Systems, Inc
	* Date : 2011.08.19
	*/
	public  CollectDataInfo collectDataInfo(String newDataFlag, String DCSpecName, String DCSpecVersion, String materialType, 
												  String materialName, String factoryName, String productSpecName, String productSpecVersion,
												  String processFlowName, String processFlowVersion, String processOperationName, 
												  String processOperationVersion, String machineName, String machineRecipeName, 
												  Timestamp createTime, String createUser, String createComment, List<SampleData> sds,
												  Map<String, String> udfs)
	{
		CollectDataInfo collectDataInfo = new CollectDataInfo();
		collectDataInfo.setNewDataFlag(newDataFlag);
		collectDataInfo.setDCSpecName(DCSpecName);
		collectDataInfo.setDCSpecVersion(DCSpecVersion);
		collectDataInfo.setMaterialType(materialType);
		collectDataInfo.setMaterialName(materialName);
		collectDataInfo.setFactoryName(factoryName);
		collectDataInfo.setProductSpecName(productSpecName);
		collectDataInfo.setProductSpecVersion(productSpecVersion);
		collectDataInfo.setProcessFlowName(processFlowName);
		collectDataInfo.setProcessFlowVersion(processFlowVersion);
		collectDataInfo.setProcessOperationName(processOperationName);
		collectDataInfo.setProcessOperationVersion(processOperationVersion);
		collectDataInfo.setMachineName(machineName);
		collectDataInfo.setMachineRecipeName(machineRecipeName);
		collectDataInfo.setCreateTime(createTime);
		collectDataInfo.setCreateUser(createUser);
		collectDataInfo.setCreateComment(createComment);
		collectDataInfo.setSds(sds);
		collectDataInfo.setUdfs(udfs);
		
		return collectDataInfo;
	}
	
	/**
	 * 
	 * @since 2014.01.01
	 * @author aim System
	 * @param sampleMaterialName
	 * @param sampleMaterialType
	 * @param sampleNo
	 * @param resultDataList
	 * @return
	 */
	public SampleData getSampleData(String sampleMaterialName, String sampleMaterialType, long sampleNo, List<ResultData> resultDataList)
	{
		SampleData sampleData = new SampleData();
		sampleData.setSampleMaterialName(sampleMaterialName);
		sampleData.setSampleMaterialType(sampleMaterialType);
		sampleData.setSampleNo(sampleNo);
		sampleData.setRds(resultDataList);
		
		return sampleData;
	}
	
	/**
	 * sample data generation for DCOL
	 * @author swcho
	 * @since 2016.06.22
	 * @param materialName
	 * @param materialType
	 * @param itemList
	 * @return
	 * @throws CustomException
	 */
	public SampleData getSampleData(String materialName, String materialType, List<Element> itemList) throws CustomException
	{
		int idx = 1;
		
		SampleData sampleData =	new SampleData();			
		sampleData.setSampleMaterialName(materialName);
		sampleData.setSampleMaterialType(materialType);
		sampleData.setSampleNo(idx);
		
		List<ResultData> resultDataList = new ArrayList<ResultData>();
		
		for (Element item : itemList)
		{
			//160826 by swcho : item is mandatory to collect
			String itemName = SMessageUtil.getChildText(item, "ITEMNAME", true);
			
			try
			{
				//String itemName = SMessageUtil.getChildText(item, "ITEMNAME", true);
				
				List<Element> siteList = SMessageUtil.getSubSequenceItemList(item, "SITELIST", true);
				
				for (Element site : siteList)
				{
					try
					{
						String siteName = SMessageUtil.getChildText(site, "SITENAME", false);
						
						boolean isSkip = false;
						
						if (siteName.isEmpty())
						{//if first site is null, only store first stuff
							siteName = "NA";
							isSkip = true;
						}
						
						String siteValue = SMessageUtil.getChildText(site, "SITEVALUE", true);
						
						ResultData resultData =  new ResultData();					
						resultData.setItemName(itemName);
						resultData.setSiteName(siteName);
						resultData.setResult(siteValue);
						
						resultDataList.add(resultData);
						
						if (isSkip) break;
					}
					catch (Exception ex)
					{
						log.error(ex.getMessage());
					}
				}
			}
			catch (Exception ex)
			{
				log.error(ex.getMessage());
			}
		}
		
		sampleData.setRds(resultDataList);
		
		return sampleData;
	}
}
