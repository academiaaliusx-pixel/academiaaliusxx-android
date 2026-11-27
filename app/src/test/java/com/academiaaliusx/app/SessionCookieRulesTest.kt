package com.academiaaliusx.app

import java.net.HttpCookie
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionCookieRulesTest {
    @Test fun roundTripsCookieAttributesForAllowedHost() {
        val cookie=HttpCookie("session","secret-value").apply{domain=".academiaaliusx.com";path="/api";secure=true;isHttpOnly=true;maxAge=-1;version=1}
        val decoded=SessionCookieRules.decode(SessionCookieRules.encode(listOf(cookie),"academiaaliusx.com"),"academiaaliusx.com")
        assertEquals(1,decoded.size);assertEquals("session",decoded.single().name);assertEquals("secret-value",decoded.single().value)
        assertEquals("academiaaliusx.com",decoded.single().domain);assertEquals("/api",decoded.single().path)
        assertTrue(decoded.single().secure);assertTrue(decoded.single().isHttpOnly)
    }
    @Test fun rejectsCookiesForUnrelatedDomains() {
        val cookie=HttpCookie("session","value").apply{domain="evil.example"}
        assertEquals("[]",SessionCookieRules.encode(listOf(cookie),"academiaaliusx.com"))
    }
    @Test fun skipsInvalidCookiesAndBoundsDecodedInput() {
        assertTrue(SessionCookieRules.decode("""[{"name":"","value":"v","domain":"academiaaliusx.com","path":"/"}]""","academiaaliusx.com").isEmpty())
        assertTrue(SessionCookieRules.decode(" ".repeat(SessionCookieRules.MAX_COOKIE_JSON_LENGTH+1),"academiaaliusx.com").isEmpty())
        assertTrue(SessionCookieRules.decode("""[{"name":"a","value":"v","domain":"attacker.com","path":"/"}]""","academiaaliusx.com").isEmpty())
    }
    @Test fun excludesExpiredCookies() {
        val live=HttpCookie("a","1").apply{domain="academiaaliusx.com";path="/"}
        val expired=HttpCookie("b","2").apply{domain="academiaaliusx.com";path="/";maxAge=0}
        val result=SessionCookieRules.decode(SessionCookieRules.encode(listOf(live,expired),"academiaaliusx.com"),"academiaaliusx.com")
        assertEquals(listOf("a"),result.map{it.name})
    }
}
