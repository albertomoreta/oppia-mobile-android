import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.listener.InstallCourseListener;
import org.digitalcampus.oppia.listener.SubmitListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.task.InstallDownloadedCoursesTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import Utils.CourseUtils;
import Utils.FileUtils;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class InstallDownloadedCoursesTest {
    private final String CORRECT_COURSE = "Correct_Course.zip";
    private final String EXISTING_COURSE = "Existing_Course.zip";
    private final String ALREADY_INSTALLED_COURSE = "";
    private final String INCORRECT_COURSE = "Incorrect_Course.zip";
    private final String NOXML_COURSE = "NoXML_Course.zip";
    private final String MALFORMEDXML_COURSE = "MalformedXML_Course.zip";

    private Context context;
    private SharedPreferences prefs;
    private CountDownLatch signal;
    private Payload response;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getTargetContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        signal = new CountDownLatch(1);
    }

    @After
    public void tearDown() throws Exception {
        signal.countDown();
    }

/*    @Test
    public void installCourse_correctCourse()throws Exception{
        String filename = CORRECT_COURSE;

        CourseUtils.cleanUp();

        FileUtils.copyZipFromAssets(filename);  //Copy course zip from assets to download path

        runInstallCourseTask();//Run test task

        signal.await();

        //Check if result is true
        assertTrue(response.isResult());

        String shortTitle = "correct_course";
        Course c = CourseUtils.getCourseFromDatabase(context, shortTitle);
        assertNotNull(c);   //Check that the course exists in the database

        String title = c.getMultiLangInfo().getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));

        //Check if the resultResponse is correct
        assertEquals(context.getString(R.string.install_course_complete, title), response.getResultResponse());

        File initialPath = new File(Storage.getDownloadPath(InstrumentationRegistry.getTargetContext()), filename);
        assertFalse(initialPath.exists());  //Check that the course does not exists in the "downloads" directory

        File finalPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getTargetContext()), shortTitle);
        assertTrue(finalPath.exists()); //Check that the course exists in the "modules" directory

    }
*/
 /*   @Test
    public void installCourse_existingCourse()throws Exception{
        String filename = EXISTING_COURSE;

        CourseUtils.cleanUp();

        for(int i = 0; i < 2; i++){
            FileUtils.copyZipFromAssets(filename); //Copy course zip from assets to download path
            runInstallCourseTask(); //Run test task
            signal.await();
            signal = new CountDownLatch(1);
        }

        //Check if result is false
        assertFalse(response.isResult());

        String shortTitle = "existing_course";
        Course c = CourseUtils.getCourseFromDatabase(context, shortTitle);
        assertNotNull(c);   //Check that the course exists in the database

        String title = c.getMultiLangInfo().getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));

        //Check if the resultResponse is correct
        assertEquals(context.getString(R.string.error_latest_already_installed, title), response.getResultResponse());

        File initialPath = new File(Storage.getDownloadPath(InstrumentationRegistry.getTargetContext()), filename);
        assertFalse(initialPath.exists()); //Check that the course does not exists in the "downloads" directory

        File finalPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getTargetContext()), shortTitle);
        assertTrue(finalPath.exists()); //Check that the course exists in the "modules" directory

    }
+7
  /*  @Test
    public void installCourse_noXMLCourse()throws Exception{
        String filename = NOXML_COURSE;

        CourseUtils.cleanUp();

        FileUtils.copyZipFromAssets(filename);  //Copy course zip from assets to download path

        runInstallCourseTask();//Run test task

        signal.await();

        //Check if result is true
        assertFalse(response.isResult());
        //Check if the resultResponse is correct
        assertEquals(context.getString(R.string.error_installing_course, filename), response.getResultResponse());
        File initialPath = new File(Storage.getDownloadPath(InstrumentationRegistry.getTargetContext()), filename);
        assertFalse(initialPath.exists());  //Check that the course does not exists in the "downloads" directory


        String shortTitle = "noxml_course";
        File finalPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getTargetContext()), shortTitle);
        assertFalse(finalPath.exists()); //Check that the course exists in the "modules" directory

        DbHelper db = DbHelper.getInstance(context);
        long courseId = db.getCourseID(shortTitle);
        long userId = db.getUserId(SessionManager.getUsername(context));
        Course c = db.getCourse(courseId, userId);
        assertNull(c);   //Check that the course exists in the database

    }
*/
  /*  @Test
    public void installCourse_malformedXMLCourse()throws Exception{
        String filename = MALFORMEDXML_COURSE;

        CourseUtils.cleanUp();

        FileUtils.copyZipFromAssets(filename);  //Copy course zip from assets to download path

        runInstallCourseTask();//Run test task

        signal.await();

        //Check if result is true
        assertFalse(response.isResult());
        //Check if the resultResponse is correct
        assertEquals(context.getString(R.string.error_installing_course, filename), response.getResultResponse());
        File initialPath = new File(Storage.getDownloadPath(InstrumentationRegistry.getTargetContext()), filename);
        assertFalse(initialPath.exists());  //Check that the course does not exists in the "downloads" directory


        String shortTitle = "malformedxml_course";
        File finalPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getTargetContext()), shortTitle);
        assertFalse(finalPath.exists()); //Check that the course exists in the "modules" directory

        DbHelper db = DbHelper.getInstance(context);
        long courseId = db.getCourseID(shortTitle);
        long userId = db.getUserId(SessionManager.getUsername(context));
        Course c = db.getCourse(courseId, userId);
        assertNull(c);   //Check that the course exists in the database

    }*/

 /*   @Test
    public void installCourse_errorInstallingCourse()throws Exception{
        String filename = INCORRECT_COURSE;

        CourseUtils.cleanUp();

        FileUtils.copyZipFromAssets(filename);  //Copy course zip from assets to download path

        runInstallCourseTask();     //Run test task
        signal.await();

        //Check if result is false
        assertFalse(response.isResult());
        //Check if the resultResponse is correct
        assertEquals(context.getString(R.string.error_installing_course, filename), response.getResultResponse());

        File initialPath = new File(Storage.getDownloadPath(InstrumentationRegistry.getTargetContext()), filename);
        assertFalse(initialPath.exists());  //Check that the course does not exists in the "downloads" directory

        String shortTitle = "incorrect_course";
        File finalPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getTargetContext()), shortTitle);
        assertFalse(finalPath.exists());    //Check that the course does not exists in the "modules" directory

        DbHelper db = DbHelper.getInstance(context);
        long courseId = db.getCourseID(shortTitle);
        long userId = db.getUserId(SessionManager.getUsername(context));
        Course c = db.getCourse(courseId, userId);
        assertNull(c);   //Check that the course does not exists in the database

    }
*/
   /* @Test
    public void installCourse_incorrectCourses()throws Exception{

        CourseUtils.cleanUp();

        String[] filenames = new String[] {
                INCORRECT_COURSE,
                NOXML_COURSE,
                MALFORMEDXML_COURSE
        };


        for(String filename : filenames) {
            FileUtils.copyZipFromAssets(filename);  //Copy course zip from assets to download path
        }

        runInstallCourseTask();     //Run test task

        signal.await();

        File initialPath = new File(Storage.getDownloadPath(InstrumentationRegistry.getTargetContext()));
        String[] children = initialPath.list();
        assertEquals(0, children.length);


        File finalPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getTargetContext()));
        children = finalPath.list();
        assertEquals(0, children.length);

    }*/

 /*   @Test
    public void installCourse_courseAlreadyInStorage()throws Exception {
        //Install a course that is already in storage system but not in database
        CourseUtils.cleanUp();
        installCourseAndRemoveItFromDatabase();
        installCourse_correctCourse();
    }
*/
    private void runInstallCourseTask(){
        ArrayList<Object> data = new ArrayList<>();
        Payload payload = new Payload(data);
        InstallDownloadedCoursesTask imTask = new InstallDownloadedCoursesTask(context);
        imTask.setInstallerListener(new InstallCourseListener() {
            @Override
            public void downloadComplete(Payload p) {  }

            @Override
            public void downloadProgressUpdate(DownloadProgress dp) {  }

            @Override
            public void installComplete(Payload r) {
                response = r;
                signal.countDown();
            }

            @Override
            public void installProgressUpdate(DownloadProgress dp) {  }
        });
        imTask.execute(payload);


    }

    public void installCourseAndRemoveItFromDatabase(){
        String filename = CORRECT_COURSE;
        FileUtils.copyZipFromAssets(filename);  //Copy course zip from assets to download path

        runInstallCourseTask();//Run test task
        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        String shortTitle = "correct_course";

        DbHelper db = DbHelper.getInstance(context);
        long courseId = db.getCourseID(shortTitle);

        db.deleteCourse((int) courseId);

        signal = new CountDownLatch(1);
    }
}
