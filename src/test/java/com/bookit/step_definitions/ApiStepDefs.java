package com.bookit.step_definitions;

import com.bookit.pages.SelfPage;
import com.bookit.utilities.BookItApiUtil;
import com.bookit.utilities.ConfigurationReader;
import com.bookit.utilities.DBUtils;
import com.bookit.utilities.Environment;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.Assert;

import java.util.Map;

import static io.restassured.RestAssured.*;

public class ApiStepDefs {
    String token;
    Response response;
    String emailGlobal;
    String studentEmail;
    String studentPassword;

    @Given("I logged Bookit api using {string} and {string}")
    public void i_logged_Bookit_api_using_and(String email, String password) {

        token = BookItApiUtil.generateToken(email,password);
        emailGlobal = email;
    }

    @When("I get the current user information from api")
    public void i_get_the_current_user_information_from_api() {
        System.out.println("token = " + token);

        //send a GET request "/api/users/me" endpoint to get current user info

         response = given().accept(ContentType.JSON)
                .and()

                 // BookIt API documentation says: "Authentication:
                 //... Simply put, we expect for the authorization token to be included in all
                 // api requests to the server in a header that looks like the following:
                 //Authorization: Bearer your-token
                .header("Authorization", token)
                .when()
                .get(Environment.BASE_URL + "/api/users/me");
                //no verification is asked in this step
    }

    @Then("status code should be {int}")
    public void status_code_should_be(int statusCode) {
        //verify status code matches with the feature file expected status code
        Assert.assertEquals(statusCode,response.statusCode());

    }

    @Then("the information about current user from api and database should match")
    public void theInformationAboutCurrentUserFromApiAndDatabaseShouldMatch() {
        System.out.println("we will compare database and api in this step");

        //GET INFORMATION FROM DB
        //connection is from hooks and it will be ready (because we put @db above Scenario
        String query = "select firstname,lastname,role from users\n" +
                "where email = '"+emailGlobal+"'";

        Map<String,Object> dbMap = DBUtils.getRowMap(query);
        System.out.println("dbMap = " + dbMap);
        //save db info into variables
        String expectedFirstName = (String) dbMap.get("firstname");
        String expectedLastName = (String) dbMap.get("lastname");
        String expectedRole = (String) dbMap.get("role");

        //GET INFORMATION FROM API
        JsonPath jsonPath = response.jsonPath();
        //save api info into variables
        String actualFirstName = jsonPath.getString("firstName");
        //this is "actual", because we assume DB is correct (EXPECTED) when comparing the values
        // (if we are not inserting something)!
        String actualLastName = jsonPath.getString("lastName");
        String actualRole = jsonPath.getString("role");

        //COMPARE DB VS API     //JUnit assertians
        Assert.assertEquals(expectedFirstName,actualFirstName);
        Assert.assertEquals(expectedLastName,actualLastName);
        Assert.assertEquals(expectedRole,actualRole);

    }

    @Then("UI,API and Database user information must be match")
    public void uiAPIAndDatabaseUserInformationMustBeMatch() {
        //GET INFORMATION FROM DATABASE
        //connection is from hooks and it will be ready
        String query = "select firstname,lastname,role from users\n" +
                "where email = '"+emailGlobal+"'";

        Map<String,Object> dbMap = DBUtils.getRowMap(query);
        System.out.println("dbMap = " + dbMap);
        //save db info into variables
        String expectedFirstName = (String) dbMap.get("firstname");
        String expectedLastName = (String) dbMap.get("lastname");
        String expectedRole = (String) dbMap.get("role");

        //GET INFORMATION FROM API
        //let's use jsonPath. We could use pojo as well
        JsonPath jsonPath = response.jsonPath();
        //save api info into variables
        String actualFirstName = jsonPath.getString("firstName");
        String actualLastName = jsonPath.getString("lastName");
        String actualRole = jsonPath.getString("role");

        //GET INFORMATION FROM UI
        SelfPage selfPage = new SelfPage();
        String actualUIName = selfPage.name.getText();
        String actualUIRole = selfPage.role.getText();

        System.out.println("actualUIName = " + actualUIName);
        System.out.println("actualUIRole = " + actualUIRole);

         //UI vs DB
        String expectedFullName = expectedFirstName+" "+expectedLastName;
        //verify ui fullname vs db fullname
        Assert.assertEquals(expectedFullName,actualUIName);
        Assert.assertEquals(expectedRole,actualUIRole);

        //UI vs API
        //Create a fullname for api

        String actualFullName = actualFirstName+" "+actualLastName;

        Assert.assertEquals(actualFullName,actualUIName);
        Assert.assertEquals(actualRole,actualUIRole);

    }

    /**
     *
     * @param path --> endpoint
     * @param studentInfo --> dynamically getting student info as a Map
     */
    @When("I send POST request to {string} endpoint with following information")
    public void i_send_POST_request_to_endpoint_with_following_information(String path, Map<String,String> studentInfo) {
        //why we prefer to get information as a map from feature file ?
        //bc we have queryParams method that takes map and pass to url as query key&value structure
        System.out.println("studentInfo = " + studentInfo);

        //assign email and password value to these variables so that we can use them later for deleting
        studentEmail = studentInfo.get("email");
        studentPassword = studentInfo.get("password");

        response = given().accept(ContentType.JSON)
//                .contentType(ContentType.JSON)      //we don't need this.
//                because we are not sending any info in body (not sending JSON)
//                we are sending in Query Params (as a Map)
//                BookIt API works with Query Params for POST request!.

                .queryParams(studentInfo)
//                why using queryParams, instead of sending as a body? because document says so!
//                there is no serialization (Java to JSON) here!
//                just converting Java (map) to query parameter (queryParams) as key and value
//                which you can also type manually to the url.
//                If the documentation said body, then we would need body-map serialization, need jackson in pom.xml,
//                and serialize to body.. we need to say contentType(ContentType.JSON).

                .and().header("Authorization",token)
                .log().all()
                .when()
                .post(Environment.BASE_URL + path)
        .then().log().all().extract().response();


    }

    @Then("I delete previously added student")
    public void i_delete_previously_added_student() {
        BookItApiUtil.deleteStudent(studentEmail,studentPassword);
    }


    @Given("I logged Bookit api as {string}")
    public void iLoggedBookitApiAs(String role) {
       token= BookItApiUtil.getTokenByRole(role);
    }
}
