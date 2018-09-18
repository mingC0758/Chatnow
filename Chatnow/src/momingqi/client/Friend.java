package momingqi.client;
/*remote test*/
/**
 * 好友信息
 * @author mingC
 *
 */
public class Friend
{
	public String id;
	public String nickname;
	public String photo;
	public boolean online;
	
	public Friend(String id, String nickname, String photo)
	{
		this.id = id;
		this.nickname = nickname;
		this.photo = photo;
		this.online = false;
	}
	
	
}
