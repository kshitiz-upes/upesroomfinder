package upes.roomfinder;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ClassDetailsActivity extends Activity {

    private static final int PADDING = 15;
    private static final int LENGTH_OF_TIME_DESCRIPTION = 17;
    private int colorThisMany = 0, hour;

    ArrayList rawResultsList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_details);
        AssetManager manager;
        manager = getAssets();
        InputStream inputStream = null;
        try {
            //inputStream = manager.open("fall2015.txt");
            inputStream = manager.open(Keywords.currentTerm);
            //be wary file is specified in 3 locations. 2 ResultsActivity and ClassDetailsActivity
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent intent = getIntent();
        String classroom = intent.getStringExtra(ResultsActivity.CLASSROOM_STRING);
        String day = intent.getStringExtra(ResultsActivity.DAY_STRING);
        hour = intent.getIntExtra(ResultsActivity.HOUR_STRING,0);

        MyHandler myHandler = new MyHandler(classroom,day,inputStream);
        myHandler.skipTimeSearch();
        Classroom specificClassroom = new Classroom(true, classroom);
        String roomName = specificClassroom.getName();
        rawResultsList = myHandler.getDetailedRooms();
        ArrayList timeResultsList = getTrimmedResults(rawResultsList);
        //My print statement to get the raw list of results. Used for debugging.
//        for (int i =0; i <rawResultsList.size(); i++) {
//            System.out.println("The raw list is: " + rawResultsList.get(i).toString());
//        }
        timeResultsList = getOrderedLists(timeResultsList);
        ScrollView sv = new ScrollView(this);
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        sv.addView(ll);
        TextView firstBox = new TextView(this);
        if (hour >12) {
            hour-=12;//revert back to standard
        }
        firstBox.setText("The classroom: " + roomName + " on " + day + " has these classes." + "\n"
            + "The times that have past " + hour + " are shaded red.");
        ll.addView(firstBox);
        for (int i =0; i < timeResultsList.size(); i++){
            TextView tv = new TextView(this);
            if (colorThisMany > i) {
                tv.setBackgroundResource(R.color.red);
            }else {
                tv.setBackgroundResource(R.color.white);
            }
            tv.setText(timeResultsList.get(i).toString());
            tv.setPadding(0, PADDING, 0, PADDING);
            tv.setTag(i);
            ll.addView(tv);
        }
        this.setContentView(sv);
    }

    //add some logic that makes past ones red. Maybe some logic of sorting
    //them should be implemented.

    /**
     * Method that will take raw class outputs and return only the times.
     * @param rawList
     * @return modifiedList which contains only the times in a format I specify.
     */
    public ArrayList getTrimmedResults(ArrayList rawList) {
        ArrayList modifiedList = new ArrayList();
        for (int i = 0; i < rawList.size(); i++){
            String currentEntry = rawList.get(i).toString();
            int colonLocation = currentEntry.indexOf(":");
            char[] totalTimeArray = new char[LENGTH_OF_TIME_DESCRIPTION];
            for (int currentPosition = 0; currentPosition < totalTimeArray.length; currentPosition++){
                totalTimeArray[currentPosition] = currentEntry.charAt(colonLocation+(currentPosition-2));
            }
            String totalTimeString = Character.toString(totalTimeArray[0]) + Character.toString(totalTimeArray[1]) +
                    Character.toString(totalTimeArray[2]) + Character.toString(totalTimeArray[3]) + Character.toString(totalTimeArray[4]) +
                    Character.toString(totalTimeArray[5]) + Character.toString(totalTimeArray[6]) + Character.toString(totalTimeArray[7]) +
                    Character.toString(totalTimeArray[8]) + Character.toString(totalTimeArray[9]) + Character.toString(totalTimeArray[10]) +
                    Character.toString(totalTimeArray[11]) + Character.toString(totalTimeArray[12]) + Character.toString(totalTimeArray[13]) +
                    Character.toString(totalTimeArray[14]) +  Character.toString(totalTimeArray[15]) +  Character.toString(totalTimeArray[16]);

            modifiedList.add(totalTimeString);
        }
        return modifiedList;
    }

    public ArrayList getOrderedLists(ArrayList inputList) {
        int[] originalTime = new int[inputList.size()];
        int[] sortedTime = new int[inputList.size()];
        for (int i =0; i < inputList.size(); i++) {
            String currentEntry = inputList.get(i).toString();
            char[] hourArray = {currentEntry.charAt(0), currentEntry.charAt(1)};
            String hour = Character.toString(hourArray[0]) + Character.toString(hourArray[1]);
            int time = Integer.parseInt(hour);
            //I spit it into military time just so I can do logical checks faster.
            if (time >=1 && time <=7){
                time +=12;
            }
            originalTime[i] = time;
            sortedTime[i] = time;
        }
        Arrays.sort(sortedTime);
        //ok so I want to remove duplicates from my sortedTime list.
        //A set does not allow duplicates so I dump everything into a set then spit back out into an array
        Set<Integer> timeSet = new HashSet<>();
        for (int i = 0; i <sortedTime.length; i++){
            timeSet.add(sortedTime[i]);
        }
        //recycling variable
        sortedTime = new int[timeSet.size()];
        int count = 0;
        for(int i : timeSet){
            sortedTime[count]= i;
            count++;
        }
        Arrays.sort(sortedTime);
        ArrayList sortedList = new ArrayList();
        for (int sorted =0;  sorted< sortedTime.length; sorted++) {
            for (int orig = 0; orig < originalTime.length; orig++) {
                if (originalTime[orig] == sortedTime[sorted] ) {
                    sortedList.add(inputList.get(orig));
                    break;
                }
            }
            if (sortedTime[sorted] <= hour){
                colorThisMany++;
            }
        }
        return sortedList;
    }

}
