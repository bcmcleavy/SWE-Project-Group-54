package gator.bot;



import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import gator.calendar.CalendarBot;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


import java.io.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.io.FileWriter;
import java.io.IOException;

import java.util.regex.*;

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

        if(Pattern.matches("!issueboard .+ (\\s|\\S)+", content))
        {
            MessageChannel channel = event.getChannel();
            int toSplit = 0;
            int count = 0;
            for(int i = 0; i < content.length(); i++)
            {
                if(content.charAt(i) == ' ')
                {
                    count += 1;
                    if(count == 2)
                    {
                        toSplit = i;
                        break;
                    }
                }
            }
            String fileName = content.substring(12, toSplit);
            String issueText = content.substring(toSplit+1);
            FileWriter myWriter = null;
            try {
                myWriter = new FileWriter("./issueBoards/" + (fileName + ".txt"), true);
            } catch (IOException e) {
                channel.sendMessage("Broken file location!").queue();
                e.printStackTrace();
            }
            if(myWriter != null)
            {
                try {
                    myWriter.write(issueText + "\n");
                    myWriter.close();
                    channel.sendMessage("Success, issue logged!").queue();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(Pattern.matches("!readboard (\\s|\\S)+", content)) {
            File textFile = null;
            MessageChannel channel = event.getChannel();
            textFile = new File("./issueBoards/" + (content.substring(11) + ".txt"));
            try {
                Scanner s = new Scanner(textFile).useDelimiter("\n");
                int lineNumber = 1;
                while(s.hasNextLine())
                {
                    String line = s.nextLine();
                    channel.sendMessage(lineNumber + ". " + line).queue();
                    lineNumber += 1;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        java.util.List<Role> authorRole = event.getMember().getRoles();
        Role roleCreator = event.getGuild().getRoleById("958889828118851645");
        Role teacher = event.getGuild().getRoleById("958912483647684668");
        Role student = event.getGuild().getRoleById("958890976640909352");
        Role ta = event.getGuild().getRoleById("958912619098562620");
        Role author = authorRole.get(0);
        if(content.startsWith("!changeToRoleCreator")){
            if(author == roleCreator){
                java.util.List<Member> move = message.getMentionedMembers();
                for (Member member : move) {
                    java.util.List<Role> curRole = member.getRoles();
                    event.getGuild().addRoleToMember(member, roleCreator).queue();
                    event.getGuild().removeRoleFromMember(member, curRole.get(0)).queue();
                    MessageChannel channel = event.getChannel();
                    channel.sendMessage("Success!").queue();
                }
            }
            else {
                MessageChannel channel = event.getChannel();
                channel.sendMessage("You do not have permission to do this!").queue();
            }
        }

        if(author == teacher || author == roleCreator) {
            if (content.startsWith("!changeToTeacher")) {
                java.util.List<Member> move = message.getMentionedMembers();
                for (Member member : move) {
                    java.util.List<Role> curRole = member.getRoles();
                    event.getGuild().addRoleToMember(member, teacher).queue();
                    event.getGuild().removeRoleFromMember(member, curRole.get(0)).queue();
                    MessageChannel channel = event.getChannel();
                    channel.sendMessage("Success!").queue();
                }

            }
            if (content.startsWith("!changeToTA")) {
                java.util.List<Member> move = message.getMentionedMembers();
                for (Member member : move) {
                    java.util.List<Role> curRole = member.getRoles();
                    event.getGuild().addRoleToMember(member, ta).queue();
                    event.getGuild().removeRoleFromMember(member, curRole.get(0)).queue();
                    MessageChannel channel = event.getChannel();
                    channel.sendMessage("Success!").queue();
                }
            }
            if (content.startsWith("!changeToStudent")) {
                java.util.List<Member> move = message.getMentionedMembers();
                for (Member member : move) {
                    java.util.List<Role> curRole = member.getRoles();
                    event.getGuild().addRoleToMember(member, student).queue();
                    event.getGuild().removeRoleFromMember(member, curRole.get(0)).queue();
                    MessageChannel channel = event.getChannel();
                    channel.sendMessage("Success!").queue();
                }
            }
        }
        else{
            MessageChannel channel = event.getChannel();
            channel.sendMessage("You do not have permission to do this!").queue();
        }
                if(Pattern.matches("!createTextChannel .*", content))
        {
            Member member = event.getMember();
            createTextChannel(member, content.substring(19));
        }
        if(Pattern.matches("!createVoiceChannel .*", content))
        {
            Member member = event.getMember();
            createVoiceChannel(member, content.substring(20));
        }
        if(Pattern.matches("!moveMember .*", content))
        {
            Member member = event.getMember();
            MessageChannel channel = event.getChannel();
            //VoiceChannel myVoiceChan = (VoiceChannel)event.getGuild().getVoiceChannelsByName(content.substring(11), false).toArray()[0];
            //movePerson(member, (VoiceChannel)event.getGuild().getVoiceChannelsByName(content.substring(11), false).toArray()[0], event);
            //MessageChannel channel = event.getChannel();
            for(int i = 0; i < event.getGuild().getVoiceChannels().size(); i++)
            {
                String curResult = event.getGuild().getVoiceChannels().get(i).getName();
                String inputo = content.substring(12);
                if(curResult.equals(inputo)) {
                    movePerson(member, event.getGuild().getVoiceChannels().get(i), event);
                    return;
                }
                else
                {
                    //channel.sendMessage(event.getGuild().getVoiceChannels().get(i).getName()).queue();
                }
            }
            channel.sendMessage("No voice channel with that name available!").queue();
            //channel.sendMessage(Integer.toString(event.getGuild().getVoiceChannelsByName(content.substring(11), true).size())).queue();
            //channel.sendMessage(Integer.toString(event.getGuild().getVoiceChannels().size())).queue();
       } //This Was Added!
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
        if(content.startsWith("!submit ")){
            String assignmentName = content.substring(8);
            Path path = Paths.get("./assignments/" + assignmentName);
            if(attachments.isEmpty()){
                channel.sendMessage("No attachment included").queue();
            }
            else if(Files.exists(path)) {
                CompletableFuture<File> future = attachments.get(0).downloadToFile(new File("./assignments/" + assignmentName + "/" + attachments.get(0).getFileName()));
                channel.sendMessage("Success").queue();
            }
            else{
                channel.sendMessage("Assignment does not exist!").queue();
            }
        }

        if(content.startsWith("!mkAssignment")){
            String dir = content.substring(14);
            if(!(author == teacher || author == ta || author == roleCreator)){
                channel.sendMessage("You do not have permission to do this!").queue();
            }
            else if(dir.isEmpty()){
                channel.sendMessage("Please include assignment name").queue();
            }
            else{
                new File("./assignments/" + dir).mkdirs();
                channel.sendMessage("Success!").queue();

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

public static String testOutput(String content)
    {
        if(Pattern.matches("!issueboard .+ (\\s|\\S)+", content))
        {
            int toSplit = 0;
            int count = 0;
            for(int i = 0; i < content.length(); i++)
            {
                if(content.charAt(i) == ' ')
                {
                    count += 1;
                    if(count == 2)
                    {
                        toSplit = i;
                        break;
                    }
                }
            }
            String fileName = content.substring(12, toSplit);
            String issueText = content.substring(toSplit+1);
            FileWriter myWriter = null;
            try {
                myWriter = new FileWriter("./issueBoards/" + (fileName + ".txt"), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(myWriter != null)
            {
                try {
                    myWriter.write(issueText + "\n");
                    myWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            File textFile = null;
            textFile = new File("./issueBoards/" + (fileName + ".txt"));
            String wholeString = "";
            try {
                Scanner s = new Scanner(textFile).useDelimiter("\n");
                int lineNumber = 1;
                while(s.hasNextLine())
                {
                    String line = lineNumber + ". " + s.nextLine();
                    wholeString += line;
                    lineNumber += 1;
                }
                return wholeString;
            } catch (FileNotFoundException e) {

            }
            return wholeString;
        }
        return "broken regex";
    }
    public static void createTextChannel(Member member, String name) {
        Guild guild = member.getGuild();
        guild.createTextChannel(name)
                .addPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL), null)
                .addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                .queue(); // this actually sends the request to discord.
    }
    public static void createVoiceChannel(Member member, String name) {
        Guild guild = member.getGuild();
        guild.createVoiceChannel(name)
                .addPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL), null)
                .addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                .queue(); // this actually sends the request to discord.
    }
    public static void movePerson(Member member, VoiceChannel voiceChannel, MessageReceivedEvent event)
    {
        MessageChannel channel = event.getChannel();
        try {
            event.getGuild().moveVoiceMember(member, voiceChannel).queue();
        }
        catch(Exception e)
        {
            channel.sendMessage("Missing Permissions").queue();
        }
    }
}
