package es.um.poa.utils;

import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class AgentLoggingHTMLFormatter extends java.util.logging.Formatter {
	@Override
	public String format(LogRecord record) {
		System.out.println("AgentLoggingHTMLFormatter");
		Object[] params = record.getParameters();
		return ("<tr><td><font color=\""+params[2]+"\">" + (new Date(record.getMillis())).toString() + "</font></td>"+
				"<td><font color=\""+params[2]+"\">"+params[0]+"</font></td>"+
				"<td><font color=\""+params[2]+"\">"+params[1]+"</font></td>"+
		"<td><font color=\""+params[2]+"\">" + record.getMessage() + "</font></td></tr>\n");
	}

	public String getHead(Handler h) {
		return ("<html>\n "+
	"<head>\n" + 
	"  <title>Bootstrap Example</title>\n" + 
	"  <meta charset=\"utf-8\">\n" + 
	"  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" + 
	"  <link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.4.0/css/bootstrap.min.css\">\n" + 
	"  <script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js\"></script>\n" + 
	"  <script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.4.0/js/bootstrap.min.js\"></script>\n" + 
	"</head>"
				+" <body>\n" + "<table class=\"table table-hover\">\n<tr>"+
				"<td>Time</td>"+
				"<td>Agent</td>"+
				"<td>Behaviour</td>"+
				"<td>Log Message</td></tr>\n");
	}

	public String getTail(Handler h) {
		return ("</table>\n</body>\n</html>");
	}
}
