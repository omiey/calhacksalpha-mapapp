package com.calhacksalpha.dawwwskigrowl.data;

import java.util.ArrayList;
import java.util.List;

import com.here.android.mpa.common.GeoCoordinate;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import android.util.Log;

public class LifeCycle {

	protected static final String TAB = "\t";
	protected static final String INVITE = "invite";
	protected static final String ACCEPT = "accept";
	private static final String TAG = "R2D2";
	private Pubnub pubnub;
	private String selfId;
	public List<String> invites = new ArrayList<String>();
	private LocationEntities locationEntities;

	private static LifeCycle pseudoSingleton;

	String[] candidates = { "tapomay_dey@gmail_com", "ketkalesourabh129@gmail_com" };

	private void initDemo() {
		String other;
		if (this.selfId.equals(candidates[0])) {
			other = candidates[1];
		} else {
			other = candidates[0];
		}
		Log.i(TAG, "Current user is:" + this.selfId + ":" + "Adding invite for:" + other);
		invites.add(other);
		Log.i("R2D2", "Adding to invites:" + other);
	}

	public static void setInstance(LifeCycle lc) {
		pseudoSingleton = lc;
	}

	public static LifeCycle getInstance() {
		return pseudoSingleton;
	}

	public LifeCycle(Pubnub pn, String selfId, LocationEntities locationEntities) {
		this.pubnub = pn;
		this.selfId = selfId;
		this.locationEntities = locationEntities;
		this.selfId = this.selfId.replace(".", "_").trim();
		initDemo();
	}

	/**
	 * Start listening for invites or accepts
	 */
	public void initPrimary() {
		try {
			pubnub.subscribe(this.selfId, new Callback() {
				public void successCallback(String channel, Object message) {
					String msgStr = (String) message;
					String[] split = msgStr.split(TAB);
					if (split.length == 2) {
						String type = split[0];
						String otherHandle = split[1];
						if (type.equals(INVITE)) {
							addSecondary(otherHandle);
						} else if (type.equals(ACCEPT)) {
							initSecondary(otherHandle);
						}
					}
				}

				public void errorCallback(String channel, PubnubError error) {
					System.out.println(error.getErrorString());
				}
			});
			Log.i(TAG, "Started primary channel");
		} catch (PubnubException e) {
			e.printStackTrace();
		}
	}

	public void closePrimary() {
		pubnub.unsubscribe(this.selfId);
	}

	private void initSecondary(String otherChannel) {
		try {
			pubnub.subscribe(otherChannel, new Callback() {
				public void successCallback(String channel, Object message) {
					String msgStr = (String) message;
					String[] split = msgStr.split(TAB);
					if (split.length == 2) {
						String latitude = split[0];
						String longitude = split[1];
						GeoCoordinate coordinate = new GeoCoordinate(Double.parseDouble(latitude),
								Double.parseDouble(longitude));
						locationEntities.updateLocation(channel, coordinate);
					}
					Log.i(TAG, msgStr);
				}

				public void errorCallback(String channel, PubnubError error) {
					System.out.println(error.getErrorString());
				}
			});
			Log.i(TAG, "Started secondary");
		} catch (PubnubException e) {
			e.printStackTrace();
		}
	}

	private void addSecondary(String msg) {
		this.invites.add(msg);
	}

	public void acceptSecondary(String channel) {
		String data = ACCEPT + TAB + this.selfId;
		pubnub.publish(channel, data, new Callback() {
		});
		Log.i(TAG, "Accept secondary: " + data);
	}

	public void updatePrimary(double latitude, double longitude) {
		String data = latitude + TAB + longitude;
		pubnub.publish(this.selfId, data, new Callback() {
		});
		Log.i(TAG, "Update primary: " + data);
	}
}
