import java.net.URL;
import java.net.URLConnection;

public class StartPilot
{
	public static void main(String[] args)
	{
		new StartPilot().sendRequest();
	}

	void sendRequest()
	{
		try
		{
			URL Url;

			Url = new URL(Constants.SERVICE_URL + "/startPilot");

			URLConnection conn = Url.openConnection();

			conn.getInputStream();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{

		}
	}
}
