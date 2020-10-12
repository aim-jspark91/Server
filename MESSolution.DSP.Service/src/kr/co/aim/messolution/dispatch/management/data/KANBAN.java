package kr.co.aim.messolution.dispatch.management.data;
import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "CT", divider="_")
public class KANBAN extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="StockerName", type="Key", dataType="String", initial="", history="")
	private String StockerName;

	@CTORMTemplate(seq = "2", name="ProductSpecName", type="Key", dataType="String", initial="", history="")
	private String ProductSpecName;
	
	@CTORMTemplate(seq = "3", name="ProcessOperationName", type="Key", dataType="String", initial="", history="")
	private String ProcessOperationName;
	
	@CTORMTemplate(seq = "4", name="KanBan", type="Number", dataType="String", initial="", history="")
	private Number KanBan;
	
	@CTORMTemplate(seq = "5", name="KanbanFlag", type="Column", dataType="String", initial="", history="")
	private String KanbanFlag;
	
	@CTORMTemplate(seq = "6", name="MinKanBan", type="Number", dataType="String", initial="", history="")
	private Number MinKanBan;

	public KANBAN()
	{
		
	}
	
	public KANBAN(String StockerName, String ProductSpecName, String ProcessOperationName)
	{
		setStockrName(StockerName);
		setProductSpecName(ProductSpecName);
		setProcessOperationName(ProcessOperationName);
	}
	
	public String getStockrName() {
		return StockerName;
	}

	public void setStockrName(String StockerName) {
		this.StockerName = StockerName;
	}

	public String getProductSpecName() {
		return ProductSpecName;
	}

	public void setProductSpecName(String ProductSpecName) {
		this.ProductSpecName = ProductSpecName;
	}

	public String getProcessOperationName() {
		return ProcessOperationName;
	}

	public void setProcessOperationName(String ProcessOperationName) {
		this.ProcessOperationName = ProcessOperationName;
	}
	
	public Number getKanBan() {
		return KanBan;
	}

	public void setKanBan(Number KanBan) {
		this.KanBan = KanBan;
	}

	public String getKanbanFlag() {
		return KanbanFlag;
	}

	public void setKanbanFlag(String KanbanFlag) {
		this.KanbanFlag = KanbanFlag;
	}

	public Number getMinKanBan() {
		return MinKanBan;
	}

	public void setMinKanBan(Number minKanBan) {
		MinKanBan = minKanBan;
	}
 
}
