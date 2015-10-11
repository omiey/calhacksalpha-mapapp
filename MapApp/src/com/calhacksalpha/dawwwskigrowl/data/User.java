package com.calhacksalpha.dawwwskigrowl.data;

import java.io.IOException;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapMarker;

import android.R;

public class User implements LocationAware{
	
	public String id;
	public MapMarker myMapMarker;
	private com.here.android.mpa.common.Image myImage;
	
	public User(String id, GeoCoordinate coordinate) throws IOException {
		this.id = id;
		myImage = new Image();
		myImage.setImageResource(R.drawable.ic_delete);
		myMapMarker = new MapMarker(coordinate, myImage);
	}

	@Override
	public void updateLocation(GeoCoordinate coordinate) {
		myMapMarker.setCoordinate(coordinate);
	}
	
}
