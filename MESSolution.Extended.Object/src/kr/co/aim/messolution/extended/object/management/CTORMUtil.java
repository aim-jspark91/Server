package kr.co.aim.messolution.extended.object.management;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.support.InvokeUtils;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CTORMUtil {

	private static Log logger = LogFactory.getLog(CTORMUtil.class);
	
	public static Log getLogger()
	{
		return logger;
	}
	
	public static final String servicePath = "kr.co.aim.messolution.extended.object.management.impl";
	
	public static Object createDataInfo(Class dataObject)
	{
		Object createdInfo = null;
		
		try
		{
			createdInfo = (Object) Class.forName(dataObject.getName()).newInstance();
			
		} catch (Exception e)
		{
			logger.warn(e, e);
		}
		
		if (createdInfo != null)
		{
			for (Field column : createdInfo.getClass().getDeclaredFields())
			{
				if (column.isAnnotationPresent(CTORMTemplate.class))
				{
					CTORMTemplate annotation = column.getAnnotation(CTORMTemplate.class);
					
					String name = annotation.name();
					String type = annotation.type();
					String dataType = annotation.dataType();
					String initial = annotation.initial();
					
					//specific data type setting
					if (dataType.equalsIgnoreCase("timestamp"))
						ObjectUtil.setFieldValue(createdInfo, name, null);
					else if (!initial.isEmpty())
						ObjectUtil.setFieldValue(createdInfo, name, initial);
				}
				else
				{
					//annotation is mandatory at hibernate
				}
				
				//udfs initialize
				/*Map<String, String> udfs = new HashMap<String, String>();
				for (ObjectAttributeDef attributeDef : ObjectAttributeDefs)
				{
					if (StringUtils.isNotEmpty(attributeDef.getDefaultValue()))
						udfs.put(attributeDef.getAttributeName(), attributeDef.getDefaultValue());
					else
						udfs.put(attributeDef.getAttributeName(), "");
				}
				ObjectUtil.setFieldValue(createdInfo, "udfs", udfs);*/
			}
		}

		return createdInfo;
	}
	
	public static String getTableNameByClassName(Class clazz)
	{
		String tableName = clazz.getSimpleName();
		
		//if (tableName.endsWith("Key"))
		//	tableName = tableName.substring(0, tableName.length() - 3);
		//else if (tableName.endsWith("ServiceImpl"))
		//	tableName = tableName.substring(0, tableName.length() - 11);
		//else if (tableName.endsWith("Service"))
		//	tableName = tableName.substring(0, tableName.length() - 7);
		
		tableName = new StringBuffer().append(getHeader(clazz)).append(tableName).toString();
		
		return tableName;
	}
	
	public static String getHistoryTableNameByClassName(Class clazz, boolean isBrief)
	{
		//if (tableName.endsWith("Key"))
		//	tableName = tableName.substring(0, tableName.length() - 3);
		//else if (tableName.endsWith("ServiceImpl"))
		//	tableName = tableName.substring(0, tableName.length() - 11);
		//else if (tableName.endsWith("Service"))
		//	tableName = tableName.substring(0, tableName.length() - 7);
		
		StringBuffer tableNameBuffer = new StringBuffer().append(getHeader(clazz)).append(clazz.getSimpleName());
		
		if (isBrief)
			tableNameBuffer.append("Hist");
		else
			tableNameBuffer.append("History");
		
		return tableNameBuffer.toString();
	}
	
	public static String getConditionSql(String sql, String condition)
	{
		if (condition.toLowerCase().trim().startsWith("where "))
		{
			//sql = sql + " " + condition.trim();
			sql = new StringBuffer(sql).append(" ").append(condition.trim()).toString();
		}
		else
		{
			//sql = sql + " where " + condition;
			sql = new StringBuffer(sql).append(" WHERE ").append(condition).toString();
		}
		return sql;
	}
	
	public static String getKeySql(String sql, Object dataInfo)
	{
		StringBuffer sCondition = new StringBuffer(sql).append(" WHERE 1=1 ");
		
		if (dataInfo != null)
		{
			for (Field column : dataInfo.getClass().getDeclaredFields())
			{
				//only by annotation presentation
				if (column.isAnnotationPresent(CTORMTemplate.class))
				{
					CTORMTemplate annotation = column.getAnnotation(CTORMTemplate.class);
					
					String name = annotation.name();
					String type = annotation.type();
					String dataType = annotation.dataType();
					String initial = annotation.initial();
					
					if (type.equalsIgnoreCase("key"))
						sCondition.append(" AND ").append(name).append("=").append("?");
				}
			}
		}
		
		return sCondition.toString();
	}
	
	public static String getUpdateSql(Class dataInfo, String tableName)
	{
		StringBuffer sql = new StringBuffer();
		StringBuffer subSql = new StringBuffer(" ").append("WHERE 1=1").append(" ");
		
		sql.append("UPDATE").append(" ").append(tableName).append(" ").append("SET").append(" ");
		
		for (Field column : dataInfo.getDeclaredFields())
		{
			if (column.isAnnotationPresent(CTORMTemplate.class))
			{
				CTORMTemplate annotation = column.getAnnotation(CTORMTemplate.class);
				
				String name = annotation.name();
				String type = annotation.type();
				//String dataType = annotation.dataType();
				String initial = annotation.initial();
				
				//never key update
				//never null update
				if (!initial.equalsIgnoreCase("never"))
				{
					if (type.equalsIgnoreCase("key"))
						subSql.append(" AND ").append(name).append("=").append("?");
					else
						sql.append(name).append("=").append("?").append(",");
				}
			}
		}
		
		String refinedSql = StringUtil.removeEnd(sql.toString(), ",");
		
		sql = new StringBuffer(refinedSql).append(subSql.toString());
		
		return sql.toString();
	}
	
	public static String getDeleteSql(Class dataInfo, String tableName)
	{
		StringBuffer sql = new StringBuffer();
		StringBuffer subSql = new StringBuffer(" ").append("WHERE 1=1").append(" ");
		
		sql.append("DELETE").append(" ").append(tableName).append(" ");
		
		for (Field column : dataInfo.getDeclaredFields())
		{
			if (column.isAnnotationPresent(CTORMTemplate.class))
			{
				CTORMTemplate annotation = column.getAnnotation(CTORMTemplate.class);
				
				String name = annotation.name();
				String type = annotation.type();
				//String dataType = annotation.dataType();
				//String initial = annotation.initial();
				
				//never key update
				if (type.equalsIgnoreCase("key"))
					subSql.append(" AND ").append(name).append("=").append("?");
				//else
				//	sql.append(name).append("=").append("?").append(",");
			}
		}
		
		String refinedSql = StringUtil.removeEnd(sql.toString(), ",");
		
		sql = new StringBuffer(refinedSql).append(subSql.toString());
		
		return sql.toString();
	}
	
	public static String getInsertSql(Class dataInfo, String tableName)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("INSERT").append(" ").append("INTO").append(" ").append(tableName).append(" ");
		
		StringBuilder attrBuilder = new StringBuilder("");
		StringBuilder valueBuilder = new StringBuilder("");
		
		for (Field column : dataInfo.getDeclaredFields())
		{
			if (column.isAnnotationPresent(CTORMTemplate.class))
			{
				CTORMTemplate annotation = column.getAnnotation(CTORMTemplate.class);
				
				String name = annotation.name();
				String type = annotation.type();
				String dataType = annotation.dataType();
				String initial = annotation.initial();
				
				if (initial.equalsIgnoreCase("never"))
					continue;
				
				{//column generation
					if (!attrBuilder.toString().isEmpty())
						attrBuilder.append(",");
					
					attrBuilder.append(name);
				}
				
				{//value generation : default setting demanded?
					if (!valueBuilder.toString().isEmpty())
						valueBuilder.append(",");
					
					if ((dataType.equalsIgnoreCase("timestamp") || dataType.equalsIgnoreCase("systemtime"))
							&& initial.equalsIgnoreCase("current"))
					{
						valueBuilder.append("SYSDATE");
					}
					else
					{
						valueBuilder.append("?");
					}
				}
				
			}
		}
		
		sql.append("(").append(attrBuilder).append(")").append(" VALUES ").append("(").append(valueBuilder).append(")");
		
		return sql.toString();
	}
	
	public static String getSql(Class dataInfo, String tableName)
	{
		StringBuffer sql = new StringBuffer();
		
		sql.append("SELECT").append(" ");
		
		for (Field column : dataInfo.getDeclaredFields())
		{
			if (column.isAnnotationPresent(CTORMTemplate.class))
			{
				CTORMTemplate annotation = column.getAnnotation(CTORMTemplate.class);
				
				String name = annotation.name();
				//String type = annotation.type();
				//String dataType = annotation.dataType();
				//String initial = annotation.initial();
				
				sql.append(name);
				sql.append(",");
			}
		}
		
		String refinedSql = StringUtil.removeEnd(sql.toString(), ",");
		
		sql = new StringBuffer(refinedSql).append(" ").append("FROM").append(" ").append(tableName);
		
		return sql.toString();
	}
	
	public static Object loadServiceProxy(Class clazz)
		throws CustomException
	{
		String serviceName = new StringBuffer(servicePath).append(".")
								.append(clazz.getSimpleName()).append("Service").toString();
		
		//String serviceName = clazz.getSimpleName();
		
		Object serviceClass = InvokeUtils.newInstance(serviceName, new Class[0], new Object[0]);
		
		if (serviceClass != null)
		{
			return serviceClass;
		}
		else
		{
			throw new CustomException("SYS-8001", serviceName);
		}
	}
	
	/**
	 * validate key parameter, delivering parameter array
	 * @author swcho
	 * @since 2014.04.29
	 * @param dataInfo
	 * @param args
	 * @return
	 */
	public static String validateKeyParam(Object dataInfo, Object[] args)
	{
		//key info validating
		try
		{
			if (dataInfo == null)
				throw new Exception("dataInfo is null");
			
			int keyCnt =0;
			
			for (Field column : dataInfo.getClass().getDeclaredFields())
			{
				//only by annotation presentation
				if (column.isAnnotationPresent(CTORMTemplate.class))
				{
					CTORMTemplate annotation = column.getAnnotation(CTORMTemplate.class);
					
					String name = annotation.name();
					String type = annotation.type();
					String dataType = annotation.dataType();
					String initial = annotation.initial();
					
					if (type.equalsIgnoreCase("key"))
						keyCnt++;	
				}
			}
			
			if (keyCnt != args.length)
				throw new Exception("key value is null");
		}
		catch (Exception ex)
		{
			//throw new greenFrameDBErrorSignal(ErrorSignal.NullPointKeySignal, "", sql, "Key can't be null");
			return ErrorSignal.NullPointKeySignal;
		}
		
		return "";
	}
	
	/**
	 * make primary key parameters
	 * @author swcho
	 * @since 2014.04.29
	 * @param dataInfo
	 * @return
	 */
	public static List<Object> makeKeyParam(Object dataInfo)
	{
		List<Object> temp = new ArrayList<Object>();
		
		if (dataInfo == null)
			return temp;
		
		int idx = 0;
		
		//find non-key binding at first
		for (Field column : dataInfo.getClass().getDeclaredFields())
		{
			//only by annotation presentation
			if (column.isAnnotationPresent(CTORMTemplate.class))
			{
				CTORMTemplate annotation = column.getAnnotation(CTORMTemplate.class);
				
				String name = annotation.name();
				String type = annotation.type();
				//String dataType = annotation.dataType();
				//String initial = annotation.initial();
				
				
				//if (type.equalsIgnoreCase("key"))
				//	subTemp.add(idx, ObjectUtil.getFieldValue(dataInfo, name));
				//else
				if (type.equalsIgnoreCase("key"))
				{
					temp.add(idx, ObjectUtil.getFieldValue(dataInfo, name));
					idx++;
				}
			}
		}
		
		return temp;
	}
	
	/**
	 * make primary non-key parameters
	 * @author swcho
	 * @since 2014.04.29
	 * @param dataInfo
	 * @return
	 */
	public static List<Object> makeNonKeyParam(Object dataInfo)
	{
		List<Object> temp = new ArrayList<Object>();
		
		if (dataInfo == null)
			return temp;
		
		int idx = 0;
		
		//find non-key binding at first
		for (Field column : dataInfo.getClass().getDeclaredFields())
		{
			//only by annotation presentation
			if (column.isAnnotationPresent(CTORMTemplate.class))
			{
				CTORMTemplate annotation = column.getAnnotation(CTORMTemplate.class);
				
				String name = annotation.name();
				String type = annotation.type();
				//String dataType = annotation.dataType();
				String initial = annotation.initial();
				
				if (!type.equalsIgnoreCase("key")
						//ORM does not require null : prohibited
						//&& ObjectUtil.getFieldValue(dataInfo, name) != null
						&& !initial.equalsIgnoreCase("never"))
				{
					temp.add(idx, ObjectUtil.getFieldValue(dataInfo, name));
					idx++;
				}
			}
		}
		
		return temp;
	}
	public static List<Object> makeNonKeyParam(Object dataInfo,EventInfo eventInfo)
	{
		List<Object> temp = new ArrayList<Object>();
		
		if (dataInfo == null)
			return temp;
		
		int idx = 0;
		
		//find non-key binding at first
		for (Field column : dataInfo.getClass().getDeclaredFields())
		{
			//only by annotation presentation
			if (column.isAnnotationPresent(CTORMTemplate.class))
			{
				CTORMTemplate annotation = column.getAnnotation(CTORMTemplate.class);
				
				String name = annotation.name();
				String type = annotation.type();
				//String dataType = annotation.dataType();
				String initial = annotation.initial();
				
				if (!type.equalsIgnoreCase("key")
						//ORM does not require null : prohibited
						//&& ObjectUtil.getFieldValue(dataInfo, name) != null
						&& !initial.equalsIgnoreCase("never"))
				{
					if(StringUtils.equalsIgnoreCase("lasteventName", name)){
						temp.add(idx, eventInfo.getEventName());
					}
					else if(StringUtils.equalsIgnoreCase("lastEventTime", name)){
						temp.add(idx, eventInfo.getEventTime());
					}
					else if(StringUtils.equalsIgnoreCase("lastEventTimeKey", name)){
						temp.add(idx, eventInfo.getEventTimeKey());
					}
					else if(StringUtils.equalsIgnoreCase("lastEventUser", name)){
						temp.add(idx, eventInfo.getEventUser());
					}
					else if(StringUtils.equalsIgnoreCase("lastEventComment", name)){
						temp.add(idx, eventInfo.getEventComment());
					}
					else{
						temp.add(idx, ObjectUtil.getFieldValue(dataInfo, name));
					}
					idx++;
				}
			}
		}
		
		return temp;
	}
	
	/**
	 * dynamic CTORM mapping header
	 * @author swcho
	 * @since 2015-10-29
	 * @param clazz
	 * @return
	 */
	public static String getHeader(Class clazz)
	{
		String headerName = "";
		String connector = "";
		
		if (clazz.isAnnotationPresent(CTORMHeader.class))
		{
			CTORMHeader header = (CTORMHeader) clazz.getAnnotation(CTORMHeader.class); 
			
			if (header != null)
			{
				headerName = header.tag();
				connector = header.divider();
			}
		}
		
		if (headerName.isEmpty()) headerName = "CT";
		if (connector.isEmpty()) connector = "_";
		
		return new StringBuilder(headerName).append(connector).toString();
	}
	
	
	
	
	
	
	//17.10.23 dmlee : Using in MOD PRODUCT Table
	public static String getUpdateSqlMODProduct(Class dataInfo, String tableName)
	{
		StringBuffer sql = new StringBuffer();
		StringBuffer subSql = new StringBuffer(" ").append("WHERE 1=1").append(" ");
		
		sql.append("UPDATE").append(" ").append(tableName).append(" ").append("SET").append(" ");
		
		for (Field column : dataInfo.getDeclaredFields())
		{
			String name = column.getName();
			
			if(name.equals("key"))
			{
				subSql.append(" AND ").append("PRODUCTNAME").append("=").append("?");
			}
			else
			{
				sql.append(name).append("=").append("?").append(",");
			}
			
		}
		
		String refinedSql = StringUtil.removeEnd(sql.toString(), ",");
		
		sql = new StringBuffer(refinedSql).append(subSql.toString());
		
		return sql.toString();
	}
	
	//17.10.23 dmlee : Using in MOD PRODUCT Table
	public static String getInsertSqlMODProduct(Class dataInfo, String tableName)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("INSERT").append(" ").append("INTO").append(" ").append(tableName).append(" ");
		
		StringBuilder attrBuilder = new StringBuilder("");
		StringBuilder valueBuilder = new StringBuilder("");
		
		for (Field column : dataInfo.getDeclaredFields())
		{
			String name = column.getName();
			
			Type type = column.getGenericType();
			String typeStr = type.toString();
			
			{//column generation
				if (!attrBuilder.toString().isEmpty())
					attrBuilder.append(",");
				
				if(name.equals("key"))
				{
					attrBuilder.append("productName");
				}
				else
				{
					attrBuilder.append(name);
				}
			}
			
			{//value generation : default setting demanded?
				if (!valueBuilder.toString().isEmpty())
					valueBuilder.append(",");
				
				if (typeStr.contains("Timestamp"))
				{
					valueBuilder.append("?");////dongmin
				}
				else
				{
					valueBuilder.append("?");
				}
			}
				
			
		}
		
		sql.append("(").append(attrBuilder).append(")").append(" VALUES ").append("(").append(valueBuilder).append(")");
		
		return sql.toString();
	}




	//17.10.23 dmlee : Using in MOD PRODUCT Table
	public static List<Object> makeKeyParamMODProduct(Object dataInfo)
	{
		List<Object> temp = new ArrayList<Object>();
		
		if (dataInfo == null)
			return temp;
		
		int idx = 0;
		
		//find non-key binding at first
		for (Field column : dataInfo.getClass().getDeclaredFields())
		{
			//only by annotation presentation


				
				String name = column.getName();
				//String dataType = annotation.dataType();
				//String initial = annotation.initial();
				
				
				//if (type.equalsIgnoreCase("key"))
				//	subTemp.add(idx, ObjectUtil.getFieldValue(dataInfo, name));
				//else
				if (name.equalsIgnoreCase("key"))
				{
					temp.add(idx, ObjectUtil.getFieldValue(dataInfo, "PRODUCTNAME"));
					idx++;
				}
			
		}
		
		return temp;
	}





	//17.10.23 dmlee : Using in MOD PRODUCT Table
	public static List<Object> makeNonKeyParamMODProduct(Object dataInfo)
	{
		List<Object> temp = new ArrayList<Object>();
		
		if (dataInfo == null)
			return temp;
		
		int idx = 0;
		
		//find non-key binding at first
		for (Field column : dataInfo.getClass().getDeclaredFields())
		{
			//only by annotation presentation


				
				String name = column.getName();

				
				if (!name.equalsIgnoreCase("key")
						//ORM does not require null : prohibited
						//&& ObjectUtil.getFieldValue(dataInfo, name) != null
						)
				{
					temp.add(idx, ObjectUtil.getFieldValue(dataInfo, name));
					idx++;
				}
			
		}
		
		return temp;
	}
	
	
}
