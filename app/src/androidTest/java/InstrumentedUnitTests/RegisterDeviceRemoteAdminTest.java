package InstrumentedUnitTests;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.api.MockApiEndpoint;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.RegisterDeviceRemoteAdminTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import TestRules.DisableAnimationsRule;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class RegisterDeviceRemoteAdminTest {

    @Rule
    public DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();


    private CountDownLatch signal;
    private MockWebServer mockServer;
    private Context context;
    private Payload response;

    @Mock SharedPreferences prefs;

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

    @Test
    public void success()throws Exception {

        try {
            mockServer = new MockWebServer();

            String filename = "responses/response_201_register.json";

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
            when(prefs.getString(PrefsActivity.PREF_USER_NAME, "")).thenReturn("nonEmptyUsername");
            when(prefs.getBoolean(PrefsActivity.GCM_TOKEN_SENT, anyBoolean())).thenReturn(false);

            RegisterDeviceRemoteAdminTask task = new RegisterDeviceRemoteAdminTask(context, new MockApiEndpoint(mockServer));
            task.execute(new Payload());

            signal.await();

            assertTrue(response.isResult());
            assertEquals(context.getString(R.string.register_complete), response.getResultResponse());

        }catch (InterruptedException ie) {
            ie.printStackTrace();
        }catch(Exception e){}
    }
}
