import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Properties;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

//needa encrpyt password, use openssl generate aes-256 key base 64
//send to one mail group so no need xml file to change config
//needa show id, number of days before expiry date and real expiry date
//send every day but 2 types (with expiry and without expiry)

public class EmailReminder {
    private static final String EMAIL_SUBJECT = "Subject: [DBA][SYSADM] Password Expiry Reminder ";
    private static final String CONTENT_TYPE="\nContent-Type: text/html; charset=\"UTF-8\"\n\n";
    private static final String EMAIL_BODY_WITHOUT_DATA= "The following accounts' passwords will expire soon:\n"+String.format("<table border=\"1\"><tr><th>%s</th><th>%s</th><th>%s</th></tr>","USER_ID","Number of days before expiry","Expiry date")+"\n";

    public static void main(String[] args) throws IOException{
        try{
            InputStream input = new FileInputStream("config.properties");
            Properties prop = new Properties();
            prop.load(input);

            String salt = prop.getProperty("encryptionSalt");
            String secretKey= prop.getProperty("encryptionKey");
            String warning_heading = "[CLEAR]";
            String bash_location = prop.getProperty("bashLocation");
            String query =prop.getProperty("db.query"); ;
            String url =prop.getProperty("db.url");;
            String username =prop.getProperty("db.username");;
            String password = AES256.decrypt(prop.getProperty("db.password"),secretKey,salt);
            String toAddress = prop.getProperty("bashLocation");;
            int no_of_rows = 0;


            Connection con = DriverManager.getConnection(url, username, password);

            Statement st = con.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = st.executeQuery(query);

            if (rs.last()){
                 no_of_rows = rs.getRow();
            }

            rs.beforeFirst();
            FileWriter writer = new FileWriter("latestRecord.txt", false);

            if (no_of_rows>0){
                warning_heading = "[WARNING]";
            }

            //fixed part of the email subject and body
            writer.write(EMAIL_SUBJECT+ warning_heading+"\nTo: "+toAddress+CONTENT_TYPE);
            writer.write(EMAIL_BODY_WITHOUT_DATA);
            //email body data part
            while (rs.next()) {
                writer.write(String.format("<tr><td>%s</td><td>%s</td><td>%s</td></tr>",rs.getString(1), rs.getString(2), rs.getString(3))+"\n");
            }
            writer.write("</table>");

            con.close();
            writer.close();

            ProcessBuilder pb = new ProcessBuilder();
            pb.command(bash_location,"-c","sendmail -i -t \""+toAddress+"\" < latestRecord.txt");
            Process process = pb.start();

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            Files.createDirectories(Paths.get("./successLog/"));
            File successLogFile = new File("./successLog/"+timeStamp+"_SuccessLog.txt");
            if (!successLogFile.exists()){
                successLogFile.createNewFile();
            }
            OutputStream successLogOS = new FileOutputStream(successLogFile);
            successLogOS.write("Email Successfully Delivered".getBytes());


        } catch (Exception e) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            Files.createDirectories(Paths.get("./errorLog/forEmailReminder_error/"));
            File errorLogFile = new File("./errorLog/forEmailReminder_error/"+timeStamp+"_ErrorLog.txt");
            if (!errorLogFile.exists()){
                errorLogFile.createNewFile();
            }
            OutputStream errorLogOS = new FileOutputStream(errorLogFile);

            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            errorLogOS.write(exceptionAsString.getBytes());
            errorLogOS.close();
        }

    }

}