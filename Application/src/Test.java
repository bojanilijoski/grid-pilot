
public class Test
{

	public static void main(String[] args)
	{
		// Application application = new Application(null, 1, "/", "/");

		// application.sendRequest();

		// Pilot job = new Pilot("2", application);

		// job.sendRequestForFile();

		// job.process();

//		System.out.println(Calendar.getInstance().getTime());
//		ProcessBuilder pb = new ProcessBuilder("sh", "start.sh");
//		try
//		{
//
//			Process process = pb.start();
//
//			String line;
//			Scanner scan = new Scanner(System.in);
//			OutputStream stdin = process.getOutputStream();
//			InputStream stderr = process.getErrorStream();
//			InputStream stdout = process.getInputStream();
//
//			BufferedReader readerOut = new BufferedReader(new InputStreamReader(stdout));
//
//			FileWriter fwOut = new FileWriter("out.txt");
//			BufferedWriter out = new BufferedWriter(fwOut);
//
//			while ((line = readerOut.readLine()) != null)
//			{
//				out.write(line + "\n");
//			}
//
//			out.close();
//
//			BufferedReader readerErr = new BufferedReader(new InputStreamReader(stderr));
//
//			FileWriter fwErr = new FileWriter("err.txt");
//			BufferedWriter err = new BufferedWriter(fwErr);
//
//			while ((line = readerErr.readLine()) != null)
//			{
//				err.write(line + "\n");
//			}
//
//			err.close();
//
//			BufferedInputStream origin = null;
//			FileOutputStream dest = new FileOutputStream("output.zip");
//			ZipOutputStream outZip = new ZipOutputStream(new BufferedOutputStream(dest));
//			// out.setMethod(ZipOutputStream.DEFLATED);
//			byte data[] = new byte[2048];
//			String files[] =
//			{ "out.txt", "err.txt" };
//
//			for (int i = 0; i < files.length; i++)
//			{
//				System.out.println("Adding: " + files[i]);
//				FileInputStream fi = new FileInputStream(files[i]);
//				origin = new BufferedInputStream(fi, 2048);
//				ZipEntry entry = new ZipEntry(files[i]);
//				outZip.putNextEntry(entry);
//				int count;
//				while ((count = origin.read(data, 0, 2024)) != -1)
//				{
//					outZip.write(data, 0, count);
//				}
//				origin.close();
//			}
//			outZip.close();
//		}
//		catch (IOException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println(Calendar.getInstance().getTime());
	}

}
