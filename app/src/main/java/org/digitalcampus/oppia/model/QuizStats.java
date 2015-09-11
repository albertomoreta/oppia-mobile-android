/*
 * This file is part of OppiaMobile - https://digital-campus.org/
 *
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.oppia.model;

import org.digitalcampus.mobile.quiz.Quiz;
import org.json.JSONException;
import org.json.JSONObject;

public class QuizStats {

    public static final String JSONPROP_QUIZID = "quiz_id";
    public static final String JSONPROP_SCORE = "score";
    public static final String JSONPROP_MAXSCORE = "maxscore";

    private int quizId;
    private String digest;
    public boolean attempted;
    private int maxScore;
    private int userScore;
    private boolean passed;

    public QuizStats(int quizId){ this.quizId = quizId; }
    public QuizStats(){ }

    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }

    public String getDigest() { return digest; }
    public void setDigest(String digest) { this.digest = digest; }

    public int getMaxScore() {
        return maxScore;
    }
    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    public int getUserScore() { return userScore; }
    public void setUserScore(int userScore) { this.userScore = userScore; }

    public boolean isAttempted(){ return attempted; }
    public void setAttempted(boolean a){
        attempted = a;
    }

    public int getPercent(){

        return (int) Math.ceil( userScore * 100.0f / Math.max(1,maxScore));
    }

    public boolean isPassed(){
        return passed;
    }
    public void setPassed(boolean passed){ this.passed = passed; }


}
