package com.calhacksalpha.dawwwskigrowl;

import java.io.IOException;
import java.lang.ref.WeakReference;

import com.calhacksalpha.dawwwskigrowl.data.LocationEntities;
import com.calhacksalpha.dawwwskigrowl.data.User;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.common.PositioningManager.LocationMethod;
import com.here.android.mpa.common.PositioningManager.LocationStatus;
import com.here.android.mpa.common.PositioningManager.OnPositionChangedListener;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class HereMapsActivity extends FragmentActivity {

	// map embedded in the map fragment
	private Map map = null;

	// map fragment embedded in this activity
	private MapFragment mapFragment = null;

	private Boolean paused = true;

	private LocationEntities locationEntities;

	private User mock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_here_maps);
		// Search for the map fragment to finish setup by calling init().
		mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapfragment);
		mapFragment.init(new OnEngineInitListener() {
			@Override
			public void onEngineInitializationCompleted(OnEngineInitListener.Error error) {
				if (error == OnEngineInitListener.Error.NONE) {
					// retrieve a reference of the map from the map fragment
					map = mapFragment.getMap();
					locationEntities = new LocationEntities(map);
					map.getPositionIndicator().setVisible(true);

					// Set the map center to the Vancouver region (no animation)
					GeoPosition lastKnownPosition = PositioningManager.getInstance().getLastKnownPosition();

					map.setCenter(lastKnownPosition.getCoordinate(), Map.Animation.NONE);
					// Set the zoom level to the average between min and max
					// map.setZoomLevel((map.getMaxZoomLevel() +
					// map.getMinZoomLevel()) / 2);
					map.setZoomLevel(map.getMaxZoomLevel() * 0.8);

					// Register positioning listener
					PositioningManager.getInstance()
							.addListener(new WeakReference<OnPositionChangedListener>(positionListener));

					try {
						mock = locationEntities.mock(lastKnownPosition.getCoordinate().getLatitude(),
								lastKnownPosition.getCoordinate().getLongitude());
						map.addMapObject(mock.myMapMarker);
					} catch (IOException e) {
						Log.e("", "", e);
					}

				} else {
					System.out.println("ERROR: Cannot initialize Map Fragment");
					// Log.e("R2D2", "", error.toString());
				}
			}
		});
	}

	// Define positioning listener
	private OnPositionChangedListener positionListener = new OnPositionChangedListener() {

		public void onPositionUpdated(LocationMethod method, GeoPosition position, boolean isMapMatched) {
			// set the center only when the app is in the foreground
			// to reduce CPU consumption
			if (!paused) {
				map.setCenter(position.getCoordinate(), Map.Animation.NONE);

				GeoCoordinate coordinate = position.getCoordinate();
				coordinate.setLatitude(coordinate.getLatitude() + 0.1);
				coordinate.setLongitude(coordinate.getLongitude() + 0.1);
				locationEntities.updateLocation(mock.id, coordinate);
				map.addMapObject(mock.myMapMarker);
			}
		}

		public void onPositionFixChanged(LocationMethod method, LocationStatus status) {
		}
	};

	// Resume positioning listener on wake up
	public void onResume() {
		super.onResume();
		paused = false;
		if (null != map)
			PositioningManager.getInstance().start(PositioningManager.LocationMethod.GPS_NETWORK);
	}

	// To pause positioning listener
	public void onPause() {
		if (null != map)
			PositioningManager.getInstance().stop();
		super.onPause();
		paused = true;
	}

	// To remove the positioning listener
	public void onDestroy() {
		// Cleanup
		PositioningManager.getInstance().removeListener(positionListener);
		map = null;
		super.onDestroy();
	}

}
