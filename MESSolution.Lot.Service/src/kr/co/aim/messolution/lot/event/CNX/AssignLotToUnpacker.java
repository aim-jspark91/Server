package kr.co.aim.messolution.lot.event.CNX;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;

import org.jdom.Document;

public class AssignLotToUnpacker extends SyncHandler {

	/**
	 * 141103 by swcho : ReserveLotList to ReserveLot
	 */
	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		//2019.02.28_hsryu_Delete all Logic.
		//NOT USE CLASS!

		return doc;
	}
}
