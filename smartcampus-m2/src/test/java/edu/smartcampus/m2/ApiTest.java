package edu.smartcampus.m2;

import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import java.net.*;
import java.net.http.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("demo")
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, properties={"spring.datasource.url=jdbc:h2:mem:apitest;MODE=MySQL;DB_CLOSE_DELAY=-1", "server.address=127.0.0.1"})
class ApiTest {
    @LocalServerPort int port;
    final ObjectMapper json = new ObjectMapper();
    record Client(HttpClient http, String csrf) {}
    HttpClient client() { return HttpClient.newBuilder().cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).build(); }
    HttpResponse<String> call(HttpClient c, String method, String path, String body, String csrf) throws Exception {
        var b = HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+path)).header("Content-Type","application/json");
        if (csrf != null) b.header("X-CSRF-Token", csrf);
        b.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        return c.send(b.build(),HttpResponse.BodyHandlers.ofString());
    }
    Client login(String user) throws Exception {
        var c=client(); var r=call(c,"POST","/api/auth/login","{\"username\":\""+user+"\",\"password\":\"demo-password\"}",null);
        assertEquals(200,r.statusCode(),r.body());
        return new Client(c,json.readTree(r.body()).path("data").path("csrfToken").asText());
    }
    @Test void requiresLogin() throws Exception { assertEquals(401,call(client(),"GET","/api/m2/me",null,null).statusCode()); }
    @Test void badCredentialsAreRejected() throws Exception { assertEquals(401,call(client(),"POST","/api/auth/login","{\"username\":\"student1\",\"password\":\"wrong\"}",null).statusCode()); }
    @Test void usesServerSideCompletedCourses() throws Exception {
        var c=login("student1"); var r=call(c.http(),"POST","/api/m2/recommendations","{\"interests\":\"machine learning\",\"goals\":\"\",\"topK\":3}",c.csrf());
        assertEquals(200,r.statusCode(),r.body()); var items=json.readTree(r.body()).path("data").path("items");
        assertFalse(items.isEmpty()); assertEquals("CS201",items.get(0).path("course").path("id").asText());
        for (var item:items) { assertNotEquals("CS301", item.path("course").path("id").asText()); assertNotEquals("CS101", item.path("course").path("id").asText()); }
    }
    @Test void csrfIsRequiredForSessionMutation() throws Exception {
        var c=login("student1"); assertEquals(403,call(c.http(),"POST","/api/m2/recommendations","{\"interests\":\"java\"}",null).statusCode());
    }
    @Test void rejectsForgedCompletedFields() throws Exception {
        var c=login("student1"); assertEquals(400,call(c.http(),"POST","/api/m2/recommendations","{\"interests\":\"machine learning\",\"completed\":[\"CS201\"]}",c.csrf()).statusCode());
    }
    @Test void rejectsStudentIdInBody() throws Exception {
        var c=login("student1"); assertEquals(400,call(c.http(),"POST","/api/m2/recommendations","{\"interests\":\"java\",\"studentId\":\"student2\"}",c.csrf()).statusCode());
    }
    @Test void historyIsPrivate() throws Exception {
        var a=login("student1"); var b=login("student2"); String unique="java privacytesttoken";
        assertEquals(200,call(a.http(),"POST","/api/m2/recommendations","{\"interests\":\""+unique+"\"}",a.csrf()).statusCode());
        var mine=call(a.http(),"GET","/api/m2/history",null,null); var theirs=call(b.http(),"GET","/api/m2/history?studentId=student1",null,null);
        assertTrue(mine.body().contains(unique)); assertFalse(theirs.body().contains(unique));
        assertEquals("no-store",mine.headers().firstValue("Cache-Control").orElse(""));
    }
    @Test void logoutInvalidatesSession() throws Exception {
        var c=login("student1"); assertEquals(200,call(c.http(),"POST","/api/auth/logout","{}",c.csrf()).statusCode());
        assertEquals(401,call(c.http(),"GET","/api/m2/me",null,null).statusCode());
    }
    @Test void validatesBlankOversizeAndTopK() throws Exception {
        var c=login("student1"); for (String s:List.of("{\"interests\":\" \"}","{\"interests\":\"java\",\"topK\":4}","{\"interests\":\""+"x".repeat(501)+"\"}"))
            assertEquals(400,call(c.http(),"POST","/api/m2/recommendations",s,c.csrf()).statusCode());
    }
    @Test void unknownWordsAreSuccessfulEmptyResult() throws Exception {
        var c=login("student1"); var r=call(c.http(),"POST","/api/m2/recommendations","{\"interests\":\"astrophysics\"}",c.csrf());
        assertEquals(200,r.statusCode()); assertTrue(json.readTree(r.body()).path("data").path("items").isEmpty());
    }
    @Test void profileAndCatalogAreAvailable() throws Exception {
        var c=login("student1"); var me=call(c.http(),"GET","/api/m2/me",null,null); assertTrue(me.body().contains("CS101"));
        var list=call(c.http(),"GET","/api/m2/courses",null,null); assertEquals(20,json.readTree(list.body()).path("data").size());
    }
    @Test void pageIsServed() throws Exception { assertTrue(call(client(),"GET","/",null,null).body().contains("SmartCampus")); }
    @Test void matrixPathsCannotBypassLogin() throws Exception {
        for(String path:List.of("/api/m2;/me","/api/m2;/courses")) assertEquals(401,call(client(),"GET",path,null,null).statusCode());
    }
    @Test void matrixLogoutCannotBypassCsrf() throws Exception {
        var c=login("student1");assertEquals(403,call(c.http(),"POST","/api/auth;/logout","{}",null).statusCode());
        assertEquals(200,call(c.http(),"GET","/api/m2/me",null,null).statusCode());
    }
    @Test void missingResourceReturns404()throws Exception {assertEquals(404,call(client(),"GET","/missing-page",null,null).statusCode());}
}
