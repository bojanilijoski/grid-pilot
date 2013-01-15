package manager;

import java.io.IOException;
import java.sql.SQLException;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/topApplicationJob")
public class TopApplicationJobs
{

	@GET
	public String getNoOfAvailablePilotJobs(@FormParam("no_of_pilot_jobs") String topApplicationId) throws IOException, SQLException
	{
		return Database.getNoOfAvailablePilotJobs(topApplicationId);
	}
}
