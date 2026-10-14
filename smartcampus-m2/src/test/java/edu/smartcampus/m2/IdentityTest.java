package edu.smartcampus.m2;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

class IdentityTest {
    private static final String TOKEN="synthetic-test-token-with-at-least-32-characters";
    @Test void bearerTokenResolvesConfiguredStudentOnly(){
        var identity=new TokenIdentity(TOKEN,"student1");var request=new MockHttpServletRequest();
        request.addHeader("Authorization","Bearer "+TOKEN);request.addHeader("X-Student-Id","student2");
        assertEquals("student1",identity.resolve(request));
    }
    @Test void wrongBearerAndStudentHeadersAreNotIdentities(){
        var identity=new TokenIdentity(TOKEN,"student1");var request=new MockHttpServletRequest();
        request.addHeader("X-Student-Id","student1");assertNull(identity.resolve(request));
        request.addHeader("Authorization","Bearer incorrect");assertNull(identity.resolve(request));
    }
    @Test void rejectsWeakToken(){assertThrows(IllegalArgumentException.class,()->new TokenIdentity("short","student1"));}
}
