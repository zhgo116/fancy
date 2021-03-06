package cn.telling.tools.sendMail;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.Address;
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
import javax.mail.internet.MimeUtility;
/**
 * <p>
 * Title: 使用javamail发送邮件
 * </p>
 */
public class MailUtils {
 String to = "";// 收件人
 String from = "";// 发件人
 String host = "";// smtp主机
 String username = "";
 String password = "";
 String filename = "";// 附件文件名
 String subject = "";// 邮件主题
 String content = "";// 邮件正文
 Vector<String> file = new Vector<String>();// 附件文件集合
 /**
  * <br>
  * 方法说明：默认构造器 <br>
  * 输入参数： <br>
  * 返回类型：
  */
 public MailUtils() {
 }
 /**
  * <br>
  * 方法说明：构造器，提供直接的参数传入 <br>
  * 输入参数： <br>
  * 返回类型：
  */
 public MailUtils(String to, String from, String smtpServer,String username, String password, String subject, String content) {
  this.to = to;
  this.from = from;
  this.host = smtpServer;
  this.username = username;
  this.password = password;
  this.subject = subject;
  this.content = content;
 }
 /**
  * <br>
  * 方法说明：设置邮件服务器地址 <br>
  * 输入参数：String host 邮件服务器地址名称 <br>
  * 返回类型：
  */
 public void setHost(String host) {
  this.host = host;
 }
 /**
  * <br>
  * 方法说明：设置登录服务器校验密码 <br>
  * 输入参数： <br>
  * 返回类型：
  */
 public void setPassWord(String pwd) {
  this.password = pwd;
 }
 /**
  * <br>
  * 方法说明：设置登录服务器校验用户 <br>
  * 输入参数： <br>
  * 返回类型：
  */
 public void setUserName(String usn) {
  this.username = usn;
 }
 /**
  * <br>
  * 方法说明：设置邮件发送目的邮箱 <br>
  * 输入参数： <br>
  * 返回类型：
  */
 public void setTo(String to) {
  this.to = to;
 }
 /**
  * <br>
  * 方法说明：设置邮件发送源邮箱 <br>
  * 输入参数： <br>
  * 返回类型：
  */
 public void setFrom(String from) {
  this.from = from;
 }
 /**
  * <br>
  * 方法说明：设置邮件主题 <br>
  * 输入参数： <br>
  * 返回类型：
  */
 public void setSubject(String subject) {
  this.subject = subject;
 }
 /**
  * <br>
  * 方法说明：设置邮件内容 <br>
  * 输入参数： <br>
  * 返回类型：
  */
 public void setContent(String content) {
  this.content = content;
 }
 /**
  * <br>
  * 方法说明：把主题转换为中文 <br>
  * 输入参数：String strText <br>
  * 返回类型：
  */
 public String transferChinese(String strText) {
  try {
   strText = MimeUtility.encodeText(new String(strText.getBytes("UTF-8")), "UTF-8", "B");
  } catch (Exception e) {
   e.printStackTrace();
  }
  return strText;
 }
 /**
  * <br>
  * 方法说明：往附件组合中添加附件 <br>
  * 输入参数： <br>
  * 返回类型：
  */
 public void attachfile(String fname) {
  file.addElement(fname);
 }
 /**
  * <br>
  * 方法说明：发送邮件 <br>
  * 输入参数： <br>
  * 返回类型：boolean 成功为true，反之为false
  */
 public boolean sendMail() {
	 /***
	  * 由于在服务器发送邮件报错才加了以下代码
	  */
	 MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
     mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
     mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
     mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
     mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
     mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
     CommandMap.setDefaultCommandMap(mc);
	 
  // 构造mail session
  Properties props = new Properties();
  props.put("mail.smtp.host", host);
  props.put("mail.smtp.auth", "true");
  Session session = Session.getDefaultInstance(props,
    new Authenticator() {
     public PasswordAuthentication getPasswordAuthentication() {
      return new PasswordAuthentication(username, password);
     }
    });
  try {
   // 构造MimeMessage 并设定基本的值
   MimeMessage msg = new MimeMessage(session);
   msg.setFrom(new InternetAddress(from));
   // //这个只能是给一个人发送email
   Address[] tos = null;
        String[] toArray = to.split(";");
        if (toArray != null){
         tos = new InternetAddress[toArray.length];
         for(int i=0; i<toArray.length; i++){
             String s = toArray[i];
                    tos[i] = new InternetAddress(s);
             }
        }
   //msg.setRecipients(Message.RecipientType.BCC,InternetAddress.parse(to));
   msg.setRecipients(Message.RecipientType.TO,tos);// 多个收件人
//   subject = transferChinese(subject);
   msg.setSubject(subject);
   // 构造Multipart
   Multipart mp = new MimeMultipart();
   // 向Multipart添加正文
 
//	 BodyPart html = new MimeBodyPart();
//	 // 设置HTML内容
//	 html.setContent(content, "text/html; charset=utf-8");
//	 mp.addBodyPart(html);
   MimeBodyPart mbpContent = new MimeBodyPart();
   mbpContent.setContent(content, "text/html;charset=utf-8");
//	 向MimeMessage添加（Multipart代表正文）
   mp.addBodyPart(mbpContent);
   // 向Multipart添加附件
   Enumeration<String> efile =  file.elements();
   while (efile.hasMoreElements()) {
    MimeBodyPart mbpFile = new MimeBodyPart();
    filename = efile.nextElement().toString();
    FileDataSource fds = new FileDataSource(filename);
    mbpFile.setDataHandler(new DataHandler(fds));
    //mbpFile.setFileName(new String(fds.getName().getBytes("UTF-8"),"UTF-8"));
    mbpFile.setFileName(MimeUtility.encodeText(fds.getName()));//修改了中文附件乱码的问题
    // 向MimeMessage添加（Multipart代表附件）
    mp.addBodyPart(mbpFile);
   }
   file.removeAllElements();
   // 向Multipart添加MimeMessage
   msg.setContent(mp);
   msg.setSentDate(new Date());
   msg.saveChanges();
   // 发送邮件
   Transport transport = session.getTransport("smtp");
   transport.connect(host, username, password);
   transport.sendMessage(msg, msg.getAllRecipients());
   transport.close();
  } catch (Exception mex) {
   mex.printStackTrace();
   return false;
  }
  return true;
 }
 /**
  * <br>
  * 方法说明：主方法，用于测试 <br>
  * 输入参数： <br>
  * 返回类型：
  */
 /*public static void main(String[] args) {
  MailUtils sendmail = new MailUtils();
  sendmail.setHost("smtp.chinatelling.com");
  sendmail.setUserName("b2bservice");
  sendmail.setPassWord("tytelling");
  sendmail.setTo("bjxiaowen@126.com");
  sendmail.setFrom("b2bservice@chinatelling.com");
  sendmail.setSubject("你好，这是测试！");
  sendmail.setContent("你好这是一个带多附件的测试！");
  sendmail.attachfile("E:\\时间比较.txt");
  String encoding = System.getProperty("file.encoding");
  System.out.println(encoding);
  System.out.println(sendmail.sendMail());
 }*/
}