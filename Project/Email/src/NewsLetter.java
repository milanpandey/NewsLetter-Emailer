import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.io.*;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;
import java.io.FileInputStream;


public class NewsLetter {
	
	// Configuration variables
	public static String DB_URL;
	public static String DB_USERNAME;
	public static String DB_PASSWORD;
	public static String SMTP_HOST;
	public static String SMTP_USERNAME;
	public static String SMTP_PASSWORD;
	public static String SMTP_PORT;
	public static String is_Master;

	static String sql;
	static int mcid;
	public static boolean flag=true; // this will ensure programs completion
	
	static void proc(Statement stmt,int k,int len,int nthrd, Connection connection,int ids[])throws Exception
	{
		int ind = (int)Math.ceil(k/(float)nthrd);
		int sta = k*mcid; // start index for the machine
		Thread tt[] = new Thread[nthrd];
		for(int i = 0; i < nthrd; i++)
		{
			//st and end are the starting and ending ids of emails that a thread works on. 
			int st = sta+(i*ind);// start index of machine plus thread offset
			int end = Math.min(sta+((i+1)*ind),Math.min(sta+k,len)); // end index for the thread
			
			// each thread is allocated a part of the current Machines block so that there is no overlap and Each emailID receives only 1 eamil.
			Emailer emailer = new  Emailer(st,end,i,ids,len); 
			//System.out.println("Starting thread: "+i);
			tt[i] = new Thread(emailer);
			tt[i].start();
		}
		for(int i = 0; i < nthrd; i++)
		{
			tt[i].join();// so that main thread waits till every thread has finished execution and then perform final checks
			
		}
		
	}
	
	static void start(Statement stmt,int nthrd, Connection connection) throws Exception
	{
		int mc=0,run=0,k,len=0;
		
		try {
			
			sql = "UPDATE run SET val = val+1 where id = 1";
		    stmt.executeUpdate(sql);
			
		    // Wait Block every Machine waits till every other machine is ready so that each reads the same thing from the database
				sql = "select val from mc";
			    ResultSet mmc = stmt.executeQuery(sql);
			    while(mmc.next())
			    mc = mmc.getInt("val");
			    
			    sql = "select val from run";
			    mmc = stmt.executeQuery(sql);
			    while(mmc.next())
			    run = mmc.getInt("val");
			   
			    while(mc != run)
			    {
			    	try {
			    	    Thread.sleep(200); //so that it does not waste cpu time and flood the database, this time can be optimized for performance
			    	} catch(InterruptedException ex) {
			    	    Thread.currentThread().interrupt();
			    	}
			    	sql = "select val from mc";
				    mmc = stmt.executeQuery(sql);
				    while(mmc.next())
				    mc = mmc.getInt("val");
				    // refreshing the values
				    sql = "select val from run";
				    mmc = stmt.executeQuery(sql);
				    while(mmc.next())
				    run = mmc.getInt("val");
				    System.out.println("Waiting for Other Machines....");
			    }
			// end of wait block
			
		    System.out.println("Machine Count: " + mc);
		    sql = "SELECT COUNT(*) FROM emailqueue where dirty_bit = 0";
		    ResultSet cnt = stmt.executeQuery(sql);
		    while(cnt.next())
		    len = cnt.getInt(1);
		    int ids[] = new int[len];
		    
			String sql = "SELECT id FROM emailqueue WHERE dirty_bit = 0";
			ResultSet res = stmt.executeQuery(sql);
			int i = 0;
			while(res.next())
			{
				ids[i] = res.getInt("id");
				i++;
				
			}
		   k = len/mc;
		    proc(stmt,k,len,nthrd, connection,ids);
		    
		} catch (SQLException e) {
		    throw new RuntimeException("Cannot connect the database!", e);
		} 
		
		
	}
	
	
	public static void main(String args[])throws IOException
	{
	
		Properties prop = new Properties();
		 
		try {
	           //load a properties file
			prop.load(new FileInputStream("config.properties"));
	
	           //get the property value
			DB_URL = prop.getProperty("db_url");
			DB_USERNAME = prop.getProperty("db_user");
			DB_PASSWORD = prop.getProperty("db_pass");
			SMTP_HOST = prop.getProperty("smtp_host");
			SMTP_USERNAME = prop.getProperty("smtp_user");
			SMTP_PASSWORD = prop.getProperty("smtp_pass");
			SMTP_PORT = prop.getProperty("smtp_port");
			is_Master = prop.getProperty("is_master");
			
			//System.out.println(DB_URL+" "+DB_USERNAME+" "+DB_PASSWORD+" "+SMTP_HOST+" "+SMTP_USERNAME);
		} catch (IOException ex) {
			ex.printStackTrace();
	    }
		
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Input the number of Threads");
		int nthrd = Integer.parseInt(br.readLine());// input the optimal number of threads based on Machine and Database configuration
		
		try {
		    System.out.println("Loading driver...");//loading JDBC driver
		    Class.forName("com.mysql.jdbc.Driver");
		    System.out.println("Driver loaded!");
		} catch (ClassNotFoundException e) {
		    throw new RuntimeException("Cannot find the driver in the classpath!", e);
		}
		
		String url = DB_URL;
		String username = DB_USERNAME;
		String password = DB_PASSWORD;
		Connection connection = null;
		try {
		    System.out.println("Connecting database...");
		    connection = (Connection) DriverManager.getConnection(url, username, password); // Connection to the database
		    System.out.println("Database connected!");
		    
		    Statement stmt = (Statement) connection.createStatement();
		    sql = "UPDATE run SET val = 0 where id = 1";
		    stmt.executeUpdate(sql);
		    sql = "select val from mc";
		    ResultSet mmc = stmt.executeQuery(sql);
		    while(mmc.next())
		    mcid = mmc.getInt("val"); // Based on mcid the Part of Database to which the current Machine deals with is decided. 
		    						  //mcid = 0 takes the first block, Machine with mcid = 1 takes 2nd block..and so on
		    System.out.println("mcid: " + mcid);
		    sql = "UPDATE mc SET val = val+1 where id=1";
		    stmt.executeUpdate(sql);

		    while(flag == true)	
				{
					flag = false;
					try{
					  		
					    	start(stmt,nthrd, connection);
					    }
					    catch(Exception e){}
				}
		    
		    //Decrementing Machine Count and Run table val signifying that This paticular machine has left the Machine pool and has completed its execution.
		    sql = "UPDATE mc SET val = val-1 where id=1";
		    stmt.executeUpdate(sql);
		    sql = "UPDATE run SET val = val-1 where id=1";
		    stmt.executeUpdate(sql);
		    
		    //Master Block.If the Machine is the master system, It will be the incharge for clearing the dirty bits of database
		    if(is_Master.equals("true"))
		    {
		    	int count = 0;
		    	sql = "SELECT COUNT(*) FROM emailqueue where dirty_bit = 0";
		 	    ResultSet cnt = stmt.executeQuery(sql);
		 	    while(cnt.next())
		 	    count = cnt.getInt(1);
				
		    	while(count>0)
		    	{
		    		 try {
		 		        Thread.sleep(2000);//so that it does not waste cpu time
		 		    } catch(InterruptedException ex) {
		 		        Thread.currentThread().interrupt();
		 		    }
		    		
		    		sql = "SELECT COUNT(*) FROM emailqueue where dirty_bit = 0";
			 	    cnt = stmt.executeQuery(sql);
			 	    while(cnt.next())
			 	    count = cnt.getInt(1);
		    		
		    	}//end while
		    	
		    	sql = "UPDATE emailqueue SET dirty_bit = 0";
			    stmt.executeUpdate(sql);
		    }
		    // End of Master Block
		} catch (SQLException e) {
		    throw new RuntimeException("Cannot connect the database!", e);
		} finally {
		   // System.out.println("Closing the connection.");
		   if (connection != null) try { connection.close(); } catch (SQLException ignore) {}
		}
	
	}



}



