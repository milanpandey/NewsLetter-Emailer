import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

public class Emailer implements Runnable {
	private static String USER_NAME = NewsLetter.SMTP_USERNAME; 
	private static String PASSWORD = NewsLetter.SMTP_PASSWORD;
	private static String host = NewsLetter.SMTP_HOST;
	private int len;
    private int st;
    private int end;
    private int thid;
    private int ids[];
    public Emailer(int st, int end,int thid, int ids[],int len) {
        this.st = st;
        this.end = end;
        this.thid = thid;
        this.ids = new int[len];
        this.len = len;
        System.arraycopy( ids, 0, this.ids, 0, len );
        
    }

    public void run() {
        
    	String url = NewsLetter.DB_URL;
    	String username = NewsLetter.DB_USERNAME;
    	String password = NewsLetter.DB_PASSWORD;
    	Connection connection = null;
    	    	
    	try{
    	
    		    		
    		
    		//SMTP Connect Code  
    		Properties props = System.getProperties();
    	        
    	       // props.put("mail.smtp.starttls.enable", "true");
    	        props.put("mail.smtp.host", host);
    	        props.put("mail.smtp.user", USER_NAME);
    	        props.put("mail.smtp.password", PASSWORD);
    	        props.put("mail.smtp.port", NewsLetter.SMTP_PORT);
    	        props.put("mail.smtp.auth", "true");
    	        Session session = Session.getDefaultInstance(props);
    	        Transport transport = session.getTransport("smtp");
                transport.connect(host, USER_NAME, PASSWORD);
                
                //database Connection
                connection = (Connection) DriverManager.getConnection(url, username, password);
                Statement up = (Statement) connection.createStatement();
                Statement stat = (Statement) connection.createStatement();
                String sql;
                int i;
		    
		   for(i = st; i<end && isRunning(stat); i++)
		    {
		    	 
		    	 sql = "SELECT * FROM emailqueue WHERE id ="+ids[i]; // reading the table values one by one
		    	 ResultSet res = stat.executeQuery(sql);
		    	 while(res.next())
		    	 {
		    		 int dbit  = res.getInt("dirty_bit");
		    		 sql = "UPDATE emailqueue SET dirty_bit = dirty_bit + 1 where id = "+ids[i]; // updating dirtybit before sending email.
			    	 up.executeUpdate(sql);
			    	 String from = res.getString("from_email_address");
			    	 String to = res.getString("to_email_address");
			         String sub = res.getString("subject");
			         String body = res.getString("body");
			         
			    	 if(dbit == 0) //DOUBLE CHECK, so that no email id receives more than one email.
			    	 {
			    		 sendFromEMail(from, PASSWORD, to, sub, body,session,transport); // sending email
			    	 }
			    	 System.out.println("Email Sent to: "+to);
		    	 }
		    }
		   if(i<end)// i.e, the Email sending process was interrupted and there is a device waiting to Join in
		   {
			   NewsLetter.flag = true; 
		   }
		   connection.commit();// Committing database
		    transport.close();
    	}
    	catch(Exception e){}
    	 finally {
    		   // System.out.println("Closing the connection.");
    		    if (connection != null) try { connection.close(); } catch (SQLException ignore) {}
    		}

   	 
    }
   // check for interrupt. This function ensures that there is no device waiting to join in.
    public static boolean isRunning(Statement stmt)
    {
    	int mc=0,run=0;
    	String sql;
    	try{
    		sql = "select val from mc";
    		ResultSet mmc = stmt.executeQuery(sql);
    	    while(mmc.next())
    	    mc = mmc.getInt("val");
    	    
    	    sql = "select val from run";
    	    mmc = stmt.executeQuery(sql);
    	    while(mmc.next())
    	    run = mmc.getInt("val");
    		
    		if(run != mc)
    			{
    				System.out.println("New Device Entered or Run Ineterupted");
    				return false;
    			}
    	}catch(Exception e)
    	{}
    	
    	
    	return true;
    }
    
    private static void sendFromEMail(String from, String pass, String to, String subject, String body,Session session,Transport transport) {
    // function that sends SMTP Emails. 

        try {
        	MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            InternetAddress toAddress = new InternetAddress(to);
            message.addRecipient(Message.RecipientType.TO, toAddress);
            message.setSubject(subject);
            message.setText(body);
            
            transport.sendMessage(message, message.getAllRecipients());
            message = null; 
        }
        catch (AddressException ae) {
            ae.printStackTrace();
        }
        catch (MessagingException me) {
            me.printStackTrace();
        }
    }
}
