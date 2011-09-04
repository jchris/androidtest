package com.couchbase.androidtest;

import com.couchbase.android.CouchbaseMobile;
import com.couchbase.android.ICouchbaseDelegate;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.ReplicationCommand;
import org.ektorp.http.AndroidHttpClient;
import org.ektorp.http.HttpClient;
import org.ektorp.impl.StdCouchDbInstance;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class AndroidtestActivity extends Activity {
	private TextView textView;
	@SuppressWarnings("unused")
	private ServiceConnection couchServiceConnection;

	protected static final String TAG = "EmptyApp";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
		CouchbaseMobile couch = new CouchbaseMobile(getBaseContext(), mDelegate);
		couchServiceConnection = couch.startCouchbase();
		
		textView = (TextView)findViewById( R.id.batteryLevel );
        registerReceiver( batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED) );
       
    }

    public void kickoffWorkload(String host, int port) {
		HttpClient httpClient = new AndroidHttpClient.Builder().host(host).port(port).build();
		CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
//		CouchDbConnector testResultDb = dbInstance.createConnector("test-results", true);		
//		CouchDbConnector replicationDb = dbInstance.createConnector("test-replication", true);
		ReplicationCommand pullReplication = new ReplicationCommand.Builder()
		.source("http://single.couchbase.net/gerrit")
		.target("test-replication")
		.continuous(true)
		.createTarget(true)
		.build();
	
		dbInstance.replicate(pullReplication);
    }
    
    public void updateBatteryCondition(int level)
    {
    textView.setText( level + "%" );
    }
    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
    	 @Override
    	 public void onReceive( Context context, Intent intent )
    	 {
    	 int level = intent.getIntExtra( "level", 0 );
    	 updateBatteryCondition(level);
    	 }
    	 };
    
    
	private final ICouchbaseDelegate mDelegate = new ICouchbaseDelegate.Stub() {
		@Override
		public void couchbaseStarted(String host, int port) {
			Log.v(TAG, "Couchbase has started");
			kickoffWorkload(host, port);
		}

		@Override
		public void installing(int completed, int total) {
		}

		@Override
		public void exit(String error) {
		}
	};
}