package kr.co.aim.messolution.userprofile.event;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import kr.co.aim.messolution.generic.errorHandler.CustomException;

import org.springframework.mail.javamail.JavaMailSenderImpl;

public class MailSender {
	private String id;
	private String pw;
	
	public MailSender(String id, String pw){
		this.id = id;
		this.pw = pw;
	}
	
	public Boolean sendSmtp(ArrayList<String> receiveIDList, String title, String contents, String filePath,String protocol,String port,String host, String Author) throws CustomException {
		try{

			System.out.println("*==============Start to Mail================*");
			Properties props = new Properties(); 
			props.put("mail.transport.protocol", protocol); 
			props.put("mail.smtp.host", host); 
			props.put("mail.smtp.port", port);       
			props.put("mail.smtp.auth", Author);  
			props.put("mail.smtp.starttls.enable","true");
			props.put("mail.smtp.ssl.trust", host);
			
			Authenticator authenticator = new Authenticator()
			{
				protected PasswordAuthentication getPasswordAuthentication()
				{
					return new PasswordAuthentication(id,pw);
	            }
			};			
			Session session = Session.getDefaultInstance(props, authenticator);
			session.setDebug(true);
			
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(id+"@everdisplay.com"));
			message.setSubject(title);

			// Set TO:
			int num = receiveIDList.size();
			for (int i=0; i < num; i++) {
				message.addRecipient(Message.RecipientType.TO, new InternetAddress((String) receiveIDList.get(i)));
			}

			//Start
			Multipart mp = new MimeMultipart();
			MimeBodyPart mbp1 = new MimeBodyPart();
			mbp1.setContent(contents, "text/html; charset=utf-8");
			mp.addBodyPart(mbp1);
			
			/*if (!filePath.equals("")){
				String filename = filePath;
				if (filename != null){
					if (fileSizeCheck(filename)){
						MimeBodyPart mbp2 = new MimeBodyPart();
						FileDataSource fds = new FileDataSource(filename);
						mbp2.setDataHandler(new DataHandler(fds));
						mbp2.setFileName(MimeUtility.encodeText(fds.getName(), "UTF-8", "B"));
			             
						mp.addBodyPart(mbp2);
					} else {
						throw new Exception("file size overflow !");
			        }
			    }
			}*/
		        
			MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
			mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
			mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
			mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
			mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
			mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
			CommandMap.setDefaultCommandMap(mc);

			message.setContent(mp);
			System.out.println("*==============Already to Mail Data================*");

			Transport.send(message);
			System.out.println("*==============Send to Mail================*");
			System.out.println("*==============End to Mail  (Success)================*");
			return true;
		
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("*==============End to Mail  (Fail)================*");
			System.out.println(e);
			String err = e.toString();
			throw new CustomException("PMS-0000", err);
		}

	}
	
	public Boolean sendSmtp2(List<String> receiveIDList, String title, String contents, String filePath,String protocol,String port,String host, String Author) throws CustomException {
		try
		{
			System.out.println("*==============Start to Mail================*");
			JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

			Properties mailProperties = new Properties(); 
			mailProperties.put("mail.transport.protocol", protocol); 
			mailProperties.put("mail.smtp.host", host);
			mailProperties.put("mail.smtp.port", port);
			mailProperties.put("mail.smtp.auth", Author);
			mailProperties.put("mail.smtp.ssl.enable", "true");
			mailProperties.put("mail.smtp.ssl.trust", host);

			Session sess = Session.getDefaultInstance(mailProperties,new javax.mail.Authenticator()
			{
				protected PasswordAuthentication getPasswordAuthentication(){
					return new PasswordAuthentication(id,pw);
				}
			});
			
			sess.setDebug(true);
			
			Message msg = new MimeMessage(sess);
			
			msg.setFrom(new InternetAddress(id+"@everdisplay.com"));
			msg.setSubject(title);
			
			// Set TO:
			int num = receiveIDList.size();
			for (int i=0; i < num; i++) {
				msg.addRecipient(Message.RecipientType.TO, new InternetAddress(receiveIDList.get(i)));
			}

			msg.setText(contents);
			
			System.out.println("*==============Already Send to Mail Data================*");
			Transport.send(msg);
			
			System.out.println("*==============End to Mail  (Success)================*");
			return true;
		}
		catch (Exception e) {
			// TODO: handle exception
			System.out.println("*==============End to Mail  (Fail)================*");
			e.printStackTrace();
			throw new CustomException("PMS-0000", e);
		}
	}

	private boolean fileSizeCheck(String filename) {
	        if (new File(filename).length() > 10485760) {
            return false;
        }
        return true;
    }


}
