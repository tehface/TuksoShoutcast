package com.shoutcastwhitelabel.player;

import android.os.Process;

import java.net.URL;
import java.net.URLConnection;

public class DownloadThread extends Thread
{
	public String m_errors = "";
	public URL m_url;
	public ShoutcastFile m_shoutcast_file = null;
	
	public DownloadThread(URL url)
	{
		m_url = url;
	}
	
	public void done()
	{
		if (m_shoutcast_file != null)
		{
			m_shoutcast_file.done();
		}
	}
	
	public String errors()
	{
		if (m_shoutcast_file != null)
		{
			return m_errors + m_shoutcast_file.errors();
		}
		return m_errors;
	}
	
	public void run()
	{
		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
		try
		{
			URLConnection connection = m_url.openConnection();
			connection.setRequestProperty("User-Agent", "Nagare");
			connection.connect();
			m_shoutcast_file = new ShoutcastFile(connection);
			m_shoutcast_file.download(this, connection.getInputStream());
		}
		catch (Exception e)
		{
			m_errors += e.toString() + "\n";
		}
	}
}
