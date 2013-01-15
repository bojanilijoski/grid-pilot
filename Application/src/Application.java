import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Calendar;

public class Application
{

	private String topApplicationId;
	private int numberOfPilotJobs;
	private String inputSendbox;
	private String outputSendbox;

	public Application(int numberOfPilotJobs, String inputSendbox, String outputSendbox, String topApplicationId)
	{
		this.numberOfPilotJobs = numberOfPilotJobs;
		this.inputSendbox = inputSendbox;
		this.outputSendbox = outputSendbox;
		this.topApplicationId = topApplicationId;
	}

	/**
	 * 
	 * @param args
	 * 1. number of pilot jobs
	 * 2. input sendbox
	 * 3. output sendbox
	 * 4. top application id
	 */
	public static void main(String[] args)
	{
		new Application(Integer.parseInt(args[0]), args[1], args[2], args[3]).sendRequest();
	}

	void sendRequest()
	{
		try
		{

			String data = URLEncoder.encode("no_of_pilot_jobs", "UTF-8") + "="
					+ URLEncoder.encode(numberOfPilotJobs + "", "UTF-8");
			data += "&" + URLEncoder.encode("input_sendbox", "UTF-8") + "="
					+ URLEncoder.encode(inputSendbox, "UTF-8");
			data += "&" + URLEncoder.encode("output_sendbox", "UTF-8") + "="
					+ URLEncoder.encode(outputSendbox, "UTF-8");
			data += "&" + URLEncoder.encode("top_application_id", "UTF-8") + "="
					+ URLEncoder.encode(topApplicationId, "UTF-8");

			URL Url;

			Url = new URL(Constants.SERVICE_URL + "/application");

			URLConnection conn = Url.openConnection();
			conn.setDoOutput(true);

			// send parameters
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(data);
			wr.flush();

			conn.getInputStream();

		}
		catch (Exception e)
		{
			try
			{
				PrintWriter out;
				out = new PrintWriter(new BufferedWriter(new FileWriter("errors.txt", true)));
				out.println("Application: "+Calendar.getInstance().getTime()+": "+e.getMessage());
			    out.close();
				e.printStackTrace();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}
		finally
		{

		}
	}

}
