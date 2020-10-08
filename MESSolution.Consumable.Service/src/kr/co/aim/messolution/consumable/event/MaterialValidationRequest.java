package kr.co.aim.messolution.consumable.event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpecKey;
import kr.co.aim.greentrack.consumable.management.info.CreateInfo;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class MaterialValidationRequest extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_MaterialValidationReply");
		
		String machineName = SMessageUtil.getBodyItemValue(doc,"MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String materialName = SMessageUtil.getBodyItemValue(doc, "MATERIALNAME", true);
		String materialPosition = SMessageUtil.getBodyItemValue(doc, "MATERIALPOSITION", false);
		String materialState = SMessageUtil.getBodyItemValue(doc, "MATERIALSTATE", false);
		String materialType = SMessageUtil.getBodyItemValue(doc, "MATERIALTYPE", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("MaterialValidationRequest", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventComment("MaterialValidation - OK");
		
		//checkSpec and get RealSpec
		String materialSpecName = "";
		
		Consumable consumableData = new Consumable();
		Map<String, String> udfs = null;
		Map<String, String> udfsSpec = null;
		int count = 0;
	/*	String[] splitMaterialName = StringUtil.split(materialName, "_");

		if ( splitMaterialName.length != 3 )
		{
			SMessageUtil.setBodyItemValue(doc, "MATERIALUSEDCOUNT", "", true);
			SMessageUtil.setBodyItemValue(doc, "MATERIALMANUFACTUREDATE", "", true);
			SMessageUtil.setBodyItemValue(doc, "MATERIALVENDOR", "", true);
			
			throw new CustomException("CONS-0003", materialName);
		}

		String prSpecName = splitMaterialName[0];
		String materialName = splitMaterialName[1];
		String expireDateById = splitMaterialName[2];
		int dExpireDateById = Integer.parseInt(expireDateById);*/
		
		
		//checkMaterialExistence
		if(MESConsumableServiceProxy.getConsumableServiceUtil().checkConsumableExistence(materialName) == true)
		{
			try
			{
    		    //getMaterialSpecName
    			materialSpecName = MESConsumableServiceProxy.getConsumableServiceUtil().getMaterialSpecByLength(materialName, "",false);
    			
    			//checkMaterialExpirationDate => Canceled by guishi 20180913
    			//this.checkMaterialExpirationDate(expireDateById);
    			
    			//Consumable
    			consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);		
    			
    			if (CommonUtil.getValue(consumableData.getUdfs(), "CONSUMABLEHOLDSTATE").equals(GenericServiceProxy.getConstantMap().FLAG_Y))
                {
                    throw new CustomException("MATERIAL-0010", materialName);
                }
                
                if (!consumableData.getConsumableState().equals(GenericServiceProxy.getConstantMap().Cons_Available))
                {
                    throw new CustomException("MATERIAL-0014", materialName, GenericServiceProxy.getConstantMap().Cons_Available);
                }
    			
    			udfs = consumableData.getUdfs();
    
    			//ConmaterialSpec
    			ConsumableSpec consumableSpecData = CommonUtil.getConsumableSpec(materialName);	
    			udfsSpec = consumableSpecData.getUdfs();
    			
    			/* hhlee, 20180926, Modify ==>> */
    			////checkRelationSpec
    			//boolean checkSqlResult = MESConsumableServiceProxy.getConsumableServiceUtil().checkRelationSpec(machineName, unitName, materialPosition, materialSpecName);
    			boolean checkSqlResult = MESConsumableServiceProxy.getConsumableServiceUtil().checkPrSpecExistenceByMU(machineName, unitName, consumableSpecData.getKey().getConsumableSpecName());
    			//if(checkSqlResult == false)
    			//{
    			//	SMessageUtil.setBodyItemValue(doc, "MATERIALUSEDCOUNT", "", true);
    			//	SMessageUtil.setBodyItemValue(doc, "MATERIALMANUFACTUREDATE", "", true);
    			//	SMessageUtil.setBodyItemValue(doc, "MATERIALVENDOR", "", true);
    			//	
    			//	throw new CustomException("CONS-0002");
    			//}
    			/* <<== hhlee, 20180926, Modify */
    			
    			SMessageUtil.setBodyItemValue(doc, "MATERIALUSEDCOUNT", String.valueOf(consumableData.getQuantity()) ,true);
    			
    			try
    			{
    				SimpleDateFormat manufacturedateformatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.s");
    				Date tomanufactureDate = manufacturedateformatter.parse(udfs.get("MANUFACTUREDATE"));
    				manufacturedateformatter = new SimpleDateFormat("yyyy-MM-dd");
    				
    				SMessageUtil.setBodyItemValue(doc, "MATERIALMANUFACTUREDATE", manufacturedateformatter.format(tomanufactureDate) ,true);			
    			}
    			catch (Exception ex)
    			{
    				eventLog.warn(ex.getMessage());
    				SMessageUtil.setBodyItemValue(doc, "MATERIALMANUFACTUREDATE", udfs.get("MANUFACTUREDATE"), true);
    			}
    			
    			/* Requires additional "MATERIALVERNOR" column to "CONSUMABLESPEC" table. */
    			//SMessageUtil.setBodyItemValue(doc, "MATERIALVENDOR", udfsSpec.get("MATERIALVENDOR"),true);
    			SMessageUtil.setBodyItemValue(doc, "MATERIALVENDOR", udfsSpec.get("GLASSVENDOR"),true);
			}
	        catch (CustomException ce)
	        {                
	            eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
	            SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
	            SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());            
	        }
	      
	        SetEventInfo setEventInfo = MESConsumableServiceProxy.getConsumableInfoUtil().setEventInfo(consumableData, consumableData.getAreaName());
	        MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(consumableData.getKey().getConsumableName(), setEventInfo, eventInfo);
		}
		else
		{
			//no Material Data than Create New 
			
			//getMaterialSpecName
			materialSpecName = MESConsumableServiceProxy.getConsumableServiceUtil().getMaterialSpecByLength(materialName, "",false);
			
			//checkMaterialExpirationDate => Canceled by guishi 20180913
			//this.checkMaterialExpirationDate(expireDateById);
			
			/* hhlee, 20180926, Modify ==>> */
			////checkRelationSpec
			//boolean checkSqlResult = MESConsumableServiceProxy.getConsumableServiceUtil().checkRelationSpec(machineName, unitName, materialPosition, materialSpecName);
			boolean checkSqlResult = MESConsumableServiceProxy.getConsumableServiceUtil().checkPrSpecExistenceByMU(machineName, unitName, materialSpecName);
			//if(checkSqlResult == false)
			//{
			//	SMessageUtil.setBodyItemValue(doc, "MATERIALUSEDCOUNT", "", true);
			//	SMessageUtil.setBodyItemValue(doc, "MATERIALMANUFACTUREDATE", "", true);
			//	SMessageUtil.setBodyItemValue(doc, "MATERIALVENDOR", "", true);
			//	
			//	throw new CustomException("CONS-0002");
			//}
			/* <<== hhlee, 20180926, Modify */
			
			eventInfo.setEventName("Create");
			
			//ConsumableSpec consumableSpecDataNew = MESConsumableServiceProxy.getConsumableServiceUtil().getConsumableSpecData(materialType);
			
			//String materialSpecName = consumableSpecDataNew.getKey().getConsumableSpecName();
			
			/*============= Create Material =============*/
			ConsumableSpecKey consumableSpecKey = new ConsumableSpecKey();
			consumableSpecKey.setConsumableSpecName(materialSpecName);
			consumableSpecKey.setConsumableSpecVersion("00001");
			consumableSpecKey.setFactoryName(GenericServiceProxy.getConstantMap().DEFAULT_FACTORY);
		
			//Created : INSTK
			Map<String, String> materialUdf = new HashMap<String, String>();
			materialUdf.put("TIMEUSED", "0");
			materialUdf.put("MACHINENAME", "");
			//materialUdf.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INSTK);
			materialUdf.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);
		    materialUdf.put("EXPIRATIONDATE", "");
			materialUdf.put("DURATIONUSED", "0");
			materialUdf.put("CONSUMABLEHOLDSTATE", "N");
			//materialUdf.put("DURATIONUSEDLIMIT", consumableSpecDataNew.getUdfs().get("DURATIONUSEDLIMIT"));

			CreateInfo createInfo = MESConsumableServiceProxy.getConsumableInfoUtil().createInfo(GenericServiceProxy.getConstantMap().DEFAULT_FACTORY, "", materialName, materialSpecName, "00001", materialType, Double.valueOf("0"), materialUdf);
			MESConsumableServiceProxy.getConsumableServiceImpl().createCrate(eventInfo, materialName, createInfo);
			
			// Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			Consumable consumableDataNew = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);
			Consumable consumableDataNew = ConsumableServiceProxy.getConsumableService().selectByKeyForUpdate(new ConsumableKey(materialName));
			
			consumableDataNew.setMaterialLocationName(materialPosition );
			consumableDataNew.setConsumableState(GenericServiceProxy.getConstantMap().Cons_InitialState);
			consumableDataNew.setCreateTime(eventInfo.getEventTime());
			
			ConsumableServiceProxy.getConsumableService().update(consumableDataNew);
			
			//Consumable
			consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);		
			udfs = consumableData.getUdfs();

			//ConmaterialSpec
			ConsumableSpec consumableSpecData = CommonUtil.getConsumableSpec(materialName);	
			udfsSpec = consumableSpecData.getUdfs();
						
            SMessageUtil.setBodyItemValue(doc, "MATERIALUSEDCOUNT", String.valueOf(consumableData.getQuantity()) ,true);
			
			try
			{
				SimpleDateFormat manufacturedateformatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.s");
				Date tomanufactureDate = manufacturedateformatter.parse(udfs.get("MANUFACTUREDATE"));
				manufacturedateformatter = new SimpleDateFormat("yyyy-MM-dd");
				
				SMessageUtil.setBodyItemValue(doc, "MATERIALMANUFACTUREDATE", manufacturedateformatter.format(tomanufactureDate) ,true);			
			}
			catch (Exception ex)
			{
				eventLog.warn(ex.getMessage());
				SMessageUtil.setBodyItemValue(doc, "MATERIALMANUFACTUREDATE", udfs.get("MANUFACTUREDATE"), true);
			}
			
			/* Requires additional "MATERIALVERNOR" column to "CONSUMABLESPEC" table. */
			//SMessageUtil.setBodyItemValue(doc, "MATERIALVENDOR", udfsSpec.get("MATERIALVENDOR"),true);
			SMessageUtil.setBodyItemValue(doc, "MATERIALVENDOR", udfsSpec.get("GLASSVENDOR"),true);
		}
		
		return doc;
	}
	
//	//add by wghuang 20180913
//	private String getMaterialSpecByLength(String materialName, String materialSpecReported, boolean checkMaterialSpec) throws CustomException
//	{
//		boolean existence = false;
//		
//		String materialSpec = "";
//		String enumName = "";	
//		String enumValue = String.valueOf(materialName.length());
//				
//		List<Map<String,Object>> enumNamelist = new ArrayList<Map<String,Object>>();
//		//put enumName New
//		enumNamelist = CommonUtil.getEnumDefValueByEnumName(GenericServiceProxy.getConstantMap().PRMaterial_Relation);
//		
//		if(enumNamelist.size() <= 0)
//			throw new CustomException("MATERIAL-0029",GenericServiceProxy.getConstantMap().PRMaterial_Relation);
//		
//		for(Map<String,Object> enumN : enumNamelist)
//		{
//            enumName = CommonUtil.getValue(enumN, "ENUMVALUE");
//			
//			if(checkEnumDefValueExistence(enumName,enumValue) == true)
//			{
//				materialSpec = enumName;
//				existence = true;
//				break;
//			}	
//		}
//		
//		if(existence == false)
//			throw new CustomException("MATERIAL-0027",enumName,enumValue);
//		else
//			this.checkConsumableSpecExistence(materialSpec);//checkConsumableSpecExistence
//		
//		
//		if(checkMaterialSpec == true)
//		{
//			if(!StringUtil.equals(materialSpec, materialSpecReported))
//				throw new CustomException("MATERIAL-0028",materialSpec,materialSpecReported);		
//		}
//			
//		return materialSpec;
//	}
//	
//	//add by wghuang 20180913
//	private boolean checkEnumDefValueExistence(String enumName, String enumvalue )
//	{
//		boolean result =  false;
//		
//		String sql = "SELECT ENUMVALUE FROM ENUMDEFVALUE WHERE ENUMNAME = :enumName AND ENUMVALUE = :enumValue ";
//
//		Map<String, String> bindMap = new HashMap<String, String>();
//		bindMap.put("enumName", enumName);
//		bindMap.put("enumValue", enumvalue);
//
//		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
//
//		if(sqlResult.size() > 0)
//		{
//			result = true;
//		}
//
//		return result;
//	}
//	
//	//add by wghuang 20180913
//	private boolean checkConsumableExistence(String consumableName) throws CustomException
//	{
//		boolean existence = false;
//		
//		String condition = "CONSUMABLENAME = ?";
//		
//		Object[] bindSet = new Object[] {consumableName};
//		try
//		{
//			List <Consumable> sqlResult = ConsumableServiceProxy.getConsumableService().select(condition, bindSet);
//			
//			if(sqlResult.size() > 0)
//			{
//				existence = true;
//				return existence;
//			}	
//		}
//		catch (NotFoundSignal ex)
//		{
//			 return existence;
//		}
//		catch (FrameworkErrorSignal fe)
//		{
//			throw new CustomException("SYS-0001", fe.getMessage());
//		}
//		return existence;
//	}
//	
//	//add by wghuang 20180913
//	private boolean checkConsumableSpecExistence(String consumableSpecName) throws CustomException
//	{
//		boolean existence = false;
//	
//		String condition = "CONSUMABLESPECNAME = ?";
//		
//		Object[] bindSet = new Object[] {consumableSpecName};
//		
//		try
//		{
//			
//			List <ConsumableSpec> sqlResult = ConsumableServiceProxy.getConsumableSpecService().select(condition, bindSet);
//			
//			if(sqlResult.size() > 0)
//			{
//				existence = true;
//				return existence;
//			}	
//		}
//		catch (NotFoundSignal ex)
//		{
//			 throw new CustomException("MATERIAL-0030",consumableSpecName);	
//		}
//		catch (FrameworkErrorSignal fe)
//		{
//			throw new CustomException("SYS-0001", fe.getMessage());
//		}
//		
//		return existence;
//	}
	
	//add by wghuang 20180913
	private boolean checkMaterialExpirationDate(String expireDate) throws CustomException
	{
		boolean outdateResult = false ;
		
		if(!expireDate.substring(0, 2).equals("20"))
			expireDate = "20" + expireDate;
		
		SimpleDateFormat df = new SimpleDateFormat(TimeStampUtil.FORMAT_SIMPLE_DAY);  
		
		 Calendar rightNow = Calendar.getInstance();  
		 rightNow.add(Calendar.DAY_OF_MONTH, 0);
		 
		 String NowDate = df.format(rightNow.getTime());
		 
		 if(Integer.parseInt(NowDate) < Integer.parseInt(expireDate))
			 outdateResult = true;
		 
		return outdateResult ;
	}
	
//	private boolean checkRelationSpec(String machineName, String unitName, String materialPosition, String MaterialSpecName) throws CustomException
//	{
//		//OK = true, NG = false
//		boolean OK_NG = false;
//		
//		List<Map<String, Object>> checkSqlResult = new ArrayList<Map<String,Object>>();
//			
//		if(this.checkRelationSpecExistenceByMU(machineName, unitName, true) == true)
//		{
//			String checkSql = " SELECT PRSPECNAME "
//							+ " FROM CT_RELATIONPRSPEC "
//							+ " WHERE MACHINENAME = :MACHINENAME "
//							+ " AND UNITNAME = :UNITNAME "
//							+ " AND POSITION = :POSITION "
//							+ " AND PRSPECNAME = :PRSPECNAME ";
//	
//			Map<String, Object> checkBindSet = new HashMap<String, Object>();
//			checkBindSet.put("MACHINENAME", machineName);
//			checkBindSet.put("UNITNAME", unitName);
//			checkBindSet.put("POSITION", materialPosition);
//			checkBindSet.put("PRSPECNAME", MaterialSpecName);
//
//			checkSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(checkSql, checkBindSet);
//			
//			if(checkSqlResult.size() > 0)
//				OK_NG = true;
//		}
//		else
//		{
//			OK_NG = true;
//		}
//					
//		return OK_NG;
//	}
//	
//	//M:MachineName, U:unitName
//	private boolean checkRelationSpecExistenceByMU(String machineName, String unitName,boolean checkPRSpec) throws CustomException
//	{
//		boolean existence = true ;
//		
//        List<Map<String, Object>> checkSqlResult = new ArrayList<Map<String,Object>>();
//		
//		String checkSql = " SELECT PRSPECNAME "
//						+ " FROM CT_RELATIONPRSPEC "
//						+ " WHERE MACHINENAME = :MACHINENAME "
//						+ " AND UNITNAME = :UNITNAME " ;
//		
//		Map<String, Object> checkBindSet = new HashMap<String, Object>();
//		checkBindSet.put("MACHINENAME", machineName);
//		checkBindSet.put("UNITNAME", unitName);
//
//
//		checkSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(checkSql, checkBindSet);
//		
//		if(checkSqlResult.size() > 0)
//		{
//			if(checkPRSpec == true)
//			{
//				for(int i = 0; i < checkSqlResult.size(); i ++)
//				{
//					if(i == checkSqlResult.size() - 1)
//						break;
//					
//					for(int j = i + 1; j < checkSqlResult.size(); j ++)
//					{
//						if(!CommonUtil.getValue(checkSqlResult.get(i), "PRSPECNAME").equals
//						  (CommonUtil.getValue(checkSqlResult.get(j), "PRSPECNAME")))
//						{
//							//need to add another ErrorCode
//							throw new CustomException("MATERIAL-0031", unitName);
//						}
//					}
//				}
//			}
//		}
//		else
//		{
//			existence = false;
//		}
//		
//		return existence;
//	}
}