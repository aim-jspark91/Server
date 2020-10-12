/**
 * 
 */
package kr.co.aim.messolution.generic.object;

/**
 * @author sjlee
 *
 */
public class ObjectAttributeDef {
	
	/**
	 * @uml.property  name="typeName"
	 */
	private String typeName;
	/**
	 * @uml.property  name="attributeName"
	 */
	private String attributeName;
	/**
	 * @uml.property  name="position"
	 */
	private int    position;
	/**
	 * @uml.property  name="primaryKeyFlag"
	 */
	private String primaryKeyFlag;
	/**
	 * @uml.property  name="attributeType"
	 */
	private String attributeType;
	/**
	 * @uml.property  name="dataType"
	 */
	private String dataType;
	
	/**
	 * @param typeName
	 * @uml.property  name="typeName"
	 */
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	/**
	 * @return
	 * @uml.property  name="typeName"
	 */
	public String getTypeName() {
		return typeName;
	}
	
	/**
	 * @param attributeName
	 * @uml.property  name="attributeName"
	 */
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	/**
	 * @return
	 * @uml.property  name="attributeName"
	 */
	public String getAttributeName() {
		return attributeName;
	}
	
	/**
	 * @param position
	 * @uml.property  name="position"
	 */
	public void setPosition(int position) {
		this.position = position;
	}
	/**
	 * @return
	 * @uml.property  name="position"
	 */
	public int getPosition() {
		return position;
	}
	
	/**
	 * @param primaryKeyFlag
	 * @uml.property  name="primaryKeyFlag"
	 */
	public void setPrimaryKeyFlag(String primaryKeyFlag) {
		this.primaryKeyFlag = primaryKeyFlag;
	}
	/**
	 * @return
	 * @uml.property  name="primaryKeyFlag"
	 */
	public String getPrimaryKeyFlag() {
		return primaryKeyFlag;
	}
	
	/**
	 * @param attributeType
	 * @uml.property  name="attributeType"
	 */
	public void setAttributeType(String attributeType) {
		this.attributeType = attributeType;
	}
	/**
	 * @return
	 * @uml.property  name="attributeType"
	 */
	public String getAttributeType() {
		return attributeType;
	}
	
	/**
	 * @param dataType
	 * @uml.property  name="dataType"
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	/**
	 * @return
	 * @uml.property  name="dataType"
	 */
	public String getDataType() {
		return dataType;
	}


}
