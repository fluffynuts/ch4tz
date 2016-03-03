package ix.notifications.android;

import com.google.gson.Gson;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import microsoft.aspnet.signalr.client.ConnectionState;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import static org.junit.Assert.*;


public class NotificationsListenerTests {
    // these urls are endpoints on the test service. In production, your Java client should provide
    //   service url which would be given to the app on logging in. This change
    private final String _testServiceUrl = "http://device-messenger-test.azurewebsites.net";
    private final String _testServiceSendTestMessagePostUrl = "http://device-messenger-test.azurewebsites.net/Home/SendJsonTestMessage";
    // urls below are only for testing from IIS express with local run from Visual Studio
    //private final String _testServiceUrl = "http://localhost:57995";
    //private final String _testServiceSendTestMessagePostUrl = "http://localhost:57995/Home/SendJsonTestMessage";
    @Test
    public void testConstructor_ShouldTakeInServiceUrlParameter() {
        // Arrange
        NotificationsListener sut = new NotificationsListener(_testServiceUrl, UUID.randomUUID());

        // Act
        String result = sut.getServiceUrl();

        // Assert
        assertEquals(_testServiceUrl, result);
    }

    @Test
    public void testRoundTrip_WithRawResult_ShouldGetNotificationMeantForThatClient() throws ExecutionException, InterruptedException, IOException {
        // Arrange
        SimpleJsonMessageHandler handler = new SimpleJsonMessageHandler();
        UUID clientId = UUID.randomUUID();
        NotificationsListener sut = new NotificationsListener(_testServiceUrl, clientId);

        // Act
        //   this is what you would use from within a Java client
        sut.start();
        sut.addJsonNotificationHandler(handler);

        // fire off a request to the web service to trigger a notification
        Thread.sleep(2000);
        CloseableHttpResponse postResult = doTestMessagePostFor(clientId);

        // Assert
        HubConnection connection = sut.getConnection();
        assertNotNull(connection);
        assertEquals(ConnectionState.Connected, connection.getState());

        assertEquals(200, postResult.getStatusLine().getStatusCode());
        String jsonResult = EntityUtils.toString(postResult.getEntity());
        Gson gson = new Gson();
        TestMessagePostResult result = gson.fromJson(jsonResult, TestMessagePostResult.class);
        assertTrue(result.ok);
        // give a small time break to cater for a little network latency
        Thread.sleep(1000);

        // verify that the message is received as sent
        assertNotNull(handler.jsonResult);
        NotificationMessage notification = gson.fromJson(handler.jsonResult, NotificationMessage.class);
        assertNotNull(notification);
        assertEquals("1.0", notification.Version);
        assertEquals("JUnit Test Message", notification.Subject);
        assertEquals("This is a message sent from a JUnit integration test.", notification.Body);
        assertEquals(1, notification.Links.size());
        assertEquals("Google", notification.Links.get(0).Title);
        assertEquals("http://www.google.com", notification.Links.get(0).Url);
        assertEquals(1, notification.Metadata.size());
        assertEquals("VehicleID", notification.Metadata.get(0).Name);
        assertEquals("123456", notification.Metadata.get(0).Value);

        sut.stop();
        assertEquals(null, sut.getConnection());
    }

    @Test
    public void testRoundTrip_WithRawResult_ShouldNotGetNotificationMeantForAnotherClient() throws ExecutionException, InterruptedException, IOException {
        // Arrange
        SimpleJsonMessageHandler handler = new SimpleJsonMessageHandler();
        UUID clientId = UUID.randomUUID();
        UUID anotherClientId = UUID.randomUUID();
        NotificationsListener sut = new NotificationsListener(_testServiceUrl, clientId);
        assertNotEquals(clientId.toString(), anotherClientId.toString());
        // Act
        //   this is what you would use from within a Java client
        sut.start();
        sut.addJsonNotificationHandler(handler);

        // fire off a request to the web service to trigger a notification after a short del
        //Thread.sleep(1000);
        CloseableHttpResponse postResult = doTestMessagePostFor(anotherClientId);

        // Assert
        assertEquals(200, postResult.getStatusLine().getStatusCode());
        String jsonResult = EntityUtils.toString(postResult.getEntity());
        Gson gson = new Gson();
        TestMessagePostResult result = gson.fromJson(jsonResult, TestMessagePostResult.class);
        assertTrue(result.ok);   // test service acknowledges attempting to send the message
        // give a small time break to cater for a little network latency, just in case
        Thread.sleep(1000);

        assertNull(handler.jsonResult);  // should not get the message as it's meant for another client
    }

    public class SimpleJsonMessageHandler extends JsonMessageHandler {
        public String jsonResult;
        @Override
        public void run(String s) {
            jsonResult = s;
        }
    }

    private class TestMessagePostResult {
        boolean ok;
    }


    private CloseableHttpResponse doTestMessagePostFor(UUID clientId) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(_testServiceSendTestMessagePostUrl);
        String messageAsJson = "{\"Version\":\"1.0\",\"Subject\":\"JUnit Test Message\",\"Body\":\"This is a message sent from a JUnit integration test.\",\"Links\":[{\"Title\":\"Google\",\"Url\":\"http://www.google.com\"}],\"Metadata\":[{\"Name\":\"VehicleID\",\"Value\":\"123456\"}]}";
        String testServicePostJson = "{\"Clients\": [\"" + clientId.toString() + "\"], \"Message\": " + messageAsJson + "}";
        StringEntity entity = new StringEntity(testServicePostJson);
        post.setEntity(entity);
        String jsonMime = "application/json";
        post.setHeader("Accept", jsonMime);
        post.setHeader("Content-Type", jsonMime);
        return client.execute(post);
    }
}
