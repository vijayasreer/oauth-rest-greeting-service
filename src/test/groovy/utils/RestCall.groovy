package utils

import groovy.transform.Canonical
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient


public class RestCall {

    @Canonical
    static public class Response {
        int status
        Map<String, List<String>> headers
        Map body

        String getHeader(String header) {
            headers.get(header)?.first() ?: ''
        }
    }

    static final def DEFAULT_HANDLER = { HttpResponseDecorator resp, data ->
        new Response(status: resp.status,
                headers: resp.allHeaders.groupBy { it.name }.collectEntries { k, v -> [(k.toLowerCase()): v.value] },
                body: data
        )
    }

    private String url
    private Map headers = [:]
    private Map args = [contentType:"application/json"]


    public RestCall(String baseUrl){
        this.url = baseUrl
    }

    public RestCall withHeaders(Map headers){
        this.args.headers = (this.headers += headers)
        this
    }

    public RestCall withBearerAuthorization(String accessToken)
    {
       withHeaders(["Authorization": "Bearer ${accessToken}"])
    }



    public RestCall withPath(String path){
        this.url += path
        this
    }

    public RestCall withQuery(Map query) { this.args.query = query; this }

    public RestCall withBody(Map body) { this.args.body = body; this }

    public final Response get() {
        createRestClient().get(args)
    }

    public final Response post() {
        createRestClient().post(args)
    }

    public final Response delete() {
        createRestClient().delete(args)
    }

    public final Response put() {
        createRestClient().put(args)
    }

    public final Response options() {
        createRestClient().options(args)
    }


    private RESTClient createRestClient() {
        def client = new RESTClient(url)

        client.ignoreSSLIssues()


        client.handler.failure = DEFAULT_HANDLER
        client.handler.success = DEFAULT_HANDLER
        client
    }



}
