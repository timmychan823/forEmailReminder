import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.Random;

public class ConfigurationEncryptionMachine {

    public static void main(String[] args) throws Exception {
        // Define your secret key and salt (keep these secure and don't hardcode in production)
        String secretKey = ConfigurationEncryptionMachine.getRandomString(20); //you should generate a random key if this is an encryption program
        String salt = ConfigurationEncryptionMachine.getRandomString(20);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());


        StringWriter sw = new StringWriter();

        try{
            InputStream input = new FileInputStream("config.properties");
            Properties prop = new Properties();
            prop.load(input);
            prop.setProperty("db.password",AES256.encrypt(prop.getProperty("db.password"), secretKey, salt));
            prop.setProperty("encryptionKey",secretKey);
            prop.setProperty("encryptionSalt",salt);

            System.out.println(prop.getProperty("db.query"));

            OutputStream output = new FileOutputStream("config.properties");
            prop.store(output,null);

        }catch (Exception e){
            Files.createDirectories(Paths.get("./errorLog/encryptionError/"));

            File file = new File("./errorLog/encryptionError/"+timeStamp+"_ErrorLog.txt");
            if (!file.exists()){
                file.createNewFile();
            }
            OutputStream os = new FileOutputStream(file);

            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            os.write(exceptionAsString.getBytes());
            os.close();
        }



    }
    private static String getRandomString(int randomStringLength) {
        String randomCharSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder randomStringBuilder = new StringBuilder();
        Random rnd = new Random();
        while (randomStringBuilder.length() < randomStringLength) { // length of the random string.
            int index = (int) (rnd.nextFloat() * randomCharSet.length());
            randomStringBuilder.append(randomCharSet.charAt(index));
        }
        String randomString = randomStringBuilder.toString();
        return randomString;

    }
}