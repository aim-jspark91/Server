package kr.co.aim.messolution.consumable.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpecKey;
import kr.co.aim.greentrack.consumable.management.info.CreateInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.jdom.Document;
import org.jdom.Element;

public class MaterialStateReport extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("MaterialStateReport", getEventUser(), getEventComment(), null, null);
		
        SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_MaterialStateCheckReply");
		
		/* 20180326 by hhlee : Send NG/OK results only in " Online Initial ". */
		SMessageUtil.setBodyItemValue(doc, "RESULT", "OK",true);
		SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "",true);
		
		Machine machineData = null;
		Machine unitData = null;
		String currentCommunicationName = StringUtil.EMPTY;
		String unitName = StringUtil.EMPTY;
		
		String machineName = SMessageUtil.getBodyItemValue(doc,"MACHINENAME", true);
		
		/* Machine Validation */
		machineData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		currentCommunicationName = machineData.getCommunicationState();
	
		List<Element> unitlist = SMessageUtil.getBodySequenceItemList(doc, "UNITLIST", true);		
	
		if (unitlist != null)
		{
			for(Element uintE : unitlist)
			{
				/* Unitname Validation */
				unitName = SMessageUtil.getChildText(uintE, "UNITNAME", false);					
				unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
				
				List<Element> materialList = SMessageUtil.getSubSequenceItemList(uintE, "MATERIALLIST", false);
				
				/* MaterialList = 0, ALL material is unmount  */
				MESConsumableServiceProxy.getConsumableServiceUtil().checkExistenceByMachineName(eventInfo, materialList, machineName, unitName, StringUtil.EMPTY, StringUtil.EMPTY);
				
				if (materialList != null && materialList.size() > 0)
				{
				    for(Element materialE : materialList)
					{					
						String materialName = SMessageUtil.getChildText(materialE, "MATERIALNAME", true);
						String materialPosition = SMessageUtil.getChildText(materialE, "MATERIALPOSITION", false);
						String materialState = SMessageUtil.getChildText(materialE, "MATERIALSTATE", false);
						String materialType = SMessageUtil.getChildText(materialE, "MATERIALTYPE", false);
						String materialUsedcount = SMessageUtil.getChildText(materialE, "MATERIALUSEDCOUNT", false);
						
						/* 20181119, hhlee, add materialUsedcount empty ==>> */
						materialUsedcount = StringUtil.isEmpty(materialUsedcount) ? "0" : materialUsedcount;
						/* <<== 20181119, hhlee, add materialUsedcount empty */
						
						
						if(StringUtil.upperCase(materialState).equals(StringUtil.upperCase(GenericServiceProxy.getConstantMap().Cons_Mount)))
				        {
				            materialState = GenericServiceProxy.getConstantMap().Cons_Mount;
				        }
				        else if(StringUtil.upperCase(materialState).equals(StringUtil.upperCase(GenericServiceProxy.getConstantMap().Cons_InUse)))
				        {
				            materialState = GenericServiceProxy.getConstantMap().Cons_InUse;
				        }
				        else if(StringUtil.upperCase(materialState).equals(StringUtil.upperCase(GenericServiceProxy.getConstantMap().Cons_Unmount)))
				        {
				            materialState = GenericServiceProxy.getConstantMap().Cons_Unmount;
				        }
						
						/*  Not used at Array, Only use at OLDE/Module */
						//String materialLotName = SMessageUtil.getBodyItemValue(doc, "MATERIALLOTNAME", false);
						//String materialRemaincount = SMessageUtil.getBodyItemValue(doc, "MATERIALREMAINCOUNT", false);
				        
						//checkMaterialExistence
				        if(MESConsumableServiceProxy.getConsumableServiceUtil().checkConsumableExistence(materialName))
				        {						
    						/* Consumable Validation */
				        	// Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//    						Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);
				        	Consumable consumableData = ConsumableServiceProxy.getConsumableService().selectByKeyForUpdate(new ConsumableKey(materialName));

    						Map<String, String> udfs = consumableData.getUdfs();
    						
    						//ConmaterialSpec
    						ConsumableSpec consumableSpecData = CommonUtil.getConsumableSpec(materialName);
    				
    						/* hhlee, 20180926, Modify ==>> */
    						//checkRelationSpec
    						//boolean checkSqlResult = MESConsumableServiceProxy.getConsumableServiceUtil().checkRelationSpec(machineName, unitName, materialPosition, consumableSpecData.getKey().getConsumableSpecName());
    						boolean checkSqlResult = MESConsumableServiceProxy.getConsumableServiceUtil().checkPrSpecExistenceByMU(machineName, unitName, consumableSpecData.getKey().getConsumableSpecName());
    						    						
    		                //if(checkSqlResult == false)
    		                //{
    		                //    //SMessageUtil.setBodyItemValue(doc, "MATERIALUSEDCOUNT", "", true);
    		                //    //SMessageUtil.setBodyItemValue(doc, "MATERIALMANUFACTUREDATE", "", true);
    		                //    //SMessageUtil.setBodyItemValue(doc, "MATERIALVENDOR", "", true);
    		                //    
    		                //    throw new CustomException("CONS-0002");
    		                //}
    		                /* <<== hhlee, 20180926, Modify */
    		                
    						/* Material Mount */
    						if(materialState.equals(GenericServiceProxy.getConstantMap().Cons_Mount))
    						{
    							eventInfo.setEventName(GenericServiceProxy.getConstantMap().Cons_Mount);
    							
    							if (CommonUtil.getValue(consumableData.getUdfs(), "CONSUMABLEHOLDSTATE").equals(GenericServiceProxy.getConstantMap().FLAG_Y))
    				            {
    				                throw new CustomException("MATERIAL-0010", materialName);
    				            }
    				            
    							MESConsumableServiceProxy.getConsumableServiceUtil().checkExistenceByMachineNamePosition(eventInfo, consumableData.getKey().getConsumableName(), machineName, unitName, 
                                        materialPosition, materialState);
                                
    							
    							MESConsumableServiceProxy.getConsumableServiceUtil().checkExistenceByMaterialName(eventInfo, consumableData.getKey().getConsumableName(),
    							        unitName, materialPosition, materialState);
    							
    							//if (!consumableData.getConsumableState().equals(GenericServiceProxy.getConstantMap().Cons_Available))
    				            //{
    				            //    throw new CustomException("MATERIAL-0014", materialName, GenericServiceProxy.getConstantMap().Cons_Available);
    				            //}	
    							
    							//MESConsumableServiceProxy.getConsumableServiceUtil().checkExistenceByMachineNamePosition(eventInfo, machineName, unitName, 
    							//        materialPosition, materialState);
    							
    							if((StringUtil.equals(consumableData.getConsumableState(),GenericServiceProxy.getConstantMap().Cons_Mount) || 
    							        StringUtil.equals(consumableData.getConsumableState(),GenericServiceProxy.getConstantMap().Cons_InUse)) &&  
    							        StringUtil.equals(consumableData.getMaterialLocationName(), materialPosition))
    							{    							    
    							}
    							else
    							{
    							    eventInfo.setEventName(GenericServiceProxy.getConstantMap().Cons_Mount);
        							consumableData.setMaterialLocationName(materialPosition);
        							udfs.put("UNITNAME", unitName);
        							udfs.put("MATERIALPOSITION", materialPosition);
        							// 2019.04.12_hsryu_Insert Logic.
        							udfs.put("LASTMOUNTTIME", eventInfo.getEventTimeKey());
        							//kit event
        							//kitConsumableData(eventInfo, consumableData, machineName);
        							MESConsumableServiceProxy.getConsumableServiceUtil().kitConsumableData(eventInfo, consumableData, machineName, materialState, materialPosition);
    							}
    						}
    						/* Material InUse */
    						else if(materialState.equals(GenericServiceProxy.getConstantMap().Cons_InUse))
    						{
    							eventInfo.setEventName(GenericServiceProxy.getConstantMap().Cons_InUse);
    										
    							////Check Validation
    							//if(!udfs.get("TRANSPORTSTATE").equals(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP))
    							//{
    							//	throw new CustomException("MATERIAL-0005", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP);
    							//}
    							
    							if (CommonUtil.getValue(consumableData.getUdfs(), "CONSUMABLEHOLDSTATE").equals(GenericServiceProxy.getConstantMap().FLAG_Y))
                                {
                                    throw new CustomException("MATERIAL-0010", materialName);
                                }
    							
    							MESConsumableServiceProxy.getConsumableServiceUtil().checkExistenceByMachineNamePosition(eventInfo, consumableData.getKey().getConsumableName(), machineName, unitName, 
                                        materialPosition, materialState);
                                
                                
                                MESConsumableServiceProxy.getConsumableServiceUtil().checkExistenceByMaterialName(eventInfo, consumableData.getKey().getConsumableName(),
                                        unitName, materialPosition, materialState);
                                
    							
    							//MESConsumableServiceProxy.getConsumableServiceUtil().checkExistenceByMachineNamePosition(eventInfo, machineName, unitName, 
                                //        materialPosition, materialState);
                                if( StringUtil.equals(consumableData.getConsumableState(),GenericServiceProxy.getConstantMap().Cons_InUse) &&  
                                        StringUtil.equals(consumableData.getMaterialLocationName(), materialPosition))
                                {                                   
                                }
                                else
                                {
                                    if(!StringUtil.equals(consumableData.getConsumableState(),GenericServiceProxy.getConstantMap().Cons_Mount))
                                    {
                                        eventInfo.setEventName(GenericServiceProxy.getConstantMap().Cons_Mount);
                                        
                                        consumableData.setMaterialLocationName(materialPosition);
                                        udfs.put("UNITNAME", unitName);
                                        udfs.put("MATERIALPOSITION", materialPosition);
            							// 2019.04.12_hsryu_Insert Logic.
            							udfs.put("LASTMOUNTTIME", eventInfo.getEventTimeKey());

                                        //kit event
                                        //kitConsumableData(eventInfo, consumableData, machineName);
                                        MESConsumableServiceProxy.getConsumableServiceUtil().kitConsumableData(eventInfo, consumableData, machineName, 
                                                GenericServiceProxy.getConstantMap().Cons_Mount, materialPosition);
                                    }
                                    
                                    eventInfo.setEventName(GenericServiceProxy.getConstantMap().Cons_InUse);
                                    eventInfo.setCheckTimekeyValidation(false);
                                    eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                                    eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
                                    
                                    consumableData.setMaterialLocationName(materialPosition);
                                    udfs.put("UNITNAME", unitName);
                                    udfs.put("MATERIALPOSITION", materialPosition);
                                    //kit event
                                    //kitConsumableData(eventInfo, consumableData, machineName);    
                                    MESConsumableServiceProxy.getConsumableServiceUtil().kitConsumableData(eventInfo, consumableData, machineName, materialState, materialPosition);
                                }
    						}
    						/* Material Unmount */
    						else if(materialState.equals(GenericServiceProxy.getConstantMap().Cons_Unmount))
    						{
    							eventInfo.setEventName(GenericServiceProxy.getConstantMap().Cons_Unmount);
    													
//    							//Check Validation
//    							if(!consumableData.getConsumableState().equals(GenericServiceProxy.getConstantMap().Cons_InUse) || !udfs.get("TRANSPORTSTATE").equals(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP))
//    							{
//    							    /* 20181119, hhlee, add throw empty ==>> */
//    								throw new CustomException("MATERIAL-2002", GenericServiceProxy.getConstantMap().Cons_InUse, GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP);
//    							}
    							
    							
//    							//Check Validation 20191010, GJJ, Mantis 4974 start  ==>> 
    							if(!udfs.get("TRANSPORTSTATE").equals(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP))
    							{    							   
    								throw new CustomException("MATERIAL-0005", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP);
    							}
    							// 20191010, GJJ, Mantis 4974 END  ==>> 
    							
    							//Unkit Material
    							consumableData.setMaterialLocationName(materialPosition);
    							//this.unkitConsumableData(eventInfo, consumableData, materialUsedcount);
    							MESConsumableServiceProxy.getConsumableServiceUtil().unkitConsumableData(eventInfo, consumableData, materialUsedcount, materialState);
    						}
    						/* Material State is not exist */
    						else
    						{
    						    /* 20181119, hhlee, add throw empty ==>> */
    							//eventLog.warn("Material State is not exist");
    							throw new CustomException("MATERIAL-0021", "exist");
    						}	
				        }
				        else
				        {
				          //no Material Data than Create New 
				            
				            //getMaterialSpecName
				            String materialSpecName = MESConsumableServiceProxy.getConsumableServiceUtil().getMaterialSpecByLength(materialName, "",false);
				            
				            //checkMaterialExpirationDate => Canceled by guishi 20180913
				            //this.checkMaterialExpirationDate(expireDateById);
				                   
				            /* hhlee, 20180926, Modify ==>> */
				            ////checkRelationSpec
				            //boolean checkSqlResult = MESConsumableServiceProxy.getConsumableServiceUtil().checkRelationSpec(machineName, unitName, materialPosition, materialSpecName);
				            boolean checkSqlResult = MESConsumableServiceProxy.getConsumableServiceUtil().checkPrSpecExistenceByMU(machineName, unitName, materialSpecName);
				            //if(checkSqlResult == false)
				            //{
				            //    //SMessageUtil.setBodyItemValue(doc, "MATERIALUSEDCOUNT", "", true);
				            //    //SMessageUtil.setBodyItemValue(doc, "MATERIALMANUFACTUREDATE", "", true);
				            //    //SMessageUtil.setBodyItemValue(doc, "MATERIALVENDOR", "", true);
				            //    
				            //    throw new CustomException("CONS-0002");
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
//				            Consumable consumableDataNew = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);
				            Consumable consumableDataNew = ConsumableServiceProxy.getConsumableService().selectByKeyForUpdate(new ConsumableKey(materialName));
				            
				            consumableDataNew.setMaterialLocationName(materialPosition );           
				            consumableDataNew.setConsumableState(GenericServiceProxy.getConstantMap().Cons_InitialState);
				            consumableDataNew.setCreateTime(eventInfo.getEventTime());
				            
				            ConsumableServiceProxy.getConsumableService().update(consumableDataNew);
				            
				            //Consumable
				            // Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				            Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);
				            Consumable consumableData = ConsumableServiceProxy.getConsumableService().selectByKeyForUpdate(new ConsumableKey(materialName));
				            
				            Map<String, String> udfs = consumableData.getUdfs();

				            //ConmaterialSpec
				            ConsumableSpec consumableSpecData = CommonUtil.getConsumableSpec(materialName); 
				            Map<String, String> udfsSpec = consumableSpecData.getUdfs();
				                        
				            //SMessageUtil.setBodyItemValue(doc, "MATERIALUSEDCOUNT", String.valueOf(consumableData.getQuantity()) ,true);
				            
				            try
				            {
				                //SimpleDateFormat manufacturedateformatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.s");
				                //Date tomanufactureDate = manufacturedateformatter.parse(udfs.get("MANUFACTUREDATE"));
				                //manufacturedateformatter = new SimpleDateFormat("yyyy-MM-dd");
				                
				                //SMessageUtil.setBodyItemValue(doc, "MATERIALMANUFACTUREDATE", manufacturedateformatter.format(tomanufactureDate) ,true);            
				            }
				            catch (Exception ex)
				            {
				                eventLog.warn(ex.getMessage());
				                //SMessageUtil.setBodyItemValue(doc, "MATERIALMANUFACTUREDATE", udfs.get("MANUFACTUREDATE"), true);
				            }
				            
				            /* Requires additional "MATERIALVERNOR" column to "CONSUMABLESPEC" table. */
				            //SMessageUtil.setBodyItemValue(doc, "MATERIALVENDOR", udfsSpec.get("MATERIALVENDOR"),true);
				            //SMessageUtil.setBodyItemValue(doc, "MATERIALVENDOR", udfsSpec.get("GLASSVENDOR"),true);
				            
				            /* Material Mount */
                            if(materialState.equals(GenericServiceProxy.getConstantMap().Cons_Mount))
                            {
                                eventInfo.setEventName(GenericServiceProxy.getConstantMap().Cons_Mount);
                                
                                if (CommonUtil.getValue(consumableData.getUdfs(), "CONSUMABLEHOLDSTATE").equals(GenericServiceProxy.getConstantMap().FLAG_Y))
                                {
                                    throw new CustomException("MATERIAL-0010", materialName);
                                }
                                
                                MESConsumableServiceProxy.getConsumableServiceUtil().checkExistenceByMachineNamePosition(eventInfo, consumableData.getKey().getConsumableName(), machineName, unitName, 
                                        materialPosition, materialState);
                                
                                
                                MESConsumableServiceProxy.getConsumableServiceUtil().checkExistenceByMaterialName(eventInfo, consumableData.getKey().getConsumableName(),
                                        unitName, materialPosition, materialState);
                                
                                //if (!consumableData.getConsumableState().equals(GenericServiceProxy.getConstantMap().Cons_Available))
                                //{
                                //    throw new CustomException("MATERIAL-0014", materialName, GenericServiceProxy.getConstantMap().Cons_Available);
                                //} 
                                
                                //MESConsumableServiceProxy.getConsumableServiceUtil().checkExistenceByMachineNamePosition(eventInfo, machineName, unitName, 
                                //        materialPosition, materialState);
                                
                                if((StringUtil.equals(consumableData.getConsumableState(),GenericServiceProxy.getConstantMap().Cons_Mount) || 
                                        StringUtil.equals(consumableData.getConsumableState(),GenericServiceProxy.getConstantMap().Cons_InUse)) &&  
                                        StringUtil.equals(consumableData.getMaterialLocationName(), materialPosition))
                                {                                   
                                }
                                else
                                {
                                    eventInfo.setEventName(GenericServiceProxy.getConstantMap().Cons_Mount);
                                    consumableData.setMaterialLocationName(materialPosition);
                                    udfs.put("UNITNAME", unitName);
                                    udfs.put("MATERIALPOSITION", materialPosition);
        							// 2019.04.12_hsryu_Insert Logic.
        							udfs.put("LASTMOUNTTIME", eventInfo.getEventTimeKey());

                                    //kit event
                                    //kitConsumableData(eventInfo, consumableData, machineName);
                                    MESConsumableServiceProxy.getConsumableServiceUtil().kitConsumableData(eventInfo, consumableData, machineName, materialState, materialPosition);
                                }
                            }
                            /* Material InUse */
                            else if(materialState.equals(GenericServiceProxy.getConstantMap().Cons_InUse))
                            {
                                eventInfo.setEventName(GenericServiceProxy.getConstantMap().Cons_InUse);
                                            
                                ////Check Validation
                                //if(!udfs.get("TRANSPORTSTATE").equals(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP))
                                //{
                                //  throw new CustomException("MATERIAL-0005", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP);
                                //}
                                
                                if (CommonUtil.getValue(consumableData.getUdfs(), "CONSUMABLEHOLDSTATE").equals(GenericServiceProxy.getConstantMap().FLAG_Y))
                                {
                                    throw new CustomException("MATERIAL-0010", materialName);
                                }
                                
                                MESConsumableServiceProxy.getConsumableServiceUtil().checkExistenceByMachineNamePosition(eventInfo, consumableData.getKey().getConsumableName(), machineName, unitName, 
                                        materialPosition, materialState);
                                
                                
                                MESConsumableServiceProxy.getConsumableServiceUtil().checkExistenceByMaterialName(eventInfo, consumableData.getKey().getConsumableName(),
                                        unitName, materialPosition, materialState);
                                
                                
                                //MESConsumableServiceProxy.getConsumableServiceUtil().checkExistenceByMachineNamePosition(eventInfo, machineName, unitName, 
                                //        materialPosition, materialState);
                                if((StringUtil.equals(consumableData.getConsumableState(),GenericServiceProxy.getConstantMap().Cons_Mount) || 
                                        StringUtil.equals(consumableData.getConsumableState(),GenericServiceProxy.getConstantMap().Cons_InUse)) &&  
                                        StringUtil.equals(consumableData.getMaterialLocationName(), materialPosition))
                                {                                   
                                }
                                else
                                {
                                    if(!StringUtil.equals(consumableData.getConsumableState(),GenericServiceProxy.getConstantMap().Cons_Mount))
                                    {
                                        eventInfo.setEventName(GenericServiceProxy.getConstantMap().Cons_Mount);
                                        
                                        consumableData.setMaterialLocationName(materialPosition);
                                        udfs.put("UNITNAME", unitName);
                                        udfs.put("MATERIALPOSITION", materialPosition);
            							// 2019.04.12_hsryu_Insert Logic.
            							udfs.put("LASTMOUNTTIME", eventInfo.getEventTimeKey());

                                        //kit event
                                        //kitConsumableData(eventInfo, consumableData, machineName);
                                        MESConsumableServiceProxy.getConsumableServiceUtil().kitConsumableData(eventInfo, consumableData, machineName, 
                                                GenericServiceProxy.getConstantMap().Cons_Mount, materialPosition);
                                    }
                                    
                                    eventInfo.setEventName(GenericServiceProxy.getConstantMap().Cons_InUse);
                                    eventInfo.setCheckTimekeyValidation(false);
                                    eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                                    eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
                                    
                                    consumableData.setMaterialLocationName(materialPosition);
                                    udfs.put("UNITNAME", unitName);
                                    udfs.put("MATERIALPOSITION", materialPosition);
                                    //kit event
                                    //kitConsumableData(eventInfo, consumableData, machineName);    
                                    MESConsumableServiceProxy.getConsumableServiceUtil().kitConsumableData(eventInfo, consumableData, machineName, materialState, materialPosition);
                                }
                            }
                            /* Material Unmount */
                            else if(materialState.equals(GenericServiceProxy.getConstantMap().Cons_Unmount))
                            {
                                eventInfo.setEventName(GenericServiceProxy.getConstantMap().Cons_Unmount);
                                                        
                                //Check Validation
//                                if(!consumableData.getConsumableState().equals(GenericServiceProxy.getConstantMap().Cons_InUse) || !udfs.get("TRANSPORTSTATE").equals(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP))
//                                {
//                                    /* 20181119, hhlee, add materialUsedcount empty ==>> */
//                                    throw new CustomException("MATERIAL-2002", GenericServiceProxy.getConstantMap().Cons_InUse, GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP);
//                                }
                                
                                //Check Validation 20191010, GJJ, Mantis  ==>> 
    							if(!udfs.get("TRANSPORTSTATE").equals(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP))
    							{    							   
    								throw new CustomException("MATERIAL-0005", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP);
    							}
                                
                                
                                
                                //Unkit Material
                                consumableData.setMaterialLocationName(materialPosition);
                                //this.unkitConsumableData(eventInfo, consumableData, materialUsedcount);
                                MESConsumableServiceProxy.getConsumableServiceUtil().unkitConsumableData(eventInfo, consumableData, materialUsedcount, materialState);
                            }
                            /* Material State is not exist */
                            else
                            {
                                /* 20181119, hhlee, add materialUsedcount empty ==>> */
                                //eventLog.warn("Material State is not exist");
                                throw new CustomException("MATERIAL-0021", "exist");
                            }        
				        }				        
					}
				}
				else
				{
				    ///* MaterialList = 0, ALL material is unmount  */
				    //MESConsumableServiceProxy.getConsumableServiceUtil().checkExistenceByMachineName(eventInfo, machineName, unitName, StringUtil.EMPTY, StringUtil.EMPTY);
				}
			}
		}
			
		return doc;
	}
		
//	/**
//	* Name : kitConsumableData
//	* Desc : Execute Kit event
//	* Author : aim system
//	* Date : 2016.07.29
//	*/
//	private void kitConsumableData(EventInfo eventInfo, Consumable materialData, String MachineName) 
//			throws CustomException
//	{
//		Map<String, String> udfs = materialData.getUdfs();
//		udfs.put("MACHINENAME", MachineName);
//		udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP);
//		
//		materialData.setUdfs(udfs);
//		
//		materialData.setConsumableState(GenericServiceProxy.getConstantMap().Cons_InUse);
//		materialData.setMaterialLocationName(MachineName);
//		
//		ConsumableServiceProxy.getConsumableService().update(materialData);
//		
//		kr.co.aim.greentrack.consumable.management.info.SetEventInfo setEventInfo = 
//				MESConsumableServiceProxy.getConsumableInfoUtil().setEventInfo(materialData, materialData.getAreaName());
//		MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(materialData.getKey().getConsumableName(), setEventInfo, eventInfo);
//	}
//	
//	private void unkitConsumableData(EventInfo eventInfo, Consumable materialData, String usedQty) throws CustomException
//	{
//		Map<String, String> udfs = materialData.getUdfs();
//		udfs.put("MACHINENAME", "");
//		udfs.put("UNITNAME", "");
//		udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);
//		
//		materialData.setConsumableState(GenericServiceProxy.getConstantMap().Cons_Available);
//		materialData.setMaterialLocationName("");
//		materialData.setUdfs(udfs);
//		
//		kr.co.aim.greentrack.consumable.management.info.SetEventInfo setEventInfo = MESConsumableServiceProxy.getConsumableInfoUtil().setEventInfo(materialData, materialData.getAreaName());
//		ConsumableServiceProxy.getConsumableService().update(materialData);
//		
//		if(Double.valueOf(usedQty) != 0)
//		{
//			//decrement
//			TransitionInfo transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().decrementQuantityInfo("", "", "", "",
//														eventInfo.getEventTimeKey(), Double.parseDouble(usedQty), udfs);
//			
//			MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(materialData,
//										(DecrementQuantityInfo) transitionInfo, eventInfo);
//			/*double quantity=materialData.getQuantity();
//			quantity=quantity-Double.valueOf(usedQty);
//			materialData.setQuantity(quantity);
//			ConsumableServiceProxy.getConsumableService().update(materialData);*/
//			//makeNotAvailable
//			materialData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialData.getKey().getConsumableName());
//			if(materialData.getQuantity() == 0 && StringUtil.equals(materialData.getConsumableState(), "Available"))
//			{
//				//eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), null, null);
//				eventInfo.setEventName("ChangeState");
//				MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();
//				makeNotAvailableInfo.setUdfs(materialData.getUdfs());
//				MESConsumableServiceProxy.getConsumableServiceImpl().makeNotAvailable(materialData, makeNotAvailableInfo, eventInfo);
//			}
//		}
//		else
//		{
//		   MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(materialData.getKey().getConsumableName(), setEventInfo, eventInfo);
//		}
//	}
//	
}
