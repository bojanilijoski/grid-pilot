package manager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Scheduler
{
	private static HashMap<String, PriorityQueue<Application>> queues = new HashMap<String, PriorityQueue<Application>>();
	private static final long sleepTime = 10000l;

	public static class Application implements Comparable<Application>
	{
		String id;
		String numberOfPilotJobs;
		String inputSendbox;
		String outputSendbox;
		String status;
		String timeStart;
		String topApplicationId;

		public Application(String id, String numberOfPilotJobs, String inputSendbox,
				String outputSendbox, String status, String timeStart, String topApplicationId)
		{
			super();
			this.id = id;
			this.numberOfPilotJobs = numberOfPilotJobs;
			this.inputSendbox = inputSendbox;
			this.outputSendbox = outputSendbox;
			this.status = status;
			this.timeStart = timeStart;
			this.topApplicationId = topApplicationId;
		}

		public int compareTo(Application arg0)
		{
			return this.timeStart.compareTo(arg0.timeStart);
		}
	}

	public static void main(String[] args)
	{
		Scheduler.run();
	}

	public static void run()
	{
		while (true)
		{
			getNewApplications();
			try
			{
				Thread.sleep(sleepTime);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}

	}

	public static void getNewApplications()
	{
		for (String[] s : Database.getReadyApplications())
		{
			if (queues.containsKey(s[6]))
				queues.get(s[6]).add(new Application(s[0], s[1], s[2], s[3], s[4], s[5], s[6]));
			else
			{
				PriorityQueue<Scheduler.Application> queue = new PriorityQueue<Scheduler.Application>();
				queue.add(new Application(s[0], s[1], s[2], s[3], s[4], s[5], s[6]));
				queues.put(s[6], queue);
			}

		}
	}

	public static Application getNextApplication(String pilotId)
	{
		String appId = Database.getTopApplicationId(pilotId);

		if (queues.get(appId) == null)
			queues.put(appId, new PriorityQueue<Scheduler.Application>());

		Application app = queues.get(appId).poll();

		// if queue is empty check database
		if (app == null)
			getNewApplications();
		
		app = queues.get(appId).poll();

		if (app == null)
			return null;

		try
		{
			// zip files before sent
			BufferedInputStream origin = null;
			FileOutputStream dest = new FileOutputStream("job_" + app.id + ".zip", false);
			ZipOutputStream outZip = new ZipOutputStream(new BufferedOutputStream(dest));
			// out.setMethod(ZipOutputStream.DEFLATED);
			byte data[] = new byte[2048];
			File f = new File(app.inputSendbox);
			String files[] = f.list();
			ZipEntry entry = null;

			for (int i = 0; i < files.length; i++)
			{
				FileInputStream fi = new FileInputStream(app.inputSendbox + "/" + files[i]);
				origin = new BufferedInputStream(fi, 2048);
				entry = new ZipEntry(files[i]);
				outZip.putNextEntry(entry);
				int count;
				while ((count = origin.read(data, 0, 2024)) != -1)
				{
					outZip.write(data, 0, count);
				}
				origin.close();
			}
			outZip.close();
		}
		catch (Exception e)
		{
			System.err.println("Can not create zip");
			e.printStackTrace();
		}

		return app;
	}
}
