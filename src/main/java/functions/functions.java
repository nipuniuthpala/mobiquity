package functions;

import common.Headers;
import io.restassured.response.Response;
import org.testng.asserts.SoftAssert;

import java.util.List;

import static DataProvider.DataProvider.*;

public class functions{

    static SoftAssert softAssert = new SoftAssert();

    public static boolean isValidEmail(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
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
                System.out.println("Is the above E-mail ID valid? " + isValidEmail(email.get(y)));}
        }
    }
}
