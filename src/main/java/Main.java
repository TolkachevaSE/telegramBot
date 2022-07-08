import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {

static Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        LOGGER.debug("Ура. все работает!");
        try {
            Bot bot=new Bot();
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new Bot());
            LOGGER.info("Бот запущен");
        } catch (TelegramApiException e) {
            e.printStackTrace();
            LOGGER.error(e.toString());}

        try {
            PingG.scheduler();
            LOGGER.debug("Ping ok");
        }catch(Exception e){
            LOGGER.error("Ping failed");
        }
            }








    //    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
//scheduler
      //  scheduleAtFixedRate(new MyTimerTask(), 0, 1, TimeUnit.MINUTES);

}
/*try {
    SQLTable table = new SQLTable();
    table.createTable(table.getConnectionBD());
}catch(Exception e)
{e.printStackTrace();}
    }}*/
