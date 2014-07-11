package ImageDownload; 

import java.io.*;
import java.net.*;
import java.sql.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler
{
    public Connection connection;
    public int urlID;
    public int ImageurlID; 
    public int NextURLID = 0;
    public int ImageNextURLID = 0;
    public int NextURLIDScanned; 
    public static long downloadSize = 0; 
    // public String nameDB; 
    public String jdbcURL; 
    public int jobID; 
    
    public confCrawler config ; 
    Crawler(int jobID, String confName) {
	urlID = 0;
	this.jobID = jobID; 
	config = new confCrawler(confName); 
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
	this.connection = DriverManager.getConnection(this.config.jdbcURL[jobID], this.config.jdbcUSERNAME, this.config.jdbcPASSWORD);
    }

    public void createDB() throws SQLException, IOException {
	openConnection();
	Statement stat = connection.createStatement();

	try {
	    stat.executeUpdate("CREATE DATABASE " + config.nameDB[jobID] ); 
	    System.out.println("Create a new database: '" + config.nameDB[jobID] + "'"); 
	} catch (Exception e) {
	    System.out.println("Database '" + config.nameDB[jobID] + "' already exists"); 
	} 
	try {
	    stat.executeUpdate("DROP TABLE " + config.nameDB[jobID] + ".URLS");
	    stat.executeUpdate("DROP TABLE ImageURLS");
	}
	catch (Exception e) {
	}
	stat.executeUpdate("CREATE TABLE " + config.nameDB[jobID] + 
			   ".URLS (urlid INT, url VARCHAR(512), description VARCHAR(512) character set utf8 ) ");
	stat.executeUpdate("CREATE TABLE " + config.nameDB[jobID] + 
			   ".ImageURLS (urlid INT,url VARCHAR(512), imageDescription VARCHAR(512) character set utf8)");
    }

    public boolean urlInDB(String urlFound, String table) throws SQLException, IOException {
	Statement stat = connection.createStatement();
	ResultSet result = stat.executeQuery( "SELECT * FROM " + config.nameDB[jobID] +"." + table +
					      " WHERE url LIKE '"+urlFound+"'");
	if (result.next()) {
	    return true;
	}
	return false;
    }
	
    public void insertURLInDB( String url) throws SQLException, IOException {
        Statement stat = connection.createStatement();
	String query = "INSERT INTO " + config.nameDB[jobID] + "." + "URLS(urlid, url) VALUES ('"+urlID+"','"+url+"')";
	//System.out.println("Executing "+query);
	stat.executeUpdate( query );
	urlID++;
    }
	
    public void insertImageURLInDB( String url, String imageDescription) throws SQLException, IOException {
        Statement stat = connection.createStatement();
	String query = "INSERT INTO " + config.nameDB[jobID] + "." 
	    + "ImageURLS(urlid, url, imageDescription) VALUES ('"+ImageurlID+"','"+url +"','" +imageDescription+"')";
	stat.executeUpdate( query );
	ImageurlID++;
    }
	
    public void insertDescription( String description, int NextURLIDScanned) throws SQLException, IOException {
     	PreparedStatement pstmt 
	    = connection.prepareStatement("update " + config.nameDB[jobID] + "." +  "URLs set description= ? where urlid = ?");
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
	    String query = "select imageDescription from " + config.nameDB[jobID] + "." + "imageURLS where (urlid= " + (ImageurlID-1) + "); "; 
	    rs = stat.executeQuery( query );
	} catch (SQLException e1) {
	    e1.printStackTrace();
	}
	if (rs.next()) {
	    name = rs.getString("imageDescription").replaceAll("[^A-Za-z0-9]", " ").replaceAll(" +","_"); 
	    if (name.length() < 2) {
		name = new String("unknown_name");
	    }
	}
	String imgType = urlstring.substring(urlstring.length()-3, urlstring.length());
	try  {
	    URL url = new URL(urlstring);
	    HttpURLConnection con=(HttpURLConnection) url.openConnection();
	    con.setConnectTimeout(5000); 
	    con.setReadTimeout(10000); 
	    image = ImageIO.read(con.getInputStream());

	    File f = new File( assignDirectory(urlstring, imgType) +  name + "_" +ImageurlID + "."+imgType); 
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

    public String assignDirectory(String imageURL, String imgType) {
	String imagePath = null;
	String otherPath = config.fullPath[jobID] +"/" + "other/";
	if(imgType.equals("png") || imgType.equals("jpg") || imgType.equals("png") || imgType.equals("gif")) {
	    imagePath = config.fullPath[jobID] +"/" + imgType +"/";
	} else {
	    imagePath = otherPath;
	}
	File dir1 = new File(otherPath);
	File dir2 = new File(imagePath); 
	if(!dir1.exists()) {
	    dir1.mkdir();
	}
	if(!dir2.exists()) {
	    dir2.mkdir();
	}
	return imagePath;
    }

}

   	






