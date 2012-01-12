package com.shoutcastwhitelabel.player;

import java.net.URL;

import com.shoutcastwhitelabel.player.INagareService;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class Nagare extends Activity implements OnClickListener 
{
	private Context m_context;
	private URL m_url;
	private ImageButton m_play_button;
	private INagareService m_nagare_service = null;
	private static final String TAG = "WhiteLabelRadio";
	private static final int NOTIFICATION_ID = 454;
	private NotificationManager mNotificationManager = null;
	
	private ServiceConnection m_nagare_service_connection = new ServiceConnection()
	{
		public void onServiceConnected(ComponentName classname, IBinder service)
		{
			m_nagare_service = INagareService.Stub.asInterface(service);
			
			//Refresh button state after the connection binds, this covers the case where the app starts
			//and the service is already in the background playing
			refresh();
		}

		public void onServiceDisconnected(ComponentName name)
		{
			m_nagare_service = null;
		}	
	};
	
	private final Handler m_handler = new Handler();
	
	public void onClick(View v)
	{
		if (m_nagare_service == null)
		{
			return;
		}
		try
		{
			int state = m_nagare_service.state();
			if (state == NagareService.STOPPED)
			{
				m_nagare_service.download(getString(R.string.default_station));
				createNotification();				
			}
			else
			{
				m_nagare_service.stop();
				mNotificationManager.cancel(NOTIFICATION_ID);
			}
		}
		catch (RemoteException e)
		{
			Log.e(TAG, getString(R.string.service_connection_error) + e.toString());
		}
		finally{
			refresh();
		}
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        m_play_button = (ImageButton) findViewById(R.id.play);        
        m_play_button.setOnClickListener(this);
        m_play_button.setImageResource(android.R.drawable.ic_media_play);
        
        m_context = this;
        bindService(new Intent(m_context, NagareService.class), m_nagare_service_connection, Context.BIND_AUTO_CREATE);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }
    
    
    @Override
    public void onStop(){
    	super.onStop();
    	
    	//Unbind the service when the user exits the application but don't stop it
    	if (m_nagare_service == null)
		{
			return;
		}
		try
		{
		    m_context.unbindService(m_nagare_service_connection);
		}
		catch( IllegalArgumentException e){}
		catch (RuntimeException e){} 
		
    }

    /**
     * Refresh the button state
     */
    public void refresh()
    {
    	if (m_nagare_service == null)
    	{
    		return;
    	}
    	try
		{
    		int state = m_nagare_service.state();
    		if (state == NagareService.STOPPED)
    		{
    			m_play_button.setImageResource(android.R.drawable.ic_media_play);
    		}
    		else
    		{
    			m_play_button.setImageResource(android.R.drawable.ic_media_pause);
    		}
			String errors = m_nagare_service.errors();
			if (errors != "")
			{
				Log.e(TAG, errors);
			}
		} 
    	catch (RemoteException e)
		{
    		Log.e(TAG, getString(R.string.service_connection_error) + e.toString());
		}
    }
    
    public void createNotification(){
    	String ns = Context.NOTIFICATION_SERVICE;
    	NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
    	
    	int icon = R.drawable.icon;
    	CharSequence tickerText = getString(R.string.notification_text);
    	long when = System.currentTimeMillis();

    	Notification notification = new Notification(icon, tickerText, when);
    	
    	Context context = getApplicationContext();
    	CharSequence contentTitle = getString(R.string.notification_title);
    	CharSequence contentText =  getString(R.string.notification_body);
    	Intent notificationIntent = new Intent(this, Nagare.class);
    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

    	notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

    	mNotificationManager.notify(NOTIFICATION_ID, notification);
    }
}