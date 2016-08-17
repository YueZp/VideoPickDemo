package com.yuezp.videopickdemo;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static int REQUEST_VIDEO_CODE = 10;
    private String TAG = MainActivity.class.getSimpleName();

    private TextView tv_VideoPath, tv_VideoDuration, tv_VideoSize, tv_VideoTitle;
    private ImageView iv_VideoImage;
    private Button btn_getVideo;
    private ForegroundColorSpan fcs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_VideoPath = (TextView) findViewById(R.id.tv_video_path);
        tv_VideoDuration = (TextView) findViewById(R.id.tv_video_duration);
        tv_VideoSize = (TextView) findViewById(R.id.tv_video_size);
        tv_VideoTitle = (TextView) findViewById(R.id.tv_video_title);
        iv_VideoImage = (ImageView) findViewById(R.id.iv_video_image);
        btn_getVideo = (Button) findViewById(R.id.btn_get_video);
    }

    @Override
    protected void onStart() {
        super.onStart();
        btn_getVideo.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_VIDEO_CODE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                ContentResolver cr = this.getContentResolver();
                /**
                 * 数据库查询操作。
                 * 第一个参数 uri：为要查询的数据库+表的名称。
                 * 第二个参数 projection ： 要查询的列。
                 * 第三个参数 selection ： 查询的条件，相当于SQL where。
                 * 第三个参数 selectionArgs ： 查询条件的参数，相当于 ？。
                 * 第四个参数 sortOrder ： 结果排序。
                 */
                Cursor cursor = cr.query(uri, null, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        // 视频ID:MediaStore.Audio.Media._ID
                        int videoId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                        // 视频名称：MediaStore.Audio.Media.TITLE
                        String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                        // 视频路径：MediaStore.Audio.Media.DATA
                        String videoPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                        // 视频时长：MediaStore.Audio.Media.DURATION
                        int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                        // 视频大小：MediaStore.Audio.Media.SIZE
                        long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));

                        // 视频缩略图路径：MediaStore.Images.Media.DATA
                        String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                        // 缩略图ID:MediaStore.Audio.Media._ID
                        int imageId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));

                        // 方法一 Thumbnails 利用createVideoThumbnail 通过路径得到缩略图，保持为视频的默认比例
                        // 第一个参数为 ContentResolver，第二个参数为视频缩略图ID， 第三个参数kind有两种为：MICRO_KIND和MINI_KIND 字面意思理解为微型和迷你两种缩略模式，前者分辨率更低一些。
                        Bitmap bitmap1 = MediaStore.Video.Thumbnails.getThumbnail(cr, imageId, MediaStore.Video.Thumbnails.MICRO_KIND, null);

                        // 方法二 ThumbnailUtils 利用createVideoThumbnail 通过路径得到缩略图，保持为视频的默认比例
                        // 第一个参数为 视频/缩略图的位置，第二个依旧是分辨率相关的kind
                        Bitmap bitmap2 = ThumbnailUtils.createVideoThumbnail(imagePath, MediaStore.Video.Thumbnails.MICRO_KIND);
                        // 如果追求更好的话可以利用 ThumbnailUtils.extractThumbnail 把缩略图转化为的制定大小
//                        ThumbnailUtils.extractThumbnail(bitmap, width,height ,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                        setText(tv_VideoPath, R.string.path, videoPath);
                        setText(tv_VideoDuration, R.string.duration, String.valueOf(duration));
                        setText(tv_VideoSize, R.string.size, String.valueOf(size));
                        setText(tv_VideoTitle, R.string.title, title);
                        iv_VideoImage.setImageBitmap(bitmap1);
                    }
                    cursor.close();
                }
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void setText(TextView view, int resource, String content) {
        SpannableString ss = new SpannableString(getResources().getString(resource, content));
        if (fcs == null)
            fcs = new ForegroundColorSpan(Color.RED);
        ss.setSpan(fcs, 5, content.length() + 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        view.setText(ss);
    }

    @Override
    public void onClick(View v) {
        /**
         * 打开方式有两种action，1.ACTION_PICK；2.ACTION_GET_CONTENT
         * 区分大意为：ACTION_PICK 为打开特定数据一个列表来供用户挑选，其中数据为现有的数据。
         * 而 ACTION_GET_CONTENT 区别在于它允许用户创建一个之前并不存在的数据。
         * api ： http://www.android-doc.com/reference/android/content/Intent.html#ACTION_GET_CONTENT
         */
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
//            intent.setType("video/*");
        startActivityForResult(intent, REQUEST_VIDEO_CODE);
    }
}
