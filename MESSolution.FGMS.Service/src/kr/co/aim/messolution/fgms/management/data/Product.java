package kr.co.aim.messolution.fgms.management.data;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "FGMS", divider="_")
public class Product extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="productName", type="Key", dataType="String", initial="", history="")
	private String productName;

	@CTORMTemplate(seq = "2", name="lotName", type="Column", dataType="String", initial="", history="")
	private String lotName;
	
	@CTORMTemplate(seq = "3", name="processGroupName", type="Column", dataType="String", initial="", history="")
	private String processGroupName;
	
	@CTORMTemplate(seq = "4", name="productSpecName", type="Column", dataType="String", initial="", history="")
	private String productSpecName;
	
	@CTORMTemplate(seq = "5", name="productionType", type="Column", dataType="String", initial="", history="")
	private String productionType;
	
	@CTORMTemplate(seq = "6", name="productRequestName", type="Column", dataType="String", initial="", history="")
	private String productRequestName;
	
	@CTORMTemplate(seq = "7", name="productType", type="Column", dataType="String", initial="", history="")
	private String productType;
	
	@CTORMTemplate(seq = "8", name="subProductType", type="Column", dataType="String", initial="", history="")
	private String subProductType;
	
	@CTORMTemplate(seq = "9", name="productGrade", type="Column", dataType="String", initial="", history="")
	private String productGrade;
	
	@CTORMTemplate(seq = "10", name="crateName", type="Column", dataType="String", initial="", history="")
	private String crateName;
	
	@CTORMTemplate(seq = "11", name="stockState", type="Column", dataType="String", initial="", history="")
	private String stockState;
	
	@CTORMTemplate(seq = "12", name="domesticExport", type="Column", dataType="String", initial="", history="")
	private String domesticExport;
	
	@CTORMTemplate(seq = "13", name="outSource", type="Column", dataType="String", initial="", history="")
	private String outSource;

	@CTORMTemplate(seq = "14", name="originalFactory", type="Column", dataType="String", initial="", history="")
	private String originalFactory;
	
	@CTORMTemplate(seq = "15", name="durableName", type="Column", dataType="String", initial="", history="")
	private String durableName;

	//instantiation
	public Product()
	{
		
	}
	
	public Product(String productName)
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

	public String getProcessGroupName() {
		return processGroupName;
	}

	public void setProcessGroupName(String processGroupName) {
		this.processGroupName = processGroupName;
	}

	public String getProductSpecName() {
		return productSpecName;
	}

	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}

	public String getProductionType() {
		return productionType;
	}

	public void setProductionType(String productionType) {
		this.productionType = productionType;
	}

	public String getProductRequestName() {
		return productRequestName;
	}

	public void setProductRequestName(String productRequestName) {
		this.productRequestName = productRequestName;
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

	public String getStockState() {
		return stockState;
	}

	public void setStockState(String stockState) {
		this.stockState = stockState;
	}

	public String getDomesticExport() {
		return domesticExport;
	}

	public void setDomesticExport(String domesticExport) {
		this.domesticExport = domesticExport;
	}
	
	public String getOutSource() {
		return outSource;
	}

	public void setOutSource(String outSource) {
		this.outSource = outSource;
	}

	public String getOriginalFactory() {
		return originalFactory;
	}

	public void setOriginalFactory(String originalFactory) {
		this.originalFactory = originalFactory;
	}
	
	public String getDurableName() {
		return durableName;
	}

	public void setDurableName(String durableName) {
		this.durableName = durableName;
	}
}
