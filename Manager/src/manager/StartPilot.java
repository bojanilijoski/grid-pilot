package manager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/startPilot")
public class StartPilot
{

	public static void main(String[] args)
	{
		new StartPilot().run(args[0]);
	}

	@GET
	public byte[] run(String topApplicationId)
	{
		try
		{
			String id = Database.addPilot(topApplicationId);

			if (id == "")
				return null;

			// create new pilot.sh file
			Writer output = null;
			File file = new File("pilot.sh");
			output = new BufferedWriter(new FileWriter(file));
			output.write("java -jar Pilot.jar " + id);
			output.close();

			// create new pilot.sh file
			// pilot.jdl must exist
			File startFile = new File("start_pilot.sh");
			if (!startFile.exists())
			{
				output = null;
				output = new BufferedWriter(new FileWriter(startFile));
				//output.write("glite-wms-job-submit -a pilot.jdl");
				output.write("bash pilot.sh");
				output.close();
			}

			// start pilot
			ProcessBuilder pb = new ProcessBuilder("bash", startFile.getAbsolutePath());
			Process process = pb.start();

			// get output
			InputStream stdout = process.getInputStream();

			BufferedReader readerOut = new BufferedReader(new InputStreamReader(stdout));
			
			String line;
			boolean findId=false;
			while ((line = readerOut.readLine()) != null)
			{
				if(findId)
				{
					Database.updatePilotId(id, line);
					break;
				}
				
				if(line.compareTo("Your job identifier is:")==0)
				{
					findId=true;
					readerOut.readLine();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}
}
