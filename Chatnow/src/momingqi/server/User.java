package momingqi.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * 在线用户对象
 * @author mingC
 *
 */
public class User
{
	public Socket socket;
	public String id;
	public String nickname;
	public String photo;
	
	public User(Socket socket, String id, String nickname, String photo)
	{
		this.socket = socket;
		this.id = id;
		this.nickname = nickname;
		this.photo = photo;
	}

	public OutputStream getOutputStream() throws IOException
	{
		return socket.getOutputStream();
	}
	
}
