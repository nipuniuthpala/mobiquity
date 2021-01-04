package tests;

import common.Headers;
import static DataProvider.DataProvider.*;
import functions.functions;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.List;



public class SearchUserAndValidateEmailTest {


    SoftAssert softAssert = new SoftAssert();


    @Test(priority = 1, description = "GetUser", alwaysRun = true)
    public void getUser() {

        Response response = Headers.GetHeader(USER_URL);


        List<String> jsonResponse = response.jsonPath().getList("username");
        List<String> jsonResponse1 = response.jsonPath().getList("id");
        List<String> jsonResponse2 = response.jsonPath().getList("name");
        System.out.println(jsonResponse);
        System.out.println(jsonResponse1);
        System.out.println(jsonResponse2);

        Response response1 = Headers.GetHeader(USER_NAME_URL);
        softAssert.assertTrue(String.valueOf(response1.getStatusCode()).equals("200"));
        softAssert.assertEquals(response1.getBody().jsonPath().get("username[0]"), "Delphine", "success message not displayed");
        softAssert.assertEquals(response1.getBody().jsonPath().get("name[0]"), "Glenna Reichert", "success message not displayed");
        softAssert.assertEquals(response1.getBody().jsonPath().get("id[0]"), 9, "success message not displayed");
        softAssert.assertAll();


    }


    @Test(priority = 2, description = "getPostsAndCommentsForTheUserAndValidateEmails", alwaysRun = true)
    public void getPostsAndCommentsEmails() {
        functions.getAllPosts();
        functions.getAllComments();

    }
}
