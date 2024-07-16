import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

public class ConfigurationEncryptionMachine {

    public static void main(String[] args) throws Exception {
        // Define your secret key and salt (keep these secure and don't hardcode in production)
        String secretKey = ConfigurationEncryptionMachine.getRandomKey(); //you should generate a random key if this is an encryption program
        String salt = "MySalt";
        FileInputStream fin=null;
        int ch;
        String configuration="";
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());


        StringWriter sw = new StringWriter();

        try{
            fin = new FileInputStream("./configuration/configuration.txt");
            if (fin != null) {
                while ((ch = fin.read()) != -1) {
                    configuration=configuration+(char)ch;
                }
            }


            // Encrypt the string
            String encryptedString = AES256.encrypt(configuration, secretKey, salt); //this should be a separate encryption program

            FileOutputStream writer = new FileOutputStream("./configuration/configuration (encrypted).txt");
            writer.write(encryptedString.getBytes());

            ProcessBuilder pb = new ProcessBuilder();
            pb.command("/bin/bash","-c","rm ./configuration/configuration.txt"); //replace this to /usr/bin/bash
            Process process = pb.start();
            writer = new FileOutputStream("./configuration/secretKey.txt");
            writer.write(secretKey.getBytes());
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
    private static String getRandomKey() {
        String randomKeySet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder key = new StringBuilder();
        Random rnd = new Random();
        while (key.length() < 20) { // length of the random string.
            int index = (int) (rnd.nextFloat() * randomKeySet.length());
            key.append(randomKeySet.charAt(index));
        }
        String randomKey = key.toString();
        return randomKey;

    }
}