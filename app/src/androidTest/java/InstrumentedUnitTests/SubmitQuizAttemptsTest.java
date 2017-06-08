package InstrumentedUnitTests;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.digitalcampus.oppia.api.MockApiEndpoint;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.listener.SubmitListener;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.SubmitQuizAttemptsTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

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
public class SubmitQuizAttemptsTest {

    @Rule
    public DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();


    private CountDownLatch signal;
    private MockWebServer mockServer;
    private Context context;
    private Payload response;

    @Mock
    SharedPreferences prefs;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getTargetContext();
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

    private QuizAttempt getTestQuizAttempt(User u){
        QuizAttempt qa = new QuizAttempt();
        qa.setId(12);
        qa.setSent(false);
        qa.setCourseId(0);
        qa.setScore(0.0f);
        qa.setMaxscore(0.0f);
        qa.setPassed(false);
        qa.setData("");
        qa.setUser(u);
        qa.setUserId(u.getUserId());
        return qa;
    }

    @Test
    public void submitUnsentQuizAttempt_Success() throws Exception {
        try {
            mockServer = new MockWebServer();

            String filename = "responses/response_201_submitquiz.json";

            mockServer.enqueue(new MockResponse()
                    .setResponseCode(201)
                    .setBody(Utils.FileUtils.getStringFromFile(InstrumentationRegistry.getContext(), filename)));

            mockServer.start();


        }catch(IOException ioe) {
            ioe.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }


        try {
            SessionManager.logoutCurrentUser(context);
            DbHelper db = DbHelper.getInstance(context);
            //Simulate user logged in
            User testUser = getTestUser();
            db.addOrUpdateUser(testUser);

            User u = db.getUser(testUser.getUsername());

            SessionManager.loginUser(context, u);

            //Delete previous quiz attempts
            db.deleteQuizAttempts(0, u.getUserId());
            QuizAttempt qa = getTestQuizAttempt(u);
            //Insert unsent quiz attempt
            db.insertQuizAttempt(qa);

            ArrayList<QuizAttempt> unsent =  db.getUnsentQuizAttempts();
            assertEquals(1, unsent.size());

            Payload p = new Payload(unsent);

            SubmitQuizAttemptsTask task = new SubmitQuizAttemptsTask(context, new MockApiEndpoint(mockServer));
            task.setRegisterListener(new SubmitListener() {
                @Override
                public void submitComplete(Payload r) {
                    response = r;
                    signal.countDown();
                }

                @Override
                public void apiKeyInvalidated() { }
            });
            task.execute(p);

            signal.await();
            assertTrue(response.isResult());
            assertEquals(0, db.getUnsentQuizAttempts().size());

        }catch (InterruptedException ie) {
            ie.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void submitUnsentQuizAttempt_BadRequest() throws Exception {
        try {
            mockServer = new MockWebServer();

            mockServer.enqueue(new MockResponse()
                    .setBody("")
                    .setResponseCode(400));

            mockServer.start();


        }catch(IOException ioe) {
            ioe.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }


        try {
            SessionManager.logoutCurrentUser(context);
            DbHelper db = DbHelper.getInstance(context);
            //Simulate user logged in
            User testUser = getTestUser();
            db.addOrUpdateUser(testUser);

            User u = db.getUser(testUser.getUsername());

            SessionManager.loginUser(context, u);

            //Delete previous quiz attempts
            db.deleteQuizAttempts(0, u.getUserId());
            QuizAttempt qa = getTestQuizAttempt(u);
            //Insert unsent quiz attempt
            db.insertQuizAttempt(qa);

            ArrayList<QuizAttempt> unsent =  db.getUnsentQuizAttempts();
            assertEquals(1, unsent.size());

            Payload p = new Payload(unsent);

            SubmitQuizAttemptsTask task = new SubmitQuizAttemptsTask(context, new MockApiEndpoint(mockServer));
            task.setRegisterListener(new SubmitListener() {
                @Override
                public void submitComplete(Payload r) {
                    response = r;
                    signal.countDown();
                }

                @Override
                public void apiKeyInvalidated() { }
            });
            task.execute(p);

            signal.await();
            assertFalse(response.isResult());
            assertEquals(0, db.getUnsentQuizAttempts().size());

        }catch (InterruptedException ie) {
            ie.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
