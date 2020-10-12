package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.greenframe.orm.info.access.UdfAccessor;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.messolution.extended.object.CTORMTemplate;

import org.apache.commons.net.ntp.TimeStamp;


public class MaterialConsumed extends UdfAccessor {
	
	public MaterialConsumed(){
		
	}		

	@CTORMTemplate(seq = "1", name="ProductName", type="Key", dataType="String", initial="", history="")
	private String ProductName;

	@CTORMTemplate(seq = "2", name="MaterialName", type="Key", dataType="String", initial="", history="")
	private String MaterialName;
	
	@CTORMTemplate(seq = "3", name="MaterialID", type="Key", dataType="String", initial="", history="")
	private String MaterialID;
	
	@CTORMTemplate(seq = "4", name="LotName", type="Column", dataType="String", initial="", history="")
	private String LotName;	

	@CTORMTemplate(seq = "5", name="Quantity", type="Column", dataType="Number", initial="", history="")
	private long Quantity;
	
	@CTORMTemplate(seq = "6", name="LASTEVENTNAME", type="Column", dataType="String", initial="", history="N")
	private String LASTEVENTNAME;
	
	@CTORMTemplate(seq = "7", name="LASTEVENTTIMEKEY", type="Column", dataType="String", initial="", history="N")
	private String LASTEVENTTIMEKEY;
	
	@CTORMTemplate(seq = "8", name="LASTEVENTTIME", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp LASTEVENTTIME;
	
	@CTORMTemplate(seq = "9", name="LASTEVENTUSER", type="Column", dataType="String", initial="", history="N")
	private String LASTEVENTUSER;
	
	@CTORMTemplate(seq = "10", name="LASTEVENTCOMMENT", type="Column", dataType="String", initial="", history="N")
	private String LASTEVENTCOMMENT;
	
	@CTORMTemplate(seq = "11", name="FactoryName", type="Column", dataType="String", initial="", history="")
	private String FactoryName;
	
	@CTORMTemplate(seq = "12", name="ProductSpecName", type="Column", dataType="String", initial="", history="")
	private String ProductSpecName;
	
	@CTORMTemplate(seq = "13", name="ProductSpecVersion", type="Column", dataType="String", initial="", history="")
	private String ProductSpecVersion;
	
	@CTORMTemplate(seq = "14", name="ProcessFlowName", type="Column", dataType="String", initial="", history="")
	private String ProcessFlowName;
	
	@CTORMTemplate(seq = "15", name="ProcessFlowVersion", type="Column", dataType="String", initial="", history="")
	private String ProcessFlowVersion;
	
	@CTORMTemplate(seq = "16", name="ProcessOperationName", type="Column", dataType="String", initial="", history="")
	private String ProcessOperationName;
	
	@CTORMTemplate(seq = "17", name="ProcessOperationVersion", type="Column", dataType="String", initial="", history="")
	private String ProcessOperationVersion;
	
	@CTORMTemplate(seq = "18", name="MachineName", type="Column", dataType="String", initial="", history="")
	private String MachineName;
	
	@CTORMTemplate(seq = "19", name="MaterialLocationName", type="Column", dataType="String", initial="", history="")
	private String MaterialLocationName;
		
	@CTORMTemplate(seq = "20", name="MaterialPosition", type="Column", dataType="String", initial="", history="")
	private String MaterialPosition;
	
	@CTORMTemplate(seq = "21", name="MaterialType", type="Column", dataType="String", initial="", history="")
	private String MaterialType;
		
	@CTORMTemplate(seq = "22", name="MaterialSubPosition", type="Column", dataType="String", initial="", history="")
	private String MaterialSubPosition;

	public String getProductName() {
		return ProductName;
	}

	public void setProductName(String productName) {
		ProductName = productName;
	}

	public String getMaterialName() {
		return MaterialName;
	}

	public void setMaterialName(String materialName) {
		MaterialName = materialName;
	}

	public String getMATERIALID() {
		return MaterialID;
	}

	public void setMATERIALID(String mATERIALID) {
		MaterialID = mATERIALID;
	}

	public String getLotName() {
		return LotName;
	}

	public void setLotName(String lotName) {
		LotName = lotName;
	}

	public long getQuantity() {
		return Quantity;
	}

	public void setQuantity(long quantity) {
		Quantity = quantity;
	}

	public String getLASTEVENTNAME() {
		return LASTEVENTNAME;
	}

	public void setLASTEVENTNAME(String lASTEVENTNAME) {
		LASTEVENTNAME = lASTEVENTNAME;
	}

	public String getLASTEVENTTIMEKEY() {
		return LASTEVENTTIMEKEY;
	}

	public void setLASTEVENTTIMEKEY(String lASTEVENTTIMEKEY) {
		LASTEVENTTIMEKEY = lASTEVENTTIMEKEY;
	}

	public Timestamp getLASTEVENTTIME() {
		return LASTEVENTTIME;
	}

	public void setLASTEVENTTIME(Timestamp lASTEVENTTIME) {
		LASTEVENTTIME = lASTEVENTTIME;
	}

	public String getLASTEVENTUSER() {
		return LASTEVENTUSER;
	}

	public void setLASTEVENTUSER(String lASTEVENTUSER) {
		LASTEVENTUSER = lASTEVENTUSER;
	}

	public String getLASTEVENTCOMMENT() {
		return LASTEVENTCOMMENT;
	}

	public void setLASTEVENTCOMMENT(String lASTEVENTCOMMENT) {
		LASTEVENTCOMMENT = lASTEVENTCOMMENT;
	}

	public String getFactoryName() {
		return FactoryName;
	}

	public void setFactoryName(String factoryName) {
		FactoryName = factoryName;
	}

	public String getProductSpecName() {
		return ProductSpecName;
	}

	public void setProductSpecName(String productSpecName) {
		ProductSpecName = productSpecName;
	}

	public String getProductSpecVersion() {
		return ProductSpecVersion;
	}

	public void setProductSpecVersion(String productSpecVersion) {
		ProductSpecVersion = productSpecVersion;
	}

	public String getProcessFlowName() {
		return ProcessFlowName;
	}

	public void setProcessFlowName(String processFlowName) {
		ProcessFlowName = processFlowName;
	}

	public String getProcessFlowVersion() {
		return ProcessFlowVersion;
	}

	public void setProcessFlowVersion(String processFlowVersion) {
		ProcessFlowVersion = processFlowVersion;
	}

	public String getProcessOperationName() {
		return ProcessOperationName;
	}

	public void setProcessOperationName(String processOperationName) {
		ProcessOperationName = processOperationName;
	}

	public String getProcessOperationVersion() {
		return ProcessOperationVersion;
	}

	public void setProcessOperationVersion(String processOperationVersion) {
		ProcessOperationVersion = processOperationVersion;
	}

	public String getMachineName() {
		return MachineName;
	}

	public void setMachineName(String machineName) {
		MachineName = machineName;
	}

	public String getMaterialLocationName() {
		return MaterialLocationName;
	}

	public void setMaterialLocationName(String materialLocationName) {
		MaterialLocationName = materialLocationName;
	}

	public String getMaterialPosition() {
		return MaterialPosition;
	}

	public void setMaterialPosition(String materialPosition) {
		MaterialPosition = materialPosition;
	}

	public String getMaterialType() {
		return MaterialType;
	}

	public void setMaterialType(String materialType) {
		MaterialType = materialType;
	}

	public String getMaterialSubPosition() {
		return MaterialSubPosition;
	}

	public void setMaterialSubPosition(String materialSubPosition) {
		MaterialSubPosition = materialSubPosition;
	}
	
	public void setEventInfo(EventInfo eventInfo){
		LASTEVENTNAME = eventInfo.getEventName();
		LASTEVENTTIMEKEY  = eventInfo.getEventTimeKey();
		LASTEVENTUSER = eventInfo.getEventUser();
		LASTEVENTTIME = eventInfo.getEventTime();
		LASTEVENTCOMMENT = eventInfo.getEventComment();
	}
		
}