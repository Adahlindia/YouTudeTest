package com.example.kddownloader;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.SparseArray;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kddownloader.databinding.ActivityMainBinding;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.MessageFormat;
import java.util.Objects;

import com.bumptech.glide.Glide;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        setSupportActionBar(binding.toolbar);


        binding.btndownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadVideo();
            }
        });

    }


    private void downloadVideo(){
        String youtubeLink = "https://www.youtube.com/watch?v=-8MLyiFDAQE";
        new YouTubeExtractor(this){
            @Override
            protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta videoMeta) {

                if(ytFiles == null){
                    Toast.makeText(MainActivity.this,"There is an error while extracting video", Toast.LENGTH_SHORT).show();
                }

                binding.titleTv.setText(MessageFormat.format("Title: {0}",videoMeta.getTitle()));
                binding.channelNameTv.setText(MessageFormat.format("Channel Name: {0}",videoMeta.getAuthor()));
                binding.videoLengthTv.setText(MessageFormat.format("Duration: {0}",getDuration(videoMeta.getVideoLength())));
               Glide.with(getApplicationContext()).load(videoMeta.getMaxResImageUrl().replace("http","https")).fitCenter().into(binding.thumbnail);


               SparseArray<YtFile> array = new SparseArray<>();

                for (int i = 0; i < ytFiles.size(); i++) {
                    YtFile ytFile = ytFiles.get(ytFiles.keyAt(i));
                    if(ytFile.getFormat().getHeight() == -1 || ytFile.getFormat().getHeight() >= 360){
                        array.put(ytFiles.keyAt(i), ytFile);
                    }
                }

                ResultAdapter adapter = new ResultAdapter(MainActivity.this, array);
                binding.recycler.setAdapter(adapter);

                adapter.setOnItemClickListener(new ResultAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(YtFile ytFile) {
                        String filename = videoMeta.getTitle()+"."+ytFile.getFormat().getExt();
                        filename = filename.replaceAll("[\\\\><\"|*?%:#/]","");
                        download(ytFile.getUrl(), videoMeta.getTitle(), filename);
                    }
                });


            }
        }.extract(Objects.requireNonNull(binding.urlET.getText()).toString(),true,true);
    }

    private void download(String url, String title, String filename) {
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setTitle(title);

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
        Toast.makeText(this, "Downloading....", Toast.LENGTH_SHORT).show();
    }

    private String getDuration(long duration) {
        final int MINUTES_IN_AN_HOUR = 60;
        final int SECONDS_IN_A_MINUTE = 60;

        int seconds = (int) (duration % SECONDS_IN_A_MINUTE);
        int totalMinutes = (int) (duration / SECONDS_IN_A_MINUTE);
        int minutes = totalMinutes % MINUTES_IN_AN_HOUR;
        int hours = totalMinutes / MINUTES_IN_AN_HOUR;

        return hours + ":" + minutes + ":" + seconds;
    }
}