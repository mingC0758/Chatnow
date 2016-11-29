package momingqi.server;

import java.net.Socket;

public class User
{
	public Socket socket;
	public String id;
	
	public User(Socket socket, String id)
	{
		this.socket = socket;
		this.id = id;
	}
	
}
