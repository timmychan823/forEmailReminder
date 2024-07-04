import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class EmailReminder {
    public static void main(String[] args) throws Exception{
        try{
            String sql = "select * from all_tables"; //change this to correct queries later
            String url = "jdbc:oracle:thin:@BSDWUAT01.INTRA.HKMA.GOV.HK:31521:epsssit";
            String username = "svy_oprs"; //change to real username later
            String password = "epss123?"; //change to real password later
            Connection con = DriverManager.getConnection(url, username, password);

            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);


            FileWriter writer = new FileWriter("latestRecord.txt",false);
            writer.write("Subject: Test\nTo: tshchan@hkma.gov.hk\nCc: chanshunhei09@gmail.com\n\n");
            writer.write("The following accounts will expire soon:\n");
            writer.write(String.format("%15s%15s%15s","Column1","Column2","Column3")+"\n"); //change to correct column names later
            writer.write(String.join("", Collections.nCopies(45, "-"))+"\n"); //change to correct length later
            while (rs.next()) {
                writer.write(String.format("%15s%15s%15s",rs.getString(1), rs.getString(2), rs.getString(3))+"\n"); //change this later to fit selection result
            }
            // step5 close the connection object

            con.close();
            writer.close();

            Process proc = Runtime.getRuntime().exec("sendmail -i -t \"tshchan@hkma.gov.hk,chanshunhei09@gmail.com\" <latestRecord.txt");

        } catch (Exception e) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            OutputStream os = new FileOutputStream(timeStamp+"_ErrorLog.txt");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            os.write(exceptionAsString.getBytes());
            os.close();
        }

    }
}