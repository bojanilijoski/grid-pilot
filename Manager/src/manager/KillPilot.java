package manager;

import java.io.IOException;
import java.sql.SQLException;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/killPilot")
public class KillPilot
{
	@POST
	public void killPilot(@FormParam("id") String id) throws IOException, SQLException
	{
		Database.updatePilotStatus(id, "KILLED");
	}
}
