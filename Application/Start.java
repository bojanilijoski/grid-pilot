import java.io.*;

public class Start
{
	public static void main(String[] args) throws IOException
	{
		for (int i = 0; i <= 10; i++)
		{
			for (int j = 0; j <= i; j++)
				System.out.println("*********************");

			if (i % 10 == 0)
			{
				try
				{
					Thread.sleep(10000l);
				}
				catch (Exception e)
				{
				}
			}
		}
	}
}

