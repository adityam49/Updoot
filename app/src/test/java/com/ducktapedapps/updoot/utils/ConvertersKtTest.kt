package com.ducktapedapps.updoot.utils

import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class ConvertersKtTest {
    //less than -1000
    @Test
    fun `getCompactCountAsString_-999_999_returns-999dot9K lessThan`() {
        assertThat(getCompactCountAsString(-999_999), `is`("-999.9K <"))
    }

    //-1001
    @Test
    fun `getCompactCountAsString_-1 001_returns-1dot0K`() {
        assertThat(getCompactCountAsString(-1_001), `is`("-1.0K <"))
    }

    //-1000
    @Test
    fun `getCompactCountAsString_-1 000_returns-1dot0K`() {
        assertThat(getCompactCountAsString(-1_000), `is`("-1000"))
    }

    //1000
    @Test
    fun `getCompactCountAsString_1 000_returns1 000`() {
        assertThat(getCompactCountAsString(1000), `is`("1000"))
    }

    //1001
    @Test
    fun `getCompactCountAsString_1 001_returns1dot0k+`() {
        assertThat(getCompactCountAsString(1001), `is`("1.0K+"))
    }

    //999 999
    @Test
    fun `getCompactCountAsString_999_999_returns999dot9K+`() {
        assertThat(getCompactCountAsString(999_999), `is`("999.9K+"))
    }
}