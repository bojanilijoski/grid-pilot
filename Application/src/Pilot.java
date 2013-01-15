import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Pilot
{

	private String id;

	public static final String pilot_job_status_created = "CREATED";
	public static final String pilot_job_status_available = "AVAILABLE";
	public static final String pilot_job_status_running = "RUNNING";
	public static final String pilot_job_status_done = "DONE";
	public static final long sleepTime = 10000l;

	public Pilot(String id)
	{
		this.id = id;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	/**
	 * 
	 * @param args
	 * pilotId
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		new Pilot(args[0]).process();
	}

	private void process()
	{
		while (true)
		{
			sendRequestForFile();
		}
	}

	private void sendRequestForFile()
	{
		try
		{
			URLConnection conn = changeStatusToAvailable();

			// get zip file
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			InputStream is = conn.getInputStream();

			String fileName = "job.zip";

			int bytesRead;
			while ((bytesRead = is.read()) != -1)
			{
				baos.write(bytesRead);
			}

			// kill myself
			if (baos.size() == 8)
			{
				byte[] b = baos.toByteArray();
				boolean check = true;
				for (int i = 0; i < 8; i++)
					if (b[i] != 0)
						check = true;

				if (check)
					System.exit(0);
			}

			// check file
			if (baos.size() != 0)
			{
				FileOutputStream fos = new FileOutputStream(fileName, false);
				fos.write(baos.toByteArray());
				fos.close();

				// unzip file
				FileInputStream inputStream = new FileInputStream(fileName);
				ZipInputStream zipStream = new ZipInputStream(inputStream);
				ZipEntry entry = null;

				while ((entry = zipStream.getNextEntry()) != null)
				{
					String entryName = entry.getName();

					int count;
					byte data2[] = new byte[2048];
					// write the files to the disk
					FileOutputStream fos2 = new FileOutputStream(entryName);
					BufferedOutputStream dest = new BufferedOutputStream(fos2, 2048);
					while ((count = zipStream.read(data2, 0, 2048)) != -1)
					{
						dest.write(data2, 0, count);
					}
					dest.flush();
					dest.close();
				}

				zipStream.close();

				// run .sh script
				Runtime runtime =  Runtime.getRuntime();

				changeStatusToRunning().getInputStream();
				Process process = runtime.exec(new String[]{"bash", "start.sh"});

				// get output from process
				String line;
				InputStream stderr = process.getErrorStream();
				InputStream stdout = process.getInputStream();

				BufferedReader readerOut = new BufferedReader(new InputStreamReader(stdout));

				FileWriter fwOut = new FileWriter("out.txt");
				BufferedWriter out = new BufferedWriter(fwOut);

				while ((line = readerOut.readLine()) != null)
				{
					out.write(line + "\n");
				}

				out.close();

				// get errors from process
				BufferedReader readerErr = new BufferedReader(new InputStreamReader(stderr));

				FileWriter fwErr = new FileWriter("err.txt");
				BufferedWriter err = new BufferedWriter(fwErr);

				while ((line = readerErr.readLine()) != null)
				{
					err.write(line + "\n");
				}

				err.close();

				// zip output and errors
				BufferedInputStream origin = null;
				FileOutputStream dest = new FileOutputStream("output.zip");
				ZipOutputStream outZip = new ZipOutputStream(new BufferedOutputStream(dest));
				// out.setMethod(ZipOutputStream.DEFLATED);
				byte data[] = new byte[2048];
				String files[] =
				{ "out.txt", "err.txt" };

				for (int i = 0; i < files.length; i++)
				{
					FileInputStream fi = new FileInputStream(files[i]);
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

				URLConnection connection = changeStatusToDone();

				connection.getInputStream();
			}
			else
			{
				Thread.sleep(sleepTime);
			}

		}
		catch (Exception e)
		{
			try
			{
				e.printStackTrace();
				Thread.sleep(sleepTime);
			}
			catch (Exception e1)
			{

			}
		}
		finally
		{
		}
	}

	private URLConnection changeStatusToAvailable()
	{
		try
		{
			String data;

			data = URLEncoder.encode("pilot_id", "UTF-8") + "=" + URLEncoder.encode(id, "UTF-8");

			data += "&" + URLEncoder.encode("status", "UTF-8") + "="
					+ URLEncoder.encode(pilot_job_status_available, "UTF-8");

			URL Url;

			Url = new URL(Constants.SERVICE_URL + "/pilot");

			URLConnection conn = Url.openConnection();
			conn.setDoOutput(true);

			// send parameters
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(data);
			wr.flush();
			wr.close();

			return conn;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	private URLConnection changeStatusToRunning()
	{
		try
		{
			String data;

			data = URLEncoder.encode("pilot_id", "UTF-8") + "=" + URLEncoder.encode(id, "UTF-8");

			data += "&" + URLEncoder.encode("status", "UTF-8") + "="
					+ URLEncoder.encode(pilot_job_status_running, "UTF-8");

			URL Url;

			Url = new URL(Constants.SERVICE_URL + "/pilot");

			URLConnection conn = Url.openConnection();
			conn.setDoOutput(true);

			// send parameters
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(data);
			wr.flush();
			wr.close();

			return conn;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	private URLConnection changeStatusToDone()
	{
		try
		{
			RandomAccessFile f = new RandomAccessFile("output.zip", "r");

			byte[] b = new byte[(int) f.length()];
			f.read(b);

			String result = "";
			for (int i = 0; i < b.length; i++)
			{
				result += (char) b[i];
			}

			String data;

			data = URLEncoder.encode("pilot_id", "UTF-8") + "=" + URLEncoder.encode(id, "UTF-8");

			data += "&" + URLEncoder.encode("status", "UTF-8") + "="
					+ URLEncoder.encode(pilot_job_status_done, "UTF-8");

			data += "&" + URLEncoder.encode("file", "UTF-8") + "="
					+ URLEncoder.encode(result, "UTF-8");

			URL Url;

			Url = new URL(Constants.SERVICE_URL + "/pilot");

			URLConnection conn = Url.openConnection();
			conn.setDoOutput(true);

			// send parameters
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(data);
			wr.flush();
			wr.close();

			return conn;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}
}
