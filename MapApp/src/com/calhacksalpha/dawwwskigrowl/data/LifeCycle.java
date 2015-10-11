package com.calhacksalpha.dawwwskigrowl.data;

import java.util.LinkedHashSet;
import java.util.Set;

import com.here.android.mpa.common.GeoCoordinate;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

public class LifeCycle {

	protected static final String TAB = "\t";
	protected static final String INVITE = "invite";
	protected static final String ACCEPT = "accept";
	private Pubnub pubnub;
	private String selfId;
	private Set<String> invites = new LinkedHashSet<String>();
	private LocationEntities locationEntities;

	public LifeCycle(Pubnub pn, String selfId, LocationEntities locationEntities) {
		this.pubnub = pn;
		this.selfId = selfId;
		this.locationEntities = locationEntities;
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
						String msg = split[1];
						if (type.equals(INVITE)) {
							addSecondary(msg);
						} else if (type.equals(ACCEPT)) {
							initSecondary(msg);
						}
					}
				}

				public void errorCallback(String channel, PubnubError error) {
					System.out.println(error.getErrorString());
				}
			});
		} catch (PubnubException e) {
			e.printStackTrace();
		}
	}

	public void closePrimary() {
		pubnub.unsubscribe(this.selfId);
	}

	private void initSecondary(String channel) {
		try {
			pubnub.subscribe(channel, new Callback() {
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
					System.out.println(message);
				}

				public void errorCallback(String channel, PubnubError error) {
					System.out.println(error.getErrorString());
				}
			});
		} catch (PubnubException e) {
			e.printStackTrace();
		}
	}

	private void addSecondary(String msg) {
		this.invites.add(msg);
	}

	public void acceptSecondary(String channel) {
		String data = channel + TAB + ACCEPT;
		pubnub.publish(channel, data, new Callback() {});
	}
	
	public void updatePrimary(double latitude, double longitude) {
		String data = latitude + TAB + longitude;
		pubnub.publish(this.selfId, data, new Callback() {});
	}
}
