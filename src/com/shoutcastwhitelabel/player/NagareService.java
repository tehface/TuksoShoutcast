package com.shoutcastwhitelabel.player;

import java.net.MalformedURLException;
import java.net.URL;

import com.shoutcastwhitelabel.player.INagareService;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.IBinder;

public class NagareService extends Service implements OnCompletionListener
{
	public static URL m_url = null;
	public static DownloadThread m_download_thread = null;
	public static MediaPlayer m_media_player = null;
	public static int m_current_position = 0;
	public static String m_errors = "";
	public static int m_state = 0;
	public static final int STOPPED = 0;
	public static final int PLAYING = 1;
	public static final int BUFFERING = 2;
	public static MediaScannerConnection m_scanner = null;
	public static final int BUFFER_BEFORE_PLAY = 65536;
	
	private final static Runnable m_run_buffer = new Runnable()
	{
		public void run()
		{
			int delay = buffer();
			if (delay > 0)
			{
				m_handler.postDelayed(this, delay);
			}
		}
	};
	
	private final static Handler m_handler = new Handler();
	
	public NagareService()
	{
		//Don't set m_state here, we want to persist it across service binds/unbinds
	}
	
	public static int buffer()
	{
		if (m_download_thread == null || m_download_thread.m_shoutcast_file == null)
		{
			if (m_state == BUFFERING)
			{
				return 1000;
			}
			else
			{
				stop();
				return 0;
			}
		}
		
		if (m_download_thread.m_shoutcast_file.m_done)
		{
			stop();
			return 0;
		}
		
		if (m_download_thread.m_shoutcast_file.m_current_write_pos - m_current_position > BUFFER_BEFORE_PLAY)
		{
			try
			{
				m_media_player.reset();
				m_media_player.setDataSource(m_download_thread.m_shoutcast_file.file_path());
				m_media_player.prepare();
			}
			catch (Exception e)
			{
				m_errors += "Error starting media player on '" + m_download_thread.m_shoutcast_file.file_path() + "': " + e.toString() + "\n";
			}
			m_media_player.seekTo(m_current_position);
			m_media_player.start();
			m_state = PLAYING;
			return 0;
		}
		else
		{
			m_state = BUFFERING;
			return 1000;
		}
	}
	
	public static void download(String url_string)
	{
		m_errors = "";
		try
		{
			m_url = new URL(url_string);
		} 
		catch (MalformedURLException e)
		{
			m_errors += "Error parsing URL (" + url_string + "): " + e.toString() + "\n";
		}
		
		if (m_errors == "")
		{
			m_download_thread = new DownloadThread(m_url);
			m_download_thread.start();
			m_current_position = 0;
			m_state = BUFFERING;
			if (m_media_player == null)
			{
				m_media_player = new MediaPlayer();
				m_media_player.setOnCompletionListener(null);
			}
			m_run_buffer.run();
		}
	}
	
	public static String errors()
	{
		if (m_download_thread != null)
		{
			return m_errors + m_download_thread.errors();		
		}
		return m_errors;
	}
	
	public static String file_name()
	{
		if (m_download_thread == null)
		{
			return null;
		}
		
		if (m_download_thread.m_shoutcast_file == null)
		{
			return null;
		}
		
		return m_download_thread.m_shoutcast_file.m_file_name;
	}
	
	public IBinder onBind(Intent intent)
	{
		return m_binder;
	}
	
	public void onCompletion(MediaPlayer mp)
	{
		m_current_position = mp.getCurrentPosition();
		m_run_buffer.run();
	}
	
	public static long position()
	{
		if (m_download_thread == null)
		{
			return -1;
		}
		
		if (m_download_thread.m_shoutcast_file == null)
		{
			return -1;
		}
		
		return m_download_thread.m_shoutcast_file.m_current_write_pos;
	}
	
	public static int state()
	{
		return m_state;
	}
	
	public static void stop()
	{
		if (m_download_thread != null)
		{
			m_download_thread.done();
			m_download_thread = null;
		}
		if (m_media_player != null)
		{
			if (m_state == PLAYING)
			{
				m_media_player.stop();
			}
		}
		m_state = STOPPED;
	}

	private final static INagareService.Stub m_binder = new INagareService.Stub()
	{
		public void download(String url)
		{
			NagareService.download(url);
		}
		
		public String errors()
		{
			return NagareService.errors();
		}
		
		public String file_name()
		{
			return NagareService.file_name();
		}
		
		public long position()
		{
			return NagareService.position();
		}
		
		public int state()
		{
			return NagareService.state();
		}
		
		public void stop()
		{
			NagareService.stop();
		}
	};

}
