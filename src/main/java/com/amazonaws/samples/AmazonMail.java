package com.amazonaws.samples;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;

public class AmazonMail {

	static final Regions REGION = Regions.US_EAST_1; //chose a region https://docs.aws.amazon.com/general/latest/gr/rande.html#ses_region
	static final String accessKey = ""; //credential aws account
	static final String secretKey = ""; //credential aws account
	
	static final String ATTACHMENT = "D:\\excel.xlsx"; //file path 
	static final String FROM = "from@gmail.com"; // Verified identities in Amazon SES.
	static final String TO = "to1@gmail.com;to2@gmail.com;to3@gmail.com"; // If your account is still in the sandbox, you must verify this address before you use it.
	
	static final String SUBJECT = "Amazon SES test (AWS SDK for Java)";
	static final String HTMLBODY = "<h1>Amazon SES test (AWS SDK for Java)</h1>"
			+ "<p>This email was sent with <a href='https://aws.amazon.com/ses/'>"
			+ "Amazon SES</a> using the <a href='https://aws.amazon.com/sdk-for-java/'>" + "AWS SDK for Java</a>";
	
	static final String TEXTBODY = "This email was sent through Amazon SES using the AWS SDK for Java.";
	

	public static void main(String[] args) throws IOException {
		System.out.println(sendEmail(FROM,TO,SUBJECT,TEXTBODY, HTMLBODY));
		System.out.println(sendEmailAttachment(FROM,TO,SUBJECT,TEXTBODY, HTMLBODY,ATTACHMENT));
	}

	public static String sendEmail(String from, String to, String subject, String textBody, String htmlBody) {
		BasicAWSCredentials basicCredencial = new BasicAWSCredentials(accessKey, secretKey);
		AWSStaticCredentialsProvider credential = new AWSStaticCredentialsProvider(basicCredencial);

		String emailFrom = from;
		List<String> adresses = Arrays.asList(to.split(";"));
		Destination emailTo = new Destination().withToAddresses(adresses);
		
		Content emailSubject = new Content().withCharset("UTF-8").withData(subject);
		Content emailHtmlBody = new Content().withCharset("UTF-8").withData(htmlBody);
		Content emailTextBody = new Content().withCharset("UTF-8").withData(textBody); 
		Body body = new Body().withHtml(emailHtmlBody).withText(emailTextBody);
		Message message = new Message().withBody(body).withSubject(emailSubject);
		
		try {
			AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
					.withRegion(REGION)
					.withCredentials(credential)
					.build();

			SendEmailRequest request = new SendEmailRequest()
					.withDestination(emailTo)
					.withSource(emailFrom)
					.withMessage(message);

			client.sendEmail(request);
			return "Email send: " + to;
		} catch (Exception ex) {
			return "Email failed: " + to + " > description: " + ex.getMessage();
		}		
	}
	
	public static String sendEmailAttachment(String from, String to, String subject, String textBody, String htmlBody, String attachment) {
		BasicAWSCredentials basicCredencial = new BasicAWSCredentials(accessKey, secretKey);
		AWSStaticCredentialsProvider credential = new AWSStaticCredentialsProvider(basicCredencial);

		Session session = Session.getDefaultInstance(new Properties());
		MimeMessage message = new MimeMessage(session);
		String adresses = to.replaceAll(";",",");
		try {
			 
			message.setSubject(subject, "UTF-8");
			message.setFrom(new InternetAddress(from));
			
			message.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(adresses));

			MimeMultipart messsageBody = new MimeMultipart("alternative");

			MimeBodyPart textPart = new MimeBodyPart();
			textPart.setContent(textBody, "text/plain; charset=UTF-8");
			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(htmlBody, "text/html; charset=UTF-8");
			messsageBody.addBodyPart(textPart);
			messsageBody.addBodyPart(htmlPart);

			MimeBodyPart wrap = new MimeBodyPart();
			wrap.setContent(messsageBody);
			MimeMultipart msg = new MimeMultipart("mixed");
			message.setContent(msg);
			msg.addBodyPart(wrap);

			MimeBodyPart att = new MimeBodyPart();
			DataSource fds = new FileDataSource(attachment);
			att.setDataHandler(new DataHandler(fds));
			att.setFileName(fds.getName());
			msg.addBodyPart(att);

			AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(REGION)
					.withCredentials(credential).build();

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			message.writeTo(outputStream);
			RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));

			SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage);

			client.sendRawEmail(rawEmailRequest);
			return "Email send: " + to;
		} catch (Exception ex) {
			return "Email failed: " + to + " > description: " + ex.getMessage();
		}
	}	

}