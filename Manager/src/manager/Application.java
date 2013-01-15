package manager;

import java.io.IOException;
import java.sql.SQLException;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/application")
public class Application
{

	@POST
	public byte[] checkIn(@FormParam("no_of_pilot_jobs") String numberOfPilotJobs,
			@FormParam("input_sendbox") String inputSendbox,
			@FormParam("output_sendbox") String outputSendbox,
			@FormParam("top_application_id") String topApplicationId) throws IOException, SQLException
	{
		// save application
		Database.addAplication(Integer.parseInt(numberOfPilotJobs), inputSendbox, outputSendbox, topApplicationId);
		// start pilot jobs
		for (int i = 0; i < Integer.parseInt(numberOfPilotJobs); i++)
			// create new pilot.sh file
			new StartPilot().run(topApplicationId);

		return null;
	}
}
