package kr.co.aim.messolution.generic.expression;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.greenflow.expression.ExtendedFunction;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.orm.ObjectAttributeDef;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;
import kr.co.aim.greenframe.util.xml.JdomUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.jaxen.XPathFunctionContext;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class CustomExpression extends ExtendedFunction implements ApplicationContextAware {

	private static Log log = LogFactory.getLog(CustomExpression.class);
	/**
	 * @uml.property  name="context"
	 * @uml.associationEnd  
	 */
	private ApplicationContext 		context = null;
	
	public void setApplicationContext(ApplicationContext ctx) throws BeansException
	{
		this.context = ctx;   
	}	
	
	public boolean isConcurrentBpelExecutionContextAware() {
		// TODO Auto-generated method stub
		return true;
	}

	public CustomExpression(){
		
	} 
	/*
	* Name : registerFunctions
	* Desc : This function is registerFunctions
	* Author : AIM Systems, Inc
	* Date : 2011.03.17
	*/
	@Override
	public void registerFunctions(XPathFunctionContext functionContext) {
		functionContext.registerFunction(null, "getListAt", new GetListAt());
		functionContext.registerFunction(null, "getXmlData", new GetXmlData());
		functionContext.registerFunction(null, "setXmlData", new SetXmlData());
		functionContext.registerFunction(null, "isNull", new isNull()); 
		functionContext.registerFunction(null, "toDouble", new ToDouble()); 
		functionContext.registerFunction(null, "toInt", new ToInteger());
		functionContext.registerFunction(null, "getXmlAttributeArray", new GetXmlAttributeArray());
		functionContext.registerFunction(null, "getXmlChildNodeValueArray", new GetXmlChildNodeValueArray());
		functionContext.registerFunction(null, "getNamedValue", new GetNamedValue());
		functionContext.registerFunction(null, "getXmlDataTimeStamp", new GetXmlDataTimeStamp());
		functionContext.registerFunction(null, "getXmlDataInt", new GetXmlDataInt());
		functionContext.registerFunction(null, "getXmlElement", new GetXmlElement());
		functionContext.registerFunction(null, "setXmlElement", new SetXmlElement());
		functionContext.registerFunction(null, "getArrayValue", new GetArrayValue());
		functionContext.registerFunction(null, "getLength", new GetLength());
		functionContext.registerFunction(null, "getXmlArraySize", new GetXMLArraySize());
		functionContext.registerFunction(null, "getXmlArrayValue", new GetXMLArrayValue());
		functionContext.registerFunction(null, "getXMLArrayElement", new GetXMLArrayElement());
		functionContext.registerFunction(null, "setNVSeq", new SetNVSeq());
		functionContext.registerFunction(null, "getDataVariable", new GetDataVariable());			
		
		functionContext.registerFunction(null, "toString", new ToString());
		functionContext.registerFunction(null, "append", new Append());
		functionContext.registerFunction(null, "getXmlElementChildList", new GetXmlElementChildList());
		functionContext.registerFunction(null, "getXmlElementChildArray", new GetXmlElementChildArray());
		functionContext.registerFunction(null, "getElementChildText", new GetElementChildText());
		functionContext.registerFunction(null, "getElementChildrenTextList", new GetElementChildrenTextList());
		functionContext.registerFunction(null, "detachXmlNode", new DetachXmlNode());
		functionContext.registerFunction(null, "getDifferTimeInSecond", new GetDifferTimeInSecond());
		functionContext.registerFunction(null, "getChildElement", new GetChildElement());
		functionContext.registerFunction(null, "setEmpty", new SetEmpty());
		functionContext.registerFunction(null, "getXmlElementChildrenList", new GetXmlElementChildrenList());
		
		functionContext.registerFunction(null, "addElement", new AddElement());
		functionContext.registerFunction(null, "getUdfsData", new GetUdfsData());
		functionContext.registerFunction(null, "setUdfsData", new SetUdfsData());
		functionContext.registerFunction(null, "getXmlElementData", new GetXmlElementData());
		functionContext.registerFunction(null, "getCurrTimeStampSQL", new GetCurrTimeStampSQL());
		functionContext.registerFunction(null, "getTimeKey", new GetTimeKey());
		functionContext.registerFunction(null, "convertToTimeStamp", new ConvertToTimeStamp());
		functionContext.registerFunction(null, "getChildNodesText", new GetChildNodesTextFunction());
		functionContext.registerFunction(null, "setXMLArrayValue", new SetXMLArrayValue());
		functionContext.registerFunction(null, "getXmlElementArraySize", new GetXmlElementArraySize());
		functionContext.registerFunction(null, "getXmlElementArrayValue", new GetXmlElementArrayValue());
		functionContext.registerFunction(null, "removeXmlNode", new RemoveXmlNode());
		functionContext.registerFunction(null, "toUpper", new ToUpper());
		functionContext.registerFunction(null, "toLower", new ToLower());
		functionContext.registerFunction(null, "decode", new Decode());
		functionContext.registerFunction(null, "getField", new GetField());
		functionContext.registerFunction(null, "getUdfs", new GetUdfs());
	

		functionContext.registerFunction(null, "getSubString", new GetSubString());
		functionContext.registerFunction(null, "getElementList", new GetElementList());
		functionContext.registerFunction(null, "setElement", new SetElement());
		functionContext.registerFunction(null, "AddElementForList", new AddElementForList());
		
		functionContext.registerFunction(null, "createElement", new CreateElement());
		functionContext.registerFunction(null, "createDocument", new CreateDocument());
		
		// -COMMENT-
		// -AHN MIN WOO-
		functionContext.registerFunction(null, "toLong", new ToLong());
	}	
	
	public class CreateElement implements Function {
		/*
		* Name : call
		* Desc : This function is CreateElement
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		@Override
		public Object call(Context context, List list)
				throws FunctionCallException {
			String nodeName = (String) list.get(0);
			Element element = new Element(nodeName);
			if(list.size() == 2) {
				element.setText((String) list.get(1));
			}
			return element;
		}
		
	}
	private class CreateDocument implements Function{
		/*
		* Name : call
		* Desc : This function is CreateElement
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		@Override
		public Object call(Context context, List list)
		throws FunctionCallException {
			String nodeName = (String) list.get(0);
			Document doc = new Document(new Element(nodeName));
			return doc;
		}
		
	}
	
	
	private class ToUpper implements Function 
	{
		/*
		* Name : call
		* Desc : This function is variableName Upper
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context context, List args) throws RuntimeException 
		{
			String variableName = String.valueOf(args.get(0));

			try
			{
				return variableName.toUpperCase();
			} catch (Exception ex) {
				log.error(ex);
				return null;
			}
		}
	}
	private class GetUdfs implements Function 
	{
		/*
		* Name : call
		* Desc : This function is variableName getUdfs
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context context, List args) throws RuntimeException 
		{
			String variableName = String.valueOf(args.get(0));
			UdfAccessor udf = (UdfAccessor)getBpelExecutionContext().getVariableData().getValue(variableName);
			
			try {
				return udf.getUdfs();
			} catch (Exception ex) {
				log.error(ex);
				return null;
			}
		}
	}
	
	private class GetField implements Function
	{
		/*
		* Name : call
		* Desc : This function is getFieldValue
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call( Context xpath, List list ) throws FunctionCallException
		{
			try
			{
				Object obj = list.get( 0 );
				
				if ( obj instanceof String )
					obj = getBpelExecutionContext().getVariableData().getValue(( String )list.get(0));
				
				String fields = ( String )list.get( 1 );
				String[] fieldNames =  fields.split("\\." );
			
				Object fieldValue = getFieldValue( obj, fieldNames );
				return fieldValue;
			}
			catch ( Exception ex )
			{
				throw new FunctionCallException( ex );
			}
		}
		/*
		* Name : getFieldValue
		* Desc : This function is get Field Value
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		private Object getFieldValue( Object obj, String[] fieldNames ) throws NoSuchFieldException, IllegalAccessException
		{
			Field field = null;
			
			for ( String fieldName : fieldNames )
			{
				field = getField( obj, fieldName );
				obj = field.get( obj );
			}
			
			return obj;
		}
		/*
		* Name : getField
		* Desc : This function is get Field
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		private Field getField( Object obj, String fieldName ) 
		{
			Field field;
			field = findField(obj.getClass(),fieldName );
			
			field.setAccessible( true );
			
			return field;
		}
		/*
		* Name : findField
		* Desc : This function is find Field
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		private Field findField(Class clazz, String fieldName) {
			if(clazz == null) {
				return null;
			}
			Field field = null;
			try {
				field = clazz.getDeclaredField(fieldName);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				return findField(clazz.getSuperclass(), fieldName);
			}
			return field;
		}
	}
	private class ToLower implements Function 
	{
		/*
		* Name : call
		* Desc : This function is variableName Lower
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context context, List args) throws RuntimeException 
		{
			String variableName = String.valueOf(args.get(0));

			try
			{
				return variableName.toLowerCase();
			} catch (Exception ex) {
				log.error(ex);
				return null;
			}
		}
	}
	
	private class Decode implements Function 
	{
		/*
		* Name : call
		* Desc : This function is variableName
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context context, List args) throws RuntimeException 
		{
			String variableName = String.valueOf(args.get(0));
			String result = "";
			try
			{
				for(int i=1; i<args.size(); i+=2)
				{
					String compare = String.valueOf(args.get(i));
					if(variableName.equals(compare))
					{
						result = String.valueOf(args.get(i+1));
						break;
					}
					
					if(i>=args.size()-1)
					{
						result = String.valueOf(args.get(i));
						break;
					}
				}
				
				return result;
			} catch (Exception ex) {
				log.error(ex);
				return null;
			}
		}
	}
	
	private class GetSubString implements Function 
	{
		/*
		* Name : call
		* Desc : This function is variableName substring
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context context, List args) throws RuntimeException 
		{
			String variableName = String.valueOf(args.get(0));
			String beginIndex = String.valueOf(args.get(1));
			String endIndex = String.valueOf(args.get(2));

			String messaage = (String)getBpelExecutionContext().getVariableData().getValue(variableName);
			
			try
			{
				messaage = messaage.substring(Integer.valueOf(beginIndex).intValue(), 
						Integer.valueOf(endIndex).intValue());
				
				return messaage;
			} catch (Exception ex) {
				log.error(ex);
				return null;
			}
		}
	}
	
	
	private class SetUdfsData implements Function 
	{
		/*
		* Name : call
		* Desc : This function is udf getValue
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context context, List args) throws RuntimeException 
		{
			String variableName = String.valueOf(args.get(0));
			String udfsName = String.valueOf(args.get(1));
			String udfsValue = String.valueOf(args.get(2));

			Map udf = (Map)getBpelExecutionContext().getVariableData().getValue(variableName);
			
			try
			{
				if(udfsValue == null || udfsValue.isEmpty()){
					udf.put(udfsName, udfsValue);
				}else{
					udf.put(udfsName, udfsValue);
				}
				
				return null;
			} catch (Exception ex) {
				log.error(ex);
				return null;
			}
		}
	}	
	
	private class GetChildNodesTextFunction implements Function
	{
		/*
		* Name : call
		* Desc : This function is textList selectSingleNode
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context xpath, List list) throws FunctionCallException
		{
			try
			{
				Object obj = list.get(0);
				
				String path = (String) list.get(1);
				
				if ( obj instanceof String )
					obj = getBpelExecutionContext().getVariableData().getValue((String)obj);
				
				Element node = (Element) XPath.selectSingleNode(obj, path);
				List<Element> children = node.getChildren();
				List<String> textList = new ArrayList<String>(); 
				for (Element element : children) {
					textList.add(element.getTextTrim());
				}
				
				return textList;
			}
			catch ( Exception ex )
			{
				throw new FunctionCallException( ex );
			}
		}
	}

	/* 
	 * 
	private class AddElement implements Function 
	{
		public Object call(Context context, List args) throws RuntimeException 
		{
			String variableName = String.valueOf(args.get(0));
			String nodeName = String.valueOf(args.get(1));
			String nodeValue = String.valueOf(args.get(2));
			
			if (nodeValue == null) nodeValue = "";
			
			//org.jdom.Document jdom = (org.jdom.Document)getBpelExecutionContext().getVariableData().getValue(variableName);
			Element xmlElement = (Element)getBpelExecutionContext().getVariableData().getValue(variableName);
			
			try {
				if (xmlElement == null)
				{
					xmlElement = new Element(variableName);
					getBpelExecutionContext().getVariableData().setValue(variableName, xmlElement);
				}
				
				Element ele = null;
				ele = new Element(nodeName);
				ele.setText(nodeValue);
				
				xmlElement.addContent(ele);
				
				return null;
				
			} catch (Exception ex) {
				log.error(ex);
				return null;
			}
		}
	}
	
	*/
	/*
	* Name : AddElement
	* Desc : This function is AddElement
	* Author : AIM Systems, Inc
	* Date : 2011.01.17
	*/
	private class AddElement implements Function 
	{
		public Object call(Context context, List args) throws RuntimeException 
		{
			try {
				if(args.size() == 4 || args.size() == 3) {
					String variableName = (String) args.get(0);
					String xpath = (String) args.get(1);
					Element newElement;
					if(args.get(2) instanceof Element) {
						newElement = (Element) args.get(2);
					}else {
						String nodeName = (String) args.get(2);
						String nodeText = (String) args.get(3);
						newElement = new Element(nodeName);
						newElement.setText(nodeText);
					}
					
					Object document = getBpelExecutionContext().getVariableData().getValue(variableName);
					Element element;
					try {
						if(xpath.isEmpty()) {
							if(document instanceof Document) {
								element = ((Document) document).getRootElement();
							}else {
								element = (Element) document;
							}
						}else {
							element = (Element) XPath.selectSingleNode(document, xpath);
						}
					} catch (JDOMException e) {
						log.error(e);
						return null;
					}
					
					element.addContent(newElement);
					
					return document;
				}
				else if(args.size() == 2) {
					Element parentElement = (Element) getBpelExecutionContext().getVariableData().getValue((String) args.get(0));
					Element element = (Element) args.get(1);
					parentElement.addContent(element);
					return parentElement;
				}
				else if(args.size() == 6)
				{
					Object document = getBpelExecutionContext().getVariableData().getValue((String)args.get(0));
					String path = (String) args.get(1);
					
					Element element;
					element = (Element) XPath.selectSingleNode(document, path);
					
					
					Element operationSet = new Element("OPERATION");

					String nodeNameNum1 = (String) args.get(2);
					String nodeTextVal1 = (String) args.get(3);
					String nodeNameNum2 = (String) args.get(4);
					String nodeTextVal2 = (String) args.get(5);
					
					Element newElement = new Element(nodeNameNum1);
					newElement.setText(nodeTextVal1);
					Element newElement2 = new Element(nodeNameNum2);
					newElement2.setText(nodeTextVal2);
					
					operationSet.addContent(newElement);
					operationSet.addContent(newElement2);
					
					element.addContent(operationSet);
					
					return document;
				}
				return null;
				
			} catch (Exception ex) {
				log.error(ex);
				return null;
			}
		}
	}
	/*
	* Name : GetCurrTimeStampSQL
	* Desc : This function is GetCurrTimeStampSQL
	* Author : AIM Systems, Inc
	* Date : 2011.01.17
	*/
	private class GetCurrTimeStampSQL implements Function 
	{
		public Object call(Context context, List args) throws RuntimeException 
		{
			try {
				Timestamp timestamp = ConvertUtil.getCurrTimeStampSQL();
				return timestamp;
			} catch (Exception ex) {
				log.error(ex);
				return null;
			}
		}
	}
	/*
	* Name : ConvertToTimeStamp
	* Desc : This function is ConvertToTimeStamp
	* Author : AIM Systems, Inc
	* Date : 2011.01.17
	*/
	private class ConvertToTimeStamp implements Function 
	{
		public Object call(Context context, List args) throws RuntimeException 
		{
			String textTime = String.valueOf(args.get(0));

			try {
				Timestamp timestamp = ConvertUtil.convertToTimeStamp(textTime);
				return timestamp;
			} catch (Exception ex) {
				log.error(ex);
				return null;
			}
		}
	}
	/*
	* Name : GetUdfsData
	* Desc : This function is GetUdfsData
	* Author : AIM Systems, Inc
	* Date : 2011.01.17
	*/
	private class GetUdfsData implements Function 
	{
		public Object call(Context context, List args) throws RuntimeException 
		{
			String variableName = String.valueOf(args.get(0));
			String udfsName = String.valueOf(args.get(1));

			UdfAccessor udf = (UdfAccessor)getBpelExecutionContext().getVariableData().getValue(variableName);
			
			try
			{
				String udfsValue = udf.getUdfs().get(udfsName);
				return udfsValue;
				
			} catch (Exception ex) {
				log.error(ex);
				return null;
			}
		}
	}	
	/*
	* Name : GetElementList
	* Desc : This function is GetElementList
	* Author : AIM Systems, Inc
	* Date : 2011.01.17
	*/
	private class GetElementList implements Function 
	{		
		
		public Object call(Context context, List args) throws RuntimeException 
		{			
			String variableName = String.valueOf(args.get(0));
			String locationPath = String.valueOf(args.get(1));
			
			Object jdom = getBpelExecutionContext().getVariableData().getValue(variableName);
			try {
				Document document;
				if(jdom instanceof Element){
					document = new Document();
					
					Element clone = (Element) ((Element)jdom).clone();
					document.setRootElement(clone);
				}
				else{
					document = (Document) jdom;
				}
				Object reply = XPath.selectNodes(document, locationPath);
				return reply;
				
			} catch (Exception ex) {
				log.warn(ex);
				return new ArrayList();
			}
		}
	}
	/*
	* Name : GetXmlElementChildrenList
	* Desc : This function is GetXmlElementChildrenList
	* Author : AIM Systems, Inc
	* Date : 2011.01.17
	*/
	private class GetXmlElementChildrenList implements Function 
	{		
		
		public Object call(Context context, List args) throws RuntimeException 
		{			
			String variableName = String.valueOf(args.get(0));
			String locationPath = String.valueOf(args.get(1));
			
			Object xmlObject = getBpelExecutionContext().getVariableData().getValue(variableName);
			
			Element replyElement = null;
			try {
				if(args.size() > 2)
				{
					int index = ExpressionUtils.getIntValue(args.get(2));
					
					replyElement = JdomUtils.getNode(xmlObject, locationPath, index);
					
					List<Element> list= replyElement.getChildren();
					
					java.util.List<Element> rlist = new ArrayList<Element>();
					
					for(Element subElement : list){
						rlist.add(subElement);
					}
					
					return rlist;
				}
				else
				{
					replyElement = JdomUtils.getNode(xmlObject, locationPath);
					
					List<Element> list= replyElement.getChildren();
					
					java.util.List<Element> rlist = new ArrayList<Element>();
					
					for(Element subElement : list){
						rlist.add(subElement);
					}
					
					return rlist;
				}
			} catch (Exception ex) {
				log.error(ex);
				return null;
			}
		}
	}

	/*
	* Name : SetEmpty
	* Desc : This function is SetEmpty
	* Author : AIM Systems, Inc
	* Date : 2011.01.17
	*/
	private class SetEmpty implements Function
	{
		public Object call(Context context, List args) throws RuntimeException
		{
			String variableName = String.valueOf(args.get(0));
			
			Object obj = (org.jdom.Document)getBpelExecutionContext().getVariableData().getValue(variableName);
			
			if(obj instanceof String){
				String var = (String)obj;
				var="";
				getBpelExecutionContext().getVariableData().setValue(variableName, var);
			}else if(obj instanceof java.lang.Integer){
				int var = ((java.lang.Integer)obj).intValue();
				var=0;
				getBpelExecutionContext().getVariableData().setValue(variableName, var);
			}else if(obj instanceof java.lang.Long){
				long var = ((java.lang.Long)obj).longValue();
				var=0L;
				getBpelExecutionContext().getVariableData().setValue(variableName, var);
			}else if(obj instanceof java.lang.Double){
				double var = ((java.lang.Double)obj).doubleValue();
				var=0D;
				getBpelExecutionContext().getVariableData().setValue(variableName, var);
			}else if(obj instanceof java.util.List){
				java.util.List list = (java.util.List)obj;
				for(int i=0; i<list.size();i++){
					list.remove(i);
				}
				getBpelExecutionContext().getVariableData().setValue(variableName, list);
			}else if(obj instanceof java.util.Map){
				java.util.Map map = (java.util.Map)obj;
				for(Object key : map.keySet()){
					map.remove(key);
				}
				getBpelExecutionContext().getVariableData().setValue(variableName, map);
			}else{
				getBpelExecutionContext().getVariableData().setValue(variableName, null);
			}
			
			return null;
		}
	}

	/*
	* Name : GetXmlElementData
	* Desc : This function is GetXmlElementData
	* Author : AIM Systems, Inc
	* Date : 2011.01.17
	*/
	private class GetXmlElementData implements Function 
	{		
		
		public Object call(Context context, List args) throws RuntimeException 
		{			
			String variableName = String.valueOf(args.get(0));
			String locationPath = String.valueOf(args.get(1));
			
			Element ele = (Element)getBpelExecutionContext().getVariableData().getValue(variableName);
			try {
				if(args.size() > 2)
				{
					int index = ExpressionUtils.getIntValue(args.get(2));
					
					String reply = JdomUtils.getNodeText(ele, locationPath, index);
//					System.out.println(reply);
					return reply;
				}
				else
				{
					String reply = JdomUtils.getNodeText(ele, locationPath);
//					System.out.println(reply);
					return reply;
				}
			} catch (Exception ex) {
				log.warn(ex);
				return "";
			}
		}
	}
	/*
	* Name : GetXmlData
	* Desc : This function is GetXmlData
	* Author : AIM Systems, Inc
	* Date : 2011.01.17
	*/
	private class GetXmlData implements Function 
	{		
		
		public Object call(Context context, List args) throws RuntimeException 
		{			
			String variableName = String.valueOf(args.get(0));
			String locationPath = String.valueOf(args.get(1));
			
			Object jdom = getBpelExecutionContext().getVariableData().getValue(variableName);
			try {
				if(args.size() > 2)
				{
					int index = ExpressionUtils.getIntValue(args.get(2));
					
					String reply = JdomUtils.getNodeText(jdom, locationPath, index);
//					log.info("reply value = " + reply);
					return reply;
				}
				else
				{
					Document document;
					if(jdom instanceof Element){
						document = new Document();
						
						Element clone = (Element) ((Element)jdom).clone();
						document.setRootElement(clone);
					}
					else{
						document = (Document) jdom;
					}
					String reply = JdomUtils.getNodeText(document, locationPath);
//					log.info("reply value = " + reply);
			
					return reply;
				}
			} catch (Exception ex) {
				log.warn(ex);
				return "";
			}
		}
	}
	/*
	* Name : GetListAt
	* Desc : This function is GetListAt
	* Author : AIM Systems, Inc
	* Date : 2011.01.17
	*/
	private class GetListAt implements Function 
	{		
		
		public Object call(Context context, List args) throws RuntimeException 
		{			
			String listVariable = String.valueOf(args.get(0));
			Object indexVariable = args.get(1);
			List list = (List) getBpelExecutionContext().getVariableData().getValue(listVariable);
			Number idx = null;
			if(indexVariable instanceof String) {
				idx = (Number) getBpelExecutionContext().getVariableData().getValue((String) indexVariable);
			}else if(indexVariable instanceof Number){
				idx = (Number) indexVariable;
			}
			return list.get(idx.intValue());
		}
	}
	
	private class SetXmlElement implements Function 
	{
		/*
		* Name : call
		* Desc : This function is getValue after setNodeText
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context context, List args) throws RuntimeException 
		{			
			String variableName = String.valueOf(args.get(0));
			String locationPath = String.valueOf(args.get(1));
			String value = String.valueOf(args.get(2));
			
			org.jdom.Element element = (org.jdom.Element)getBpelExecutionContext().getVariableData().getValue(variableName);
			
			try {
				if(args.size() == 3)
				{
					JdomUtils.setNodeText(element, locationPath, value);
				}
				else if(args.size() > 3)
				{
					String nodeValue = String.valueOf(args.get(3));
	
					// jdom, parentPath, nodename, nodevalue
					JdomUtils.setNodeText(element, locationPath, value, nodeValue);
					
				}
				
				return null;

			} catch (Exception ex) {
				log.warn(ex);
				return null;
			}
		}
	}	
	
	private class SetXmlData implements Function 
	{
		/*
		* Name : call
		* Desc : This function is getValue after setNodeText
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context context, List args) throws RuntimeException 
		{			
			String variableName = String.valueOf(args.get(0));
			String locationPath = String.valueOf(args.get(1));
			String value = String.valueOf(args.get(2));
			
			org.jdom.Document jdom = (org.jdom.Document)getBpelExecutionContext().getVariableData().getValue(variableName);
			
			try {
				if(args.size() == 3)
				{
					JdomUtils.setNodeText(jdom, locationPath, value);
				}
				else if(args.size() > 3)
				{
					String nodeValue = String.valueOf(args.get(3));
	
					// jdom, parentPath, nodename, nodevalue
					JdomUtils.setNodeText(jdom, locationPath, value, nodeValue);
				}
				
				return null;

			} catch (Exception ex) {
				log.warn(ex);
				return null;
			}
		}
	}	
	
	private class AddElementForList implements Function 
	{
		/*
		* Name : call
		* Desc : This function is getValue after add Element
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context context, List args) throws RuntimeException 
		{
			try {
				if(args.size() == 4 || args.size() == 3) {
					String variableName = (String) args.get(0);
					String xpath = (String) args.get(1);
					Element newElement;
					if(args.get(2) instanceof Element) {
						newElement = (Element) args.get(2);
					}else {
						String nodeName = (String) args.get(2);
						String nodeText = (String) args.get(3);
						newElement = new Element(nodeName);
						newElement.setText(nodeText);
					}
					Object document = getBpelExecutionContext().getVariableData().getValue(variableName);
					
					Element element;
					try {
						element = (Element) XPath.selectSingleNode(document, xpath);
					} catch (JDOMException e) {
						log.error(e);
						return null;
					}
					
					element.addContent(newElement);
					
					return document;
				}
				else if(args.size() == 2) {
					Element parentElement = (Element) getBpelExecutionContext().getVariableData().getValue((String) args.get(0));
					Element element = (Element) args.get(1);
					parentElement.addContent(element);
					return parentElement;
				}
				return null;
				
			} catch (Exception ex) {
				log.error(ex);
				return null;
			}
		}
	}
	
	private class SetElement implements Function 
	{
		/*
		* Name : call
		* Desc : This function is getValue after setText
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context context, List args) throws RuntimeException 
		{
			String variableName = (String) args.get(0);
			String xpath = (String) args.get(1);
			String nodeText = (String) args.get(2);
			
			Object jdom = getBpelExecutionContext().getVariableData().getValue(variableName);
			
			Element element;
			try {
				Document document;
				if(jdom instanceof Element){
					document = new Document();
					
					Element clone = (Element) ((Element)jdom).clone();
					document.setRootElement(clone);
				}
				else{
					document = (Document) jdom;
				}
				element = (Element) XPath.selectSingleNode(document, xpath);
			} catch (JDOMException e) {
				log.error(e);
				return null;
			}
			element.setText(nodeText);
			
			return jdom;
			
		}
	}
	
	private class isNull implements Function
	{
		/*
		* Name : call
		* Desc : This function is check Null
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context ctx, List list) throws FunctionCallException
		{
			if (list.get(0) == null) return "true";
			double length = 0.0;
			String variableName = String.valueOf(list.get(0));
			Object variable = getBpelExecutionContext().getVariableData().getValue(variableName);
			length = ExpressionUtils.getTypesLength(variable);
			return (length == 0) ? "true" : "false";
		}
	}		

	private class ToDouble implements Function 
	{
		/*
		* Name : call
		* Desc : This function is Parse Double
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context ctx, List args) throws RuntimeException 
		{
			String value = "";
			try {
				value = args.get(0).toString();
				return Double.valueOf(value);
			} catch (Exception e) {
				log.error("Could not convert double [" + e.getMessage() + "]");
				return args.get(0);
			}			
		}
	}
	
	private class ToInteger implements Function
	{
		/*
		* Name : call
		* Desc : This function is Parse int
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context ctx, List args) throws RuntimeException 
		{
			String value = "";
			try {
				value = args.get(0).toString();
				
				int index = value.indexOf( "." );
				if ( index > 0 )
					value = value.substring( 0, index );
					
				return Integer.valueOf(value);
				
			} catch (Exception e) {
				log.error("Could not convert integer [" + e.getMessage() + "]");
				return args.get(0);
			}			
		}
	}
	
	private class ToLong implements Function
	{
		/*
		* Name : call
		* Desc : This function is Parse Long
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context ctx, List args) throws RuntimeException 
		{
			String value = "";
			try {
				value = args.get(0).toString();
				
				int index = value.indexOf( "." );
				if ( index > 0 )
					value = value.substring( 0, index );
					
				return Long.valueOf(value);
				
			} catch (Exception e) {
				log.error("Could not convert integer [" + e.getMessage() + "]");
				return args.get(0);
			}			
		}
	}
	private class GetXmlAttributeArray implements Function 
	{		
		/*
		* Name : call
		* Desc : This function is GetXmlAttributeArray
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context context, List args) throws RuntimeException 
		{			
			String variableName = String.valueOf(args.get(0));
			String locationPath = String.valueOf(args.get(1));
			
			org.jdom.Document jdom = (org.jdom.Document)getBpelExecutionContext().getVariableData().getValue(variableName);
			Object[] object = null;
			try {
				List<String> list = JdomUtils.getNodeTextList(jdom,locationPath);

	            object = new Object[list.size()];
				                                 
			    for( int i=0; i< list.size(); i++ )
			    {
			    	object[i] = list.get(i);
			    }
				return object;

			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
	}
	
	private class GetXmlChildNodeValueArray implements Function 
	{	
		/*
		* Name : call
		* Desc : This function is GetXmlChildNodeValueArray
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context context, List args) throws RuntimeException 
		{	
			String variableName = String.valueOf(args.get(0));
			String locationPath = String.valueOf(args.get(1));
			
			org.jdom.Document jdom = (org.jdom.Document)getBpelExecutionContext().getVariableData().getValue(variableName);
			Object[] objList = null;
						
			try 
			{
			   List<Element> list =  JdomUtils.getNode(jdom, locationPath).getChildren();
			   
			   if( list.size() != 0 )
			   {
			       objList = new Object[list.size()];
			      
		           for( int i=0; i<list.size(); i++ )
		           {
		    	      objList[i] = list.get(i).getValue();
		           }
			   }
			 		    	
			   return objList;
		    } 
			catch (Exception ex) 
			{
			   throw new RuntimeException(ex);
		    }	
		}
	}
	
	private class GetNamedValue implements Function 
	{		
		/*
		* Name : call
		* Desc : This function is GetNamedValue
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Map<String, String>  call(Context context, List args) throws RuntimeException

		{	
			String variableName = String.valueOf(args.get(0));
			String locationPath = String.valueOf(args.get(1));
	
			org.jdom.Document jdom = (org.jdom.Document)getBpelExecutionContext().getVariableData().getValue(variableName);
			Map<String, String> udfs = null; 
			
			try 
			{
			   Element E = JdomUtils.getNode(jdom, locationPath);

			   if( E != null)
			   {
			      List<Element> list =  JdomUtils.getNode(jdom, locationPath).getChildren();
			   					
			      if( list.size() != 0 )
			      {
			    	  udfs = new HashMap<String, String>(list.size());
			      
		              for( int i=0; i<list.size(); i++ )
		              {
		            	  udfs.put(list.get(i).getName(), list.get(i).getText());
		              }		           		           	           
			      }
			   }
			   else 
				   udfs = new HashMap<String, String>();
			   
			   return udfs;	
		    } 
			catch (Exception ex) 
			{
			   throw new RuntimeException(ex);
		    }
		}
	}
	
	private class GetXmlDataTimeStamp implements Function 
	{		
		/*
		* Name : call
		* Desc : This function is GetXmlDataTimeStamp
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Timestamp call(Context context, List args) throws RuntimeException 
		{			
			String variableName = String.valueOf(args.get(0));
			String locationPath = String.valueOf(args.get(1));
			Timestamp timeStamp =null;
			
			org.jdom.Document jdom = (org.jdom.Document)getBpelExecutionContext().getVariableData().getValue(variableName);
			try {
				if(args.size() > 2)
				{
					int index = ExpressionUtils.getIntValue(args.get(2));
					
					String reply = JdomUtils.getNodeText(jdom, locationPath, index);
					
					timeStamp = ConvertUtil.convertToTimeStamp(reply);
					return timeStamp;
				}
				else
				{
					String reply = JdomUtils.getNodeText(jdom, locationPath);
					timeStamp = ConvertUtil.convertToTimeStamp(reply);
					return timeStamp;
				}
			} catch (Exception ex) {
				log.warn(ex);
				return timeStamp;
			}
		}
	}
	
	private class GetXmlDataInt implements Function 
	{		
		/*
		* Name : call
		* Desc : This function is GetXmlDataInt
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Integer call(Context context, List args) throws RuntimeException 
		{			
			String variableName = String.valueOf(args.get(0));
			String locationPath = String.valueOf(args.get(1));
			
			int iReply=0;
			org.jdom.Document jdom = (org.jdom.Document)getBpelExecutionContext().getVariableData().getValue(variableName);
			try {
				if(args.size() > 2)
				{
					int index = ExpressionUtils.getIntValue(args.get(2));
					
					String reply = JdomUtils.getNodeText(jdom, locationPath, index);
					
					iReply = Integer.parseInt(reply);
					return iReply;
				}
				else
				{
					String reply = JdomUtils.getNodeText(jdom, locationPath);
					iReply = Integer.parseInt(reply);
					return iReply;
				}
			} catch (Exception ex) {
				log.warn(ex);
				return iReply;
			}
		}
	}
	
	private class GetXmlElement implements Function 
	{		
		/*
		* Name : call
		* Desc : This function is GetXmlElement
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context context, List args) throws RuntimeException 
		{			
			String variableName = String.valueOf(args.get(0));
			String locationPath = String.valueOf(args.get(1));
			log.info("GetXmlElement : [variableName = " + variableName + "]," + "[locationPath = " + locationPath + "]");
			Object jdom = getBpelExecutionContext().getVariableData().getValue(variableName);
			Element replyElement = null;
			try {
				if(args.size() > 2)
				{
					int index = ExpressionUtils.getIntValue(args.get(2));
					
					replyElement = JdomUtils.getNode(jdom, locationPath, index);
					
					String  r = replyElement.getValue().toString();
					log.info("GetXmlElement : [values = "+ r +"]");
					return replyElement;
				}
				else
				{
					replyElement = (Element)XPath.selectSingleNode(jdom, locationPath);
					//replyElement = JdomUtils.getNode(jdom, locationPath);
					String  r1 = replyElement.getValue().toString();
					return replyElement;
				}
			} catch (Exception ex) {
				log.error("ERR - GetXmlElement : " + ex);
				return replyElement;
			}
		}
	}
	
	private class GetArrayValue implements Function 
	{
		/*
		* Name : call
		* Desc : This function is GetArrayValue
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context ctx, List args) throws RuntimeException 
		{
			String variableName = String.valueOf(args.get(0));
			int idx1 = 0, idx2 = 0;
			try {
				idx1 = ConvertUtil.Object2Int(args.get(1));
				idx2 = ConvertUtil.Object2Int(args.get(2));
			} catch (Exception e) {
				idx2 = -1;
			}
			Object vValue = null;
			try {
				vValue = getBpelExecutionContext().getVariableData().getValue(variableName);
			} catch (Exception e) {
				variableName = variableName + "[" + String.valueOf(idx1) + "]";
				if (idx2 >= 0)
					variableName = variableName + "[" + String.valueOf(idx2) + "]";
				try {
					vValue = getBpelExecutionContext().getVariableData().getValue(variableName);
				} catch (Exception ex) {
					log.error(ex);
				}
			}
			
			try {
				if (vValue instanceof String)
				{
					log.info(vValue.toString() + " is by " + variableName);
					return vValue;
				}
				else if (vValue instanceof String[])
				{
					log.info(((String[])vValue)[idx1] + " is by " + variableName + "[" + idx1 + "]");
					return ((String[])vValue)[idx1];
				}
				else if (vValue instanceof String[][])
				{
					log.info(((String[][])vValue)[idx1][idx2] + " is by " + variableName + "[" + idx1 + "][" + idx2 + "]");
					return ((String[][])vValue)[idx1][idx2];
				}
				else if (vValue instanceof List)
				{
					log.info(((List)vValue).get(idx1) + " is by " + variableName + "[" + idx1 + "]");
					return ((List)vValue).get(idx1);
				}
				else {
					log.error("no matched input index : variable [" + variableName + "]");
					return "";
				}
			} catch (Exception e) {
				log.error(e);
				return null;
			}
		}
	}	
	
	private class GetDataVariable implements Function 
	{		
		/*
		* Name : call
		* Desc : This function is GetDataVariable
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context context, List args) throws RuntimeException 
		{	
			Object objectData   = args.get(0);
			String variableName = String.valueOf(args.get(1));
			
			try 
			{	
				String className =  objectData.getClass().getName().toString();
				Class clazz = Class.forName(className);
				
				Field [] fields = objectData.getClass().getDeclaredFields();
				AccessibleObject.setAccessible(fields, true);
				Object fieldData =	null;
				
				for ( int i = 0; i < fields.length; i++ )
				{
					if ( fields[i].getName().equals(variableName))
					{
						fieldData =  fields[i].get(objectData);
						break;
					}
				}
				
				
				if ( fieldData.getClass().getName().equals("java.lang.String"))
				{
					String data = fieldData.toString();
					return data;
				}
				else if ( fieldData.getClass().getName().equals("long"))
				{
					long data = Long.parseLong(fieldData.toString());
					return data;
				}
				else if ( fieldData.getClass().getName().equals("double"))
				{
					double data = Double.parseDouble(fieldData.toString());
					return data;
				}
				else if ( fieldData.getClass().getName().equals("java.sql.Timestamp"))
				{
					java.sql.Timestamp data = Timestamp.valueOf(fieldData.toString());
					
					return data;
				}
				else
				{
					return null;
				}
				
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
	}
	
	private class GetLength implements Function 
	{
		/*
		* Name : call
		* Desc : This function is GetLength
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context ctx, List args) throws RuntimeException 
		{
			
			if (args.get(0) == null) return Double.valueOf("0");
			
			String variableName = String.valueOf(args.get(0));
			boolean isRegister = getBpelExecutionContext().getVariableData().getVariables().containsKey(variableName);
			
			Object variable = getBpelExecutionContext().getVariableData().getValue(variableName);
			
			if (variable instanceof String[][])
			{
				String[][] objResult = (String[][])variable;

				try {
					String indx = String.valueOf(args.get(1));
					return ExpressionUtils.toDouble(objResult[Integer.valueOf(indx)].length);
					
				} catch (Exception e) {
					return ExpressionUtils.toDouble(objResult.length);
				}
				
			}
			return ExpressionUtils.getTypesLength(variable);
		}
	}

	private class GetXMLArraySize implements Function 
	{
		/*
		* Name : call
		* Desc : This function is GetXMLArraySize
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context ctx, List args) throws RuntimeException 
		{
			String variableName = String.valueOf(args.get(0));
			String locationPath = String.valueOf(args.get(1));
			
			org.jdom.Document doc = (org.jdom.Document)getBpelExecutionContext().getVariableData().getValue(variableName);

			int size;
			try {
				size = XPath.selectNodes(doc, locationPath).size();
			} catch (JDOMException e) {
				size = -1;
			}
			return Integer.parseInt(String.valueOf(size));
		}
	}


	private class GetXMLArrayElement implements Function 
	{
		/*
		* Name : call
		* Desc : This function is GetXMLArrayElement
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context ctx, List args) throws RuntimeException 
		{
			String variableName = String.valueOf(args.get(0));
			String locationPath = String.valueOf(args.get(1));
			String index 		= String.valueOf(args.get(2));
			
			org.jdom.Document doc = (org.jdom.Document)getBpelExecutionContext().getVariableData().getValue(variableName);
			locationPath += String.format("[%s]", index);

			Element element = null;

			try {
				element =  JdomUtils.getNode(doc, locationPath);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return element;
		}
	}

	
	private class GetXMLArrayValue implements Function 
	{
		/*
		* Name : call
		* Desc : This function is GetXMLArrayValue
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context ctx, List args) throws RuntimeException 
		{
			String variableName = String.valueOf(args.get(0));
			String locationPath = String.valueOf(args.get(1));
			String index 		= String.valueOf(args.get(2));
			String nodeName 	= String.valueOf(args.get(3));
			
			log.info("GetXMLArrayValue : [variableName = "+ variableName +"], [locationPath = "+ locationPath +"], [index = "+ index +"], [nodeName = "+ nodeName +"] ");
			org.jdom.Document doc = (org.jdom.Document)getBpelExecutionContext().getVariableData().getValue(variableName);

			locationPath += String.format("[%s]/%s", index , nodeName);
			
			try {
				return ((Element)XPath.selectSingleNode(doc, locationPath)).getValue();
			} catch (Exception e) {
				return "";
			}
		}
	}

	private class SetNVSeq implements Function 
	{
		/*
		* Name : call
		* Desc : This function is SetNVSeq
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context ctx, List args) throws RuntimeException 
		{
			String objectType   = String.valueOf(args.get(0));
			String variableName = String.valueOf(args.get(1));		
			
			org.jdom.Element element = (org.jdom.Element)getBpelExecutionContext().getVariableData().getValue(variableName);
			
			Map<String, String> map = new HashMap<String, String>();
				
			ArrayList<ObjectAttributeDef> objectAttributeDefs 
			  = ( ArrayList<ObjectAttributeDef> )greenFrameServiceProxy.getObjectAttributeMap().getAttributeNames(objectType, "ExtendedC");
	
			if ( objectAttributeDefs != null )
			{
				String name = "";
				String value = "";
				
				for ( int i = 0; i < objectAttributeDefs.size(); i++ )
				{
					name = objectAttributeDefs.get(i).getAttributeName();
					
					if ( element != null )
					{
						for ( int j = 0; j < element.getContentSize(); j++ )
						{
							if ( element.getChildText(objectAttributeDefs.get(i).getAttributeName()) != null )
							{		
								value = element.getChildText(objectAttributeDefs.get(i).getAttributeName());
							}
						}
					}else
					{
						value = ""; 
					}
					if ( name.equals("") != true )
						map.put( name, value );
				}
			}
			return map;	
		}
	}
	
	private class SetXMLArrayValue implements Function 
	{
		/*
		* Name : call
		* Desc : This function is SetXMLArrayValue
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context ctx, List args) throws RuntimeException 
		{
			String variableName = String.valueOf(args.get(0));
			String locationPath = String.valueOf(args.get(1));
			String index 		= String.valueOf(args.get(2));
			Element chindElement = null;
			if(args.size() == 4) {
				chindElement = (Element) args.get(3);
			}else if(args.size() == 5) {
				String nodeName 	= String.valueOf(args.get(3));
				String nodeValue    = String.valueOf(args.get(4));
				chindElement = new Element(nodeName);
				chindElement.setText(nodeValue);
			}
			
			Object doc = getBpelExecutionContext().getVariableData().getValue(variableName);

			locationPath += String.format("[%s]", index);
			
//			System.out.println("locationPath = " + locationPath + " " + "Index = " + index + " " + "Cnt = " + args.get(4));

			try {
				Element element = (Element)XPath.selectSingleNode(doc, locationPath);
				element.addContent(chindElement);
				return "";
			} catch (Exception e) {
				return "";
			}
		}
	}
	
	/*
	public static Element getElement( Document doc, String xpath, int idx)
	{
		xpath += String.format("[%d]", idx);
		return XPath.selectSingleNode(doc, xpath);
	}

	public static String getElement( Document doc, String xpath, int idx, String child)
	{
		xpath += String.format("[%d]/%s", idx,child);
		return XPath.selectSingleNode(doc, xpath);
	}
	
	public static int getXmlArraySize(Document doc, String xpath) 
	{
		try {
			int size = XPath.selectNodes(doc, xpath).size();
			return size;
		} catch (JDOMException e) {
			return -1;
		}
	}*/
	
	
	
	
	private class GetChildElement implements Function 
	{		
		/*
		* Name : call
		* Desc : This function is GetChildElement
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context context, List args) throws RuntimeException 
		{			
			String variableName = String.valueOf(args.get(0));
			String childNodeName = String.valueOf(args.get(1));
			
			//org.jdom.Document jdom = (org.jdom.Document)getBpelExecutionContext().getVariableData().getValue(variableName);
			Element xmlElement = (Element)getBpelExecutionContext().getVariableData().getValue(variableName);
			
			try {
				
				return xmlElement.getChild(childNodeName);
				
			} catch (Exception ex) {
				log.error(ex);
				return null;
			}
		}	
	}

	private class RemoveXmlNode implements Function 
	{		
		/*
		* Name : call
		* Desc : This function is RemoveXmlNode
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context context, List args) throws RuntimeException 
		{			
			String variableName = String.valueOf(args.get(0));
			String locationPath = String.valueOf(args.get(1));
			String childNodeName = String.valueOf(args.get(2));
			Object jdom = getBpelExecutionContext().getVariableData().getValue(variableName);
			
			try {
//				locationPath += "/" + childNodeName;
				Element findElement = 
					JdomUtils.getNode(jdom, locationPath);
				List<Element> children = findElement.getChildren(childNodeName);
				
				while (children.size() > 0)
				{
					children.get(0).detach();
				}
				/*
				for (Element childElement : children) {
					childElement.detach();
				}
				*/
				
				    /*
					Element detachElement = 
						JdomUtils.getNode(jdom, locationPath);
					
					if(detachElement.getChildren().size() > 0)
						detachElement.removeChild(childNodeName);
					*/
					
					return null;
				
			} catch (Exception ex) {
				log.warn("Can Not Find Node Id : [" + childNodeName + "] " + ex.getMessage());
				return "";
			}
		}
	}
	
	private class DetachXmlNode implements Function 
	{		
		/*
		* Name : call
		* Desc : This function is DetachXmlNode
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context context, List args) throws RuntimeException 
		{			
			String variableName = String.valueOf(args.get(0));
			String locationPath = String.valueOf(args.get(1));
			Object jdom = getBpelExecutionContext().getVariableData().getValue(variableName);
			
			try {
					Element detachElement = 
						JdomUtils.getNode(jdom, locationPath);
					
					if(detachElement.getChildren().size() > 0)
						detachElement.detach();
					return null;
				
			} catch (Exception ex) {
				log.error(ex);
				return "";
			}
		}
	}
	
	private class GetXmlElementChildList implements Function 
	{		
		/*
		* Name : call
		* Desc : This function is GetXmlElementChildList
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context context, List args) throws RuntimeException 
		{			
			String variableName = String.valueOf(args.get(0));
			String locationPath = String.valueOf(args.get(1));
			
			//org.jdom.Document jdom = (org.jdom.Document)getBpelExecutionContext().getVariableData().getValue(variableName);
			Object xmlObject = getBpelExecutionContext().getVariableData().getValue(variableName);
			
			Element replyElement = null;
			try {
				if(args.size() > 2)
				{
					int index = ExpressionUtils.getIntValue(args.get(2));
					
					replyElement = JdomUtils.getNode(xmlObject, locationPath, index);
					
					return replyElement.getChildren();
				}
				else
				{
					replyElement = JdomUtils.getNode(xmlObject, locationPath);
					return replyElement.getChildren();
				}
			} catch (Exception ex) {
				log.error(ex);
				return null;
			}
		}
	}
	

	private class GetXmlElementArrayValue implements Function 
	{
		/*
		* Name : call
		* Desc : This function is GetXmlElementArrayValue
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context context, List args) throws RuntimeException 
		{			
			String variableName = String.valueOf(args.get(0));
			String locationPath = String.valueOf(args.get(1));
			int index = ExpressionUtils.getIntValue(args.get(2));
			
			//org.jdom.Document jdom = (org.jdom.Document)getBpelExecutionContext().getVariableData().getValue(variableName);
			Object xmlObject = getBpelExecutionContext().getVariableData().getValue(variableName);
			
			Element replyElement = null;
			try {
				if(args.size() > 3)
				{
					String nodeName = String.valueOf(args.get(3));
					
					replyElement = JdomUtils.getNode(xmlObject, locationPath, index);
					return replyElement.getChildText(nodeName);
				}
				else
				{
					replyElement = JdomUtils.getNode(xmlObject, locationPath, index);
					return replyElement;
				}
			} catch (Exception ex) {
				log.error(ex);
				return null;
			}
		}
	}
	
	
	private class GetXmlElementChildArray implements Function 
	{		
		/*
		* Name : call
		* Desc : This function is GetXmlElementChildArray
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context context, List args) throws RuntimeException 
		{			
			String variableName = String.valueOf(args.get(0));
			String locationPath = String.valueOf(args.get(1));
			
			//org.jdom.Document jdom = (org.jdom.Document)getBpelExecutionContext().getVariableData().getValue(variableName);
			Object xmlObject = getBpelExecutionContext().getVariableData().getValue(variableName);
			
			Element replyElement = null;
			try {
				if(args.size() > 2)
				{
					int index = ExpressionUtils.getIntValue(args.get(2));
					
					replyElement = JdomUtils.getNode(xmlObject, locationPath, index);
					
					return replyElement.getChildren().toArray(new Element[0]);
				}
				else
				{
					replyElement = JdomUtils.getNode(xmlObject, locationPath);
					return replyElement.getChildren().toArray(new Element[0]);
				}
			} catch (Exception ex) {
				log.error(ex);
				return null;
			}
		}
	}
	private class GetElementChildrenTextList implements Function 
	{		
		/*
		* Name : call
		* Desc : This function is GetElementChildrenTextList
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context context, List args) throws RuntimeException 
		{			
			String variableName = String.valueOf(args.get(0));
			String xpath = String.valueOf(args.get(1));
			
			//org.jdom.Document jdom = (org.jdom.Document)getBpelExecutionContext().getVariableData().getValue(variableName);
			Object xmlElement = getBpelExecutionContext().getVariableData().getValue(variableName);
			
			try {
				
				List<String> textList = new ArrayList<String>();
				List<Element> list =  XPath.selectNodes(xmlElement, xpath);
				for (Element element : list) {
					textList.add(element.getTextTrim());
				}
				return textList;
			} catch (Exception ex) {
				log.error(ex);
				return "";
			}
		}
	}
	
	private class GetElementChildText implements Function 
	{		
		/*
		* Name : call
		* Desc : This function is GetElementChildText
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context context, List args) throws RuntimeException 
		{			
			String variableName = String.valueOf(args.get(0));
			String childNodeName = String.valueOf(args.get(1));
			
			//org.jdom.Document jdom = (org.jdom.Document)getBpelExecutionContext().getVariableData().getValue(variableName);
			Element xmlElement = (Element)getBpelExecutionContext().getVariableData().getValue(variableName);
			
			try {

				return xmlElement.getChildText(childNodeName);
				
			} catch (Exception ex) {
				log.error(ex);
				return "";
			}
		}
	}
	
	private class Append implements Function
	{
		/*
		* Name : call
		* Desc : This function is Append
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context context, List args) throws RuntimeException
		{
			try
            {
				Object prefix = (String)args.get(0);
				Object suffix = (String)args.get(1);
				
                return String.valueOf(prefix) + String.valueOf(suffix);
            }
            catch(Exception ex)
            {
            	log.error(ex);
            	
				return "";
            }
		}
	}
	
	private class ToString implements Function
	{
		/*
		* Name : call
		* Desc : This function is ToString
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context context, List args) throws RuntimeException
		{
			Object value = null;
			StringBuilder sb = new StringBuilder();
			for (Object object : args) {
				try {
					value = object;
					if(value  == null)
					{
						return "";
					}
					else
					{
						if( value instanceof String && args.size() == 1)
						{
							String variableName = value.toString();
							value = getBpelExecutionContext().getVariableData().getValue(variableName);
							sb.append( String.valueOf(value));
						}
						else if (value instanceof Timestamp)
						{
							sb.append( ConvertUtil.toString((Timestamp)value));
						}
						else if(value instanceof Double)
						{
							String tempVariableStr = String.valueOf(value);
							int delemiterPos = StringUtils.indexOf(tempVariableStr, '.'); 
							if(delemiterPos != -1)
							{
								sb.append(StringUtils.substringBefore(tempVariableStr, "."));
							}
							else
							{
								sb.append(tempVariableStr);	
							}
						}
						else
						{
							sb.append(String.valueOf(value));
						}
					}
					
					
				} catch (Exception e) {
					log.error(e);
					return args.get(0);
				}
				
			}
			return sb.toString();
		}
	}
	
	

	private class GetTimeKey implements Function 
	{		
		/*
		* Name : call
		* Desc : This function is GetTimeKey
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context context, List args) throws RuntimeException 
		{	
			return ConvertUtil.getCurrTimeKey();
		}
	}
	
	private class GetDifferTimeInSecond implements Function 
	{		
		/*
		* Name : call
		* Desc : This function is GetDifferTimeInSecond
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context context, List args) throws RuntimeException 
		{			
			Object firstTime = args.get(0);
			Object secondTime = args.get(1);
			
			try {
				if(firstTime instanceof Timestamp && secondTime instanceof Timestamp)
				{
					Timestamp tempFTimeStamp = (Timestamp)firstTime;
					Timestamp tempSTimeStamp = (Timestamp)secondTime;
					return (tempFTimeStamp.getTime() - tempSTimeStamp.getTime()) / 1000;
					
				}
				else if(firstTime instanceof Date && secondTime instanceof Date)
				{
					Date tempFTimeStamp = (Date)firstTime;
					Date tempSTimeStamp = (Date)secondTime;
					return (tempFTimeStamp.getTime() - tempSTimeStamp.getTime()) / 1000;
				}
				else
				{
					return null;
				}
			} catch (Exception e) {
				log.error(e);
				return null;
			}
		}
	}
	
	private class GetXmlElementArraySize implements Function
	{
		/*
		* Name : call
		* Desc : This function is GetXmlElementArraySize
		* Author : AIM Systems, Inc
		* Date : 2011.01.17
		*/
		public Object call(Context ctx, List args) throws RuntimeException 
		{
			String variableName = String.valueOf(args.get(0));
			String locationPath = String.valueOf(args.get(1));
			
//			org.jdom.Document doc = (org.jdom.Document)getBpelExecutionContext().getVariableData().getValue(variableName);
			org.jdom.Element element = (org.jdom.Element)getBpelExecutionContext().getVariableData().getValue(variableName);

			int size = 0;
			try {
				//size = XPath.selectNodes(element, locationPath).size();
				size = JdomUtils.getNode(element, locationPath).getChildren().size();
			} catch (JDOMException e) {
				size = -1;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return size;
		}
	}	
}



class ExpressionUtils
{
	/*
	* Name : getIntValue
	* Desc : This function is getIntValue
	* Author : AIM Systems, Inc
	* Date : 2011.01.17
	*/
	public static int getIntValue(Object obj)
	{
		int value;
		if(obj instanceof Number)
			value = ((Number)obj).intValue();
		else
			value = Integer.parseInt(obj.toString());
		return value;
	}
	/*
	* Name : getTypesLength
	* Desc : This function is getTypesLength
	* Author : AIM Systems, Inc
	* Date : 2011.01.17
	*/
	public static double getTypesLength(Object objData)
	{
		if (objData == null) return Double.valueOf("0");
		if (objData instanceof String[][])
		{
			try {
				return ((String[][])objData)[0].length;
			} catch (Exception e) {
				return ((String[][])objData).length;
			}
		}
		else if (objData instanceof List)
			return ((List)objData).size();
		else if (objData instanceof String)
			return Double.valueOf((String.valueOf(objData).length()));
		else if(objData instanceof Object[]){
			return ((Object[])objData).length;
		} else if(objData instanceof Collection) {
			return ((Collection) objData).size();
		} else if(objData instanceof Map){
			return ((Map) objData).values().size();
		} else {
			return 1;
		}		
	}
	/*
	* Name : toDouble
	* Desc : This function is toDouble
	* Author : AIM Systems, Inc
	* Date : 2011.01.17
	*/
	public static Object toDouble(Object data) 
	{
		try {
			return Double.valueOf(data.toString());
		} catch (Exception e) {
			return data;
		}
	}
}