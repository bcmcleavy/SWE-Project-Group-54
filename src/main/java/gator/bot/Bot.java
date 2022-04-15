
package gator.bot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import gator.calendar.CalendarBot;

public class Bot {
    public static void main(String[] arguments) throws IOException, GeneralSecurityException, Exception
    {

        //Calendar API
        CalendarBot calendarAPI = new CalendarBot();
        calendarAPI.buildCalendar();
        //calendarAPI.testAddEvent();
        calendarAPI.listEvents();




        //Discord API
        FileReader input = null;
        String token;
        try {
            input = new FileReader("tokens.txt");
            char[] chars = new char[59];
            int n = input.read(chars);
            token = new String(chars);

        }finally {
            if (input != null) {
                input.close();
            }
        }
        MyListener listener = new MyListener();
        listener.calendar = calendarAPI;
        JDA api = JDABuilder.createDefault(token).addEventListeners(listener).build();
        //Addition of Slash Commands (Takes up to 1 hr to update)
        api.upsertCommand("slash", "Slash Command Test").queue();
        api.upsertCommand("faq", "Frequently Asked Questions").queue();
        api.upsertCommand("info","Class Information").queue();
    }
}

