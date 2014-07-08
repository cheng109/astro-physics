package ImageDownload; 

import java.io.*;
import java.net.*;
import java.util.regex.*;
import java.sql.*;
import java.util.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
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
		//	stat.executeUpdate("DROP TABLE URLS");
			System.out.println("DROP TABLE " + nameDB + ".URLS");
			stat.executeUpdate("DROP TABLE " + nameDB + ".URLS");
			stat.executeUpdate("DROP TABLE ImageURLS");
		}
		catch (Exception e) {
		}
		// Create the table
        	stat.executeUpdate("CREATE TABLE " + nameDB + ".URLS (urlid INT, url VARCHAR(512), description VARCHAR(512) character set utf8 ) ");
        	stat.executeUpdate("CREATE TABLE " + nameDB + ".ImageURLS (urlid INT,url VARCHAR(512))");
	}

	public boolean urlInDB(String urlFound, String table) throws SQLException, IOException {
        Statement stat = connection.createStatement();
		ResultSet result = stat.executeQuery( "SELECT * FROM " + nameDB +"." + table +" WHERE url LIKE '"+urlFound+"'");
		if (result.next()) {
			return true;
		}
//	       System.out.println("[URL to " + table + "]: "+urlFound);
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

	public String makeAbsoluteURL(String url, String parentURL, boolean flag) {
		if (url.length() >=3 && url.indexOf("://")>0) {
			return url;
		}
		if (flag == false && url.length() > 0 ) {
		
			String newParentURL = new String(parentURL); 
			StringBuffer TempUrl = new StringBuffer(url); 

			if(url.charAt(0) != '/' && parentURL.charAt(parentURL.length()-1)!='/' && parentURL.charAt(0)!= '.')  {
				StringBuilder builder = new StringBuilder();
				 builder.append(newParentURL).append("/");
				 newParentURL =builder.toString(); 
 			}
			if( url.length()>1 && url.charAt(0) == '/' &&  parentURL.charAt(parentURL.length()-1)=='/')  {
				newParentURL = parentURL.substring(0,newParentURL.length()-1); 
			}
			
			// For relative URL; 
			if(url.length()>=4 && url.substring(0,3).equals("../") && parentURL.charAt(parentURL.length()-1)=='/' ) {
				int pos = newParentURL.substring(0, newParentURL.length()-1).lastIndexOf("/"); 
				newParentURL = newParentURL.substring(0,pos+1);
				TempUrl.delete(0,3); 
			}	
			if(url.length()>=4 && url.substring(0,3).equals("../") && parentURL.charAt(parentURL.length()-1)!='/' ) {
				TempUrl.delete(0,3); 
			}
			
			TempUrl.insert(0,newParentURL);
			return TempUrl.toString().replaceAll("\\s","");
		} 
		
		if (flag == true && url.length() > 0 ) {
			
			String newParentURL_1 = new String(parentURL.replaceAll("://", "...")); 
			StringBuffer TempUrl_1 = new StringBuffer(url); 
			int pos1 = newParentURL_1.indexOf("/"); 
			newParentURL_1 = parentURL.substring(0, pos1); 
			TempUrl_1.insert(0,newParentURL_1);
			return TempUrl_1.toString().replaceAll("\\s","");
		} 
		return url;
	}

	
	public void fetchDescription(String urlScanned, int NextURLIDScanned) {
		urlScanned.length(); 
		
		try {
			URL url = new URL(urlScanned);
			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			huc.setConnectTimeout(5 * 1000);
			System.out.println("[urlscanned]="+urlScanned); 
			System.out.println("[url.path]="+url.getPath());
    			InputStreamReader in = 
       				new InputStreamReader(url.openStream());
    			StringBuilder input = new StringBuilder();
    			int ch;
			while ((ch = in.read()) != -1) {
         			input.append((char) ch);
			}

			String description = new String(); 
			String patternString = "<title>([^<]*)</title>"; 
   	
			Pattern pattern = 			
    	    			Pattern.compile(patternString, 
    	     		Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(input);
    			StringBuilder DescribeBuilder = new StringBuilder();
 			while (matcher.find()) {
				description = DescribeBuilder.append(matcher.group(1)).toString();
			}
			if (description.length()>100){
				description = description.substring(0, 100); 
			}
			System.out.println("[Descripton]: "+description); 
			insertDescription(description, NextURLIDScanned);
			System.out.println(NextURLIDScanned+"/"+NextURLID+"*************************\n"); 
		
		}
      		catch (Exception e)
      		{
       			e.printStackTrace();
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
			image = ImageIO.read(url);
			File f = new File( this.path +   "[" + (ImageurlID-1) +"]-" +name  + ".png"); 
            ImageIO.write(image, "png", f);
            downloadSize = downloadSize + f.length(); 
		}catch(IOException e){
            e.printStackTrace();
        }
		
		
		
	}
	
   	public void fetchURL(String urlScanned) {
		try {
			URL url = new URL(urlScanned);
			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			huc.setConnectTimeout(5 * 1000);
//			System.out.println("[urlscanned]="+urlScanned ); 
//			System.out.println("[url.path]="+url.getPath());
    			// open reader for URL
    			InputStreamReader in = 
       				new InputStreamReader(url.openStream());
    			// read contents into string builder
    			StringBuilder input = new StringBuilder();
    			int ch;
			while ((ch = in.read()) != -1) {
         			input.append((char) ch);
			}
			new String(); 
  			String patternString =  "(<a\\s+href\\s*=\\s*\"([^\"]*)\"[^>]*\\s*>[^<]*</a>)[^<]*</[^>]*>";    	
 
  			Pattern pattern = 			
    				Pattern.compile(patternString, 
    				Pattern.CASE_INSENSITIVE);
    			Matcher matcher = pattern.matcher(input);
			while (matcher.find() ) {
				
				String urlFound = matcher.group(2).replaceAll("\"","");
				int len = urlFound.length(); 
				if(len>=4)		{
					String EndSubstring = urlFound.substring(len-4,len); 
					String BeginSubstring = urlFound.substring(0,4); 
					if (EndSubstring.equals(".pdf") 
						|| EndSubstring.equals(".ppt") 
						|| EndSubstring.equals(".doc")
						|| BeginSubstring.equals("mail")
						|| BeginSubstring.equals("ftp:"))
						continue; 
				}
				urlFound = makeAbsoluteURL(urlFound, urlScanned, true).replaceAll("\\s",""); 
				if (!urlInDB(urlFound, "URLS")) {
					NextURLID ++;
					insertURLInDB(urlFound);
				}				
 			}
			
			String remain = matcher.replaceAll("###############"); 
			StringBuilder input_1 = new StringBuilder();
			input_1.append(remain); 
			String patternString_1 =  "<a\\s+href\\s*=\\s*\"([^\"]*\"|[^\\s>]*)\\s*>";    		  // original regex
			int pos2= patternString_1.indexOf(" ");
			if(pos2!=-1) {
				patternString_1 = patternString_1.substring(0, pos2); 
			}
			
			Pattern pattern_1 = 			
    				Pattern.compile(patternString_1, 
    				Pattern.CASE_INSENSITIVE);
 
    		Matcher matcher_1 = pattern_1.matcher(input_1);
			while (matcher_1.find() ) {
				
				String urlFound_1 = matcher_1.group(1).replaceAll("\"",""); 
				int len = urlFound_1.length(); 
				if(len>=4)		{
					String EndSubstring_1 = urlFound_1.substring(len-4,len); 
					String BeginSubstring_1 = urlFound_1.substring(0,4); 
					if (EndSubstring_1.equals(".pdf") 
						|| EndSubstring_1.equals(".ppt") 
						|| EndSubstring_1.equals(".doc")
						|| BeginSubstring_1.equals("mail")
						|| BeginSubstring_1.equals("ftp:"))
						continue; 
				}
				urlFound_1 = makeAbsoluteURL(urlFound_1, urlScanned, false).replaceAll("\\s",""); 
				if (!urlInDB(urlFound_1, "URLS")) {
					NextURLID ++;
					insertURLInDB(urlFound_1);
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
			urlFoundImage = makeAbsoluteURL(urlFoundImage, urlScanned, true).replaceAll("\\s",""); 
			test = test +1; 
				 if (!urlInDB(urlFoundImage, "ImageURLS")) {
				//	ImageNextURLID ++;
//					System.out.println("~~~ImageFound = "+ urlFoundImage);
//					System.out.println("~~~" +ImageurlID   + "\n" +  test  + "\n"); 
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

   	






