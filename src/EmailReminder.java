import org.json.JSONObject;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

//needa encrpyt password, use openssl generate aes-256 key base 64
//send to one mail group so no need xml file to change config
//needa show id, number of days before expiry date and real expiry date
//send every day but 2 types (with expiry and without expiry)

public class EmailReminder {
    public static void main(String[] args) throws Exception{
        try{

            FileInputStream fin=null;
            int ch;
            String configuration="";
            String salt = "MySalt";
            String secretKey="";


            fin = new FileInputStream("./configuration/configuration (encrypted).txt");
            if (fin != null) {
                while ((ch = fin.read()) != -1) {
                    configuration=configuration+(char)ch;
                }
            }

            fin = new FileInputStream("./configuration/secretKey.txt");
            if (fin != null) {
                while ((ch = fin.read()) != -1) {
                    secretKey=secretKey+(char)ch;
                }
            }

            String decryptedConfiguration = AES256.decrypt(configuration,secretKey,salt);
            JSONObject jsonObject = new JSONObject(decryptedConfiguration);




            String sql = jsonObject.getString("query");
            String url = jsonObject.getString("url");
            String username = jsonObject.getString("username");
            String password = jsonObject.getString("password");
            String toAddress = jsonObject.getString("toAddress");
            Connection con = DriverManager.getConnection(url, username, password);

            Statement st = con.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = st.executeQuery(sql);

            //int[] columnLengths= EmailReminder.maxColumns(rs);
            rs.beforeFirst();

            FileWriter writer = new FileWriter("latestRecord.txt",false);
            writer.write("Subject: Password Expiry Reminder\nTo: "+toAddress+"\nContent-Type: text/html; charset=\"UTF-8\"\n\n");
            writer.write("The following accounts' passwords will expire soon:\n");
            writer.write(String.format("<table border=\"1\"><tr><th>%s</th><th>%s</th><th>%s</th></tr>","id","Number of days before expiry","Expiry date")+"\n"); //change to correct column names later
            while (rs.next()) {
                writer.write(String.format("<tr><td>%s</td><td>%s</td><td>%s</td></tr>",rs.getString(1), rs.getString(2), rs.getString(3))+"\n"); //change this later to fit selection result
            }
            writer.write("</table>");
            // step5 close the connection object

            con.close();
            writer.close();

            ProcessBuilder pb = new ProcessBuilder();
            pb.command("/usr/bin/bash","-c","sendmail -i -t \""+toAddress+"\" < latestRecord.txt");
            Process process = pb.start();

        } catch (Exception e) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            OutputStream os = new FileOutputStream("./errorLog/forEmailReminder_error/"+timeStamp+"_ErrorLog.txt");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            os.write(exceptionAsString.getBytes());
            os.close();
        }

    }

    public static int[] maxColumns(ResultSet resultSet) throws SQLException {
        int columnNumber = resultSet.getMetaData().getColumnCount();
        int[] max = new int[columnNumber];
        int currentRow = resultSet.getRow();
        int fetchDirection = resultSet.getFetchDirection();
        if (fetchDirection == ResultSet.FETCH_FORWARD) {
            resultSet.beforeFirst();
        }else {
            resultSet.afterLast();
        }

        while (resultSet.next()) {
            for (int i = 1; i <= columnNumber; i++) { //Null Pointer Exception
                if (resultSet.getString(i).length() > max[i-1]) {
                    max[i - 1] = resultSet.getString(i).length();
                }
            }
        }
        resultSet.absolute(currentRow);
        return max;
    }
}