package com.shoutcastbitzend.player;

interface PlayListFile
{
	PlayList play_list();
	void parse();
	String errors();
}


