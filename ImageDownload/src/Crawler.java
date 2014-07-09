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
	    stat.executeUpdate("DROP TABLE " + nameDB + ".URLS");
	    stat.executeUpdate("DROP TABLE ImageURLS");
	}
	catch (Exception e) {
	}
	stat.executeUpdate("CREATE TABLE " + nameDB + 
			   ".URLS (urlid INT, url VARCHAR(512), description VARCHAR(512) character set utf8 ) ");
	stat.executeUpdate("CREATE TABLE " + nameDB + 
			   ".ImageURLS (urlid INT,url VARCHAR(512), imageDescription VARCHAR(512) character set utf8)");
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
	
    public void insertImageURLInDB( String url, String imageDescription) throws SQLException, IOException {
        Statement stat = connection.createStatement();
	String query = "INSERT INTO " + nameDB + "." 
	    + "ImageURLS(urlid, url, imageDescription) VALUES ('"+ImageurlID+"','"+url +"','" +imageDescription+"')";
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

    public void imageDownload(String urlstring) throws SQLException {
	BufferedImage image = null; 
	String name ="no name" ;
	ResultSet rs = null; 
	try {
	    Statement stat = connection.createStatement();
	    String query = "select imageDescription from " + nameDB + "." + "imageURLS where (urlid= " + (ImageurlID-1) + "); "; 
	    rs = stat.executeQuery( query );
	} catch (SQLException e1) {
	    e1.printStackTrace();
	}
	if (rs.next()) {
	    name = rs.getString("imageDescription"); 
	    if (name.length() < 2) {
		name = new String("Unknown_name_");
	    }
	}
		
	try  {
	    URL url = new URL(urlstring);
	    HttpURLConnection con=(HttpURLConnection) url.openConnection();
	    con.setConnectTimeout(5000); 
	    con.setReadTimeout(10000); 
	    image = ImageIO.read(con.getInputStream());
	    File f = new File( assignDirectory(urlstring) +  name + "_" +ImageurlID); 
	    if (image!=null) {
		ImageIO.write(image, "png", f);
		//System.out.println("Successful Downloaded: '" +url + "'");
		downloadSize = downloadSize + f.length(); 
	    }else {
		//System.out.println("No Images: '" +url + "'");
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
		if (!urlInDB(urlFound, "URLS")) {
		    NextURLID ++;
		    insertURLInDB(urlFound);
		}	
	    }
	}catch (Exception e){
	    e.printStackTrace();
	}
    }
	   	
    public void fetchImageURL(String urlScanned) {
	try {
	    Document doc = Jsoup.connect(urlScanned).get();
	    Elements links = doc.select("img");
	    String description = doc.title(); 
	    insertDescription(description, NextURLIDScanned);		   
	    for (Element link:links) {
		String urlFoundImage = link.absUrl("src");
		if (!urlInDB(urlFoundImage, "ImageURLS")) {
		    insertImageURLInDB(urlFoundImage,link.attr("alt")); 
 		    imageDownload(urlFoundImage); 
		}	
	    }
	}catch (Exception e){
	    e.printStackTrace();
	}
    }

    public String assignDirectory(String imageURL) {
	String imgType = imageURL.substring(imageURL.length()-3, imageURL.length());
	if(imgType=="png" || imgType=="jpg" || imgType=="png") {
	    String imagePath = this.path + imgType +"/";
	} else {
	    String imagePath = this.path +"other/"+ imgType +"/";
	}
	File dir = new File(imagePath); 
	if(!dir.exists()) {
	    dir.mkdir();
	}
	return imagePath;
    }

}

   	






