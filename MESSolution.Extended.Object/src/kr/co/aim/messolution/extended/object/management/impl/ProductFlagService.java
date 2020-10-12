package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ProductFlag;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
//import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;

public class ProductFlagService extends CTORMService<ProductFlag> {

	public static Log logger = LogFactory.getLog(ProductFlagService.class);

	private final String historyEntity = "ProductFlagHistory";

	public List<ProductFlag> select(String condition, Object[] bindSet)
			throws CustomException {

		List<ProductFlag> result = super.select(condition, bindSet,
				ProductFlag.class);

		return result;
	}

	public ProductFlag selectByKey(boolean isLock, Object[] keySet)
			throws CustomException, greenFrameDBErrorSignal {
		try
		{
			return super.selectByKey(ProductFlag.class, isLock, keySet);
		}
		catch(greenFrameDBErrorSignal ns)
		{
			throw ns;
		}

	}

	public ProductFlag create(EventInfo eventInfo, ProductFlag dataInfo)
			throws CustomException {

		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());

	}

	public void remove(EventInfo eventInfo, ProductFlag dataInfo)
			throws CustomException {

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);

	}

	public ProductFlag modify(EventInfo eventInfo, ProductFlag dataInfo)
			throws CustomException {
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());

	}

	/**
     * @Name     setLotInfoDownLoadSendProductFlag
     * @since    2018. 6. 6.
     * @author   kyjung
     * @contents Lot Process End/Abort ProductFlag Setting
     *
     * @param productElement
     * @param factoryName
     * @param productName
     * @return
     * @throws CustomException
     */
    public Element setLotInfoDownLoadSendProductFlag(Element productElement, String factoryName, String productName)  throws CustomException
    {
    	String strSql = "SELECT ENUMVALUE FLAG " +
    			"  FROM ENUMDEFVALUE  " +
    			" WHERE ENUMNAME = :ENUMNAME ";
    	
        Map<String, Object> bindMap = new HashMap<String, Object>();
        bindMap.put("ENUMNAME", GenericServiceProxy.getConstantMap().PRODUCTFLAGDOWNLOAD);

        List<Map<String, Object>> downLoadFlagList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);

        if ( downLoadFlagList != null && downLoadFlagList.size() > 0 )
        {
        	String strSqlFlag = "SELECT * " +
        			"  FROM CT_PRODUCTFLAG " +
        			" WHERE PRODUCTNAME = :PRODUCTNAME ";
        	
        	Map<String, Object> bindMapFlag = new HashMap<String, Object>();
        	bindMapFlag.put("PRODUCTNAME", productName);
        	
        	List<Map<String, Object>> productFlag = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSqlFlag, bindMapFlag);
        	
        	if(productFlag != null)
        	{
        		for(int i = 0; i < downLoadFlagList.size(); i++ )
                {
                    Element productflagE = new Element(downLoadFlagList.get(i).get("FLAG").toString());
                    if(StringUtil.isEmpty((String)productFlag.get(0).get(downLoadFlagList.get(i).get("FLAG"))))
                    {
                    	productflagE.setText("");
                    }
                    else
                    {
                    	productflagE.setText((String)productFlag.get(0).get(downLoadFlagList.get(i).get("FLAG")));
                    }
                    
                    productElement.addContent(productflagE);
                }
        	}
        }

        return productElement;
    }
    
    /**
     * @Name     checkProductFlagElaQtime
     * @since    2018. 6. 20.
     * @author   hhlee
     * @contents ELA Q-time Flag Check
     * @param productList
     * @return
     * @throws CustomException
     */
    public boolean checkProductFlagElaQtime(List<Element> productList) throws CustomException
    {
        logger.info("checkProductFlagElaQtime Started.");

        boolean checkElaQtime = false;
        List<ProductFlag> productFlagDataList = new ArrayList<ProductFlag>();
        try
        {
            if (productList != null && productList.size() > 0) 
            {
                Object[] bindSet = new Object[productList.size() + 1];
                            
                for (int index = 0; index < productList.size(); index++) {
                    bindSet[index] =  SMessageUtil.getChildText(productList.get(index), "PRODUCTNAME", false); //productList.get(index).get;
                }
                String condition = "PRODUCTNAME IN (" + StringUtils.removeEnd(StringUtils.repeat("?,", productList.size()), ",") + ")"
                                 + " AND ELAQTIMEFLAG = ? ";
                bindSet[productList.size()] = GenericServiceProxy.getConstantMap().FLAG_Y;
                
                productFlagDataList = ExtendedObjectProxy.getProductFlagService().select(condition, bindSet);
            }
            else
            {
                productFlagDataList = ExtendedObjectProxy.getProductFlagService().select("", new Object[] {});
            }
            
            if(productFlagDataList != null && productFlagDataList.size() > 0)
            {
                checkElaQtime = true;
            }
        } 
        catch (Exception e) 
        {
            logger.info(e);
        }
        
        logger.info("checkProductFlagElaQtime Ended.");
        
        return checkElaQtime;
    }
    
    /**
     * @author smkang
     * @since 2019.01.09
     * @param productDataList
     * @return boolean
     * @throws CustomException
     */
    public boolean checkProductFlagElaQtimeByProduct(List<Product> productDataList) throws CustomException {
        logger.info("checkProductFlagElaQtime Started.");

        boolean checkElaQtime = false;
        List<ProductFlag> productFlagDataList = new ArrayList<ProductFlag>();
        
        try {
            if (productDataList != null && productDataList.size() > 0) {
                Object[] bindSet = new Object[productDataList.size() + 1];
                            
                for (int index = 0; index < productDataList.size(); index++) {
                	// 2019.04.15_hsryu_Modify Logic. avoid [Invalid column type] Error.
                    //bindSet[index] =  productDataList.get(index);
                    bindSet[index] =  productDataList.get(index).getKey().getProductName();
                }
                String condition = "PRODUCTNAME IN (" + StringUtils.removeEnd(StringUtils.repeat("?,", productDataList.size()), ",") + ")"
                                 + " AND ELAQTIMEFLAG = ? ";
                bindSet[productDataList.size()] = GenericServiceProxy.getConstantMap().FLAG_Y;
                
                productFlagDataList = ExtendedObjectProxy.getProductFlagService().select(condition, bindSet);
            } else {
                productFlagDataList = ExtendedObjectProxy.getProductFlagService().select("", new Object[] {});
            }
            
            if(productFlagDataList != null && productFlagDataList.size() > 0) {
                checkElaQtime = true;
            }
        } catch (Exception e) {
            logger.info(e);
        }
        
        logger.info("checkProductFlagElaQtime Ended.");
        
        return checkElaQtime;
    }
    
    public void setLotInfoReceiveProductFlag(EventInfo eventInfo, Element productEle, String flagType)  throws CustomException
    {
    	eventInfo.setEventName("UpdateProductFlag");
    	eventInfo.setCheckTimekeyValidation(false);
		eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
		eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
    	
    	String productName = SMessageUtil.getChildText(productEle, "PRODUCTNAME", true);
    	
    	String strSql = "SELECT ENUMVALUE FLAG " +
    			"  FROM ENUMDEFVALUE  " +
    			" WHERE ENUMNAME = :ENUMNAME ";
    	
        Map<String, Object> bindMap = new HashMap<String, Object>();
        bindMap.put("ENUMNAME", flagType);

        List<Map<String, Object>> upLoadFlagList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
        
        if(upLoadFlagList != null && upLoadFlagList.size() > 0)
        {
        	String strSqlFlag = "SELECT * " +
        			"  FROM CT_PRODUCTFLAG " +
        			" WHERE PRODUCTNAME = :PRODUCTNAME ";
        	
        	Map<String, Object> bindMapFlag = new HashMap<String, Object>();
        	bindMapFlag.put("PRODUCTNAME", productName);
        	
        	List<Map<String, Object>> productFlag = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSqlFlag, bindMapFlag);
        	
        	if(productFlag != null && productFlag.size() > 0)
        	{
        		int updateFlag = 0;
        		String updateSql = "UPDATE CT_PRODUCTFLAG SET ";
        		
    			for(int i = 0; i < upLoadFlagList.size(); i++ )
                {
        			String mesFlag = (String)productFlag.get(0).get(upLoadFlagList.get(i).get("FLAG"));
        			String bcFlag = SMessageUtil.getChildText(productEle, (String)upLoadFlagList.get(i).get("FLAG"), false);
        			
        			if(!StringUtil.isEmpty(bcFlag) || !StringUtil.isEmpty(mesFlag))
        			{
        				if(!StringUtil.equals(mesFlag, bcFlag))
            			{
            				updateSql = updateSql + upLoadFlagList.get(i).get("FLAG") + "= '" + bcFlag + "'," + " ";
            				
            				if(updateFlag == 0)
            				{
            					updateFlag = 1;
            				}
            			}
        			}
                }
    			
    			if(updateFlag == 1)
    			{
    				updateSql = updateSql + " LASTEVENTUSER = :LASTEVENTUSER, LASTEVENTTIME = :LASTEVENTTIME, LASTEVENTTIMEKEY = :LASTEVENTTIMEKEY, LASTEVENTNAME = :LASTEVENTNAME, LASTEVENTCOMMENT = :LASTEVENTCOMMENT WHERE PRODUCTNAME = :PRODUCTNAME";
    				
    				bindMapFlag.put("LASTEVENTUSER", eventInfo.getEventUser());
    				bindMapFlag.put("LASTEVENTTIME", eventInfo.getEventTime());
    				bindMapFlag.put("LASTEVENTTIMEKEY", eventInfo.getEventTimeKey());
    				bindMapFlag.put("LASTEVENTNAME", eventInfo.getEventName());
    				bindMapFlag.put("LASTEVENTCOMMENT", eventInfo.getEventComment());
    				
    				greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(updateSql, bindMapFlag);
    				
    				
    				ProductFlag productFlagData = null;
    				
    				try
    				{
    					productFlagData = ExtendedObjectProxy.getProductFlagService().selectByKey(false, new Object[] {productName});
    				}
    				catch (Exception ex)
    				{
    					productFlagData = null;
    				}
    				
    				if(productFlagData != null)
    				{
    					super.addHistory(eventInfo, this.historyEntity, productFlagData, logger);
    				}
    			}
        	}
        }
    }

    public void setLotInfoReceiveProductFlagByTurn(EventInfo eventInfo, Element productEle, String flagType)  throws CustomException
    {
        eventInfo.setEventName("UpdateProductFlag");
        eventInfo.setCheckTimekeyValidation(false);
        /* 20181128, hhlee, EventTime Sync */
        //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
        
        String productName = SMessageUtil.getChildText(productEle, "PRODUCTNAME", true);        
        String turnOverFlag = SMessageUtil.getChildText(productEle, "TURNOVERFLAG", false);
        String turnSideFlag = SMessageUtil.getChildText(productEle, "PROCESSTURNFLAG", false);
        
        String strSql = "SELECT ENUMVALUE FLAG " +
                "  FROM ENUMDEFVALUE  " +
                " WHERE ENUMNAME = :ENUMNAME ";
        
        Map<String, Object> bindMap = new HashMap<String, Object>();
        bindMap.put("ENUMNAME", flagType);

        List<Map<String, Object>> upLoadFlagList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
        
        if(upLoadFlagList != null && upLoadFlagList.size() > 0)
        {
            String strSqlFlag = "SELECT * " +
                    "  FROM CT_PRODUCTFLAG " +
                    " WHERE PRODUCTNAME = :PRODUCTNAME ";
            
            Map<String, Object> bindMapFlag = new HashMap<String, Object>();
            bindMapFlag.put("PRODUCTNAME", productName);
            
            List<Map<String, Object>> productFlag = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSqlFlag, bindMapFlag);
            
            if(productFlag != null && productFlag.size() > 0)
            {
                String processTurnFlag = StringUtil.EMPTY;
                if(productFlag.get(0).get("PROCESSTURNFLAG") != null)
                {
                    processTurnFlag = productFlag.get(0).get("PROCESSTURNFLAG").toString();
                }
                
                
                if((StringUtil.isEmpty(processTurnFlag) || StringUtil.equals(processTurnFlag, GenericServiceProxy.getConstantMap().Flag_N)) &&
                        StringUtil.equals(turnSideFlag, GenericServiceProxy.getConstantMap().Flag_Y))
                {
                    //productUdfs.put("TURNSIDEFLAG", GenericServiceProxy.getConstantMap().Flag_Y);
                    turnSideFlag = GenericServiceProxy.getConstantMap().Flag_Y;
                }
                else if(StringUtil.equals(processTurnFlag, GenericServiceProxy.getConstantMap().Flag_Y) &&
                        StringUtil.equals(turnSideFlag, GenericServiceProxy.getConstantMap().Flag_Y))
                {
                    //productUdfs.put("PROCESSTURNFLAG", GenericServiceProxy.getConstantMap().Flag_N);
                    turnSideFlag = GenericServiceProxy.getConstantMap().Flag_N;
                }
                else
                {   
                    turnSideFlag = processTurnFlag;
                }               
                
                
                String bcTurnOverFlag = StringUtil.EMPTY;
                if(productFlag.get(0).get("TURNOVERFLAG") != null)
                {
                    bcTurnOverFlag = productFlag.get(0).get("TURNOVERFLAG").toString();
                }
                
                
                if((StringUtil.isEmpty(bcTurnOverFlag) || StringUtil.equals(bcTurnOverFlag, GenericServiceProxy.getConstantMap().Flag_N)) &&
                        StringUtil.equals(turnOverFlag, GenericServiceProxy.getConstantMap().Flag_Y))
                {
                    //productUdfs.put("TURNSIDEFLAG", GenericServiceProxy.getConstantMap().Flag_Y);
                    turnOverFlag = GenericServiceProxy.getConstantMap().Flag_Y;            
                }
                else if(StringUtil.equals(bcTurnOverFlag, GenericServiceProxy.getConstantMap().Flag_Y) &&
                        StringUtil.equals(turnOverFlag, GenericServiceProxy.getConstantMap().Flag_Y))
                {
                    //productUdfs.put("TURNOVERFLAG", GenericServiceProxy.getConstantMap().Flag_N);
                    turnOverFlag = GenericServiceProxy.getConstantMap().Flag_N;   
                }
                else
                {   
                    turnOverFlag = bcTurnOverFlag;
                }
                
                int updateFlag = 0;
                String updateSql = "UPDATE CT_PRODUCTFLAG SET ";
                
                for(int i = 0; i < upLoadFlagList.size(); i++ )
                {
                    String mesFlag = (String)productFlag.get(0).get(upLoadFlagList.get(i).get("FLAG"));
                    String bcFlag = SMessageUtil.getChildText(productEle, (String)upLoadFlagList.get(i).get("FLAG"), false);
                    
                    if(StringUtil.equals((String)upLoadFlagList.get(i).get("FLAG"),"PROCESSTURNFLAG"))
                    {
                        bcFlag = turnSideFlag;
                    }
                    
                    if(StringUtil.equals((String)upLoadFlagList.get(i).get("FLAG"),"TURNOVERFLAG"))
                    {
                        bcFlag = turnOverFlag;
                    }
                    
                    if(!StringUtil.isEmpty(bcFlag) || !StringUtil.isEmpty(mesFlag))
                    {
                        if(!StringUtil.equals(mesFlag, bcFlag))
                        {
                            updateSql = updateSql + upLoadFlagList.get(i).get("FLAG") + "= '" + bcFlag + "'," + " ";
                            
                            if(updateFlag == 0)
                            {
                                updateFlag = 1;
                            }
                        }
                    }
                }
                
                if(updateFlag == 1)
                {
                    updateSql = updateSql + " LASTEVENTUSER = :LASTEVENTUSER, LASTEVENTTIME = :LASTEVENTTIME, LASTEVENTTIMEKEY = :LASTEVENTTIMEKEY, LASTEVENTNAME = :LASTEVENTNAME, LASTEVENTCOMMENT = :LASTEVENTCOMMENT WHERE PRODUCTNAME = :PRODUCTNAME";
                    
                    bindMapFlag.put("LASTEVENTUSER", eventInfo.getEventUser());
                    bindMapFlag.put("LASTEVENTTIME", eventInfo.getEventTime());
                    bindMapFlag.put("LASTEVENTTIMEKEY", eventInfo.getEventTimeKey());
                    bindMapFlag.put("LASTEVENTNAME", eventInfo.getEventName());
                    bindMapFlag.put("LASTEVENTCOMMENT", eventInfo.getEventComment());
                    
                    greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(updateSql, bindMapFlag);
                    
                    
                    ProductFlag productFlagData = null;
                    
                    try
                    {
                        productFlagData = ExtendedObjectProxy.getProductFlagService().selectByKey(false, new Object[] {productName});
                    }
                    catch (Exception ex)
                    {
                        productFlagData = null;
                    }
                    
                    if(productFlagData != null)
                    {
                        super.addHistory(eventInfo, this.historyEntity, productFlagData, logger);
                    }
                }
            }
        }
    }
    
    public void setCreateProductFlagForUPK(EventInfo eventInfo, List<Product> productList)  throws CustomException
    {
    	eventInfo.setEventName("CreateProductFlag");
    	eventInfo.setCheckTimekeyValidation(false);
    	/* 20181128, hhlee, EventTime Sync */
    	//eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
		eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
		
		String strSql = "SELECT DISTINCT ENUMVALUE FLAG   " +
				"  FROM ENUMDEFVALUE    " +
				" WHERE ENUMNAME IN (:ENUMNAME1, :ENUMNAME2, :ENUMNAME3) ";;
    	
        Map<String, Object> bindMap = new HashMap<String, Object>();
        bindMap.put("ENUMNAME1", GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
        bindMap.put("ENUMNAME2", GenericServiceProxy.getConstantMap().PRODUCTFLAGMANUAL);
        bindMap.put("ENUMNAME3", GenericServiceProxy.getConstantMap().PRODUCTFLAGDOWNLOAD);

        List<Map<String, Object>> upLoadFlagList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
        
        if(upLoadFlagList != null && upLoadFlagList.size() > 0)
        {
    		String createSql = "INSERT INTO CT_PRODUCTFLAG (PRODUCTNAME, ";
    		
    		for(int i = 0; i < upLoadFlagList.size(); i++ )
            {
				createSql = createSql + upLoadFlagList.get(i).get("FLAG") + "," + " ";
            }
    			
			createSql = createSql + " LASTEVENTUSER, LASTEVENTTIME, LASTEVENTTIMEKEY, LASTEVENTNAME, LASTEVENTCOMMENT)";
			createSql = createSql + " VALUES (:PRODUCTNAME, ";
			
			for(int i = 0; i < upLoadFlagList.size(); i++)
			{
				createSql = createSql + "'N'" + "," + " ";
			}
			
			createSql = createSql + " :LASTEVENTUSER, :LASTEVENTTIME, :LASTEVENTTIMEKEY, :LASTEVENTNAME, :LASTEVENTCOMMENT)";
			
			for(Product productData : productList)
			{
			    ProductFlag productFlagData = null;
			    
			    try
                {
                    productFlagData = ExtendedObjectProxy.getProductFlagService().selectByKey(false, new Object[] {productData.getKey().getProductName()});
                }
                catch (Exception ex)
                {
                    productFlagData = null;
                }
			    
			    if(productFlagData == null)
			    {
    			    Map<String, Object> bindMapFlag = new HashMap<String, Object>();
        			bindMapFlag.put("PRODUCTNAME", productData.getKey().getProductName());
    				bindMapFlag.put("LASTEVENTUSER", eventInfo.getEventUser());
    				bindMapFlag.put("LASTEVENTTIME", eventInfo.getEventTime());
    				bindMapFlag.put("LASTEVENTTIMEKEY", eventInfo.getEventTimeKey());
    				bindMapFlag.put("LASTEVENTNAME", eventInfo.getEventName());
    				bindMapFlag.put("LASTEVENTCOMMENT", eventInfo.getEventComment());
    				    				
    				greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(createSql, bindMapFlag);
    				
    				try
    				{
    					productFlagData = ExtendedObjectProxy.getProductFlagService().selectByKey(false, new Object[] {productData.getKey().getProductName()});
    				}
    				catch (Exception ex)
    				{
    					productFlagData = null;
    				}
    				
    				if(productFlagData != null)
    				{
    					super.addHistory(eventInfo, this.historyEntity, productFlagData, logger);
    				}
			    }
			}
        }
    }
    
    /**
     * @param oldProductFlagData
     * @param newProductFlagData
     * @param note
     * @return StringBuilder
     * @throws CustomException
     * @author ParkJeongSu
     */
    public void getNoteChangedFlag(ProductFlag oldProductFlagData,ProductFlag newProductFlagData,StringBuilder note) throws CustomException{
    	// 기존 oldProductFlagData 와 바뀐 newProductFlagData 를 비교한다.
    	// 하나라도 바뀐 컬럼이 존재한다면, changeFlag 를 true로 변경하고
    	// changeGlassList 의 변경된 값을 추가한다.
    	// note는 기존 프로덕트에 변경된 note값이 저장되어있고, append 후 반환한다.
    	StringBuilder changeGlass = new StringBuilder("Change Glass : \n");
    	StringBuilder returnNote= new StringBuilder("["+oldProductFlagData.getProductName()+"] ");
    	StringBuilder changeGlassList = new StringBuilder("");
    	boolean changeFlag = false;
    	try {
    		if(!oldProductFlagData.getelaQTimeFlag().equals(newProductFlagData.getelaQTimeFlag()))
    		{
    			changeGlassList.append("[ELAQTimeFlag : "+newProductFlagData.getelaQTimeFlag()+"] ");
    			changeFlag = true;
    		}
    		
    		if(!oldProductFlagData.getProcessTurnFlag().equals(newProductFlagData.getProcessTurnFlag()))
    		{
    			changeGlassList.append("[ProcessTurnFlag : "+newProductFlagData.getProcessTurnFlag()+"] ");
    			changeFlag = true;
    		}
    		
    		if(!oldProductFlagData.getTrackFlag().equals(newProductFlagData.getTrackFlag()))
    		{
    			changeGlassList.append("[TrackFlag : "+newProductFlagData.getTrackFlag()+"] ");
    			changeFlag = true;
    		}
    		
    		if(!oldProductFlagData.getTurnOverFlag().equals(newProductFlagData.getTurnOverFlag()))
    		{
    			changeGlassList.append("[TurnOverFlag : "+newProductFlagData.getTurnOverFlag()+"] ");
    			changeFlag = true;
    		}
    		
            if(changeFlag==true){
            	if(note==null || StringUtils.isEmpty(note.toString())){
            		note.append(changeGlass.append(returnNote).append(changeGlassList));
            	}
            	else{
            		note.append("\n").append(returnNote).append(changeGlassList);
            	}
            }

		} catch (Exception e) {

		}
    }
    
    public ProductFlag getProductFlagByElement(Element productEle) throws CustomException{
    	String productName = SMessageUtil.getChildText(productEle, "PRODUCTNAME", true);
    	return this.selectByKey(false, new Object[] {productName});
    }
    
}
