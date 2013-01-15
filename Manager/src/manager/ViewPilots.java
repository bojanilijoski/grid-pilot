package manager;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/viewPilots")
public class ViewPilots
{
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String sayHtmlHello()
	{

		String body = "<table border='1'><tr><th>ID</th><th>PILOT_ID</th><th>STATUS</th><th>APPLICATION</th><th>START_TIME</th><th>LAST_CHECK_IN_TIME</th><th>FINISH_TIME</th><th>TOP_APPLICATION_ID</th></tr>";
		for (String[] s : Database.getAllPilots())
			body += "<tr><td>" + s[0] + "</td><td>" + s[1] + "</td><td>" + s[2] + "</td><td>"
					+ s[3] + "</td><td>" + s[4] + "</td><td>" + s[5] + "</td><td>" + s[6]
					+ "</td><td>" + s[7] + "</td></tr>";
		body += "</table>";
		return "<html> " + "<title>" + "Running Pilots" + "</title>" + "<body><h1>"
				+ "Running Pilots</h1> <br /> " + body + "</body>" + "</html> ";
	}
}
