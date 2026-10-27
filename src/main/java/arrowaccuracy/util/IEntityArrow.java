package arrowaccuracy.util;

import arrowaccuracy.client.StuckArrow;

public interface IEntityArrow {
	StuckArrow arrowaccuracy$getPending();
	void arrowaccuracy$setPending(StuckArrow pending);
	boolean arrowaccuracy$hasHitSomething();
	boolean arrowaccuracy$isInGround();
}