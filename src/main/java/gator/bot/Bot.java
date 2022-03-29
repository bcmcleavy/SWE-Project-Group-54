
package gator.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;

public class Bot {
    public static void main(String[] arguments) throws Exception
    {
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

        JDA api = JDABuilder.createDefault(token).addEventListeners(new MyListener()).build();
    }
}

