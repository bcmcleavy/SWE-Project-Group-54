package gator.bot;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import gator.calendar.CalendarBot;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class MyListener extends ListenerAdapter
{
    public CalendarBot calendar;
    static boolean validDate(String date){
    try {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        dateFormat.parse(date);
        return true;
    }catch (ParseException e){
        return false;
    }

    }
    static Date parseDate(String date){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date result = dateFormat.parse(date);
            return result;
        } catch (ParseException e) {
            return null;
        }

    }
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (event.getAuthor().isBot()) return;
        // We don't want to respond to other bot accounts, including ourself
        Message message = event.getMessage();
        String content = message.getContentRaw();
        List<Message.Attachment> attachments = event.getMessage().getAttachments();
        // getContentRaw() is an atomic getter
        // getContentDisplay() is a lazy getter which modifies the content for e.g. console view (strip discord formatting)
        //-----Pong Command-----//
        if (content.equals("!ping"))
        {
            MessageChannel channel = event.getChannel();
            channel.sendMessage("Pong!").queue(); // Important to call .queue() on the RestAction returned by sendMessage(...)
        }


        if(content.equals("!image")) {
            if(attachments.isEmpty())
            {
                return;
            }
            MessageChannel channel = event.getChannel();
            CompletableFuture<File> future = attachments.get(0).downloadToFile(new File("./imageStore/" + "image.jpg"));
            channel.sendMessage("Blue!").queue();
        }
        Message commandMsg = event.getMessage();
        String[] commandArray = commandMsg.getContentRaw().split(" ");
        MessageChannel channel = event.getChannel();
        
        //--- Create Event ---//
        if(commandArray[0].equals("!createEvent")){
            if(commandArray.length < 3){
                channel.sendMessage("Invalid Usage! !createEvent [Name] [Start Date] [End Date(opt)]").queue();
            }
            if(validDate(commandArray[2]) == false) {
                channel.sendMessage("Invalid Start Date! Use YYYY-MM-DD").queue();
            }else if (!(commandArray.length >= 4 && validDate(commandArray[3]))) {
                channel.sendMessage("Invalid End Date! Use YYYY-MM-DD").queue();

            } else {
                Event calendarEvent = new Event().setSummary(commandArray[1]);

                DateTime startTime = new DateTime(commandArray[2]);
                EventDateTime start = new EventDateTime().setDate(startTime);
                calendarEvent.setStart(start);
                if(commandArray.length >= 4){
                    //Parse End date
                    DateTime endTime = new DateTime(commandArray[3]);
                    EventDateTime end = new EventDateTime().setDate(endTime);
                    calendarEvent.setEnd(end);
                } else {
                    calendarEvent.setEnd(start);
                }
                try {
                    calendarEvent = calendar.service.events().insert("primary", calendarEvent).execute();
                    channel.sendMessage(("Event Created! " + calendarEvent.getHtmlLink())).queue();
                } catch (IOException e){
                    channel.sendMessage("Error Adding Event!").queue();
                }
            }


        }
        //-- Modify Event ---//
        if(commandArray[0].equals("!modifyEvent")){
            Event target = null;
            String targetID = null;
            if(commandArray.length < 4){
                channel.sendMessage("Invalid Usage! !modifyEvent [Name] [New Start] [New End]").queue();
            } else {
                DateTime now = new DateTime(System.currentTimeMillis());
                try {
                    Events events = calendar.service.events().list("primary")
                            .setTimeMin(now)
                            .setOrderBy("startTime")
                            .setSingleEvents(true)
                            .execute();
                    List<Event> items = events.getItems();

                    for (Event eventOfList : items) {
                        String title = eventOfList.getSummary();
                        if(title.equals(commandArray[1])){
                            //Title match, get event ID and delete
                            target = eventOfList;
                            targetID = eventOfList.getId();
                            break;
                        }
                    }
                    if(target == null || targetID == null) {
                        channel.sendMessage("No event of name \"" + commandArray[1] + "\" found!").queue();
                        return;
                    }
                    //Modify
                    DateTime startTime = new DateTime(commandArray[2]);
                    EventDateTime start = new EventDateTime().setDate(startTime);
                    target.setStart(start);
                    if(commandArray.length > 4) {
                        //Parse End date
                        DateTime endTime = new DateTime(commandArray[3]);
                        EventDateTime end = new EventDateTime().setDate(endTime);
                        target.setEnd(end);
                    } else {
                        target.setEnd(start);
                    }
                    //Update
                    try {
                        target = calendar.service.events().update("primary", targetID,target).execute();
                        channel.sendMessage(("Event Modified! " + target.getHtmlLink())).queue();
                    } catch (IOException e){
                        channel.sendMessage("Error Modifying Event!").queue();
                    }


                } catch (IOException e) {
                    //Error thrown by Calendar API
                    channel.sendMessage("Error Accessing Calendar!").queue();
                }
            }
        }
        //--- Delete Event ---//
        if(commandArray[0].equals("!deleteEvent")){
            if(commandArray.length < 2){
                channel.sendMessage("Invalid Usage! !deleteEvent [Name]").queue();
            } else {
                DateTime now = new DateTime(System.currentTimeMillis());
                try {
                    Events events = calendar.service.events().list("primary")
                            .setTimeMin(now)
                            .setOrderBy("startTime")
                            .setSingleEvents(true)
                            .execute();
                    List<Event> items = events.getItems();

                    for (Event eventOfList : items) {
                        String title = eventOfList.getSummary();
                        if(title.equals(commandArray[1])){
                            //Title match, get event ID and delete
                            String targetId = eventOfList.getId();
                            calendar.service.events().delete("primary", targetId).execute();
                            channel.sendMessage("Event \"" + title + "\" Deleted!").queue();

                            return;
                        }
                    }
                    //No Event Located
                    channel.sendMessage("No event of name \"" + commandArray[1] + "\" found!").queue();
                } catch (IOException e) {
                    //Error thrown by Calendar API
                    channel.sendMessage("Error Accessing Calendar!").queue();
                }

            }

        }

    }
    //Slash Commands
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
    {

        /*--- Slash Test Command ---*/
        if (event.getName().equals("slash")) { //Check Name
            long time = System.currentTimeMillis();
            event.reply("Slash Command Test!").setEphemeral(true) // reply with ghost message
                    .flatMap(v ->
                            event.getHook().editOriginalFormat("Delay: %d ms", System.currentTimeMillis() - time) // edit original
                    ).queue(); // Queue both reply and edit
        }
        if (event.getName().equals("faq")){
            //FAQ Command

            StringBuilder msgFAQ = new StringBuilder();
            //Read FAQ text file
            try {
                File file = new File("faq.txt");
                Scanner sc = new Scanner(file);
                while(sc.hasNextLine()){
                    msgFAQ.append(sc.nextLine());
                    msgFAQ.append("\n");
                }
                sc.close();
                event.reply(msgFAQ.toString()).queue();
            } catch (FileNotFoundException e) {
                event.reply("FAQ Not Available!").queue();
            }
        }
        if (event.getName().equals("info")){
            //Info Command

            StringBuilder msgInfo = new StringBuilder();
            //Read Info text file
            try {
                File file = new File("info.txt");
                Scanner sc = new Scanner(file);
                while(sc.hasNextLine()){
                    msgInfo.append(sc.nextLine());
                    msgInfo.append("\n");
                }
                sc.close();
                event.reply(msgInfo.toString()).queue();
            } catch (FileNotFoundException e) {
                event.reply("Info Not Available!").queue();
            }
        }

        return;
    }
}