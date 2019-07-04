package tech.clusterfunk;

import org.json.JSONObject;
import spark.Spark;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static spark.Spark.*;

public class Main {

    private static final String SMTP = "smtp.gmail.com";
    private static final String PORT = "587";
    private static final String USER = "jompa010@gmail.com";
    private static final String PASSWORD = "lxfkubkanxacdevr";

    private static final String KEYSTORE_PATH = "keystore.jks";
    private static final String KEYSTORE_PW = "mysupersecurepw";

    public static void main(String[] args) {
        Spark.secure(KEYSTORE_PATH, KEYSTORE_PW, null , null);

        path("/api", () -> {
            get("/info", (request, response) -> {
                response.status(200);
                response.body("Mail API is running");
                return response;
            });

            post("/mail", (request, response) -> {

                Properties prop = new Properties();
                prop.put("mail.smtp.host", SMTP);
                prop.put("mail.smtp.port", PORT);
                prop.put("mail.smtp.auth", "true");
                prop.put("mail.smtp.starttls.enable", "true");

                Session session = Session.getInstance(prop,
                        new Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(USER, PASSWORD);
                            }
                        });

                if (request.body() != null) {
                    JSONObject json = new JSONObject(request.body());
                    String subject = json.getString("subject");
                    String body = json.getString("body");
                    String from = json.getString("from");

                    try {

                        Message message = new MimeMessage(session);
                        message.setFrom(new InternetAddress(from));
                        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(USER));
                        message.setSubject(subject);
                        message.setText(body);

                        Transport.send(message);

                        System.out.println("Sent mail");

                    } catch (MessagingException e) {
                        e.printStackTrace();
                        response.status(500);
                        response.body("ERROR: Mail could not be send");
                    }
                } else {
                    response.status(400);
                    response.body("ERROR: Missing request body");
                    return response;
                }
                response.status(200);
                response.body("");
                return response;
            });
        });
    }
}
