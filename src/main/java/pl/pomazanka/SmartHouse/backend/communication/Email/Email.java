package pl.pomazanka.SmartHouse.backend.communication.Email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class Email {

  @Autowired private final JavaMailSender javaMailSender;

  public Email() {
    javaMailSender = javaMailService();
  }

  @Bean
  public JavaMailSender javaMailService() {
    final JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

    javaMailSender.setHost("smtp.gmail.com");
    javaMailSender.setPort(587);

    javaMailSender.setJavaMailProperties(getMailProperties());
    javaMailSender.setUsername("pomazanka.dom@gmail.com");
    javaMailSender.setPassword("fotrcqxqmvjzqesb");

    return javaMailSender;
  }

  private Properties getMailProperties() {
    final Properties properties = new Properties();
    properties.setProperty("mail.transport.protocol", "smtp");
    properties.setProperty("mail.smtp.auth", "true");
    properties.setProperty("mail.smtp.starttls.enable", "true");
    properties.setProperty("mail.debug", "false");
    return properties;
  }

  public void sendEmail(final String html) {
    //		SimpleMailMessage mail = new SimpleMailMessage();
    //		mail.setTo("s.pomazanka@gmail.com");
    //		mail.setFrom("pomazanka.dom@gmail.com");
    //		mail.setSubject("DOM - ALARM!!!");
    //		mail.setText(message);
    try {
      final MimeMessage message = javaMailSender.createMimeMessage();
      message.setSubject("DOM - ALARM!!!");
      final MimeMessageHelper helper;
      helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom("pomazanka.dom@gmail.com");
      helper.setTo("s.pomazanka@gmail.com");
      helper.setText(html, true);
      javaMailSender.send(message);
    } catch (final MessagingException e) {
      e.printStackTrace();
    }
  }
}
