package com.calhacksalpha.dawwwskigrowl.data;

import com.here.android.mpa.common.GeoCoordinate;

public interface LocationAware {
	void updateLocation(GeoCoordinate coordinate);
}
