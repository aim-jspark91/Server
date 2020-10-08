package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ShipProduct extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="productName", type="Key", dataType="String", initial="", history="")
	private String productName;

	@CTORMTemplate(seq = "2", name="lotName", type="Column", dataType="String", initial="", history="")
	private String lotName;
	
	@CTORMTemplate(seq = "3", name="durableName", type="Column", dataType="String", initial="", history="")
	private String durableName;
	
	@CTORMTemplate(seq = "4", name="boxName", type="Column", dataType="String", initial="", history="")
	private String boxName;
	
	@CTORMTemplate(seq = "5", name="palletName", type="Column", dataType="String", initial="", history="")
	private String palletName;	
	
	@CTORMTemplate(seq = "6", name="productRequestName", type="Column", dataType="String", initial="", history="")
	private String productRequestName;
	
	@CTORMTemplate(seq = "7", name="productSpecName", type="Column", dataType="String", initial="", history="")
	private String productSpecName;	
	
	@CTORMTemplate(seq = "8", name="productType", type="Column", dataType="String", initial="", history="")
	private String productType;
	
	@CTORMTemplate(seq = "9", name="subProductType", type="Column", dataType="String", initial="", history="")
	private String subProductType;
	
	@CTORMTemplate(seq = "10", name="productGrade", type="Column", dataType="String", initial="", history="")
	private String productGrade;
	
	@CTORMTemplate(seq = "11", name="crateName", type="Column", dataType="String", initial="", history="")
	private String crateName;
	
	@CTORMTemplate(seq = "12", name="receiveFlag", type="Column", dataType="String", initial="", history="")
	private String receiveFlag;
	
//	@CTORMTemplate(seq = "13", name="domesticExport", type="Column", dataType="String", initial="", history="")
//	private String domesticExport;
	
	@CTORMTemplate(seq = "14", name="productionType", type="Column", dataType="String", initial="", history="")
	private String productionType;

	//20170418 Add by yudan
	@CTORMTemplate(seq = "15", name="outSource", type="Column", dataType="String", initial="", history="")
	private String outSource;
		
	@CTORMTemplate(seq = "16", name="rohs", type="Column", dataType="String", initial="", history="")
	private String rohs;
		
	@CTORMTemplate(seq = "17", name="mto", type="Column", dataType="String", initial="", history="")
	private String mto;
		
	@CTORMTemplate(seq = "18", name="saleOrder", type="Column", dataType="String", initial="", history="")
	private String saleOrder;
		
	@CTORMTemplate(seq = "19", name="tradeType", type="Column", dataType="String", initial="", history="")
	private String tradeType;

	//instantiation
	public ShipProduct()
	{
		
	}
	
	public ShipProduct(String productName)
	{
		setProductName(productName);
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getLotName() {
		return lotName;
	}

	public void setLotName(String lotName) {
		this.lotName = lotName;
	}

	public String getDurableName() {
		return durableName;
	}

	public void setDurableName(String durableName) {
		this.durableName = durableName;
	}

	public String getBoxName() {
		return boxName;
	}

	public void setBoxName(String boxName) {
		this.boxName = boxName;
	}

	public String getPalletName() {
		return palletName;
	}

	public void setPalletName(String palletName) {
		this.palletName = palletName;
	}

	public String getProductRequestName() {
		return productRequestName;
	}

	public void setProductRequestName(String productRequestName) {
		this.productRequestName = productRequestName;
	}

	public String getProductSpecName() {
		return productSpecName;
	}

	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public String getSubProductType() {
		return subProductType;
	}

	public void setSubProductType(String subProductType) {
		this.subProductType = subProductType;
	}

	public String getProductGrade() {
		return productGrade;
	}

	public void setProductGrade(String productGrade) {
		this.productGrade = productGrade;
	}

	public String getCrateName() {
		return crateName;
	}

	public void setCrateName(String crateName) {
		this.crateName = crateName;
	}

	public String getReceiveFlag() {
		return receiveFlag;
	}

	public void setReceiveFlag(String receiveFlag) {
		this.receiveFlag = receiveFlag;
	}

//	public String getDomesticExport() {
//		return domesticExport;
//	}
//
//	public void setDomesticExport(String domesticExport) {
//		this.domesticExport = domesticExport;
//	}

	public String getProductionType() {
		return productionType;
	}

	public void setProductionType(String productionType) {
		this.productionType = productionType;
	}
	
	public String getOutSource() {
		return outSource;
	}

	public void setOutSource(String outSource) {
		this.outSource = outSource;
	}
	
	public String getROHS() {
		return rohs;
	}

	public void setROHS(String rohs) {
		this.rohs = rohs;
	}
	
	public String MTO() {
		return mto;
	}

	public void setMTO(String mto) {
		this.mto = mto;
	}
	
	public String SaleOrder() {
		return saleOrder;
	}

	public void setSaleOrder(String saleOrder) {
		this.saleOrder = saleOrder;
	}
	
	public String getTradeType() {
		return tradeType;
	}

	public void setTradeType(String tradeType) {
		this.tradeType = tradeType;
	}	
	
}
