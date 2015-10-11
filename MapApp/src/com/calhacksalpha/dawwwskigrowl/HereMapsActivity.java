package com.calhacksalpha.dawwwskigrowl;

import java.io.IOException;
import java.lang.ref.WeakReference;

import com.calhacksalpha.dawwwskigrowl.data.LifeCycle;
import com.calhacksalpha.dawwwskigrowl.data.LocationEntities;
import com.calhacksalpha.dawwwskigrowl.data.User;
import com.calhacksalpha.dawwwskigrowl.data.UserEmailFetcher;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.common.PositioningManager.LocationMethod;
import com.here.android.mpa.common.PositioningManager.LocationStatus;
import com.here.android.mpa.common.PositioningManager.OnPositionChangedListener;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;
import com.pubnub.api.Pubnub;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

public class HereMapsActivity extends FragmentActivity {

	private static final String PUB_KEY = "pub-c-d61a4887-c713-4a84-893a-401a26d932ce";

	private static final String SUB_KEY = "sub-c-65b4c226-7000-11e5-81f9-0619f8945a4f";

	private Pubnub pubnub = new Pubnub(PUB_KEY, SUB_KEY);

	// map embedded in the map fragment
	private Map map = null;

	// map fragment embedded in this activity
	private MapFragment mapFragment = null;

	private Boolean paused = true;

	private LocationEntities locationEntities;

	private User mock;

	private LifeCycle lifeCycle;
	
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
					
					String selfId = UserEmailFetcher.getEmail(getApplicationContext());
					lifeCycle = new LifeCycle(pubnub, selfId , locationEntities);
					
					LifeCycle.setInstance(lifeCycle);
					lifeCycle.initPrimary();
					
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

//				GeoCoordinate coordinate = position.getCoordinate();
//				coordinate.setLatitude(coordinate.getLatitude() + 0.01);
//				coordinate.setLongitude(coordinate.getLongitude() + 0.01);
				locationEntities.updateLocation(mock.id, position.getCoordinate());
				lifeCycle.updatePrimary(position.getCoordinate().getLatitude(),
						position.getCoordinate().getLongitude());
				//				map.addMapObject(mock.myMapMarker);
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
	
	public void forward3(View v) {
		Intent intent = new Intent(this, InvitesActivity.class);
		startActivity(intent);
	}

}
