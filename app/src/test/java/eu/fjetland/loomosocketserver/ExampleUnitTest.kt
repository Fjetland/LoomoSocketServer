package eu.fjetland.loomosocketserver

import org.json.JSONObject
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
        val string = """{"a":1,"b":[1,2,3,4],"c":{"v":"yo","t":false}}"""
//"{'a':1,'b':[1,2,3,4],'c':{'v':'yo','t':false}}"
        println(string)

        val js = JSONObject(string)
        println(js.toString())
        //val str = js.getInt("a")
        val strobj = js.getString("c.v")
        //val str2 = strobj.getString("v")


        println(strobj)
    }
}
