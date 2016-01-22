package com.bettercloud.vault.rest;

import com.bettercloud.vault.json.Json;
import com.bettercloud.vault.json.JsonObject;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests relating the REST client processing of POST requests.
 */
public class PostTests {

    /**
     * Verify a basic POST request, with no parameters or headers.
     *
     * @throws RestException
     * @throws UnsupportedEncodingException If there's a problem parsing the response JSON as UTF-8
     */
    @Test
    public void testPost_Plain() throws RestException, UnsupportedEncodingException {
        final RestResponse restResponse = new Rest()
                .url("https://httpbin.org/post")
                .post();
        assertEquals(200, restResponse.getStatus());
        assertEquals("application/json", restResponse.getMimeType());

        final String jsonString = new String(restResponse.getBody(), "UTF-8");
        final JsonObject jsonObject = Json.parse(jsonString).asObject();
        assertEquals("https://httpbin.org/post", jsonObject.getString("url", null));
    }

    /**
     * Verify a POST request that has no query string on the base URL, but does have additional
     * parameters passed.  The base URL should remain unmodified, and the parameters should be
     * sent with the request body.
     *
     * @throws RestException
     * @throws UnsupportedEncodingException If there's a problem parsing the response JSON as UTF-8
     */
    @Test
    public void testPost_InsertParams() throws RestException, UnsupportedEncodingException {
        final RestResponse restResponse = new Rest()
                .url("https://httpbin.org/post")
                .parameter("foo", "bar")
                .parameter("apples", "oranges")
                .parameter("multi part", "this parameter has whitespace in its name and value")
                .post();
        assertEquals(200, restResponse.getStatus());
        assertEquals("application/json", restResponse.getMimeType());

        final String jsonString = new String(restResponse.getBody(), "UTF-8");
        final JsonObject jsonObject = Json.parse(jsonString).asObject();
        assertEquals("https://httpbin.org/post", jsonObject.getString("url", null));

        // Note that with a POST (as with a PUT) to this "httpbin.org" test service, parameters are returned
        // within a JSON object called "form", unlike it's "args" counterpart when doing a GET.
        final JsonObject form = jsonObject.get("form").asObject();
        assertEquals("bar", form.getString("foo", null));
        assertEquals("oranges", form.getString("apples", null));
        assertEquals("this parameter has whitespace in its name and value", form.getString("multi part", null));
    }

    /**
     * Verify a POST request that already has a query string on the base URL, but also has additional
     * parameters passed.  The base URL should remain unmodified, and the parameters should be
     * sent with the request body.
     *
     * @throws RestException
     * @throws UnsupportedEncodingException If there's a problem parsing the response JSON as UTF-8
     */
    @Test
    public void testPost_UpdateParams() throws RestException, UnsupportedEncodingException {
        final RestResponse restResponse = new Rest()
                .url("https://httpbin.org/post?hot=cold")
                .parameter("foo", "bar")
                .parameter("apples", "oranges")
                .parameter("multi part", "this parameter has whitespace in its name and value")
                .post();
        assertEquals(200, restResponse.getStatus());
        assertEquals("application/json", restResponse.getMimeType());

        final String jsonString = new String(restResponse.getBody(), "UTF-8");
        final JsonObject jsonObject = Json.parse(jsonString).asObject();
        assertEquals("https://httpbin.org/post?hot=cold", jsonObject.getString("url", null));
        final JsonObject args = jsonObject.get("args").asObject();
        assertEquals("cold", args.getString("hot", null));
        final JsonObject form = jsonObject.get("form").asObject();
        assertEquals("bar", form.getString("foo", null));
        assertEquals("oranges", form.getString("apples", null));
        assertEquals("this parameter has whitespace in its name and value", form.getString("multi part", null));
    }

    /**
     * <p>Verify a POST request that passes HTTP headers.</p>
     *
     * <p>Note that even though our header names are all lowercase, the round-trip process
     * converts them to camel case (e.g. <code>two-part</code> to <code>Two-Part</code>).</p>
     *
     * @throws RestException
     * @throws UnsupportedEncodingException If there's a problem parsing the response JSON as UTF-8
     */
    @Test
    public void testPost_WithHeaders() throws RestException, UnsupportedEncodingException {
        final RestResponse restResponse = new Rest()
                .url("https://httpbin.org/post")
                .header("black", "white")
                .header("day", "night")
                .header("two-part", "Note that headers are send in url-encoded format")
                .post();
        assertEquals(200, restResponse.getStatus());
        assertEquals("application/json", restResponse.getMimeType());

        final String jsonString = new String(restResponse.getBody(), "UTF-8");
        final JsonObject jsonObject = Json.parse(jsonString).asObject();
        assertEquals("https://httpbin.org/post", jsonObject.getString("url", null));
        final JsonObject headers = jsonObject.get("headers").asObject();
        assertEquals("white", headers.getString("Black", null));
        assertEquals("night", headers.getString("Day", null));
        assertEquals("Note+that+headers+are+send+in+url-encoded+format", headers.getString("Two-Part", null));
    }

}
