package com.github.terrakok.navigation3.browser

import kotlin.test.*

class BrowserHistoryFragmentTest {
    
    @Test
    fun checkNameParser() {
        assertNull(getBrowserHistoryFragmentName(""))

        //Simple fragment
        assertEquals("name", getBrowserHistoryFragmentName("#name"))

        //Fragment with empty query
        assertEquals("name", getBrowserHistoryFragmentName("#name?"))

        //Fragment with query parameters
        assertEquals("name", getBrowserHistoryFragmentName("#name?param=value"))
        assertEquals("name", getBrowserHistoryFragmentName("#name?p1=123&p2=456"))

        //Fragment with encoded name
        assertEquals("test name", getBrowserHistoryFragmentName("#test%20name"))
        assertEquals("test+name", getBrowserHistoryFragmentName("#test+name"))

        //Fragment with special characters
        assertEquals("name!@#$", getBrowserHistoryFragmentName("#name!@%23$"))

        //Additional special URL characters
        assertEquals("name:/?=", getBrowserHistoryFragmentName("#name%3A%2F%3F%3D"))
        assertEquals("name&+", getBrowserHistoryFragmentName("#name%26%2B"))

        //Double encoded characters
        assertEquals("test%%name", getBrowserHistoryFragmentName("#test%25%25name"))

        //Invalid fragments return null
        assertNull(getBrowserHistoryFragmentName("name")) //no # prefix
        assertNull(getBrowserHistoryFragmentName("#")) //empty name
        assertNull(getBrowserHistoryFragmentName("#?param=value")) //missing name
        assertNull(getBrowserHistoryFragmentName("#name#")) //multiple #
        assertNull(getBrowserHistoryFragmentName("#name?param?value")) //multiple ?
        assertNull(getBrowserHistoryFragmentName("#name??param=value")) //consecutive ?
    }

    @Test
    fun checkParametersParser() {
        assertTrue(getBrowserHistoryFragmentParameters("").isEmpty())
        assertTrue(getBrowserHistoryFragmentParameters("#name").isEmpty())
        assertTrue(getBrowserHistoryFragmentParameters("#name?").isEmpty())
        
        assertEquals(
            mapOf("param" to "value"),
            getBrowserHistoryFragmentParameters("#name?param=value")
        )
        assertEquals(
            mapOf("x" to "123"),
            getBrowserHistoryFragmentParameters("#screen?x=123")
        )
        
        assertEquals(
            mapOf(
                "p1" to "123",
                "p2" to "456"
            ),
            getBrowserHistoryFragmentParameters("#name?p1=123&p2=456")
        )
        assertEquals(
            mapOf(
                "x" to "1",
                "y" to "2",
                "z" to "3"
            ),
            getBrowserHistoryFragmentParameters("#screen?x=1&y=2&z=3")
        )
        
        assertEquals(
            mapOf("key" to "test value"),
            getBrowserHistoryFragmentParameters("#name?key=test%20value")
        )
        assertEquals(
            mapOf(
                "special" to "!@#$",
                "encoded" to "a/b?c=d"
            ),
            getBrowserHistoryFragmentParameters("#name?special=!@%23$&encoded=a%2Fb%3Fc%3Dd")
        )

        //Empty parameter values
        assertEquals(
            mapOf("param" to ""),
            getBrowserHistoryFragmentParameters("#name?param=")
        )
        assertEquals(
            mapOf("param" to null),
            getBrowserHistoryFragmentParameters("#name?param")
        )
        assertEquals(
            mapOf("" to "value"),
            getBrowserHistoryFragmentParameters("#name?=value")
        )
        assertEquals(
            mapOf(
                "p1" to "",
                "p2" to "value"
            ),
            getBrowserHistoryFragmentParameters("#name?p1=&p2=value")
        )
        assertEquals(
            mapOf(
                "p1" to "",
                "p2" to null,
                "p3" to "value",
            ),
            getBrowserHistoryFragmentParameters("#name?p1=&p2&p3=value")
        )

        //Special characters in parameter values
        assertEquals(
            mapOf("param" to "%"),
            getBrowserHistoryFragmentParameters("#name?param=%25")
        )
        assertEquals(
            mapOf("%%%" to "+"),
            getBrowserHistoryFragmentParameters("#name?%25%25%25=%2B")
        )

        //Multiple values for same parameter (last one wins)
        assertEquals(
            mapOf("param" to "last"),
            getBrowserHistoryFragmentParameters("#name?param=first&param=last")
        )

        assertTrue(getBrowserHistoryFragmentParameters("invalid").isEmpty())
        assertTrue(getBrowserHistoryFragmentParameters("#name#param=value").isEmpty())
    }


    @Test
    fun checkFragmentBuilder() {
        //No params
        assertEquals("#name", buildBrowserHistoryFragment("name"))
        assertEquals("#test%20name", buildBrowserHistoryFragment("test name"))
        assertEquals("#test%2Bname", buildBrowserHistoryFragment("test+name"))
        assertEquals("#te%232st%3F", buildBrowserHistoryFragment("te#2st?"))
        assertEquals("#!%40%23%24", buildBrowserHistoryFragment("!@#$"))
        assertEquals("#name%3A%2F%3F%3D", buildBrowserHistoryFragment("name:/?="))

        //Empty params map must not add '?'
        assertEquals("#screen", buildBrowserHistoryFragment("screen", emptyMap()))

        //One param
        assertEquals(
            "#name?param=value",
            buildBrowserHistoryFragment("name", mapOf("param" to "value"))
        )

        //Two params (must be sorted by key)
        assertEquals(
            "#name?a=2&z",
            buildBrowserHistoryFragment("name", mapOf("z" to null, "a" to "2"))
        )

        //Encoding in params (space, plus, slash, question, equals)
        assertEquals(
            "#name?k=a%20b&v=c%2Bd%20e%2Ff%3Fg%3Dh",
            buildBrowserHistoryFragment(
                name = "name",
                parameters = mapOf(
                    "k" to "a b",
                    "v" to "c+d e/f?g=h"
                )
            )
        )

        //Key needs encoding and value is a plus sign
        assertEquals(
            "#name?%25%25%25=%2B",
            buildBrowserHistoryFragment(
                "name",
                mapOf("%%%" to "+")
            )
        )

        //Special characters in params and empty value
        assertEquals(
            "#name?key=&special=!%40%23%24",
            buildBrowserHistoryFragment(
                name = "name",
                parameters = mapOf(
                    "special" to "!@#$",
                    "key" to ""
                )
            )
        )

        //Round trip: build -> parse name
        val fragment1 = buildBrowserHistoryFragment(
            name = "Hello World",
            parameters = mapOf("p1" to "123", "p2" to "x y")
        )
        assertEquals("Hello World", getBrowserHistoryFragmentName(fragment1))
        assertEquals(
            mapOf("p1" to "123", "p2" to "x y"),
            getBrowserHistoryFragmentParameters(fragment1)
        )
    }
}
