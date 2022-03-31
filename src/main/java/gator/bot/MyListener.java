package gator.bot;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.*;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.io.FileWriter;
import java.io.IOException;

import java.util.regex.*;

public class MyListener extends ListenerAdapter
{
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
        return;
    }
}