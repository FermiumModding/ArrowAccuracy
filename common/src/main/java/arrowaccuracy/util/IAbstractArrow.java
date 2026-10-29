package arrowaccuracy.util;

import arrowaccuracy.client.StuckArrow;

public interface IAbstractArrow {
	StuckArrow arrowaccuracy$getPending();
	void arrowaccuracy$setPending(StuckArrow pending);
	boolean arrowaccuracy$hasHitSomething();
	boolean arrowaccuracy$isInGround();
}