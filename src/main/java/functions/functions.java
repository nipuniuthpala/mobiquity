package functions;

import common.Headers;
import io.restassured.response.Response;
import org.testng.asserts.SoftAssert;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.io.*;



import static DataProvider.DataProvider.*;

public class functions {

    static SoftAssert softAssert = new SoftAssert();

    public static boolean isValidEmailAddress(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }


    private static int hear( BufferedReader in ) throws IOException {
        String line = null;
        int res = 0;

        while ( (line = in.readLine()) != null ) {
            String pfx = line.substring( 0, 3 );
            try {
                res = Integer.parseInt( pfx );
            }
            catch (Exception ex) {
                res = -1;
            }
            if ( line.charAt( 3 ) != '-' ) break;
        }

        return res;
    }

    private static void say( BufferedWriter wr, String text )
            throws IOException {
        wr.write( text + "\r\n" );
        wr.flush();

        return;
    }
    private static ArrayList getMX( String hostName )
            throws NamingException {
        // Perform a DNS lookup for MX records in the domain
        Hashtable env = new Hashtable();
        env.put("java.naming.factory.initial",
                "com.sun.jndi.dns.DnsContextFactory");
        DirContext ictx = new InitialDirContext( env );
        Attributes attrs = ictx.getAttributes
                ( hostName, new String[] { "MX" });
        Attribute attr = attrs.get( "MX" );

        // if we don't have an MX record, try the machine itself
        if (( attr == null ) || ( attr.size() == 0 )) {
            attrs = ictx.getAttributes( hostName, new String[] { "A" });
            attr = attrs.get( "A" );
            if( attr == null )
                throw new NamingException
                        ( "No match for name '" + hostName + "'" );
        }

        ArrayList res = new ArrayList();
        NamingEnumeration en = attr.getAll();

        while ( en.hasMore() ) {
            String mailhost;
            String x = (String) en.next();
            String f[] = x.split( " " );
            //  THE fix *************
            if (f.length == 1)
                mailhost = f[0];
            else if ( f[1].endsWith( "." ) )
                mailhost = f[1].substring( 0, (f[1].length() - 1));
            else
                mailhost = f[1];
            //  THE fix *************
            res.add( mailhost );
        }
        return res;
    }

    public static boolean isAddressValid( String address ) {
        // Find the separator for the domain name
        int pos = address.indexOf( '@' );

        // If the address does not contain an '@', it's not valid
        if ( pos == -1 ) return false;

        // Isolate the domain/machine name and get a list of mail exchangers
        String domain = address.substring( ++pos );
        ArrayList mxList = null;
        try {
            mxList = getMX( domain );
        }
        catch (NamingException ex) {
            return false;
        }


        if ( mxList.size() == 0 ) return false;



        for (int mx = 0; mx < mxList.size();mx++ ) {
            boolean valid = false;
            try {
                int res;
                //
                Socket skt = new Socket( (String) mxList.get(mx), 25 );
                BufferedReader rdr = new BufferedReader
                        ( new InputStreamReader( skt.getInputStream() ) );
                BufferedWriter wtr = new BufferedWriter
                        ( new OutputStreamWriter( skt.getOutputStream() ) );

                res = hear( rdr );
                if ( res != 220 ) throw new Exception( "Invalid header" );
                say( wtr, "EHLO rgagnon.com" );

                res = hear( rdr );
                if ( res != 250 ) throw new Exception( "Not ESMTP" );

                // validate the sender address
                say( wtr, "MAIL FROM: <tim@orbaker.com>" );
                res = hear( rdr );
                if ( res != 250 ) throw new Exception( "Sender rejected" );

                say( wtr, "RCPT TO: <" + address + ">" );
                res = hear( rdr );

                // be polite
                say( wtr, "RSET" ); hear( rdr );
                say( wtr, "QUIT" ); hear( rdr );
                if ( res != 250 )
                    throw new Exception( "Address is not valid!" );

                valid = true;
                rdr.close();
                wtr.close();
                skt.close();
            }
            catch (Exception ex) {
                // Do nothing but try next host
                ex.printStackTrace();
            }
            finally {
                if ( valid ) return true;
            }
        }
        return false;
    }

    public static List<Integer> getAllPosts(){

        Response response_users = Headers.GetHeader(USER_NAME_URL);
        int id=response_users.getBody().jsonPath().get("id[0]");
        Response response = Headers.GetHeader(POSTS_URL+id);
        softAssert.assertTrue(String.valueOf(response.getStatusCode()).equals("200"));
        List<Integer> ids = response.jsonPath().getList("id");
        System.out.println(ids);
        return ids;

    }

    public static void getAllComments() {
        List<Integer> ids=getAllPosts();
        for (int i = 0; i < ids.size(); i++) {
            Response response1 = Headers.GetHeader(COMMENTS_URL + ids.get(i));
            softAssert.assertTrue(String.valueOf(response1.getStatusCode()).equals("200"));
            List<String> email = response1.jsonPath().getList("email");
            for (int y = 0; y < email.size(); y++) {
                System.out.println(email.get(y));
                //check emails in proper format
                System.out.println("Is the above E-mail ID valid? " + isValidEmailAddress(email.get(y)));
                //check emails actually sent or have a proper domain or not
                System.out.println("Is the above emailID is in valid domain?" +isAddressValid(email.get(y)));
            }


        }
    }
}
