package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ShipBox extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="boxName", type="Key", dataType="String", initial="", history="")
	private String boxName;

	@CTORMTemplate(seq = "2", name="durableName", type="Column", dataType="String", initial="", history="")
	private String durableName;
	
	@CTORMTemplate(seq = "3", name="palletName", type="Column", dataType="String", initial="", history="")
	private String palletName;
	
	@CTORMTemplate(seq = "4", name="productRequestName", type="Column", dataType="String", initial="", history="")
	private String productRequestName;
	
	@CTORMTemplate(seq = "5", name="materialType", type="Column", dataType="String", initial="", history="")
	private String materialType;	
	
	@CTORMTemplate(seq = "6", name="detailMaterialType", type="Column", dataType="String", initial="", history="")
	private String detailMaterialType;
	
	@CTORMTemplate(seq = "7", name="materialQuantity", type="Column", dataType="int", initial="", history="")
	private int materialQuantity;	
	
	@CTORMTemplate(seq = "8", name="productSpecName", type="Column", dataType="String", initial="", history="")
	private String productSpecName;
	
	@CTORMTemplate(seq = "9", name="receiveFlag", type="Column", dataType="String", initial="", history="")
	private String receiveFlag;
	
//	@CTORMTemplate(seq = "10", name="domesticExport", type="Column", dataType="String", initial="", history="")
//	private String domesticExport;
	
	@CTORMTemplate(seq = "11", name="productionType", type="Column", dataType="String", initial="", history="")
	private String productionType;
	
	//20170418 Add by yudan
	@CTORMTemplate(seq = "12", name="outSource", type="Column", dataType="String", initial="", history="")
	private String outSource;
	
	@CTORMTemplate(seq = "13", name="rohs", type="Column", dataType="String", initial="", history="")
	private String rohs;
	
	@CTORMTemplate(seq = "14", name="mto", type="Column", dataType="String", initial="", history="")
	private String mto;
	
	@CTORMTemplate(seq = "15", name="saleOrder", type="Column", dataType="String", initial="", history="")
	private String saleOrder;
	
	@CTORMTemplate(seq = "16", name="tradeType", type="Column", dataType="String", initial="", history="")
	private String tradeType;


	//instantiation
	public ShipBox()
	{
		
	}
	
	public ShipBox(String boxName)
	{
		setBoxName(boxName);
	}

	public String getBoxName() {
		return boxName;
	}

	public void setBoxName(String boxName) {
		this.boxName = boxName;
	}

	public String getDurableName() {
		return durableName;
	}

	public void setDurableName(String durableName) {
		this.durableName = durableName;
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

	public String getMaterialType() {
		return materialType;
	}

	public void setMaterialType(String materialType) {
		this.materialType = materialType;
	}

	public String getDetailMaterialType() {
		return detailMaterialType;
	}

	public void setDetailMaterialType(String detailMaterialType) {
		this.detailMaterialType = detailMaterialType;
	}

	public int getMaterialQuantity() {
		return materialQuantity;
	}

	public void setMaterialQuantity(int materialQuantity) {
		this.materialQuantity = materialQuantity;
	}

	public String getProductSpecName() {
		return productSpecName;
	}

	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
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
