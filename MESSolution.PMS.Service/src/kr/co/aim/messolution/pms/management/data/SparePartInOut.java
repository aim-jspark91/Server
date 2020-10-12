package kr.co.aim.messolution.pms.management.data;
import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "PMS", divider="_")
public class SparePartInOut extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="orderNo", type="Key", dataType="String", initial="", history="")
	private String orderNo;
	
	@CTORMTemplate(seq = "2", name="partID", type="Key", dataType="String", initial="", history="")
	private String partID;

	@CTORMTemplate(seq = "3", name="orderType", type="Column", dataType="String", initial="", history="")
	private String orderType;
	
	@CTORMTemplate(seq = "4", name="orderReason", type="Column", dataType="String", initial="", history="")
	private String orderReason;
	
	@CTORMTemplate(seq = "5", name="orderStatus", type="Column", dataType="String", initial="", history="")
	private String orderStatus;
	
	@CTORMTemplate(seq = "6", name="orderUser", type="Column", dataType="String", initial="", history="")
	private String orderUser;
	
	@CTORMTemplate(seq = "7", name="orderDate", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp orderDate;
	
	@CTORMTemplate(seq = "8", name="approveUser", type="Column", dataType="String", initial="", history="")
	private String approveUser;
	
	@CTORMTemplate(seq = "9", name="approveDate", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp approveDate;

	@CTORMTemplate(seq = "10", name="orderQuantity", type="Column", dataType="Number", initial="", history="")
	private Number orderQuantity;
	
	@CTORMTemplate(seq = "11", name="RequestID", type="Column", dataType="String", initial="", history="")
	private String requestID;
	
	@CTORMTemplate(seq = "12", name="useType", type="Column", dataType="String", initial="", history="")
	private String useType;
	
	@CTORMTemplate(seq = "13", name="availableOrderQty", type="Column", dataType="Number", initial="", history="")
	private Number availableOrderQty;
	
	@CTORMTemplate(seq = "14", name="description", type="Column", dataType="String", initial="", history="")
	private String description;
	
	public SparePartInOut()
	{
		
	}
	
	public SparePartInOut(String orderNo, String partID)
	{
		setOrderNo(orderNo);
		setPartID(partID);
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	
	public String getPartID() {
		return partID;
	}

	public void setPartID(String partID) {
		this.partID = partID;
	}


	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public String getOrderReason() {
		return orderReason;
	}

	public void setOrderReason(String orderReason) {
		this.orderReason = orderReason;
	}

	public String getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}

	public String getOrderUser() {
		return orderUser;
	}

	public void setOrderUser(String orderUser) {
		this.orderUser = orderUser;
	}

	public Timestamp getOrderDate() {
		return orderDate;
	}
 
	public void setOrderDate(Timestamp orderDate) {
		this.orderDate = orderDate;
	}

	public String getApproveUser() {
		return approveUser;
	}

	public void setApproveUser(String approveUser) {
		this.approveUser = approveUser;
	}

	public Timestamp getApproveDate() {
		return approveDate;
	}

	public void setApproveDate(Timestamp approveDate) {
		this.approveDate = approveDate;
	}

	public Number getOrderQuantity() {
		return orderQuantity;
	}

	public void setOrderQuantity(Number orderQuantity) {
		this.orderQuantity = orderQuantity;
	}
	
	public String getRequestID() {
		return requestID;
	} 

	public void setRequestID(String requestID) {
		this.requestID = requestID;
	}
	
	public String getUseType() {
		return useType;
	} 

	public void setUseType(String useType) {
		this.useType = useType;
	}
	
	public Number getAvailableOrderQty() {
		return availableOrderQty;
	} 

	public void setAvailableOrderQty(Number availableOrderQty) {
		this.availableOrderQty = availableOrderQty;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
