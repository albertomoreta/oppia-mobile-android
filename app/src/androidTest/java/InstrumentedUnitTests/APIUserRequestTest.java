package InstrumentedUnitTests;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.api.MockApiEndpoint;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.APIUserRequestTask;
import org.digitalcampus.oppia.task.Payload;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import TestRules.DisableAnimationsRule;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class APIUserRequestTest {

    @Rule
    public DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();


    private CountDownLatch signal;
    private MockWebServer mockServer;
    private Context context;
    private Payload response;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getTargetContext();
        //DbHelper.getInstance(context).resetDatabase();
        signal = new CountDownLatch(1);
    }

    @After
    public void tearDown() throws Exception {
        signal.countDown();
        mockServer.shutdown();
    }

    private User getTestUser(){
        User u = new User();
        u.setUsername("bbbbbbb");
        u.setPassword("bbbbbbb");
        return u;
    }

    @Test
    public void apiUserRequest_ResponseSuccess()throws Exception {
        try {
            mockServer = new MockWebServer();

            mockServer.enqueue(new MockResponse()
                    .setResponseCode(201));

            mockServer.start();

        }catch(IOException ioe) {
            ioe.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }

        SessionManager.logoutCurrentUser(context);
        DbHelper db = DbHelper.getInstance(context);
        //Simulate user logged in
        User testUser = getTestUser();
        db.addOrUpdateUser(testUser);

        User u = db.getUser(testUser.getUsername());
        SessionManager.loginUser(context, u);

        ArrayList<Object> users = new ArrayList<>();
        users.add(u);

        Payload p = new Payload(users);
        p.setUrl("/api/v1/quizattempt/");

        try {
            APIUserRequestTask task = new APIUserRequestTask(context, new MockApiEndpoint(mockServer));
            task.setAPIRequestListener(new APIRequestListener() {
                @Override
                public void apiRequestComplete(Payload r) {
                    response = r;
                    signal.countDown();
                }

                @Override
                public void apiKeyInvalidated() {

                }
            });
            task.execute(p);

            signal.await();

            assertTrue(response.isResult());

        }catch (InterruptedException ie) {
            ie.printStackTrace();
        }catch(Exception e){}

    }

}
