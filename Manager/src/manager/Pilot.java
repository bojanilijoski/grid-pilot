package manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/pilot")
public class Pilot
{
	@POST
	public byte[] heartbeatPilot(@FormParam("pilot_id") String pilotId,
			@FormParam("status") String status, @FormParam("file") String file) throws IOException
	{
		System.out.println(pilotId + ": " + status);
		// should be killed
		if (Database.getPilotStatus(pilotId).compareTo(Constants.pilot_job_status_killed) == 0)
		{
			try
			{
				byte[] b =
				{ 0, 0, 0, 0, 0, 0, 0, 0 };
				return b;
			}
			catch (Exception e)
			{
				// file don't exist
				return null;
			}
		}

		// if pilot send results then get file
		// get file
		if (file != null)
		{
			byte[] byteArray = new byte[file.length()];
			for (int i = 0; i < file.length(); i++)
			{
				byteArray[i] = (byte) file.charAt(i);
			}

			String dirPath = Database.getApplicationOutputSandbox(Database
					.getApplicationId(pilotId));
			File dir = new File(dirPath);
			if (!dir.exists())
				new File(dirPath).mkdir();

			File someFile = new File(dirPath + "/output.zip");
			FileOutputStream fos = new FileOutputStream(someFile);
			fos.write(byteArray);
			fos.flush();
			fos.close();
		}

		Database.updatePilotStatus(pilotId, status);

		// class for data base communication
		// Forward details to service layer.

		if (status == null)
			return null;

		if (status.compareTo(Constants.pilot_job_status_available) == 0)
		{
			// Check for application in queue
			Scheduler.Application app = Scheduler.getNextApplication(pilotId);

			if (app == null)
				return null;

			Database.schedulePilotToApplication(pilotId, app.id);

			// scheduler always .zip files in job.zip
			String fileName = "job_" + app.id + ".zip";

			// file to byte array
			try
			{
				RandomAccessFile f = new RandomAccessFile(fileName, "r");

				byte[] b = new byte[(int) f.length()];
				f.read(b);

				// after loading delete file
				File fileSend = new File(fileName);
				if (fileSend.exists())
					fileSend.delete();

				if (b.length > 0)
					return b;
			}
			catch (Exception e)
			{
				// file don't exist
				e.printStackTrace();
				return null;
			}
		}

		if (status.compareTo(Constants.pilot_job_status_done) == 0)
		{
			Database.updateApplication(Database.getApplicationId(pilotId), "DONE");
		}

		return null;
	}
}
