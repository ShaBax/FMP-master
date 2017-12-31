package com.dozydroid.fmp.activities;

import android.content.ContentResolver;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.dozydroid.fmp.R;
import com.dozydroid.fmp.models.Video;
import com.dozydroid.fmp.utilities.VideosDBHandler;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.DynamicConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.FileDataSource;

import java.util.ArrayList;
import java.util.List;

import tcking.github.com.giraffeplayer2.DefaultPlayerListener;
import tcking.github.com.giraffeplayer2.GiraffePlayer;
import tcking.github.com.giraffeplayer2.PlayerListener;
import tcking.github.com.giraffeplayer2.VideoInfo;
import tcking.github.com.giraffeplayer2.VideoView;


public class VideoPlayerActivity extends AppCompatActivity {

    private final String STATE_PLAYER_FULLSCREEN = "full_screen";
    private final String STATE_PLAYER_LANDSCAPE = "landscape";
    private Handler handler;
    WindowManager.LayoutParams layoutParams;
    //Variable to store brightness value
//    private int brightness;
    //Content resolver used as a handle to the system's settings
    private ContentResolver cResolver;
    //Window object, that will store a reference to the current window
    private Window window;


    VideosDBHandler videosDBHandler;

    ArrayList<Video> videoItemsList = new ArrayList<>();
    ArrayList<String> videoPathsList;
    List<MediaSource> videosList;
    MediaSource[] mediaSources;

    private static final String TAG = "MediaDemo";
    private String uriString;
    private String videoTitle;
    private String videoDuration;
    private String videoResolution;
    private boolean isAlreadyFavorite;
    private Video thisVideo;
    private int videoPosition;


    ///

    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        videosDBHandler = new VideosDBHandler(VideoPlayerActivity.this, null, null, 0);
        //Get the content resolver
        cResolver = getContentResolver();

        //Get the current window
        window = getWindow();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_video_player);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Bundle bundle = getIntent().getExtras();
        uriString = bundle.getString("uriString");
        videoTitle = bundle.getString("videoTitle");
        videoDuration = bundle.getString("videoDuration");
        videoResolution = bundle.getString("videoResolution");
        videoItemsList = bundle.getParcelableArrayList("videosList");

        Log.d(TAG,"uri String :"+uriString);
        Log.d(TAG,"videoTitle :"+videoTitle);
        Log.d(TAG," video resolution:"+videoResolution);
        Log.d(TAG," videolistitem:"+videoItemsList.toString());

        Uri videoURI = Uri.parse(uriString);

       /* if(videoItemsList!=null && videoItemsList.size()>1){
            videoPosition = bundle.getInt("videoPosition");
            //prepareExoPlayerFromList();
        }else{
            Uri videoURI = Uri.parse(uriString);
            //prepareExoPlayerFromFileUri(videoURI);
        }*/
    }


    private void CreatePlayer() {
        Log.d(TAG, "createPlayer() called");
        try {
            // Create LibVLC
            videoView = (VideoView) findViewById(R.id.video_view);
            videoView.setPlayerListener(playerListener);
            VideoInfo videoInfo = new VideoInfo(Uri.parse(uriString))
                    .setTitle(videoTitle) //config title
                    //.setAspectRatio(3) //aspectRatio
                    .setShowTopBar(true) //show mediacontroller top bar
                    .setPortraitWhenFullScreen(false);//portrait when full screen

            videoView.videoInfo(videoInfo);
            videoView.getPlayer().start();
        }
        catch (Exception e) {
            Toast.makeText(this, "Error in creating player!", Toast
                    .LENGTH_LONG).show();
            Log.d(TAG, "Error in creating player: " +e );
        }
    }

    private PlayerListener playerListener=new DefaultPlayerListener() {//example of using playerListener
        @Override
        public void onPreparing(GiraffePlayer giraffePlayer) {


            //Toast.makeText(PlayerActivity.this, "start playing:"+giraffePlayer.getVideoInfo().getUri(),Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCompletion(GiraffePlayer giraffePlayer) {

            Log.d(TAG, "onCompletion(GiraffePlayer giraffePlayer)" );

        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        CreatePlayer();
        /*if(exoPlayer!=null)
            exoPlayer.setPlayWhenReady(true);*/
    }
    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
/*        outState.putBoolean(STATE_PLAYER_FULLSCREEN, isFullScreen);
        outState.putBoolean(STATE_PLAYER_LANDSCAPE, isLandscape);*/
        super.onSaveInstanceState(outState, outPersistentState);
    }
    private void prepareExoPlayerFromList(){
       /* exoPlayer = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector(), new DefaultLoadControl());
        exoPlayer.addListener(eventListener);*/

        DynamicConcatenatingMediaSource mediaSources = new DynamicConcatenatingMediaSource();
        for(int i=0; i<videoItemsList.size(); i++){
            Uri uri = Uri.parse(videoItemsList.get(i).getData());
            DataSpec dataSpec = new DataSpec(uri);
            final FileDataSource fileDataSource = new FileDataSource();
            try {
                fileDataSource.open(dataSpec);
            } catch (FileDataSource.FileDataSourceException e) {
                e.printStackTrace();
            }

            DataSource.Factory factory = new DataSource.Factory() {
                @Override
                public DataSource createDataSource() {
                    return fileDataSource;
                }
            };
            MediaSource audioSource = new ExtractorMediaSource(fileDataSource.getUri(),
                    factory, new DefaultExtractorsFactory(), null, null);
            mediaSources.addMediaSource(audioSource);
        }
    /*    exoPlayer.prepare(mediaSources);
        exoPlayer.seekTo(videoPosition, 0);*/


    }

    private void prepareExoPlayerFromFileUri(Uri uri){
      /*  exoPlayer = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector(), new DefaultLoadControl());
        exoPlayer.addListener(eventListener);*/

        DataSpec dataSpec = new DataSpec(uri);
        final FileDataSource fileDataSource = new FileDataSource();
        try {
            fileDataSource.open(dataSpec);
        } catch (FileDataSource.FileDataSourceException e) {
            e.printStackTrace();
        }

        DataSource.Factory factory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return fileDataSource;
            }
        };
        MediaSource audioSource = new ExtractorMediaSource(fileDataSource.getUri(),
                factory, new DefaultExtractorsFactory(), null, null);

      /*  exoPlayer.prepare(audioSource);
        initMediaControls();*/
    }



}
