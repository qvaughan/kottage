package kottage.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

/**
 * @author Michael Vaughan
 */

class UriBuilderTest {

    @Test
    fun testUriBuilder() {
        assertThat(UriBuilder("/hello/world?foo=bar&baz=foo#anchor")).isEqualTo(Uri("/hello/world", "foo=bar&baz=foo", "anchor"))
        assertThat(UriBuilder("?foo=bar&baz=foo#anchor")).isEqualTo(Uri("/", "foo=bar&baz=foo", "anchor"))
        assertThat(UriBuilder("/hello/world?foo=bar&baz=foo")).isEqualTo(Uri("/hello/world", "foo=bar&baz=foo", null))
        assertThat(UriBuilder("/hello/world#anchor")).isEqualTo(Uri("/hello/world", null, "anchor"))
        assertThat(UriBuilder("/hello/world?#anchor")).isEqualTo(Uri("/hello/world", null, "anchor"))
        assertThat(UriBuilder("/hello/world?foo=bar&baz=foo#")).isEqualTo(Uri("/hello/world", "foo=bar&baz=foo", null))
        assertThat(UriBuilder("/hello/world?#")).isEqualTo(Uri("/hello/world", null, null))
        assertThat(UriBuilder("#anchor")).isEqualTo(Uri("/", null, "anchor"))
        assertThat(UriBuilder("?foo=bar")).isEqualTo(Uri("/", "foo=bar", null))
    }
}
