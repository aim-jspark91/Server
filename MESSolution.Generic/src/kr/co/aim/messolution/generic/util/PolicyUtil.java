package kr.co.aim.messolution.generic.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.sys.SystemPropHelper;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PolicyUtil {
	private static Log logger = LogFactory.getLog(PolicyUtil.class);
	public static String NEWLINE = SystemPropHelper.CR;

	/**
	 * get machine recipe in POS policy
	 * @author swcho
	 * @since 2014.09.03
	 * @param factoryName
	 * @param productSpecName
	 * @param processFlowName
	 * @param processOperationName
	 * @param machineName
	 * @return
	 */
	public static ListOrderedMap getMachineRecipeName(String factoryName, 
											  String productSpecName, 
											  String processFlowName, 
											  String processOperationName, 
											  String machineName)
		throws CustomException
	{
		StringBuffer sqlBuffer = new StringBuffer("")
								.append(" SELECT P.MACHINERECIPENAME").append(NEWLINE)
								.append("   FROM TPFOPOLICY T, POSMACHINE P ").append(NEWLINE)
								.append("  WHERE 1=1 ").append(NEWLINE)
								.append("	 AND T.FACTORYNAME= ? ").append(NEWLINE)
								.append("    AND T.PRODUCTSPECNAME = ? ").append(NEWLINE)
								.append("    AND T.PRODUCTSPECVERSION = ? ").append(NEWLINE)
								.append("    AND T.PROCESSFLOWNAME = ? ").append(NEWLINE)
								.append("    AND T.PROCESSFLOWVERSION = ? ").append(NEWLINE)
								.append("    AND T.PROCESSOPERATIONNAME= ? ").append(NEWLINE)
								.append("    AND T.PROCESSOPERATIONVERSION = ?").append(NEWLINE)
								.append("    AND P.MACHINENAME = ?").append(NEWLINE)
								.append("    AND T.CONDITIONID = P.CONDITIONID ").append(NEWLINE)
								.append("").append(NEWLINE);
		
		String sqlStmt = sqlBuffer.toString();
		
		Object[] bindSet = new String[]{factoryName, productSpecName, "00001", processFlowName, "00001", processOperationName, "00001", machineName};
		try
		{
			List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmt, bindSet);
			
			if(sqlResult.size() > 0)
				return sqlResult.get(0);
			else 
				throw new CustomException("SYS-9999", "POSMachine",
						String.format("Machine[%s] is not enable with [%s, %s, %s, %s]", machineName, factoryName, productSpecName, processFlowName, processOperationName));
		}
		catch (FrameworkErrorSignal de)
		{
			throw new CustomException("SYS-9999", "POSMachine", de.getMessage());
		}
	}
	
	public static ListOrderedMap getMachineRecipeNameByTPEFOMPolicy(String factoryName, 
			String productSpecName, 
			String processFlowName, 
			String processOperationName, 
			String machineName,
			String ecCode)
					throws CustomException
	{
		if ( ecCode.isEmpty() || ecCode.equals("") )
		{
			ecCode = "*";
		}
		
		String recipeSql = StringUtil.EMPTY;
		recipeSql = recipeSql + " SELECT P.MACHINERECIPENAME                                                                                  \n";
		recipeSql = recipeSql + "   FROM TPEFOMPOLICY T                                                                                       \n";
		recipeSql = recipeSql + "   INNER JOIN POSRECIPE P ON T.CONDITIONID  = P.CONDITIONID                                                  \n";
		recipeSql = recipeSql + "  WHERE ((T.FACTORYNAME = :FACTORYNAME) OR (T.FACTORYNAME = :STAR))                                            \n";
		recipeSql = recipeSql + "    AND ((T.PRODUCTSPECNAME = :PRODUCTSPECNAME) OR (T.PRODUCTSPECNAME = :STAR))                                \n";
		recipeSql = recipeSql + "    AND ((T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION) OR (T.PRODUCTSPECVERSION = :STAR))                       \n";
		recipeSql = recipeSql + "    AND ((T.ECCODE = :ECCODE) OR (T.ECCODE = :STAR))                                                           \n";
		recipeSql = recipeSql + "    AND ((T.PROCESSFLOWNAME = :PROCESSFLOWNAME) OR (T.PROCESSFLOWNAME = :STAR))                                \n";
		recipeSql = recipeSql + "    AND ((T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION) OR (T.PROCESSFLOWVERSION = :STAR))                       \n";
		recipeSql = recipeSql + "    AND ((T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME) OR (T.PROCESSOPERATIONNAME = :STAR))                 \n";
		recipeSql = recipeSql + "    AND ((T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION) OR (T.PROCESSOPERATIONVERSION = :STAR))        \n";
		recipeSql = recipeSql + "    AND ((T.MACHINENAME = :MACHINENAME) OR (T.MACHINENAME = :STAR))                                            \n";
		recipeSql = recipeSql + "  ORDER BY DECODE (FACTORYNAME, :STAR, 9999, 0),                                                               \n";
		recipeSql = recipeSql + "           DECODE (PRODUCTSPECNAME, :STAR, 9999, 0),                                                           \n";
		recipeSql = recipeSql + "           DECODE (PRODUCTSPECVERSION, :STAR, 9999, 0),                                                        \n";
		recipeSql = recipeSql + "           DECODE (ECCODE, :STAR, 9999, 0),                                                                    \n";
		recipeSql = recipeSql + "           DECODE (PROCESSFLOWNAME, :STAR, 9999, 0),                                                           \n";
		recipeSql = recipeSql + "           DECODE (PROCESSFLOWVERSION, :STAR, 9999, 0),                                                        \n";
		recipeSql = recipeSql + "           DECODE (PROCESSOPERATIONNAME, :STAR, 9999, 0),                                                      \n";
		recipeSql = recipeSql + "           DECODE (PROCESSOPERATIONVERSION, :STAR, 9999, 0),                                                   \n";
		recipeSql = recipeSql + "           DECODE (MACHINENAME, :STAR, 9999, 0)                                                                \n";

		Map<String, Object> recipeBindSet = new HashMap<String, Object>();
		recipeBindSet.put("FACTORYNAME", factoryName);
		recipeBindSet.put("PRODUCTSPECNAME", productSpecName);
		recipeBindSet.put("PRODUCTSPECVERSION", "00001");
		recipeBindSet.put("ECCODE", ecCode);
		recipeBindSet.put("PROCESSFLOWNAME", processFlowName);
		recipeBindSet.put("PROCESSFLOWVERSION", "00001");
		recipeBindSet.put("PROCESSOPERATIONNAME", processOperationName);
		recipeBindSet.put("PROCESSOPERATIONVERSION", "00001");
		recipeBindSet.put("MACHINENAME", machineName);
		recipeBindSet.put("STAR", "*");

		try
		{
			List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(recipeSql, recipeBindSet);

			if(sqlResult.size() > 0)
			{
				return sqlResult.get(0);
			}
			else
			{
				throw new CustomException("POLICY-0011",factoryName,productSpecName,ecCode,processFlowName,processOperationName,machineName);
			}
		}
		catch (FrameworkErrorSignal de)
		{
			throw new CustomException("SYS-9999", "POSMachine", de.getMessage());
		}
	}
	
	public static String getMachineProbeCardTypeByTPEFOMPolicy(String factoryName, 
			String productSpecName, 
			String processFlowName, 
			String processOperationName, 
			String machineName,
			String ecCode)
					throws CustomException
	{
		if ( ecCode.isEmpty() || ecCode.equals("") )
		{
			ecCode = "*";
		}
		
		StringBuffer sqlBuffer = new StringBuffer("")
		.append(" SELECT P.MACHINERECIPENAME").append(NEWLINE)
		.append("   FROM TPEFOMPolicy T ").append(NEWLINE)
		.append("   INNER JOIN POSRECIPE P ON T.CONDITIONID  = P.CONDITIONID  ").append(NEWLINE)
		.append("  WHERE 1 = 1 ").append(NEWLINE)
		.append("	 AND T.FACTORYNAME= ? ").append(NEWLINE)
		.append("    AND T.PRODUCTSPECNAME = ? ").append(NEWLINE)
		.append("    AND T.PRODUCTSPECVERSION = ? ").append(NEWLINE)
		.append("    AND T.ECCODE = ? ").append(NEWLINE)
		.append("    AND T.PROCESSFLOWNAME = ? ").append(NEWLINE)
		.append("    AND T.PROCESSFLOWVERSION = ? ").append(NEWLINE)
		.append("    AND T.PROCESSOPERATIONNAME= ? ").append(NEWLINE)
		.append("    AND T.PROCESSOPERATIONVERSION = ?").append(NEWLINE)
		.append("    AND T.MACHINENAME = ?").append(NEWLINE)
		.append("").append(NEWLINE);

		String sqlStmt = sqlBuffer.toString();

		Object[] bindSet = new String[]{factoryName, productSpecName, "00001", ecCode, processFlowName, "00001", processOperationName, "00001", machineName};
		try
		{
			List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmt, bindSet);

			if(sqlResult.size() > 0)
				return sqlResult.get(0).toString();
			else 
				throw new CustomException("POLICY-0011",factoryName,productSpecName,ecCode,processFlowName,processOperationName,machineName);
		}
		catch (FrameworkErrorSignal de)
		{
			throw new CustomException("SYS-9999", "POSMachine", de.getMessage());
		}
	}
	
	//2018-05-24 dmlee
		public static ListOrderedMap getMachineRecipeNameByTPEFOMPolicyV2(String factoryName, 
				String productSpecName, 
				String processFlowName, 
				String processOperationName, 
				String machineName,
				String ecCode)
						throws CustomException
		{
			// Modified by smkang on 2018.07.11 - Asterisk of machine name is permitted in TPEFOMPolicy, so if the result of query has no data, MES should find with machine group again.
//			StringBuffer sqlBuffer = new StringBuffer("")
//									.append(" SELECT T.MACHINERECIPENAME,").append(NEWLINE)
//									.append(" T.FACTORYNAME,").append(NEWLINE)
//									.append(" T.PRODUCTSPECNAME,").append(NEWLINE)
//									.append(" T.ECCODE,").append(NEWLINE)
//									.append(" T.PROCESSFLOWNAME,").append(NEWLINE)
//									.append(" T.PROCESSOPERATIONNAME").append(NEWLINE)
//									.append("   FROM TPEFOMPolicy T ").append(NEWLINE)
//									.append("  WHERE 1 = 1 ").append(NEWLINE)
//									.append("	 AND T.FACTORYNAME= ? ").append(NEWLINE)
//									.append("    AND (T.PRODUCTSPECNAME = ? OR T.PRODUCTSPECNAME = '*') ").append(NEWLINE)
//									.append("    AND T.PRODUCTSPECVERSION = '00001' ").append(NEWLINE)
//									.append("    AND (T.ECCODE = ? OR T.ECCODE = '*') ").append(NEWLINE)
//									.append("    AND (T.PROCESSFLOWNAME = ? OR T.PROCESSFLOWNAME ='*') ").append(NEWLINE)
//									.append("    AND T.PROCESSFLOWVERSION = '00001' ").append(NEWLINE)
//									.append("    AND (T.PROCESSOPERATIONNAME= ? OR T.PROCESSOPERATIONNAME = '*') ").append(NEWLINE)
//									.append("    AND T.PROCESSOPERATIONVERSION = '00001'").append(NEWLINE)
//									.append("    AND T.MACHINENAME = ?").append(NEWLINE)
//									.append("  ORDER BY T.PRODUCTSPECNAME DESC, T.ECCODE DESC, T.PROCESSFLOWNAME DESC, T.PROCESSOPERATIONNAME DESC ").append(NEWLINE)
//									.append("").append(NEWLINE);
			StringBuffer sqlBuffer = new StringBuffer("")
									.append(" SELECT P.MACHINERECIPENAME,").append(NEWLINE)
									.append(" T.FACTORYNAME,").append(NEWLINE)
									.append(" T.PRODUCTSPECNAME,").append(NEWLINE)
									.append(" T.ECCODE,").append(NEWLINE)
									.append(" T.PROCESSFLOWNAME,").append(NEWLINE)
									.append(" T.PROCESSOPERATIONNAME").append(NEWLINE)
									.append("   FROM TPEFOMPolicy T ").append(NEWLINE)
									.append("   INNER JOIN POSRECIPE P ON T.CONDITIONID  = P.CONDITIONID  ").append(NEWLINE)
									.append("  WHERE 1 = 1 ").append(NEWLINE)
									.append("	 AND T.FACTORYNAME= ? ").append(NEWLINE)
									.append("    AND (T.PRODUCTSPECNAME = ? OR T.PRODUCTSPECNAME = ?) ").append(NEWLINE)
									.append("    AND (T.PRODUCTSPECVERSION = ? OR T.PRODUCTSPECVERSION = ?) ").append(NEWLINE)
									.append("    AND (T.ECCODE = ? OR T.ECCODE = ?) ").append(NEWLINE)
									.append("    AND (T.PROCESSFLOWNAME = ? OR T.PROCESSFLOWNAME = ?) ").append(NEWLINE)
									.append("    AND (T.PROCESSFLOWVERSION = ? OR T.PROCESSFLOWVERSION = ?) ").append(NEWLINE)
									.append("    AND (T.PROCESSOPERATIONNAME= ? OR T.PROCESSOPERATIONNAME = ?) ").append(NEWLINE)
									.append("    AND (T.PROCESSOPERATIONVERSION = ? OR T.PROCESSOPERATIONVERSION = ?) ").append(NEWLINE)
									.append("    AND (T.MACHINENAME = ? OR T.MACHINENAME = ?) ").append(NEWLINE)
									.append("    ORDER BY DECODE(T.FACTORYNAME, ?, 9999, 0), ").append(NEWLINE)
									.append("    DECODE(T.PRODUCTSPECNAME, ?, 9999, 0), ").append(NEWLINE)
									.append("    DECODE(T.PRODUCTSPECVERSION, ?, 9999, 0), ").append(NEWLINE)
									.append("    DECODE(T.ECCODE, ?, 9999, 0), ").append(NEWLINE)
									.append("    DECODE(T.PROCESSFLOWNAME, ?, 9999, 0), ").append(NEWLINE)
									.append("    DECODE(T.PROCESSFLOWVERSION, ?, 9999, 0), ").append(NEWLINE)
									.append("    DECODE(T.PROCESSOPERATIONNAME, ?, 9999, 0), ").append(NEWLINE)
									.append("    DECODE(T.PROCESSOPERATIONVERSION, ?, 9999, 0), ").append(NEWLINE)
									.append("    DECODE(T.MACHINENAME, ?, 9999, 0) ").append(NEWLINE)
									.append("").append(NEWLINE);

			String sqlStmt = sqlBuffer.toString();

			Object[] bindSet = new String[]{factoryName, productSpecName, "*", "00001", "*", ecCode, "*", processFlowName, "*", "00001", "*", 
											processOperationName, "*", "00001", "*", machineName, "*", "*", "*", "*", "*", "*", "*", "*", "*", "*"};
			try
			{
				List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmt, bindSet);

				if(sqlResult.size() > 0)
				{
					return sqlResult.get(0);
				}
				else 
					throw new CustomException("POLICY-0011",factoryName,productSpecName,ecCode,processFlowName,processOperationName,machineName);
			}
			catch (FrameworkErrorSignal de)
			{
				throw new CustomException("SYS-9999", "POSMachine", de.getMessage());
			}
		}
	
	
	//add by wghuang 20180527
	public static void checkMachineProbeCardTypeByTPEFOMPolicy(
			String factoryName, 
			String productSpecName, 
			String processFlowName, 
			String processOperationName, 
			String machineName,
			String ecCode,
			String machineRecipeName,
			String probeCard)throws CustomException
	{
		if ( ecCode.isEmpty())
		{
			ecCode = "*";
		}
		
		//String strSql = "SELECT T.MACHINERECIPENAME " +
        //      "  FROM TPEFOMPolicy T " +
        //      " WHERE     1 = 1 " +
        //      "       AND T.FACTORYNAME = :FACTORYNAME " +
        //      "       AND T.PRODUCTSPECNAME = :PRODUCTSPECNAME " +
        //      "       AND T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION " +
        //      "       AND T.ECCODE = :ECCODE " +
        //      "       AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME " +
        //      "       AND T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION " +
        //      "       AND T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME " +
        //      "       AND T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION " +
        //      "       AND T.MACHINENAME = :MACHINENAME " +
        //      "       AND T.MACHINERECIPENAME = :MACHINERECIPENAME " +
        //      "       AND T.PROBECARD = :PROBECARD " ;
		
		String strSql= StringUtil.EMPTY;
		strSql = strSql + "  SELECT P.MACHINERECIPENAME                                                                             \n";
		strSql = strSql + "    FROM TPEFOMPOLICY T                                                                                \n";
		strSql = strSql + "    INNER JOIN POSRECIPE P ON T.CONDITIONID  = P.CONDITIONID                                             \n";
		strSql = strSql + "   WHERE 1 = 1                                                                                         \n";
		strSql = strSql + "     AND ((T.FACTORYNAME = :FACTORYNAME) OR (T.FACTORYNAME = :STAR))                                     \n";
		strSql = strSql + "     AND ((T.PRODUCTSPECNAME = :PRODUCTSPECNAME) OR (T.PRODUCTSPECNAME = :STAR))                         \n";
		strSql = strSql + "     AND ((T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION) OR (T.PRODUCTSPECVERSION = :STAR))                \n";
		strSql = strSql + "     AND ((T.ECCODE = :ECCODE) OR (T.ECCODE = :STAR))                                                    \n";
		strSql = strSql + "     AND ((T.PROCESSFLOWNAME = :PROCESSFLOWNAME) OR (T.PROCESSFLOWNAME = :STAR))                         \n";
		strSql = strSql + "     AND ((T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION) OR (T.PROCESSFLOWVERSION = :STAR))                \n";
		strSql = strSql + "     AND ((T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME) OR (T.PROCESSOPERATIONNAME = :STAR))          \n";
		strSql = strSql + "     AND ((T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION) OR (T.PROCESSOPERATIONVERSION = :STAR)) \n";
		strSql = strSql + "     AND ((T.MACHINENAME = :MACHINENAME) OR (T.MACHINENAME = :STAR))                                     \n";
		strSql = strSql + "     AND P.MACHINERECIPENAME = :MACHINERECIPENAME                                                      \n";
		strSql = strSql + "     AND P.PROBECARD = :PROBECARD                                                                      \n";
		strSql = strSql + " ORDER BY DECODE (T.FACTORYNAME, :STAR, 9999, 0),                                                          \n";
		strSql = strSql + "          DECODE (T.PRODUCTSPECNAME, :STAR, 9999, 0),                                                      \n";
		strSql = strSql + "          DECODE (T.PRODUCTSPECVERSION, :STAR, 9999, 0),                                                   \n";
		strSql = strSql + "          DECODE (T.ECCODE, :STAR, 9999, 0),                                                               \n";
		strSql = strSql + "          DECODE (T.PROCESSFLOWNAME, :STAR, 9999, 0),                                                      \n";
		strSql = strSql + "          DECODE (T.PROCESSFLOWVERSION, :STAR, 9999, 0),                                                   \n";
		strSql = strSql + "          DECODE (T.PROCESSOPERATIONNAME, :STAR, 9999, 0),                                                 \n";
		strSql = strSql + "          DECODE (T.PROCESSOPERATIONVERSION, :STAR, 9999, 0),                                              \n";
		strSql = strSql + "          DECODE (T.MACHINENAME, :STAR, 9999, 0)                                                           \n";
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PRODUCTSPECNAME", productSpecName);
		bindMap.put("PRODUCTSPECVERSION", "00001");
		bindMap.put("ECCODE", ecCode);
		bindMap.put("PROCESSFLOWNAME", processFlowName);
		bindMap.put("PROCESSFLOWVERSION", "00001");
		bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		bindMap.put("PROCESSOPERATIONVERSION", "00001");
		bindMap.put("MACHINENAME", machineName);
		bindMap.put("MACHINERECIPENAME", machineRecipeName);
		bindMap.put("PROBECARD", probeCard);
		bindMap.put("STAR", "*");
		
		try
		{
			List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindMap);

			if(sqlResult !=null && sqlResult.size() <= 0)
			{
				throw new CustomException("POLICY-0015",factoryName,productSpecName,ecCode,processFlowName,processOperationName,machineName,machineRecipeName,probeCard);
			}
		}
		catch (NotFoundSignal ne) /* 20190108, hhlee, add */
		{
		    throw new CustomException("SYS-9999", "POSMachine", ne.getMessage());
		}
		catch (FrameworkErrorSignal de)
		{
			throw new CustomException("SYS-9999", "POSMachine", de.getMessage());
		}

	}
	
	
	//add by wghuang 20180527
	public static void checkDefinePhotoMaskAndProbeCardByTPEFOMPolicy(
			String factoryName, 
			String productSpecName, 
			String processFlowName, 
			String processOperationName, 
			String machineName,
			String ecCode,
			String machineRecipeName,
			String type)throws CustomException
	{
		if ( ecCode.isEmpty())
		{
			ecCode = "*";
		}
		
		//String strSql = "SELECT T.MACHINERECIPENAME " +
        //      "  FROM TPEFOMPolicy T " +
        //      " WHERE     1 = 1 " +
        //      "       AND T.FACTORYNAME = :FACTORYNAME " +
        //      "       AND T.PRODUCTSPECNAME = :PRODUCTSPECNAME " +
        //      "       AND T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION " +
        //      "       AND T.ECCODE = :ECCODE " +
        //      "       AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME " +
        //      "       AND T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION " +
        //      "       AND T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME " +
        //      "       AND T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION " +
        //      "       AND T.MACHINENAME = :MACHINENAME " +
        //      "       AND T.MACHINERECIPENAME = :MACHINERECIPENAME " +
        //      "       AND T.PROBECARD = :PROBECARD " ;
		
		String strSql= StringUtil.EMPTY;
		strSql = strSql + "  SELECT P.PHOTOMASK, P.PROBECARD                                                                            \n";
		strSql = strSql + "    FROM TPEFOMPOLICY T                                                                                \n";
		strSql = strSql + "    INNER JOIN POSRECIPE P ON T.CONDITIONID  = P.CONDITIONID                                           \n";
		strSql = strSql + "   WHERE 1 = 1                                                                                         \n";
		strSql = strSql + "     AND ((T.FACTORYNAME = :FACTORYNAME) OR (T.FACTORYNAME = :STAR))                                     \n";
		strSql = strSql + "     AND ((T.PRODUCTSPECNAME = :PRODUCTSPECNAME) OR (T.PRODUCTSPECNAME = :STAR))                         \n";
		strSql = strSql + "     AND ((T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION) OR (T.PRODUCTSPECVERSION = :STAR))                \n";
		strSql = strSql + "     AND ((T.ECCODE = :ECCODE) OR (T.ECCODE = :STAR))                                                    \n";
		strSql = strSql + "     AND ((T.PROCESSFLOWNAME = :PROCESSFLOWNAME) OR (T.PROCESSFLOWNAME = :STAR))                         \n";
		strSql = strSql + "     AND ((T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION) OR (T.PROCESSFLOWVERSION = :STAR))                \n";
		strSql = strSql + "     AND ((T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME) OR (T.PROCESSOPERATIONNAME = :STAR))          \n";
		strSql = strSql + "     AND ((T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION) OR (T.PROCESSOPERATIONVERSION = :STAR)) \n";
		strSql = strSql + "     AND ((T.MACHINENAME = :MACHINENAME) OR (T.MACHINENAME = :STAR))                                     \n";
		strSql = strSql + "     AND P.MACHINERECIPENAME = :MACHINERECIPENAME                                                      \n";
		strSql = strSql + " ORDER BY DECODE (T.FACTORYNAME, :STAR, 9999, 0),                                                          \n";
		strSql = strSql + "          DECODE (T.PRODUCTSPECNAME, :STAR, 9999, 0),                                                      \n";
		strSql = strSql + "          DECODE (T.PRODUCTSPECVERSION, :STAR, 9999, 0),                                                   \n";
		strSql = strSql + "          DECODE (T.ECCODE, :STAR, 9999, 0),                                                               \n";
		strSql = strSql + "          DECODE (T.PROCESSFLOWNAME, :STAR, 9999, 0),                                                      \n";
		strSql = strSql + "          DECODE (T.PROCESSFLOWVERSION, :STAR, 9999, 0),                                                   \n";
		strSql = strSql + "          DECODE (T.PROCESSOPERATIONNAME, :STAR, 9999, 0),                                                 \n";
		strSql = strSql + "          DECODE (T.PROCESSOPERATIONVERSION, :STAR, 9999, 0),                                              \n";
		strSql = strSql + "          DECODE (T.MACHINENAME, :STAR, 9999, 0)                                                           \n";
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PRODUCTSPECNAME", productSpecName);
		bindMap.put("PRODUCTSPECVERSION", "00001");
		bindMap.put("ECCODE", ecCode);
		bindMap.put("PROCESSFLOWNAME", processFlowName);
		bindMap.put("PROCESSFLOWVERSION", "00001");
		bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		bindMap.put("PROCESSOPERATIONVERSION", "00001");
		bindMap.put("MACHINENAME", machineName);
		bindMap.put("MACHINERECIPENAME", machineRecipeName);
		bindMap.put("STAR", "*");
		
		try
		{
			List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindMap);

			if(sqlResult !=null && sqlResult.size() <= 0)
			{
				if(StringUtils.equals(type, "PHOTOMASK"))
				{
					throw new CustomException("POLICY-0016",factoryName,productSpecName,ecCode,processFlowName,processOperationName,machineName,machineRecipeName);
				}
				else
				{
					throw new CustomException("POLICY-0028",factoryName,productSpecName,ecCode,processFlowName,processOperationName,machineName,machineRecipeName);
				}

			}
			else
			{
				if(StringUtils.equals(type, "PHOTOMASK"))
				{
					String photoMask = (String)sqlResult.get(0).get("PHOTOMASK");
					
					if(photoMask==null)
						throw new CustomException("POLICY-0026",factoryName,productSpecName,ecCode,processFlowName,processOperationName,machineName,machineRecipeName);
				}
				else
				{
					String probeCard = (String)sqlResult.get(0).get("PROBECARD");
					
					if(probeCard==null)
						throw new CustomException("POLICY-0029",factoryName,productSpecName,ecCode,processFlowName,processOperationName,machineName,machineRecipeName);
				}
			}
		}
		catch (FrameworkErrorSignal de)
		{
			throw new CustomException("SYS-9999", "POSMachine", de.getMessage());
		}

	}
	
	//add by wghuang 20180527
	/* 20190514, hhlee, add validation, PhotoMaskName RecipeParaMeter(Mask Name) Validation */
	//public static void checkMachinePhotoMaskByTPEFOMPolicy(
	public static String checkMachinePhotoMaskByTPEFOMPolicy(String factoryName, String productSpecName, String processFlowName, 
	        String processOperationName, String machineName, String ecCode, String machineRecipeName, List<Durable> photoMaskList)throws CustomException
	{
		if ( ecCode.isEmpty())
		{
			ecCode = "*";
		}

		Map<String, Object> bindMap = new HashMap<String, Object>();

		/* 20190514, hhlee, add validation, PhotoMaskName RecipeParaMeter(Mask Name) Validation ==>> */
		String photoMaskName = StringUtil.EMPTY;
		/* <<== 20190514, hhlee, add validation, PhotoMaskName RecipeParaMeter(Mask Name) Validation */
		
		//String strSql = "SELECT T.MACHINERECIPENAME " +
		//          "  FROM TPEFOMPolicy T " +
		//          " WHERE     1 = 1 " +
		//          "       AND T.FACTORYNAME = :FACTORYNAME " +
		//          "       AND T.PRODUCTSPECNAME = :PRODUCTSPECNAME " +
		//          "       AND T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION " +
		//          "       AND T.ECCODE = :ECCODE " +
		//          "       AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME " +
		//          "       AND T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION " +
		//          "       AND T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME " +
		//          "       AND T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION " +
		//          "       AND T.MACHINENAME = :MACHINENAME " +
		//          "       AND T.MACHINERECIPENAME = :MACHINERECIPENAME " +
		//          "       AND T.PHOTOMASK = :PHOTOMASK " ;

		String strSql= StringUtil.EMPTY;
		strSql = strSql + "  SELECT P.PHOTOMASK                                                                                \n";
		strSql = strSql + "    FROM TPEFOMPOLICY T                                                                                \n";
		strSql = strSql + "     INNER JOIN POSRECIPE P ON T.CONDITIONID  = P.CONDITIONID                                           \n";
		strSql = strSql + "   WHERE 1 = 1                                                                                         \n";
		strSql = strSql + "     AND ((T.FACTORYNAME = :FACTORYNAME) OR (T.FACTORYNAME = :STAR))                                     \n";
		strSql = strSql + "     AND ((T.PRODUCTSPECNAME = :PRODUCTSPECNAME) OR (T.PRODUCTSPECNAME = :STAR))                         \n";
		strSql = strSql + "     AND ((T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION) OR (T.PRODUCTSPECVERSION = :STAR))                \n";
		strSql = strSql + "     AND ((T.ECCODE = :ECCODE) OR (T.ECCODE = :STAR))                                                    \n";
		strSql = strSql + "     AND ((T.PROCESSFLOWNAME = :PROCESSFLOWNAME) OR (T.PROCESSFLOWNAME = :STAR))                         \n";
		strSql = strSql + "     AND ((T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION) OR (T.PROCESSFLOWVERSION = :STAR))                \n";
		strSql = strSql + "     AND ((T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME) OR (T.PROCESSOPERATIONNAME = :STAR))          \n";
		strSql = strSql + "     AND ((T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION) OR (T.PROCESSOPERATIONVERSION = :STAR)) \n";
		strSql = strSql + "     AND ((T.MACHINENAME = :MACHINENAME) OR (T.MACHINENAME = :STAR))                                     \n";
		strSql = strSql + "     AND P.MACHINERECIPENAME = :MACHINERECIPENAME                                                      \n";
		strSql = strSql + " ORDER BY DECODE (FACTORYNAME, :STAR, 9999, 0),                                                          \n";
		strSql = strSql + "          DECODE (PRODUCTSPECNAME, :STAR, 9999, 0),                                                      \n";
		strSql = strSql + "          DECODE (PRODUCTSPECVERSION, :STAR, 9999, 0),                                                   \n";
		strSql = strSql + "          DECODE (ECCODE, :STAR, 9999, 0),                                                               \n";
		strSql = strSql + "          DECODE (PROCESSFLOWNAME, :STAR, 9999, 0),                                                      \n";
		strSql = strSql + "          DECODE (PROCESSFLOWVERSION, :STAR, 9999, 0),                                                   \n";
		strSql = strSql + "          DECODE (PROCESSOPERATIONNAME, :STAR, 9999, 0),                                                 \n";
		strSql = strSql + "          DECODE (PROCESSOPERATIONVERSION, :STAR, 9999, 0),                                              \n";
		strSql = strSql + "          DECODE (MACHINENAME, :STAR, 9999, 0)                                                           \n";

		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PRODUCTSPECNAME", productSpecName);
		bindMap.put("PRODUCTSPECVERSION", "00001");
		bindMap.put("ECCODE", ecCode);
		bindMap.put("PROCESSFLOWNAME", processFlowName);
		bindMap.put("PROCESSFLOWVERSION", "00001");
		bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		bindMap.put("PROCESSOPERATIONVERSION", "00001");
		bindMap.put("MACHINENAME", machineName);
		bindMap.put("MACHINERECIPENAME", machineRecipeName);
		bindMap.put("STAR", "*");


		List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindMap);

		if(sqlResult == null || sqlResult.size() <= 0)
			throw new CustomException("POLICY-0016",factoryName,productSpecName,ecCode,processFlowName,processOperationName,machineName,machineRecipeName);
		else
		{
			String photoMask = (String)sqlResult.get(0).get("PHOTOMASK");

			if(photoMask==null)
				throw new CustomException("POLICY-0026",factoryName,productSpecName,ecCode,processFlowName,processOperationName,machineName,machineRecipeName);
			else
			{
				boolean maskExistFlag = false;

				// 2019.03.25_hsryu_Modify Logic. Mantis 0003193. if TPEFOM PhotoMask is 'NONE', same 'NONE1','NONE2','NONE3'.....
				if(StringUtils.equals(photoMask, GenericServiceProxy.getConstantMap().VIRTUAL_PHOTOMASKNAME)){
					for(int i=0; i<photoMaskList.size(); i++)
					{
						String mountMaskName = photoMaskList.get(i).getKey().getDurableName();
						if(mountMaskName.contains(photoMask)){
							maskExistFlag = true;
							break;
						}
					}
				}
				else{
					for(int i=0; i<photoMaskList.size(); i++)
					{
						String mountMaskName = photoMaskList.get(i).getKey().getDurableName();
						if(StringUtils.equals(mountMaskName, photoMask))
						{
							maskExistFlag = true;
							break;
						}
					}
				}

				if(!maskExistFlag)
				{
					throw new CustomException("POLICY-0027",factoryName,productSpecName,ecCode,processFlowName,processOperationName,machineName,machineRecipeName,photoMask);
				}					
			}
			
			/* 20190514, hhlee, add validation, PhotoMaskName RecipeParaMeter(Mask Name) Validation ==>> */
			//if(!StringUtils.equals(photoMask, GenericServiceProxy.getConstantMap().VIRTUAL_PHOTOMASKNAME))
            //{
            //    MachineSpec machineSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);
            //    if(StringUtil.equals(machineSpecData.getUdfs().get("RMSFLAG").toString(), GenericServiceProxy.getConstantMap().FLAG_Y))
            //    {
            //        
            //    }
            //}			
			photoMaskName = photoMask;
            /* <<== 20190514, hhlee, add validation, PhotoMaskName RecipeParaMeter(Mask Name) Validation */
		}	
		return photoMaskName;
	}
	
	/**
	 * get available machine in any group
	 * @author swcho
	 * @since 2015.03.05
	 * @param factoryName
	 * @param machineGroupName
	 * @return
	 * @throws CustomException
	 */
	public static List<ListOrderedMap> getAvailableMachine(String factoryName, String machineGroupName)
		throws CustomException
	{
		StringBuffer sqlBuffer = new StringBuffer("")
									.append(" SELECT P.machineName, P.rollType ").append(NEWLINE)
									.append("   FROM TGPolicy C, POSMachine P ").append(NEWLINE)
									.append("  WHERE C.conditionId = P.conditionId ").append(NEWLINE)
									.append("   AND C.factoryName= ? ").append(NEWLINE)
									.append("  	AND C.machineGroupName = ? ").append(NEWLINE);
		
		Object[] bindSet = new String[]{factoryName, machineGroupName};
		
		try
		{
//			List<ListOrderedMap> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bindSet); 
			List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bindSet);
			
			if(sqlResult.size() < 1)
				throw new CustomException("SYS-9001", "POSMachine");
			else 
				return sqlResult;
		}
		catch (FrameworkErrorSignal de)
		{
			throw new CustomException("SYS-9999", "POSMachine", de.getMessage());
		}
	}
	
	/**
	 * get available machine in any group
	 * @author xzquan
	 * @since 2015.07.23
	 * @param factoryName
	 * @param machineGroupName
	 * @return
	 * @throws CustomException
	 */
	public static List<ListOrderedMap> getAvailableMachineByTPFO(String factoryName, String productSpecname, String processFlowName,
			String processOperationName, String machineGroupName)
		throws CustomException
	{
		StringBuffer sqlBuffer = new StringBuffer("")
									.append(" SELECT P.MACHINENAME, P.ROLLTYPE ").append(NEWLINE)
									.append("   FROM TPFOPOLICY C, POSMACHINE P ").append(NEWLINE)
									.append(" WHERE     C.CONDITIONID = P.CONDITIONID ").append(NEWLINE)
									.append("       AND C.FACTORYNAME = ? ").append(NEWLINE)
									.append("       AND C.PRODUCTSPECNAME = ? ").append(NEWLINE)
									.append("       AND C.PROCESSFLOWNAME = ? ").append(NEWLINE)
									.append("       AND C.PROCESSOPERATIONNAME = ? ").append(NEWLINE)
									.append("       AND C.MACHINEGROUPNAME = ? ").append(NEWLINE);
		
		Object[] bindSet = new String[]{factoryName, productSpecname, processFlowName, processOperationName, machineGroupName};
		
		try
		{
			//List<ListOrderedMap> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bindSet);
			List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bindSet);
			
			if(sqlResult.size() < 1)
				throw new CustomException("SYS-9001", "POSMachine");
			else 
				return sqlResult;
		}
		catch (FrameworkErrorSignal de)
		{
			throw new CustomException("SYS-9999", "POSMachine", de.getMessage());
		}
	}

	/*
	* Name : getProductSample
	* Desc : This function is getProductSample
	* Author : AIM Systems, Inc
	* Date : 2011.01.19
	*/
	public static String getProductSample(String factoryName, 
			   							  String productSpecName, 
			   							  String processFlowName, 
			   							  String processOperationName)
	{
		String sqlStmt =  " SELECT SAMPLEPOSITION " + NEWLINE
						+ "   FROM POSPRODUCTSAMPLE " + NEWLINE
						+ "  WHERE CONDITIONID = " + NEWLINE
						+ "      (SELECT CONDITIONID " + NEWLINE
						+ "   		FROM TPFOPOLICY " + NEWLINE
						+ "  		WHERE FACTORYNAME = ? " + NEWLINE
						+ "  		  AND PRODUCTSPECNAME = ? " + NEWLINE
						+ "  		  AND PRODUCTSPECVERSION = '00001' " + NEWLINE
						+ "  		  AND PROCESSFLOWNAME = ? " + NEWLINE
						+ "   		  AND PROCESSFLOWVERSION = '00001' " + NEWLINE
						+ "   		  AND PROCESSOPERATIONNAME = ? " + NEWLINE
						+ "   		  AND PROCESSOPERATIONVERSION = '00001' )" + NEWLINE;
		
		Object[] bindSet = new String[]{factoryName, productSpecName, processFlowName, processOperationName};
		
		String[][] sqlResult = greenFrameServiceProxy.getSqlTemplate().queryForStringArray(sqlStmt, bindSet);
		
		if(sqlResult.length > 0 && sqlResult[0].length > 0)
		return sqlResult[0][0];
		else 
		return "";
	}

	/*
	* Name : getDCSpecPrefix
	* Desc : This function is getDCSpecPrefix
	* Author : AIM Systems, Inc
	* Date : 2011.01.19
	*/
	public static String[][] getDCSpecPrefix(String factoryName, 
									      	 String productSpecName, 
									      	 String processFlowName, 
									      	 String processOperationName, 
									      	 String machineName)
	{
		String sqlStmt =  " SELECT ITEMPREFIX, SITEPREFIX " + NEWLINE
						+ "   FROM POSDCSPECPREFIX " + NEWLINE
						+ "  WHERE CONDITIONID = " + NEWLINE
						+ "      (SELECT CONDITIONID " + NEWLINE
						+ "   		FROM TPFOMPOLICY " + NEWLINE
						+ "  		WHERE FACTORYNAME = ? " + NEWLINE
						+ "  		  AND PRODUCTSPECNAME = ? " + NEWLINE
						+ "  		  AND PRODUCTSPECVERSION = '00001' " + NEWLINE
						+ "  		  AND PROCESSFLOWNAME = ? " + NEWLINE
						+ "   		  AND PROCESSFLOWVERSION = '00001' " + NEWLINE
						+ "   		  AND PROCESSOPERATIONNAME = ? " + NEWLINE
						+ "   		  AND PROCESSOPERATIONVERSION = '00001' " + NEWLINE
						+ "  		  AND MACHINENAME = ?) " + NEWLINE;
		
		Object[] bindSet = new String[]{factoryName, productSpecName, processFlowName, processOperationName, machineName};
		
		String[][] sqlResult = greenFrameServiceProxy.getSqlTemplate().queryForStringArray(sqlStmt, bindSet);
		
		if(sqlResult.length > 0 && sqlResult[0].length > 0)
			return sqlResult;
		else 
			return null;
	}
	
	/**
	 * get defined OP mode of destined Machine
	 * @author swcho
	 * @param factoryName
	 * @param machineName
	 * @param operationMode
	 * @return
	 * @throws CustomException
	 */
	public String convertOperationModeCode(String factoryName, String machineName, String operationMode)
		throws CustomException
	{
		String result = "";
		
		try
		{
			StringBuffer queryBuffer = new StringBuffer();
			queryBuffer.append("SELECT P.factoryName, P.machineName, P.conditionId," + "\n");
			queryBuffer.append("       O.operationMode, O.operationValue" + "\n");
			queryBuffer.append("    FROM TMPolicy P, POSOperationMode O" + "\n");
			queryBuffer.append(" WHERE P.conditionId = O.conditionId" + "\n");
			queryBuffer.append("    AND P.factoryName = ?" + "\n");
			queryBuffer.append("    AND P.machineName = ?" + "\n");
			queryBuffer.append("    AND O.operationValue = ?" + "\n");
			
			@SuppressWarnings("unchecked")
//			List<ListOrderedMap> resultList =
//				kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(),
//																		new Object[] {factoryName, machineName, operationMode});
			
			List<ListOrderedMap> resultList =
			GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(),
					new Object[] {factoryName, machineName, operationMode});
			
			//select by key
			for (ListOrderedMap row : resultList)
			{
				result = row.get("OPERATIONMODE").toString();
				break;
			}
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("MACHINE-0001", operationMode, machineName);
		}
		
		if (result.isEmpty())
			throw new CustomException("MACHINE-0001", operationMode, machineName);
		
		return result;
	}
	
	/**
	 * get possible Machine with Lot
	 * @author swcho
	 * @since 2013.10.21
	 * @param lotData
	 * @return
	 */
	public static String getAvailableMachine(Lot lotData)
	{
		logger.info("getAvailableMachine started....");
		
		String sqlStmt =  " SELECT P.MACHINENAME " + NEWLINE 
							+ "   FROM POSMACHINE P, TPFOPOLICY T " + NEWLINE
							+ "  WHERE P.CONDITIONID = T.CONDITIONID " + NEWLINE
							+ "	   AND T.FACTORYNAME= ? " + NEWLINE
							+ "    AND T.PRODUCTSPECNAME = ? " + NEWLINE
							+ "    AND T.PRODUCTSPECVERSION = ? " + NEWLINE
							+ "    AND T.PROCESSFLOWNAME = ? " + NEWLINE
							+ "    AND T.PROCESSFLOWVERSION = ? " + NEWLINE
							+ "    AND T.PROCESSOPERATIONNAME= ? " + NEWLINE
							+ "    AND T.PROCESSOPERATIONVERSION = ?";
	
		Object[] bindSet = new String[]{lotData.getFactoryName(),
										lotData.getProductSpecName(),
										"00001",
										lotData.getProcessFlowName(),
										"00001",
										lotData.getProcessOperationName(),
										"00001"};
//		List<ListOrderedMap> resultList = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(
//											sqlStmt.toString(), bindSet);
		List<ListOrderedMap> resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmt.toString(), bindSet);
		
		for (ListOrderedMap row : resultList)
		{
			return row.get("MACHINENAME").toString();
		}
		
		return "";
	}
	
	/**
	 * decide where is next
	 * 160310 by swcho : modified
	 * @author swcho
	 * @since 2014.06.25
	 * @param lotData
	 * @param targetFactoryName
	 * @param targetFlowName
	 * @param targetOperationName
	 * @param lotGrade
	 * @param condition
	 * @return
	 * @throws CustomException
	 */
	public static String getWhereNext(Lot lotData,
									  String targetFactoryName, String targetFlowName, String targetOperationName,
									  String condition, String value, String reworkFlag)
		throws CustomException
	{
		//hard coding must be changed
		if (reworkFlag.equals(GenericServiceProxy.getConstantMap().Flag_Y))
			condition = "Rework";
		
		String destinationNodeStack = "";
		String toFactoryName = "";
		String toFlowName = "";
		String toOperationName = "";
		String returnFlowName = "";
		String returnOperationName = "";
		
		try
		{
			//160310 by swcho : get flexible route
			//List<ListOrderedMap> alterPathList = getAlterProcessOperation(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessOperationName());
			List<ListOrderedMap> alterPathList = getAlterProcessOperation(targetFactoryName, targetFlowName, targetOperationName);
			
			for (ListOrderedMap alterPath : alterPathList)
			{
				String conditionName = CommonUtil.getValue(alterPath, "CONDITIONNAME");
				String conditionValue = CommonUtil.getValue(alterPath, "CONDITIONVALUE");
				
				if (condition.equalsIgnoreCase(conditionName) && value.equalsIgnoreCase(conditionValue))
				{
					toFactoryName = CommonUtil.getValue(alterPath, "FACTORYNAME");
					toFlowName = CommonUtil.getValue(alterPath, "TOPROCESSFLOWNAME");
					toOperationName = CommonUtil.getValue(alterPath, "TOPROCESSOPERATIONNAME");
					returnFlowName = CommonUtil.getValue(alterPath, "RETURNPROCESSFLOWNAME");
					returnOperationName = CommonUtil.getValue(alterPath, "RETURNOPERATIONNAME");
					
					//return info setup into track-out Lot
					lotData.getUdfs().put("RETURNFLOWNAME", returnFlowName);
					lotData.getUdfs().put("RETURNOPERATIONNAME", returnOperationName);
					
					//destination is only one
					break;
				}
			}
			
			if (!StringUtil.isEmpty(toFactoryName) && !StringUtil.isEmpty(toFlowName) && !StringUtil.isEmpty(toOperationName))
				destinationNodeStack = NodeStack.getNodeID(toFactoryName, toFlowName, toOperationName);
			
			//return destination setup by specific
			/*if (condition.equals("Rework"))
			{
				String currentNodeId = NodeStack.getNodeID(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessOperationName());
				destinationNodeStack = new StringBuffer(ProcessFlowServiceProxy.getProcessFlowService().getNextNode(currentNodeId, "Normal", "").getKey().getNodeId())
											.append(".").append(destinationNodeStack).toString();
			}*/
			
			if (!StringUtil.isEmpty(returnFlowName) && !StringUtil.isEmpty(returnOperationName))
			{
				String returnNodeId = NodeStack.getNodeID(lotData.getFactoryName(), returnFlowName, returnOperationName);
				destinationNodeStack = new StringBuffer(returnNodeId).append(".").append(destinationNodeStack).toString();
			}
			
			
		}
		catch (Exception ex)
		{
			//TK OUT have to be successful
			/*if (ex instanceof CustomException)
				throw (CustomException) ex;
			else if (ex.getCause() instanceof CustomException)
				throw (CustomException) ex.getCause();
			else
				throw new CustomException("Cannot find next ProcessOperation");*/
			
			logger.error("Cannot find next ProcessOperation");
			//default is according to route designer
			destinationNodeStack = "";
		}
		
		return destinationNodeStack;
	}
	
	/**
	 * find alter-path for that step
	 * @author swcho
	 * @since 2014.06.25
	 * @param factoryName
	 * @param processFlowName
	 * @param processOperationName
	 * @return
	 * @throws CustomException
	 */
	public static List<ListOrderedMap> getAlterProcessOperation(String factoryName, String processFlowName, String processOperationName)
		throws CustomException
	{
		StringBuffer queryBuffer = new StringBuffer()
				.append(" SELECT C.factoryName, C.processFlowName, C.processOperationName, \n")
				.append("        P.toProcessFlowName, P.toProcessOperationName, 		   \n")
				.append("        P.returnProcessFlowName, P.returnOperationName,		   \n")
				.append("        P.conditionName, P.conditionValue, P.reworkFlag,	       \n")
				.append(" 	     P.reworkCount	     								   \n")
				.append(" FROM POSAlterProcessOperation P, TPEFOPOLICY C                     \n")
				.append(" WHERE C.conditionId = P.conditionId                              \n")
				.append("     AND C.factoryName = :FACTORYNAME                             \n")
				.append("     AND C.processFlowName = :PROCESSFLOWNAME                     \n")
				.append("     AND C.processFlowVersion = :PROCESSFLOWVERSION               \n")
				.append("     AND C.processOperationName = :PROCESSOPERATIONNAME           \n")
				.append("     AND C.processOperationVersion = :PROCESSOPERATIONVERSION     \n");
		
		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PROCESSFLOWNAME", processFlowName);
		bindMap.put("PROCESSFLOWVERSION", "00001");
		bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		bindMap.put("PROCESSOPERATIONVERSION", "00001");
		
		try
		{
			//List<ListOrderedMap> result = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);
			
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);
			
			return result;
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "POSPolicy", fe.getMessage());
		}
	}
	
	/**
	 * get POS Queue time spec
	 * 141104 by swcho : dropped columns reflected
	 * 141216 by swcho : modified condition reflected
	 * @author swcho
	 * @since 2014.06.18
	 * @param factoryName
	 * @param productSpecName
	 * @param processFlowName
	 * @param processOperationName
	 * @return
	 * @throws CustomException
	 */
	public static List<ListOrderedMap> getQTimeSpec(String factoryName, String productSpecName, String processFlowName, String processOperationName)
		throws CustomException
	{
		StringBuffer queryBuffer = new StringBuffer()
			//.append("SELECT C.factoryName, C.productSpecName, C.processFlowName, C.processOperationName,\n")
			.append("SELECT C.factoryName, C.processFlowName, C.processOperationName,\n")
			.append("       P.toProcessOperationName, P.warningDurationLimit, P.interlockDurationLimit, P.TOPROCESSFLOWNAME   \n")
			//.append("FROM POSQueueTime P, TPFOPolicy C                                                  \n")
			.append("FROM POSQueueTime P, TPFOPolicy C                                                  \n")
			.append("WHERE C.conditionId = P.conditionId                                                \n")
			.append("    AND C.factoryName = :FACTORYNAME                                               \n")
			.append("    AND C.productSpecName = :PRODUCTSPECNAME                                       \n")
			.append("    AND C.productSpecVersion = :PRODUCTSPECVERSION                                 \n")
			.append("    AND C.processFlowName = :PROCESSFLOWNAME                                       \n")
			.append("    AND C.processFlowVersion = :PROCESSFLOWVERSION                                 \n")
			.append("    AND C.processOperationName = :PROCESSOPERATIONNAME                             \n")
			.append("    AND C.processOperationVersion = :PROCESSOPERATIONVERSION                       \n");
		
		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PRODUCTSPECNAME", productSpecName);
		bindMap.put("PRODUCTSPECVERSION", "00001");
		bindMap.put("PROCESSFLOWNAME", processFlowName);
		bindMap.put("PROCESSFLOWVERSION", "00001");
		bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		bindMap.put("PROCESSOPERATIONVERSION", "00001");
		
		try
		{
//			List<ListOrderedMap> result = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);
			
			return result;
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "POSPolicy", fe.getMessage());
		}
	}
	
	/**
	 * get next destination step of Lot
	 * @author swcho
	 * @since 2015.02.05
	 * @param lotData
	 * @return
	 * @throws CustomException
	 */
	public static Node getNextOperation(Lot lotData)
		throws CustomException
	{
		String[] nodeStackArray = StringUtil.split(lotData.getNodeStack(), ".");
		
		Node nextNode = null;
		boolean isCurrent = true;
		
		for (int idx=nodeStackArray.length; idx > 0; idx--)
		{
			if (isCurrent)
			{
				try
				{
					//though which is successful, first loop must be descreminated
					isCurrent = false;
					
					nextNode = getNextNode(nodeStackArray[idx - 1]);
					
					if (StringUtil.isEmpty(nextNode.getNodeAttribute1()) || StringUtil.isEmpty(nextNode.getProcessFlowName()))
						throw new Exception();
					
					
					break;
				}
				catch (Exception ex)
				{
					logger.debug("It is last node");
				}
			}
			else
			{
				nextNode = ProcessFlowServiceProxy.getNodeService().getNode(nodeStackArray[idx - 1]);
				break;
			}
		}
		
		if (nextNode != null)
			return nextNode;
		else
			throw new CustomException("", "");
	}
	
	/**
	 * get next node of certain node
	 * @author swcho
	 * @since 2015.02.05
	 * @param currentNodeStack
	 * @return
	 * @throws Exception
	 */
	public static Node getNextNode(String currentNodeStack)
		throws Exception
	{
		Node nextNode = ProcessFlowServiceProxy.getProcessFlowService().getNextNode(currentNodeStack, "Normal", "");
		
		return nextNode;
	}
	
	/**
	 * get POS Queue time spec
	 * 141104 by swcho : dropped columns reflected
	 * 141216 by swcho : modified condition reflected
	 * @author hwlee89
	 * @since 2014.06.18
	 * @param factoryName
	 * @param productSpecName
	 * @param processFlowName
	 * @param processOperationName
	 * @return
	 * @throws CustomException
	 */
	public static List<ListOrderedMap> getPOSQTimeSpec(String productSpecName,String processOperationName, String toProcessOperationName, String toProcessFlowName)
		throws CustomException
	{
		StringBuffer queryBuffer = new StringBuffer()
		.append("SELECT C.FACTORYNAME, \n")
		.append("       C.PROCESSFLOWNAME, \n")
		.append("       C.PROCESSOPERATIONNAME, \n")
		.append("       P.TOPROCESSOPERATIONNAME, \n")
		.append("       P.WARNINGDURATIONLIMIT, \n")
		.append("       P.INTERLOCKDURATIONLIMIT, \n")
		.append("       P.TOPROCESSFLOWNAME, \n")
		.append("       P.ALARMCODE, \n")
		.append("       P.QUEUETYPE \n")
		.append("       FROM POSQUEUETIME P, TPFOPOLICY C \n")
		.append("       WHERE     C.CONDITIONID = P.CONDITIONID \n")
		//2017-07-31 modify by yuhonghao  add productSpec column
		.append("       AND C.PRODUCTSPECNAME = :PRODUCTSPECNAME \n")
	    .append("       AND C.PRODUCTSPECVERSION = :PRODUCTSPECVERSION \n")
		.append("       AND C.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME \n")
		.append("       AND C.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION \n")
		.append("       AND P.TOPROCESSOPERATIONNAME = :TOPROCESSOPERATIONNAME \n")
		.append("       AND P.TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME \n");
		
		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("PRODUCTSPECNAME", productSpecName);
		bindMap.put("PRODUCTSPECVERSION", "00001");
		bindMap.put("TOPROCESSFLOWNAME", toProcessFlowName);
		bindMap.put("PROCESSFLOWVERSION", "00001");
		bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		bindMap.put("PROCESSOPERATIONVERSION", "00001");
		bindMap.put("TOPROCESSOPERATIONNAME", toProcessOperationName);
		
		try
		{
//			List<ListOrderedMap> result = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);
			
			return result;
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "POSPolicy", fe.getMessage());
		}
	}
	
	// --COMMENT
	// 2016.02.23 LEE HYEON WOO
	public static String getMachineRecipeName(String factoryName, String productSpecName, String processOperationName, String machineName)
	{
		String sqlStmt =  " SELECT MACHINERECIPENAME " + NEWLINE
		                + "   FROM POSMACHINERECIPE " + NEWLINE
		                + "  WHERE CONDITIONID = " + NEWLINE
			            + "      ( SELECT CONDITIONID " + NEWLINE
		                + "          FROM TPOMPOLICY " + NEWLINE
		                + "         WHERE FACTORYNAME = ? " + NEWLINE
		                + "           AND PRODUCTSPECNAME = ? " + NEWLINE
		                + "           AND PRODUCTSPECVERSION = '00001' " + NEWLINE
		                + "           AND PROCESSOPERATIONNAME = ? " + NEWLINE
		                + "           AND PROCESSOPERATIONVERSION = '00001' " + NEWLINE
		                + "           AND MACHINENAME = ?) " + NEWLINE;
		
		Object[] bindSet = new String[]{factoryName, productSpecName, processOperationName, machineName};
		String[][] sqlResult = greenFrameServiceProxy.getSqlTemplate().queryForStringArray(sqlStmt, bindSet);
		
		if(sqlResult.length > 0 && sqlResult[0].length > 0)
			return sqlResult[0][0];
		else 
			return "";
	}
	
	/**
	 * validate cross type in factory relation
	 * @author swcho
	 * @since 2016.07.12
	 * @param lotName
	 * @param factoryName
	 * @param productSpecName
	 * @param targetFactoryName
	 * @param targetProductSpecName
	 * @throws CustomException
	 */
	public static void validateCrossFactory(String lotName, String factoryName, String productSpecName, String targetFactoryName, String targetProductSpecName)
		throws CustomException
	{
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("SELECT C.factoryName, C.productSpecName, P.toFactoryName,").append("\n")
					.append("       P.toProductSpecName, P.shipUnit, P.jobType").append("\n")
					.append(" FROM TPPolicy C, POSFactoryRelation P").append("\n")
					.append(" WHERE C.conditionId = P.conditionId").append("\n")
					.append("  AND jobType = ?").append("\n")
					.append("  AND C.factoryName = ?").append("\n")
					.append("  AND C.productSpecName = ?").append("\n")
					.append("  AND P.toFactoryName = ?").append("\n")
					.append("  AND P.toProductSpecName = ?").append("\n");
		
		Object[] bindArray = new Object[] {"Cross", factoryName, productSpecName, targetFactoryName, targetProductSpecName};
		
		try
		{
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuilder.toString(), bindArray);
			
			if (result.size() < 1)
				throw new CustomException("LOT-3001", lotName, factoryName, productSpecName, targetFactoryName, targetProductSpecName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "POSFactoryRelation", fe.getMessage());
		}
	}
	
	/**
	 * get slot reservation by TFOM
	 * @author swcho
	 * @since 2016.08.01
	 * @param factoryName
	 * @param processFlowName
	 * @param processOperationName
	 * @param machineName
	 * @param toProcessOperationName
	 * @param position
	 * @return
	 * @throws CustomException
	 */
	public static ListOrderedMap getSlotReservationTFOM(String factoryName, String processFlowName, String processOperationName, String machineName,
														String toProcessOperationName, String position)
		throws CustomException
	{		
		StringBuffer sqlBuffer = new StringBuffer("")
								.append(" SELECT PP.toProcessOperationName, PP.position, PP.MACHINERECIPENAME").append(NEWLINE)
								.append("     FROM TFOMPOLICY TFOM, POSPOSITION PP ").append(NEWLINE)
								.append("  WHERE 1=1 ").append(NEWLINE)
								.append("	 AND TFOM.CONDITIONID = PP.CONDITIONID ").append(NEWLINE)
								.append("    AND TFOM.FACTORYNAME = ? ").append(NEWLINE)
								.append("    AND TFOM.PROCESSFLOWNAME = ? ").append(NEWLINE)
								.append("    AND TFOM.PROCESSOPERATIONNAME = ? ").append(NEWLINE)
								.append("    AND TFOM.MACHINENAME = ? ").append(NEWLINE)
								.append("    AND PP.TOPROCESSOPERATIONNAME = ? ").append(NEWLINE)
								.append("    AND PP.POSITION = ?").append(NEWLINE)
								.append("");
		
		String sqlStmt = sqlBuffer.toString();
		
		Object[] bindSet = new String[]{factoryName, processFlowName, processOperationName, machineName, toProcessOperationName, position};
		try
		{
			List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmt, bindSet);
			
			if(sqlResult.size() < 1)
				throw new CustomException("SYS-9999", "POSMachine", "Position policy not found");
			else
				return sqlResult.get(0);
		}
		catch (FrameworkErrorSignal de)
		{
			throw new CustomException("SYS-9999", "POSMachine", de.getMessage());
		}
	}
	
	/**
	 * find To Unit
	 * @author Aim System
	 * @since 2016.11.10
	 * @param factoryName
	 * @param productSpecName
	 * @param processFlowName
	 * @param processOperationName
	 * @return
	 * @throws CustomException
	 */
	public static List<ListOrderedMap> getReworkLimit(String factoryName, String productSpecName,
			String processFlowName, String processOperationName)
		throws CustomException
	{
		StringBuffer queryBuffer = new StringBuffer()
		.append("SELECT TPF.FACTORYNAME, \n")
		.append("       TPF.PRODUCTSPECNAME, \n")
		.append("       TPF.PROCESSFLOWNAME, \n")
		.append("       RL.PROCESSOPERATIONNAME, \n")
		.append("       RL.LIMITCOUNT \n")
		.append("  FROM TPFPOLICY TPF, POSREWORKLIMIT RL \n")
		.append(" WHERE     TPF.CONDITIONID = RL.CONDITIONID \n")
		.append("       AND TPF.FACTORYNAME = :FACTORYNAME \n")
		.append("       AND TPF.PRODUCTSPECNAME = :PRODUCTSPECNAME \n")
		.append("       AND TPF.PRODUCTSPECVERSION = :PRODUCTSPECVERSION \n")
		.append("       AND TPF.PROCESSFLOWNAME = :PROCESSFLOWNAME \n")
		.append("       AND TPF.PROCESSFLOWVERSION = :PROCESSFLOWVERSION \n")
		.append("       AND RL.PROCESSOPERATIONNAME IN (:PROCESSOPERATIONNAME, 'NA') \n");
		
		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PRODUCTSPECNAME", productSpecName);
		bindMap.put("PRODUCTSPECVERSION", "00001");
		bindMap.put("PROCESSFLOWNAME", processFlowName);
		bindMap.put("PROCESSFLOWVERSION", "00001");
		bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		
		try
		{
//			List<ListOrderedMap> result = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);
			
			return result;
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "POSPolicy", fe.getMessage());
		}
	}
	/**
	 * validate MQC type in factory relation
	 * @author yudan
	 * @since 2016.08.5
	 * @param lotName
	 * @param factoryName
	 * @param productSpecName
	 * @param targetFactoryName
	 * @param targetProductSpecName
	 * @throws CustomException
	 */
	public static void validateReleaseFactoryMQC(String jobType, String lotName, String factoryName, String productSpecName, String targetFactoryName, String targetProductSpecName)
		throws CustomException
	{
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("SELECT C.factoryName, C.productSpecName, P.toFactoryName,").append("\n")
					.append("       P.toProductSpecName, P.shipUnit, P.jobType").append("\n")
					.append(" FROM TPPolicy C, POSFactoryRelation P").append("\n")
					.append(" WHERE C.conditionId = P.conditionId").append("\n")
					.append("  AND jobType = ?").append("\n")
					.append("  AND C.factoryName = ?").append("\n")
					.append("  AND C.productSpecName = ?").append("\n")
					.append("  AND P.toFactoryName = ?").append("\n")
					.append("  AND P.toProductSpecName = ?").append("\n");
		
		Object[] bindArray = new Object[] {jobType, factoryName, productSpecName, targetFactoryName, targetProductSpecName};
		
		try
		{
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuilder.toString(), bindArray);
			
			if (result.size() < 1)
				throw new CustomException("LOT-0208", lotName, factoryName, productSpecName, targetFactoryName, targetProductSpecName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "POSFactoryRelation", fe.getMessage());
		}
	}
	
	/**
	 * find To Unit
	 * @author Aim System
	 * @since 2016.08.25
	 * @param factoryName
	 * @param ProductSpecName
	 * @param processFlowName
	 * @param processOperationName
	 * @return
	 * @throws CustomException
	 */
	public static String getToUnit(String factoryName, String ProductSpecName, String processFlowName, String processOperationName)
		throws CustomException
	{
		String queryBuffer =  " SELECT P.toUnitName " + NEWLINE
                + "   FROM TPFOPolicy T, POSMachine P " + NEWLINE
                + "  WHERE T.conditionId = P.conditionId " + NEWLINE
	            + "    AND T.factoryName = ? " + NEWLINE
                + "    AND T.productSpecName = ? " + NEWLINE
                + "    AND T.productSpecVersion = '00001' " + NEWLINE
                + "    AND T.processFlowName = ? " + NEWLINE
                + "    AND T.processFlowVersion = '00001' " + NEWLINE
                + "    AND T.processOperationName = ? " + NEWLINE
                + "    AND T.processOperationVersion = '00001' " + NEWLINE;
		
		Object[] bindSet = new String[]{factoryName, ProductSpecName, processFlowName, processOperationName};
		String[][] sqlResult = greenFrameServiceProxy.getSqlTemplate().queryForStringArray(queryBuffer, bindSet);
		
		if(sqlResult.length > 0 && sqlResult[0].length > 0)
			return sqlResult[0][0];
		else 
			return "";		
	}	
	
	/**
	 * get sampling rule according to count
	 * @author swcho
	 * @since 2016.12.19
	 * @param factoryName
	 * @param productSpecName
	 * @param processFlowName
	 * @param processOperationName
	 * @param machineName
	 * @return
	 * @throws CustomException
	 */
	public static List<ListOrderedMap> getFlowSamplingRule(String factoryName, String productSpecName, String processFlowName, String processOperationName, String machineName)
		throws CustomException
	{
		//variable limit according to FlowSampleLotCount schema
		
		HashMap<String, String> baseMap = new HashMap<String, String>();
		baseMap.put("factoryName", factoryName);
		baseMap.put("productSpecName", productSpecName);
		baseMap.put("processFlowName", processFlowName);
		baseMap.put("processOperationName", processOperationName);
		baseMap.put("machineName", machineName);
		
		StringBuilder conditionQuery = new StringBuilder()
										.append("SELECT PC.policyName, PC.conditionName ").append("\n")
										.append("    FROM POSPolicyConditionDef PC ").append("\n")
										.append("WHERE PC.policyName = ? ").append("\n")
										.append("ORDER BY PC.searchPosition ");
		
		List<ListOrderedMap> conditionList = new ArrayList<ListOrderedMap>();
		try
		{
			conditionList = GenericServiceProxy.getSqlMesTemplate().queryForList(conditionQuery.toString(), new Object[] {"FlowSampling"});
		}
		catch (FrameworkErrorSignal fe)
		{
			logger.error("count as nothing POS FlowSampling defined");
			//cease
			return new ArrayList<ListOrderedMap>();
		}
		
		//find out sampling rule
		List<ListOrderedMap> result = new ArrayList<ListOrderedMap>();
		for (ListOrderedMap condition : conditionList)
		{
			String conditionName = CommonUtil.getValue(condition, "CONDITIONNAME");
			
			logger.info(String.format("Flow sampling rule search by [%s]", conditionName));
			
			StringBuilder attrQuery = new StringBuilder()
										.append("SELECT CA.conditionName, CA.attributeName, CA.position ").append("\n")
										.append("    FROM POSConditionAttributeDef CA ").append("\n")
										.append("WHERE CA.conditionName = ? ").append("\n")
										.append("ORDER BY CA.position ");
			
			List<ListOrderedMap> attrList = new ArrayList<ListOrderedMap>();
			try
			{
				attrList = GenericServiceProxy.getSqlMesTemplate().queryForList(attrQuery.toString(), new Object[] {conditionName});
			}
			catch (FrameworkErrorSignal fe)
			{
				logger.error(fe);
				continue;
			}
			
			StringBuilder columnString = new StringBuilder();
			StringBuilder conditionString = new StringBuilder();
			String policyConditionName = conditionName + "Policy";
			
			//generate columns & condition
			for (ListOrderedMap attr : attrList)
			{
				String attributeName = CommonUtil.getValue(attr, "ATTRIBUTENAME");
				
				if (!attributeName.contains("Version"))
				{
					if (columnString.length() > 0)
						columnString.append(",");
					
					columnString.append(attributeName);
				}
				
				if (attributeName.contains("Version"))
					conditionString.append(" AND ").append(attributeName).append(" = ").append("'00001'").append("\n");
				else
					conditionString.append(" AND ").append(attributeName).append(" = ").append(":").append(attributeName).append("\n");
			}
			
			//columns filling
			for (String key : baseMap.keySet())
			{
				if (!columnString.toString().contains(key))
				{
					if (columnString.length() > 0)
						columnString.append(",");
					
					columnString.append(" 'NA' ").append(key);
				}
			}
			
			//generate query
			StringBuilder policyQuery = new StringBuilder()
											.append("SELECT ").append(columnString.toString()).append("\n")
											.append(",")
											.append("P.toProcessFlowName, P.toProcessOperationName, P.lotSamplingCount, P.productSamplingCount, P.productSamplingPosition").append("\n")
											.append("    FROM ").append(policyConditionName).append(" C, POSFlowSample P ").append("\n")
											.append("WHERE C.conditionId = P.conditionId ").append("\n")
											.append(conditionString.toString());
			
			List<ListOrderedMap> policyList = new ArrayList<ListOrderedMap>();
			try
			{
				policyList = GenericServiceProxy.getSqlMesTemplate().queryForList(policyQuery.toString(), baseMap);
			}
			catch (FrameworkErrorSignal fe)
			{
				logger.info("Not found Flow sampling rule by this condition");
			}
			
			if (policyList.size() < 1)
			{
				logger.info("Not found Flow sampling rule by this condition");
			}
			else
			{
				logger.info(String.format("Flow sampling rule searched by [%s] : [%d]", conditionName, policyList.size()));
				
				result.addAll(policyList);
			}
		}
		
		return result;
	}
	
	/**
	 * decide where is next
	 * 161213 by hwlee89
	 * @author hwlee89
	 * @since 2016.12.13
	 * @param lotData
	 * @param targetFactoryName
	 * @param targetFlowName
	 * @param targetOperationName
	 * @param 
	 * @param 
	 * @return
	 * @throws CustomException
	 */
	public static String getWhereNextForFlowInsp(Lot lotData,
									  String targetFactoryName, String targetFlowName, String targetOperationName,
									  String conditionName, String conditionValue)
		throws CustomException
	{		
		String destinationNodeStack = "";
		String toFactoryName = "";
		String toFlowName = "";
		String toOperationName = "";
		String returnFlowName = "";
		String returnOperationName = "";
		
		try
		{
			List<ListOrderedMap> alterPathList = getAlterProcessOperation(targetFactoryName, targetFlowName, targetOperationName);
			
			for (ListOrderedMap alterPath : alterPathList)
			{
				String lConditionName = CommonUtil.getValue(alterPath, "CONDITIONNAME");
				String lConditionValue = CommonUtil.getValue(alterPath, "CONDITIONVALUE");
				
				if (conditionName.equalsIgnoreCase(lConditionName) && conditionValue.equalsIgnoreCase(lConditionValue))
				{					
					toFactoryName = CommonUtil.getValue(alterPath, "FACTORYNAME");
					toFlowName = CommonUtil.getValue(alterPath, "TOPROCESSFLOWNAME");
					toOperationName = CommonUtil.getValue(alterPath, "TOPROCESSOPERATIONNAME");
					returnFlowName = CommonUtil.getValue(alterPath, "RETURNPROCESSFLOWNAME");
					returnOperationName = CommonUtil.getValue(alterPath, "RETURNOPERATIONNAME");
					
					//return info setup into track-out Lot
					lotData.getUdfs().put("RETURNFLOWNAME", returnFlowName);
					lotData.getUdfs().put("RETURNOPERATIONNAME", returnOperationName);
					
					//destination is only one
					break;
				}
			}
			
			if (!StringUtil.isEmpty(toFactoryName) && !StringUtil.isEmpty(toFlowName) && !StringUtil.isEmpty(toOperationName))
				destinationNodeStack = NodeStack.getNodeID(toFactoryName, toFlowName, toOperationName);
			
			if (!StringUtil.isEmpty(returnFlowName) && !StringUtil.isEmpty(returnOperationName))
			{
				String returnNodeId = NodeStack.getNodeID(lotData.getFactoryName(), returnFlowName, returnOperationName);
				destinationNodeStack = new StringBuffer(returnNodeId).append(".").append(destinationNodeStack).toString();
			}
			
			
		}
		catch (Exception ex)
		{
			//TK OUT have to be successful
			/*if (ex instanceof CustomException)
				throw (CustomException) ex;
			else if (ex.getCause() instanceof CustomException)
				throw (CustomException) ex.getCause();
			else
				throw new CustomException("Cannot find next ProcessOperation");*/
			
			logger.error("Cannot find next ProcessOperation");
			//default is according to route designer
			destinationNodeStack = "";
		}
		
		return destinationNodeStack;
	}
	
	/**
	 * get data from POSMASK 
	 * @author zhongsl
	 * @since 2016.11.29
	 * @param productSpec
	 * @param processFlowName
	 * @param processOperationName
	 * @param machineName
	 * @return
	 */
	public static List<ListOrderedMap> getTPFOMPolicyMaskList(String productSpec,String processFlowName,String processOperationName,String machineName) 
			throws CustomException                                                                                                                          
	{     
		MachineSpec macSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);		
		StringBuffer sqlBuffer =new StringBuffer();                                                  
	    
		if(CommonUtil.getValue(macSpecData.getUdfs(), "CONSTRUCTTYPE").equals("EXP")
			 || CommonUtil.getValue(macSpecData.getUdfs(), "CONSTRUCTTYPE").equals("CSL")
			 || CommonUtil.getValue(macSpecData.getUdfs(), "CONSTRUCTTYPE").equals("RGB")
			 || CommonUtil.getValue(macSpecData.getUdfs(), "CONSTRUCTTYPE").equals("BML"))
		{
			sqlBuffer = new StringBuffer("")                                                                                                     
			.append("   SELECT PM.MASKBARCODEID,PM.MASKSPEC,       ").append(NEWLINE) 
			.append("   PM.BACKUPBARCODEID,PM.BACKUPBARCODEIDB,   ").append(NEWLINE) 
			.append("   BACKUPBARCODEIDC,BACKUPBARCODEIDD          ").append(NEWLINE)
			.append("   FROM TPFOMPOLICY TP,POSMASK PM             ").append(NEWLINE)                                                             
			.append("   WHERE 1 = 1                                ").append(NEWLINE)                                                             
			.append("	AND TP.CONDITIONID = PM.CONDITIONID        ").append(NEWLINE)                                                               
			.append("	AND TP.PRODUCTSPECNAME = ?                 ").append(NEWLINE)                                                               
			.append("	AND TP.PROCESSFLOWNAME = ?                 ").append(NEWLINE)                                                               
			.append("	AND TP.PROCESSOPERATIONNAME = ?            ").append(NEWLINE)                                                               
			.append("   AND TP.MACHINENAME = ?                     ").append(NEWLINE); 
		}
		else
		{
			sqlBuffer = new StringBuffer("")                                                                                                     
			.append("   SELECT DISTINCT PM.MASKSPEC                ").append(NEWLINE)                                                                         
			.append("   FROM TPFOMPOLICY TP,POSMASK PM             ").append(NEWLINE)                                                             
			.append("   WHERE 1 = 1                                ").append(NEWLINE)                                                             
			.append("	AND TP.CONDITIONID = PM.CONDITIONID        ").append(NEWLINE)                                                               
			.append("	AND TP.PRODUCTSPECNAME = ?                 ").append(NEWLINE)                                                               
			.append("	AND TP.PROCESSFLOWNAME = ?                 ").append(NEWLINE)                                                               
			.append("	AND TP.PROCESSOPERATIONNAME = ?            ").append(NEWLINE)                                                               
			.append("   AND TP.MACHINENAME = ?                     ").append(NEWLINE); 
		}
		
		String qryString = sqlBuffer.toString();                                                                                                          
		Object[] bindSet = new String[] {productSpec,processFlowName,processOperationName,machineName};                                                   
		                                                                                                                                                  
		try                                                                                                                                               
		{                                                                                                                                                 
			List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(qryString, bindSet);                                      
			if(!sqlResult.isEmpty())                                                                                                                        
				return sqlResult;                                                                                                                             
			else if(CommonUtil.getValue(macSpecData.getUdfs(), "CONSTRUCTTYPE").equals("EXP")
					 || CommonUtil.getValue(macSpecData.getUdfs(), "CONSTRUCTTYPE").equals("CSL")
					 || CommonUtil.getValue(macSpecData.getUdfs(), "CONSTRUCTTYPE").equals("RGB")
					 || CommonUtil.getValue(macSpecData.getUdfs(), "CONSTRUCTTYPE").equals("BML"))
				throw new CustomException("MASK-0081", productSpec,processFlowName,processOperationName,machineName); 
			else                                                                                                                                         
				throw new CustomException("MASK-0093", productSpec,processFlowName,processOperationName,machineName); 
		}                                                                                                                                                 
		catch (FrameworkErrorSignal de)                                                                                                                   
		{                                                                                                                                                 
			throw new CustomException("SYS-9999", "TPFOMPolicy (Mask)", de.getMessage());                                                                   
		}                                                                                                                                                 
	} 
	
	
	/**
	 * find To Unit
	 * @author Aim System
	 * @since 2016.11.10
	 * @param factoryName
	 * @param productSpecName
	 * @param processFlowName
	 * @param processOperationName
	 * @return
	 * @throws CustomException
	 */
	public static List<ListOrderedMap> getProductReworkLimit(String factoryName, String productSpecName, String productSpecVersion, String ecCode, String fromFlowName,
			String toFlowName, String fromOperationName, String toOperationName)
		throws CustomException
	{
		StringBuffer queryBuffer = new StringBuffer()
		.append("SELECT TPEFO.FACTORYNAME, \n")
		.append("       TPEFO.PROCESSFLOWNAME, \n")
		.append("       TPEFO.PROCESSFLOWVERSION, \n")
		.append("       TPEFO.PROCESSOPERATIONNAME, \n")
		.append("       TPEFO.PROCESSOPERATIONVERSION, \n")
		.append("       PA.TOPROCESSFLOWNAME, \n")
		.append("       PA.REWORKCOUNT \n")
		.append("  FROM TPEFOPOLICY TPEFO, POSALTERPROCESSOPERATION PA \n")
		.append(" WHERE     TPEFO.CONDITIONID = PA.CONDITIONID \n")
		.append("       AND ((TPEFO.FACTORYNAME = :FACTORYNAME) OR (TPEFO.FACTORYNAME = :STAR)) \n")
		.append("       AND ((TPEFO.PRODUCTSPECNAME = :PRODUCTSPECNAME) OR (TPEFO.PRODUCTSPECNAME = :STAR)) \n")
		.append("       AND ((TPEFO.PRODUCTSPECVERSION = :PRODUCTSPECVERSION) OR (TPEFO.PRODUCTSPECVERSION = :STAR)) \n")
		.append("       AND ((TPEFO.ECCODE = :ECCODE) OR (TPEFO.ECCODE = :STAR)) \n")
		.append("       AND ((TPEFO.PROCESSFLOWNAME = :BEFOREPROCESSFLOWNAME) OR (TPEFO.PROCESSFLOWNAME = :STAR)) \n")
		.append("       AND ((TPEFO.PROCESSFLOWVERSION = :BEFOREPROCESSFLOWVERSION) OR (TPEFO.PROCESSFLOWVERSION = :STAR)) \n")
		.append("       AND ((TPEFO.PROCESSOPERATIONNAME = :BEFOREPROCESSOPERATIONNAME) OR (TPEFO.PROCESSOPERATIONNAME = :STAR)) \n")
		.append("       AND ((TPEFO.PROCESSOPERATIONVERSION = :BEFOREPROCESSOPERATIONVERSION) OR (TPEFO.PROCESSOPERATIONVERSION = :STAR))  \n")
		.append("       AND PA.TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME  \n")
		.append("       AND UPPER(PA.CONDITIONNAME) = :CONDITIONNAME  \n")
		.append("       ORDER BY DECODE (TPEFO.FACTORYNAME, :STAR, 9999, 0),  \n")
		.append("       DECODE (TPEFO.PROCESSFLOWNAME, :STAR, 9999, 0),  \n")
		.append("       DECODE (TPEFO.PROCESSFLOWVERSION, :STAR, 9999, 0),  \n")
		.append("       DECODE (TPEFO.PROCESSOPERATIONNAME, :STAR, 9999, 0),  \n")
		.append("       DECODE (TPEFO.PROCESSOPERATIONVERSION, :STAR, 9999, 0)  \n");
		
		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PRODUCTSPECNAME", productSpecName);
		bindMap.put("PRODUCTSPECVERSION", productSpecVersion);
		bindMap.put("ECCODE", ecCode);
		bindMap.put("BEFOREPROCESSFLOWNAME", fromFlowName);
		bindMap.put("BEFOREPROCESSFLOWVERSION", "00001");
		bindMap.put("BEFOREPROCESSOPERATIONNAME", fromOperationName);
		bindMap.put("BEFOREPROCESSOPERATIONVERSION", "00001");
		bindMap.put("TOPROCESSFLOWNAME", toFlowName);
		bindMap.put("CONDITIONNAME", "REWORK");
		bindMap.put("STAR", "*");

		try
		{
//			List<ListOrderedMap> result = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);
			
			return result;
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "POSPolicy", fe.getMessage());
		}
	}
	
   /**
    *  
    * @Name     checkMachineOperationByTOPolicy
    * @since    2018. 9. 30.
    * @author   hhlee
    * @contents 
    *           
    * @param factoryName
    * @param processOperationName
    * @param machineName
    * @param operationMode
    * @throws CustomException
    */
	public static void checkMachineOperationByTOPolicy(String factoryName, String processOperationName, String machineName, String operationMode) throws CustomException
    {        
        String strSql = " SELECT TP.CONDITIONID, TP.FACTORYNAME, TP.PROCESSOPERATIONNAME,          \n"+
                        "        TP.PROCESSOPERATIONVERSION, PO.MACHINENAME,                       \n"+
                        "            PO.OPERATIONMODE, PO.DESCRIPTION                              \n"+
                        "   FROM TOPOLICY TP , POSOPERATIONMODE PO                                 \n"+
                        "  WHERE 1=1                                                               \n"+
                        "    AND TP.CONDITIONID = PO.CONDITIONID                                   \n"+
                        "    AND TP.FACTORYNAME = :FACTORYNAME                                     \n"+
                        "    AND REGEXP_LIKE(TP.PROCESSOPERATIONNAME, :PROCESSOPERATIONNAME)       \n"+
                        "    AND REGEXP_LIKE(TP.PROCESSOPERATIONVERSION, :PROCESSOPERATIONVERSION) \n"+
                        "    AND PO.MACHINENAME = :MACHINENAME                                     \n"+
                        "    AND PO.OPERATIONMODE = :OPERATIONMODE                                 \n";
            
        Map<String, Object> bindMap = new HashMap<String, Object>();
        bindMap.put("FACTORYNAME", factoryName);            
        bindMap.put("PROCESSOPERATIONNAME", "^[*]|" +  processOperationName);
        bindMap.put("PROCESSOPERATIONVERSION", "^[*]|" +  "00001");
        bindMap.put("MACHINENAME", machineName);
        bindMap.put("OPERATIONMODE", operationMode);

        List<Map<String, Object>> toPolicyOperationMode = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindMap);
        //TOPolicy has no data !! Factory:[{0}], Machine:[{1}], OperationMode:[{2}].
        if ( toPolicyOperationMode == null ||
                toPolicyOperationMode.size() <= 0 )
        {
            throw new CustomException("POLICY-0021", factoryName, machineName, operationMode);
        }       
    }	
}
