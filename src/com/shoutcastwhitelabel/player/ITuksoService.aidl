package net.shoutcastbitzend.player;

interface ITuksoService
{
	void download(String url);
	String errors();
	String file_name();
	long position();
	void stop();
	int state();
}
