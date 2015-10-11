package com.calhacksalpha.dawwwskigrowl;

import com.pubnub.api.Pubnub;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {

	private static final String PUB_KEY = "pub-c-d61a4887-c713-4a84-893a-401a26d932ce";

	private static final String SUB_KEY = "sub-c-65b4c226-7000-11e5-81f9-0619f8945a4f";

	private Pubnub pubnub = new Pubnub(PUB_KEY, SUB_KEY);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void forward1(View v) {
		Intent intent = new Intent(this, GoogleMapsActivity.class);
		startActivity(intent);
	}

	public void forward2(View v) {
		Intent intent = new Intent(this, MyLocationDemoActivity.class);
		startActivity(intent);
	}

	public void forward3(View v) {
		Intent intent = new Intent(this, HereMapsActivity.class);
		startActivity(intent);
	}
}
