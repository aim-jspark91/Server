
package kr.co.aim.messolution.generic.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

 

@SuppressWarnings("serial")
public class NodeStack extends Stack<String>
{
	private static Log log = LogFactory.getLog(NodeStack.class);
	
	/*
	* Name : stringToNodeStack
	* Desc : This function is stringToNodeStack
	* Author : AIM Systems, Inc
	* Date : 2011.01.19
	*/
	public static NodeStack stringToNodeStack(final String nodeText)
	{
		NodeStack nodeStack = new NodeStack();
		
		nodeStack.addAll(Arrays.asList(nodeText.split("\\.")));
		
		return nodeStack;
	}
	/*
	* Name : nodeStackToString
	* Desc : This function is nodeStackToString
	* Author : AIM Systems, Inc
	* Date : 2011.01.19
	*/
	public static String nodeStackToString(final NodeStack nodeStack)
	{
		String nodeText = "";
		
		for ( int i = 0; i < nodeStack.size(); i++ )
		{
			nodeText += nodeStack.get(i);
			
			if ( i < nodeStack.size() - 1 )
				nodeText += ".";				
		}
		
		return nodeText;
	}
	/*
	* Name : getNodeID
	* Desc : This function is getNodeID
	* Author : AIM Systems, Inc
	* Date : 2011.01.19
	*/
	public static String getNodeID(String factoryName,
							String processFlowName,
							String processOperationName) throws CustomException
	{		
		ProcessFlowKey pfKey = new ProcessFlowKey();
		pfKey.setFactoryName( factoryName );
		pfKey.setProcessFlowName( processFlowName );
		pfKey.setProcessFlowVersion( "00001" );
		
		String nodeId  = "";
		
		if ( processOperationName == null )
		{
			nodeId = ProcessFlowServiceProxy.getProcessFlowService().getStartNode(pfKey).getKey().getNodeId();
		}
		else
		{
			String sql = "SELECT NODEID FROM NODE " + " WHERE FACTORYNAME = ? "
			+ "   AND PROCESSFLOWNAME = ? "
			+ "   AND PROCESSFLOWVERSION = ? "
			+ "   AND NODEATTRIBUTE1 = ? " + "   AND NODEATTRIBUTE2 = ? "
			+ "   AND NODETYPE = ? ";
			
			Object[] bind = new Object[] { factoryName,
				processFlowName, "00001", processOperationName,				
				"00001", "ProcessOperation" };
			
			String[][] result = null;
			result = greenFrameServiceProxy.getSqlTemplate().queryForStringArray(sql, bind);

			nodeId = result[0][0];
			
		}
		
		return nodeId;
	}
	
	/*
	 * Name : getNextMandatoryNodeID
	 * Desc : This function is getNextMandatoryNodeID
	 * Author : AIM Systems, Inc
	 * Date : 2018.05.27
	 */
	
	public static Map<String, Object> getNextMandatoryNodeID(String factoryName, String processFlowName,String processFlowVersion, String processOperationName, String processOperationVersion)
			throws Exception, CustomException
	{		
		if ( log.isInfoEnabled())
		{
			log.info(String.format("getNodeID FactoryName[%s] ProcessFlowName[%s], FlowVer[%s], ProcessOperationName[%s], OperVer[%s]" , 
					factoryName, processFlowName,processFlowVersion, processOperationName, processOperationVersion));
		}
		
		Node nodeData = new Node();

		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT PS.PROCESSOPERATIONNAME, PS.PROCESSOPERATIONVERSION, PS.DESCRIPTION, N.NODEID, N.PROCESSFLOWNAME ");
		sql.append("   FROM PROCESSOPERATIONSPEC PS ");
		sql.append("   JOIN( ");
		sql.append("     SELECT   N.NODEID ");
		sql.append("            , A.TONODEID ");
		sql.append("            , N.NODETYPE ");
		sql.append("            , N.NODEATTRIBUTE1   PROCESSOPRATIONNAME ");
		sql.append("            , NODEATTRIBUTE2     PROCESSOPERATIONVERSION ");
		sql.append("            , N.FACTORYNAME ");
		sql.append("            , N.PROCESSFLOWNAME ");
		sql.append("            , ROWNUM SEQ ");
		sql.append("        , N.PROCESSFLOWVERSION ");
		sql.append("       FROM NODE   N ");
		sql.append("       JOIN ARC    A ON(N.NODEID = A.FROMNODEID) ");
		sql.append("      WHERE N.NODETYPE = 'ProcessOperation' ");
		sql.append("        AND N.FACTORYNAME = :FACTORYNAME ");
		sql.append("        AND N.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("        AND N.PROCESSFLOWVERSION = '00001' ");
		sql.append("    CONNECT BY N.NODEID = PRIOR A.TONODEID ");
		sql.append("     START WITH N.NODEATTRIBUTE1 = :PROCESSOPERATIONNAME) N ");
		sql.append("         ON(PS.FACTORYNAME = N.FACTORYNAME ");
		sql.append("            AND PS.PROCESSOPERATIONNAME = N.PROCESSOPRATIONNAME ");
		sql.append("            AND PS.PROCESSOPERATIONVERSION = N.PROCESSOPERATIONVERSION ");
		sql.append("            AND PS.MANDATORYOPERATIONFLAG = 'Y') ");
		sql.append("  WHERE N.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("    AND N.PROCESSFLOWVERSION = '00001' ");
		sql.append("    AND PS.FACTORYNAME = :FACTORYNAME ");
		sql.append("    AND N.PROCESSOPRATIONNAME <> :PROCESSOPERATIONNAME ");
		sql.append(" ORDER BY N.SEQ ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PROCESSFLOWNAME", processFlowName);
		bindMap.put("PROCESSOPERATIONNAME", processOperationName);

		List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().queryForList(sql.toString(), bindMap);
		
		if(sqlResult.size() > 0)
		{
			return sqlResult.get(0);
		}
		return null;
	}
	
	/**
	 * @param factoryName
	 * @param processFlowName
	 * @param processOperationName
	 * @return
	 * @throws Exception
	 */
	public static String getFlowNodeID(String factoryName, String currentFlowName, String nextFlowName)
	{
		StringBuffer sqlStatement = new StringBuffer();
		sqlStatement.append("SELECT NODEID FROM NODE WHERE FACTORYNAME = ? ")
					  .append("   AND PROCESSFLOWNAME = ? ")
					  .append("   AND PROCESSFLOWVERSION = ? ")
					  .append("   AND NODEATTRIBUTE1 = ? ")
					  .append("   AND NODEATTRIBUTE2 = ? ")
					  .append("   AND NODETYPE = ? ");
		
		Object[] bind = new Object[] { factoryName,	currentFlowName, "00001", nextFlowName, "00001", "ProcessFlow" };		
		
		String[][] result = greenFrameServiceProxy.getSqlTemplate().queryForStringArray(sqlStatement.toString(), bind);
		
		if ( result.length > 0 ) 
			return result[0][0];
		else
			return "";
	}
	
	/**
	 * get previous main step node 
	 * @author swcho
	 * @since 2016.12.23
	 * @param factoryName
	 * @param processFlowName
	 * @param processOperationName
	 * @param currentNodeId
	 * @return
	 * @throws CustomException
	 */
	public static String getPreviousNode(String factoryName, String processFlowName, String processOperationName, String currentNodeId)
		throws CustomException
	{
		//get latest location
		String[] nodeStackArray = StringUtil.split(currentNodeId, ".");
		currentNodeId = nodeStackArray[nodeStackArray.length - 1];
		
		//search previous location
		List<ListOrderedMap> priorSequence;
		try
		{
			StringBuffer sqlStatement = new StringBuffer()
				.append("SELECT O.factoryName, O.processOperationName, O.processOperationVersion, ").append("\n")
				.append("       O.processOperationType, TN.nodeId, TN.nodeType ").append("\n")
				.append("    FROM Node TN, ProcessOperationSpec O ").append("\n")
				.append("WHERE TN.nodeId IN ").append("\n")
				.append("          (SELECT A.fromNodeId ").append("\n")
				.append("                FROM Node N, Arc A ").append("\n")
				.append("            WHERE 1=1 ").append("\n")
				.append("                AND N.factoryName = ? ").append("\n")
				.append("                AND N.nodeAttribute1 = ? ").append("\n")
				.append("                AND N.nodeAttribute2 = ? ").append("\n")
				.append("                AND N.nodeId = ? ").append("\n")
				.append("                AND A.toNodeId = N.nodeId ").append("\n")
				.append("                AND A.arcType = ?) ").append("\n")
				.append("AND nodeType = ? ").append("\n")
				.append("AND O.factoryName = TN.factoryName ").append("\n")
				.append("AND O.processOperationName = TN.nodeAttribute1 ").append("\n")
				.append("AND O.processOperationVersion = TN.nodeAttribute2 ");
			
			//one or die
			priorSequence = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStatement.toString(),
					 			new Object[] {factoryName, processOperationName, "00001", currentNodeId, "Normal", "ProcessOperation"});
		}
		catch (FrameworkErrorSignal fe)
		{
			priorSequence = new ArrayList<ListOrderedMap>();
		}
		
		if (priorSequence.size() < 1)
		{
			return currentNodeId;
		}
		else
		{
			ListOrderedMap priorNode = priorSequence.get(0);
			
			String priorOperationName = CommonUtil.getValue(priorNode, "PROCESSOPERATIONNAME");
			String priorNodeId = CommonUtil.getValue(priorNode, "NODEID");
			String operationType = CommonUtil.getValue(priorNode, "ProcessOperationType");
			
			if (operationType.equals("Inspection"))
			{
				//recursive
				priorNodeId = getPreviousNode(factoryName, processFlowName, priorOperationName, priorNodeId);
			}
			
			return priorNodeId;
		}
	}
}
