package manager;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/viewApplications")
public class ViewApplications
{

	// This method is called if HTML is request
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String sayHtmlHello()
	{

		String html = "<html> "
				+ "<title>"
				+ "Applications"
				+ "</title>"
				+ "<body><h1>"
				+ "Applications <br /> <table><tr><th>ID</th><th>No of pilots</th><th>Input sendbox</th><th>Output sendbox</th><th>Status</th><th>Start time</th><th>Finish time</th><th>Top application id</th></tr>";

		for (String[] s : Database.getAllApplications())
			html += "<tr><td>" + s[0] + "</td><td>" + s[1] + "</td><td>" + s[2] + "</td><td>"
					+ s[3] + "</td><td>" + s[4] + "</td><td>" + s[5] + "</td><td>" + s[6] + "</td><td>" + s[7] + "</td></tr>";

		html += "</table></body></h1>" + "</html> ";

		return html;
	}
}
