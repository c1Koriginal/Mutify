package com.digitalsmart.mutify;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import no.danielzeller.blurbehindlib.BlurBehindLayout;


public class MapsActivity extends FragmentActivity
{

    private SlidingUpPanelLayout drawer;
    private LocationServiceManager locationServiceManager;
    private UserDataManager userDataManager;
    private BlurBehindLayout blurBackground;


    //HomePager populates a traditional ViewPager with ViewGroups (eg. ConstraintLayout) instead of Fragments
    public static HomePager homePager;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //initialize location service manager
        locationServiceManager = new LocationServiceManager(this);

        //set up blur
        drawer = findViewById(R.id.drawer);
        homePager = findViewById(R.id.home_pager);
        homePager.setAdapter(new SectionsPagerAdapter());
        blurBackground = findViewById(R.id.blur_background);
        BlurController blurController = new BlurController(findViewById(R.id.background), blurBackground);
        drawer.addPanelSlideListener(blurController);


        //initialize view model
        userDataManager = new UserDataManager();
        RecyclerView locationList = findViewById(R.id.recyclerview);
        locationList.setLayoutManager(new LinearLayoutManager(this));

        //todo: remove this
        //populating user location list with dummy data
        userDataManager.generateDummyData();

        //accessing the recyclerview adapter via UserDataManager
        locationList.setAdapter(userDataManager.getAdapter());

        locationServiceManager.configureCameraIdle();
        locationServiceManager.getCurrentLocation();

    }


    //open the bottom drawer and slide to the RecyclerView page
    public void menuButtonClicked(View view)
    {
        //add the current marker location to the list
        drawer.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        homePager.setCurrentItem(1,true);
    }

    //todo: change this after modifying activity_maps.xml
    //open the bottom drawer and slide to the edit page
    public void addButtonClicked(View view)
    {

        //test methods to display the location info of the current marker location
        drawer.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        homePager.setCurrentItem(0,true);
        TextView name = findViewById(R.id.add_name);
        TextView country = findViewById(R.id.add_country);
        TextView locality = findViewById(R.id.add_locality);

        UserLocation l = locationServiceManager.getMarkerUserLocation();

        if (l!= null)
        {
            name.setText(l.getName());
            country.setText(l.getCountry());
            locality.setText(l.getLocality());
        }
    }

    //todo: change this, this is a dummy method to add the current marker location to the list
    public void confirmButtonClicked(View view)
    {
        //add the current marker location to the list
        userDataManager.add(locationServiceManager.getMarkerUserLocation());
        homePager.setCurrentItem(1, true);
    }


    //todo: add a new "locate" button that calls this method to reset the user's marker location


    @Override
    protected void onPause()
    {
        super.onPause();
        blurBackground.disable();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        locationServiceManager.checkPermissionAndService();
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        locationServiceManager.checkPermissionAndService();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        blurBackground.disable();
    }
}