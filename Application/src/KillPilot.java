import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;


public class KillPilot
{
	public static void main(String[] args)
	{
		new KillPilot().sendRequest(args[0]);
	}
	
	void sendRequest(String id)
	{
		try
		{
			String data = URLEncoder.encode("id", "UTF-8") + "="
					+ URLEncoder.encode(id + "", "UTF-8");

			URL Url;

			Url = new URL(Constants.SERVICE_URL + "/killPilot");

			URLConnection conn = Url.openConnection();
			conn.setDoOutput(true);

			// send parameters
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(data);
			wr.flush();
			wr.close();

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
