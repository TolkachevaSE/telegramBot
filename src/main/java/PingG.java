import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;

import java.net.URL;

public class PingG{
    static Logger LOGGER = LoggerFactory.getLogger(PingG.class);


    static private void pingGo(){
        try {
            URL url = new URL("https://www.google.com/");
            HttpURLConnection connection= (HttpURLConnection) url.openConnection();
            connection.connect();
            LOGGER.info("Ping  OK: response code {} !",connection.getResponseCode());
            connection.disconnect();

        }
        catch (IOException e){
            LOGGER.error("Не могу пинговать", e);
        }
    }

    static Thread run = new Thread(new Runnable() {
        @Override
        public void run() {
            while(true){
                try {
                    pingGo();
                    Thread.sleep(1200000); //1000 - 1 сек
                } catch (InterruptedException ex) {
                    LOGGER.error("Ping был прерван", ex);
                }            }        }    });

    static public void scheduler(){
        run.start();
    }



    }
