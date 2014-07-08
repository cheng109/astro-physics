package ImageDownload; 

import java.io.*;
import java.net.*;
import java.util.regex.*;
import java.sql.*;
import java.util.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler
{
    public Connection connection;
    public int urlID;
    public int ImageurlID; 
    public Properties props;
    public String path; 
    public int NextURLID = 0;
    public int ImageNextURLID = 0;
    public int NextURLIDScanned; 
    public static long downloadSize = 0; 
    public String nameDB; 
    public String jdbcURL; 
    Crawler() {
	urlID = 0;
    }

    public void readProperties() throws IOException {
	props = new Properties();
	FileInputStream in = new FileInputStream("database.properties");
	props.load(in);
	in.close();
    }

    public void openConnection() throws SQLException, IOException
    {
	try {
	    Class.forName("com.mysql.jdbc.Driver").newInstance();
	} catch (InstantiationException e) {
	    e.printStackTrace();
	} catch (IllegalAccessException e) {
	    e.printStackTrace();
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	} 
	String drivers = props.getProperty("jdbc.drivers");
	if (drivers != null) System.setProperty("jdbc.drivers", drivers);
	String username = props.getProperty("jdbc.username");
	String password = props.getProperty("jdbc.password");
	connection = DriverManager.getConnection( jdbcURL, username, password);
    }

    public void createDB() throws SQLException, IOException {
	openConnection();
	Statement stat = connection.createStatement();
	// Delete the table first if any
	try {
	    //System.out.println("DROP TABLE " + nameDB + ".URLS");
	    stat.executeUpdate("DROP TABLE " + nameDB + ".URLS");
	    stat.executeUpdate("DROP TABLE ImageURLS");
	}
	catch (Exception e) {
	}
	// Create the table
	stat.executeUpdate("CREATE TABLE " + nameDB + 
			   ".URLS (urlid INT, url VARCHAR(512), description VARCHAR(512) character set utf8 ) ");
	stat.executeUpdate("CREATE TABLE " + nameDB + ".ImageURLS (urlid INT,url VARCHAR(512))");
    }

    public boolean urlInDB(String urlFound, String table) throws SQLException, IOException {
	Statement stat = connection.createStatement();
	ResultSet result = stat.executeQuery( "SELECT * FROM " + nameDB +"." + table +
					      " WHERE url LIKE '"+urlFound+"'");
	if (result.next()) {
	    return true;
	}
	return false;
    }
	
    public void insertURLInDB( String url) throws SQLException, IOException {
        Statement stat = connection.createStatement();
	String query = "INSERT INTO " + nameDB + "." + "URLS(urlid, url) VALUES ('"+urlID+"','"+url+"')";
	//System.out.println("Executing "+query);
	stat.executeUpdate( query );
	urlID++;
    }
	
    public void insertImageURLInDB( String url) throws SQLException, IOException {
        Statement stat = connection.createStatement();
	String query = "INSERT INTO " + nameDB + "." + "ImageURLS(urlid, url) VALUES ('"+ImageurlID+"','"+url+"')";
	//System.out.println("Executing "+query);
	stat.executeUpdate( query );
	ImageurlID++;
    }
	
    public void insertDescription( String description, int NextURLIDScanned) throws SQLException, IOException {
     	PreparedStatement pstmt 
	    = connection.prepareStatement("update " + nameDB + "." +  "URLs set description= ? where urlid = ?");
     	pstmt.setString(1,description);
     	pstmt.setInt(2, NextURLIDScanned); 
     	pstmt.executeUpdate(); 
    }	

    public String makeAbsoluteURL(String url, String parentURL) {
	if (url.length() >=3 && url.indexOf("://")>0) {
	    return url;
	}
	if (url.length() > 0 ) {
	    String newParentURL = new String(parentURL); 
	    StringBuffer TempUrl = new StringBuffer(url); 
	    // For path like:    < http://parent/path > < path/to/myfile/ >    
	    if(url.charAt(0) != '/' && parentURL.charAt(parentURL.length()-1)!='/' && parentURL.charAt(0)!= '.')  {
		StringBuilder builder = new StringBuilder();
		builder.append(newParentURL).append("/");
		newParentURL =builder.toString(); 
	    }
	    // For path like:   < http://parent/path/ > < /path/to/myfile/ >
	    if( url.length()>1 && url.charAt(0) == '/' &&  parentURL.charAt(parentURL.length()-1)=='/')  {
		newParentURL = parentURL.substring(0,newParentURL.length()-1); 
	    }
	    
	    // For path like:   < http://parent/path/ > < ../path/to/myfile/ >
	    if(url.length()>=4 && url.substring(0,3).equals("../") && parentURL.charAt(parentURL.length()-1)=='/' ) {
		int pos = newParentURL.substring(0, newParentURL.length()-1).lastIndexOf("/"); 
		newParentURL = newParentURL.substring(0,pos+1);
		TempUrl.delete(0,3); 
	    }	
	   
	    // For path like:   < http://parent/path > < ../path/to/myfile >
	    if(url.length()>=4 && url.substring(0,3).equals("../") && parentURL.charAt(parentURL.length()-1)!='/' ) {
		TempUrl.delete(0,3); 
	    }
			
	    TempUrl.insert(0,newParentURL);
	    return TempUrl.toString().replaceAll("\\s","");
	} 
	return url;
    }

	
    public void fetchDescription(String urlScanned, int NextURLIDScanned) throws Exception {
	urlScanned.length(); 
	try {
	    URL url = new URL(urlScanned);
	    HttpURLConnection huc = (HttpURLConnection) url.openConnection();
	    HttpURLConnection.setFollowRedirects(false);
	    huc.setConnectTimeout(5000);
	    huc.setReadTimeout(5000);
	    //huc.setRequestProperty("Content-type", "text/xml; charset=utf-8");
	    System.out.println("[urlscanned]='"+urlScanned + "'"); 
	    //System.out.println("[url.path]='"+url.getPath() + "'");
	    if(huc.getInputStream() ==null) {
		return; 
	    }	
	    InputStreamReader in = new InputStreamReader(huc.getInputStream());
	    StringBuilder input = new StringBuilder();
	    int ch;
	    while ((ch = in.read()) != -1) {
		input.append((char) ch);
	    }
	    //System.out.println(input);
	    String description = new String(); 
	    String patternString = "<title>([^<]*)</title>"; 
	    Pattern pattern = 			
		Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
	    Matcher matcher = pattern.matcher(input);
	    StringBuilder DescribeBuilder = new StringBuilder();
	    while (matcher.find()) {
		description = DescribeBuilder.append(matcher.group(1)).toString();
	    }
	    if (description.length()>100){
		description = description.substring(0, 100); 
	    }
	    System.out.println("[Descripton]: '"+description+"'"); 
	    insertDescription(description, NextURLIDScanned);
	    System.out.println(NextURLIDScanned+"/"+NextURLID+"\n*************************\n"); 
	} catch (Exception ste){
	    ste.printStackTrace();
	} 
	
    }

    public void imageDownload(String urlstring) throws SQLException {
	BufferedImage image = null; 
	String name =new String("no_name"); 
	ResultSet rs = null; 
	try {
	    Statement stat = connection.createStatement();
	    String query = "select description from " + nameDB + "." + "URLS where (urlid= " + NextURLIDScanned + "); "; 
	    rs = stat.executeQuery( query );
	} catch (SQLException e1) {
	    e1.printStackTrace();
	}
	if (rs.next()) {
	    name = rs.getString("description"); 
	}
		
	try  {
	    URL url = new URL(urlstring);
	    HttpURLConnection con=(HttpURLConnection) url.openConnection();
	    //con.addRequestProperty("User-Agent", "Mozilla/4.76"); 
	    con.setConnectTimeout(10000); 
	    con.setReadTimeout(10000); 
	    image = ImageIO.read(con.getInputStream());
	    File f = new File( this.path +   "[" + (ImageurlID-1) +"]-" +name  + ".png"); 
            
	    if (image!=null) {
		ImageIO.write(image, "png", f);
		System.out.println("Successful Downloaded: '" +url + "'");
		downloadSize = downloadSize + f.length(); 
	    }else {
		System.out.println("No Images: '" +url + "'");
	    }
	}catch(IOException e){
            e.printStackTrace();
        }
				
    }
	
    public void fetchURL(String urlScanned) {
	try {
	    Document doc = Jsoup.connect(urlScanned).get();
	    Elements links = doc.select("a");
	    for (Element link:links) {
		String urlFound = link.absUrl("href"); 
		System.out.println(urlFound2); 
		if (!urlInDB(urlFound, "URLS")) {
		    NextURLID ++;
		    insertURLInDB(urlFound);
		}	
	    }
	}
      	 catch (Exception e)
	     {
    		e.printStackTrace();
	     }
    }
	
   	
    public void fetchImageURL(String urlScanned) {
	try {
	    URL url = new URL(urlScanned);
	    HttpURLConnection huc = (HttpURLConnection) url.openConnection();
	    huc.setConnectTimeout(5 * 1000);
	    huc.setReadTimeout(5 * 1000);
	    InputStreamReader in = 
		new InputStreamReader(url.openStream());
	    StringBuilder input = new StringBuilder();
	    int ch;
	    while ((ch = in.read()) != -1) {
		input.append((char) ch);
	    }
	    String patternImageString = "<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>"; 
	    Pattern patternImage = 			
		Pattern.compile(patternImageString, 
    				Pattern.CASE_INSENSITIVE);
	    Matcher matcherImage = patternImage.matcher(input);
	    int test = 0 ; 
	    while (matcherImage.find() ) {
		String urlFoundImage = matcherImage.group(1).replaceAll("\"","");	
		urlFoundImage = makeAbsoluteURL(urlFoundImage, urlScanned).replaceAll("\\s",""); 
		test = test +1; 
		if (!urlInDB(urlFoundImage, "ImageURLS")) {
		    insertImageURLInDB(urlFoundImage); 
		    imageDownload(urlFoundImage); 
		}				
	    }
	}
    	catch (Exception e)
	    {
    		e.printStackTrace();
	    }
    }
}

   	






