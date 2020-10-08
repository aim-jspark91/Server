package kr.co.aim.messolution.fgms.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ShipRequestPS extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="invoiceDetailNo", type="Key", dataType="String", initial="", history="")
	private String invoiceDetailNo;

	@CTORMTemplate(seq = "2", name="invoiceNo", type="Key", dataType="String", initial="", history="")
	private String invoiceNo;
	
	@CTORMTemplate(seq = "3", name="boxQuantity", type="Column", dataType="Number", initial="", history="")
	private Number boxQuantity;
	
	@CTORMTemplate(seq = "4", name="palletQuantity", type="Column", dataType="Number", initial="", history="")
	private Number palletQuantity;
	
	@CTORMTemplate(seq = "5", name="productSpecName", type="Column", dataType="String", initial="", history="")
	private String productSpecName;
	
	@CTORMTemplate(seq = "6", name="productQuantity", type="Column", dataType="Number", initial="", history="")
	private Number productQuantity;
	
	@CTORMTemplate(seq = "7", name="createUser", type="Column", dataType="String", initial="", history="")
	private String createUser;
	
	@CTORMTemplate(seq = "8", name="requestPanelQuantity", type="Column", dataType="Number", initial="", history="")
	private Number requestPanelQuantity;
	
	public String getInvoiceDetailNo() {
		return invoiceDetailNo;
	}

	public void setInvoiceDetailNo(String invoiceDetailNo) {
		this.invoiceDetailNo = invoiceDetailNo;
	}

	public String getInvoiceNo() {
		return invoiceNo;
	}

	public void setInvoiceNo(String invoiceNo) {
		this.invoiceNo = invoiceNo;
	}

	public Number getBoxQuantity() {
		return boxQuantity;
	}

	public void setBoxQuantity(Number boxQuantity) {
		this.boxQuantity = boxQuantity;
	}

	public Number getPalletQuantity() {
		return palletQuantity;
	}

	public void setPalletQuantity(Number palletQuantity) {
		this.palletQuantity = palletQuantity;
	}

	public String getProductSpecName() {
		return productSpecName;
	}

	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}

	public Number getProductQuantity() {
		return productQuantity;
	}

	public void setProductQuantity(Number productQuantity) {
		this.productQuantity = productQuantity;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Number getRequestPanelQuantity() {
		return requestPanelQuantity;
	}

	public void setRequestPanelQuantity(Number requestPanelQuantity) {
		this.requestPanelQuantity = requestPanelQuantity;
	}

	//instantiation
	public ShipRequestPS()
	{
		
	}
	
	public ShipRequestPS(String invoiceDetailNo, String invoiceNo)
	{
		setInvoiceDetailNo(invoiceDetailNo);
		setInvoiceNo(invoiceNo);
	}
	
}
