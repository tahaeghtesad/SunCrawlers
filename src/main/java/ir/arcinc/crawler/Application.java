package ir.arcinc.crawler;

import ir.arcinc.crawler.model.User;
import ir.arcinc.crawler.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;

/**
 * Created by tahae on 8/17/2016.
 */
@SpringBootApplication
public class Application implements CommandLineRunner {
    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);
        String[] beans = ctx.getBeanDefinitionNames();
        Arrays.sort(beans, (o1, o2) -> o1.toLowerCase().compareTo(o2.toLowerCase()));
        for (int i = 0; i < beans.length; i++)
            System.out.println(i + 1 + ") " + beans[i]);
        System.out.println(
                "  ___           ___                 _         \n" +
                " / __|_  _ _ _ / __|_ _ __ ___ __ _| |___ _ _ \n" +
                " \\__ \\ || | ' \\ (__| '_/ _` \\ V  V / / -_) '_|\n" +
                " |___/\\_,_|_||_\\___|_| \\__,_|\\_/\\_/|_\\___|_|  \n" +
                "                                              "
        );
    }

    @Autowired
    private IUserRepository repository;

    @Override
    public void run(String... strings) throws Exception {
        repository.deleteAll();
        repository.save(new User("taha","taha eghtesad", "this is me", "chertopert"));
    }
}
