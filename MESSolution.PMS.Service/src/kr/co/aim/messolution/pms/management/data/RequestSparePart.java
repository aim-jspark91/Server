package kr.co.aim.messolution.pms.management.data;

import java.util.Iterator;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "PMS", divider="_")
public class RequestSparePart extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="requestId", type="Key", dataType="String", initial="", history="")
	private String requestId;

	@CTORMTemplate(seq = "2", name="partId", type="Key", dataType="String", initial="", history="")
	private String partId;
	
	@CTORMTemplate(seq = "3", name="requestState", type="Column", dataType="String", initial="", history="")
	private String requestState;
	
	@CTORMTemplate(seq = "4", name="requestQuantity", type="Column", dataType="String", initial="", history="")
	private String requestQuantity;
	
	@CTORMTemplate(seq = "5", name="description", type="Column", dataType="String", initial="", history="")
	private String description;
	
	@CTORMTemplate(seq = "5", name="orderNo", type="Column", dataType="String", initial="", history="")
	private String orderNo;
			
	@CTORMTemplate(seq = "6", name="requestType", type="Column", dataType="String", initial="", history="")
	private String requestType;
	
	public RequestSparePart()
	{
		
	}
	
	public RequestSparePart(String requestId, String partID)
	{
		setRequestId(requestId);
		setPartId(partID);
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId =requestId;
	}

	public String getPartId() {
		return partId;
	}

	public void setPartId(String partId) {
		this.partId = partId;
	}
	
	public String getRequestState() {
		return requestState;
	} 

	public void setRequestState(String requestState) {
		this.requestState = requestState;
	}
	
	public String getRequestQuantity() {
		return requestQuantity;
	} 

	public void setRequestQuantity(String requestQuantity) {
		this.requestQuantity = requestQuantity;
	}
	
	public String getDescription() {
		return description;
	} 

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getOrderNo() {
		return orderNo;
	} 

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	
	public String getRequestType() {
		return requestType;
	} 

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public Iterator iterator() {
		return null;
	}
}