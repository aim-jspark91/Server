package kr.co.aim.messolution.dispatch.management.data;
import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "CT", divider="_")
public class EmptySTKBalance extends UdfAccessor {
	

	@CTORMTemplate(seq = "1", name="StockerName", type="Key", dataType="String", initial="", history="")
	private String StockerName;
	
	@CTORMTemplate(seq = "2", name="EmptyMAXCST", type="Column", dataType="Number", initial="", history="")
	private Number EmptyMAXCST;

	@CTORMTemplate(seq = "3", name="EmptyMINCST", type="Column", dataType="Number", initial="", history="")
	private Number EmptyMINCST;
 
	
	public EmptySTKBalance()
	{
		
	}
	
	public EmptySTKBalance(String StockerName )
	{
		setStockerName(StockerName);
	}

 	
	public String getStockerName() {
		return StockerName;
	}

	public void setStockerName(String StockerName) {
		this.StockerName = StockerName;
	}

 	public Number getEmptyMAXCST() {
		return EmptyMAXCST;
	}

	public void setEmptyMAXCST(Number EmptyMAXCST) {
		this.EmptyMAXCST = EmptyMAXCST;
	}

 	public Number getEmptyMINCST() {
		return EmptyMINCST;
	}

	public void setEmptyMINCST(Number EmptyMINCST) {
		this.EmptyMINCST = EmptyMINCST;
	}
}
