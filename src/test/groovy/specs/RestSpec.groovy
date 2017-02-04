package specs

import groovy.json.JsonBuilder
import org.apache.http.HttpStatus
import spock.lang.Specification
import utils.RestCall
import groovy.transform.Canonical
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient

import static org.apache.http.HttpHeaders.AUTHORIZATION

/**
 * Created by vijayasree.r on 28/01/2017.
 */
class RestSpec extends Specification{

    protected static final String GREETING_BASIC_AUTH = "Basic Y2xpZW50YXBwOjEyMzQ1Ng=="
    protected static final String X_REQUESTED_FROM_HEADER = "X-Requested-From"
    protected static final String GREETINGS_CLIENT_ID = "clientapp"
    protected static final Map<String, String> LOGIN_PAYLOAD_PASSWORD = ["grant_type": "password", "password": "spring", "username": "roy", "scope": "read write", "client_secret": "123456", "client_id": "clientapp"]

    public static final String GREETING_BASE_URL = "http://localhost:8080"

    protected static final String AUTHENTICATION_ENDPOINT = GREETING_BASE_URL + "/oauth/token"
    protected static final String GREETING_ENDPOINT = GREETING_BASE_URL + "/greeting"


    protected RestCall buildRestCall(String authorizationHeader=GREETING_BASIC_AUTH, String endpoint) {
        new RestCall(endpoint)
                .withHeaders("Accept": "application/json")
                .withHeaders("Authorization": GREETING_BASIC_AUTH)
                .withQuery(LOGIN_PAYLOAD_PASSWORD)
    }


    def "get access token grant type password happy path"(){

        given:

        RestCall restCall = buildRestCall(GREETING_BASIC_AUTH, AUTHENTICATION_ENDPOINT)

        when:
        def response = restCall.post()

        then:
        response.status == HttpStatus.SC_OK
        response.body.access_token
        response.body.token_type
        response.body.expires_in
        response.body.refresh_token
        response.body.scope

        println new JsonBuilder( response.body ).toPrettyString()

    }

    def "use access token to login happy path"(){

        given:
        String accessToken = "Bearer ${buildRestCall(GREETING_BASIC_AUTH, AUTHENTICATION_ENDPOINT).withQuery(LOGIN_PAYLOAD_PASSWORD).post().body.access_token}"
        //println accessToken

        when:

        def response =  buildRestCall(GREETING_BASIC_AUTH, GREETING_ENDPOINT).withHeaders("Authorization": accessToken).post()


        then:
        response.status == HttpStatus.SC_OK

    }

    def "use refresh token to retrive new token happy path"(){

        given:
        String refreshToken = "Bearer ${buildRestCall(GREETING_BASIC_AUTH, AUTHENTICATION_ENDPOINT).withQuery(LOGIN_PAYLOAD_PASSWORD).post().body.refresh_token}"
        //println refreshToken

        when:

        def response =  buildRestCall(GREETING_BASIC_AUTH, AUTHENTICATION_ENDPOINT).post()


        then:
        response.status == HttpStatus.SC_OK
        response.body.access_token
        response.body.token_type
        response.body.expires_in
        response.body.refresh_token
        response.body.scope

        println new JsonBuilder( response.body ).toPrettyString()

    }
}
