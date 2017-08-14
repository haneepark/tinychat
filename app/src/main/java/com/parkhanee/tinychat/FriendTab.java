package com.parkhanee.tinychat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class FriendTab extends Fragment implements View.OnClickListener {
    final String TAG = "FriendTab";
    private FriendTabAdapter adapter;
    private ViewGroup header;
    MySQLite db = null;
    private View myprofile;
    MyPreferences pref=null;
    UserProfileDialog.Builder dialog=null;

    // 내 프로필 사진 설정하기
    static final int REQUEST_GALLERY = 1;
    static final int REQUEST_IMAGE_CAPTURE = 12;
    private Bitmap bitmap;
    String imageFileName, image_path;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_fragment_friend,container,false);
        header = (ViewGroup)inflater.inflate(R.layout.listview_friend_header, container, false);
        myprofile = header.findViewById(R.id.myprofile);
        myprofile.setOnClickListener(this); // header안에 있는 애니까 header에서 찾아줌 !!
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (db==null){
            db = MySQLite.getInstance(getActivity());
        }
        if (pref==null){
            pref = MyPreferences.getInstance(getActivity());
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ((TextView)myprofile.findViewById(R.id.header_name)).setText(pref.getString("name"));
        // TODO: 2017. 8. 4. 내프로필사진 보이기
        // ((ImageView)myprofile.findViewById(R.id.header_img)) <-- (pref.getString("img"));

        adapter = new FriendTabAdapter(getActivity());
        ListView listView = (ListView) view.findViewById(R.id.friend_list_view);
        listView.setAdapter(adapter);
        listView.addHeaderView(header, null, false);
        adapter.setFriendArrayList(db.getAllFriends());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.myprofile :
                // TODO: 2017. 8. 4. init and show my profile dialog
                if (dialog == null){
                    Log.d(TAG, "onClick: dialog init");
                    dialog = new UserProfileDialog.Builder(getActivity())
                            .setMine(true)
                            .setTextName(pref.getString("name"))
                            .setTextNumber(pref.getString("nid"))
                            .setImageUrl(pref.getString("img"))
                            .setEditing(false)
                            .setOnEditImageClicked(new UserProfileDialog.OnEditImageClicked() {
                                        @Override
                                        public void OnClick(View view, Dialog dialog) {

                                            final CharSequence[] items = {
                                                    "사진 촬영", "사진 앨범에서 선택"
                                            };

                                            // show an alert window
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                            builder.setTitle("프로필 사진 변경하기")
                                                    .setItems(items, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            switch (i){
                                                                case 0 : // 사진 촬영
                                                                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                                                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

                                                                    break;
                                                                case 1 : // 사진 앨범에서 선택
                                                                    Intent intent = new Intent();
                                                                    intent.setType("image/*");
                                                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                                                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                                                                    startActivityForResult(intent, REQUEST_GALLERY);
                                                                    break;
                                                            }
                                                        }
                                                    });
                                            AlertDialog alertDialog = builder.create();
                                            alertDialog.show();
                                        }
                            })
                            .build();
                } else if (dialog.isEditing()){ // 프로필 사진 수정하고 나서 서버에 저장안하고 그냥 다이알로그 닫기 하고나서 다시 연 경우
                        dialog.setEditing(false);
                }
                dialog.show();
                break;
        }
    }

    /**
     * 카메라 인텐트 처리
     * */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK){

            String path="";

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            imageFileName = pref.getString("id")+"_" + timeStamp + ".jpg";

            if (requestCode == REQUEST_GALLERY ) {

                Uri selectedImage = data.getData();
                path = getPath(getActivity(), selectedImage);

                Log.d(TAG, "onActivityResult: decodeFile start");
                //bitmap = BitmapFactory.decodeFile(path);
                bitmap = decodeSampledBitmapFromFile(path,1000,1000); // TODO: 2016. 10. 14. adjust required width and height
                Log.d(TAG, "onActivityResult: decodeFile end");

            } else if (requestCode == REQUEST_IMAGE_CAPTURE ){
            /*
                The Android Camera application encodes the photo in the return Intent delivered to onActivityResult()
                as a small Bitmap in the extras, under the key "data".
             */
                Bundle extras = data.getExtras();
                bitmap = (Bitmap) extras.get("data");
                // Create an image file name

                path = saveToInternalStorage(bitmap,imageFileName);
            }


            try {
                // 인텐트로 불러온 이미지를 비트맵으로 저장할 때 자동으로 -90도 돌아가는거 조정해주는 클래스
                bitmap = MyUtil.rotateBitmap(path,bitmap);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            // 다이알로그 프로필 이미지뷰에 방금 선택한 이미지 보이기
            // 아직 서버에 저장은 안한 상태.
            dialog.setImageBitmap(bitmap)
                    .setEditing(true)
                    .setOnPositiveClicked(new UserProfileDialog.OnPositiveClicked() { // "수정 사항 저장" 눌렀을 때
                        @Override
                        public void OnClick(View view, Dialog dialog) {
                            // show an alert window
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("수정 사항을 저장하시겠습니까?")
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (MyUtil.IsNetworkConnected(getActivity())){
                                                // TODO: 2017. 8. 12.  서버에 업로드 !! 여기서부터 !!
//                                                Toast.makeText(getActivity(), "save", Toast.LENGTH_SHORT).show();
                                                onUploadImageRequested();
                                            } else {
                                                // TODO: 2017. 8. 12. 경고
                                                Toast.makeText(getActivity(), "인터넷 연결 안됨", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    })
                                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    });
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        }
                    })
                    .build();
            dialog.show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onUploadImageRequested () {
        RequestQueue queue = MyVolley.getInstance(getActivity()).
                getRequestQueue();

        String url = getString(R.string.server)+getString(R.string.server_uploadImage);

        StringRequest stringRequest = new StringRequest(Request.Method.POST,url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String resultCode = jsonObject.getString("resultCode");
                    String result = jsonObject.getString("result");
                    // result code 확인
                    if (!resultCode.equals("100")){
                        // TODO: 2017. 8. 13.  이미지 업로드실패 처리 경고
                        Log.d(TAG, "onResponse: 이미지 업로드 실패, code : " + resultCode+", result : "+result);
                        Toast.makeText(getActivity(),"이미지 업로드 실패, code : " + resultCode+", result : "+result, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // TODO: 2017. 8. 13. 이미지업로드 성공 했습니다 알림
                    Toast.makeText(getActivity(), "이미지 업로드에 성공 했습니다", Toast.LENGTH_SHORT).show();

                    // TODO: 2017. 8. 13. 수정중이던 프로필 다이알로그 닫고
                    // editing==false 설정하고
                    // pref의 내 이미지 url 바꿔주고

                    String url = jsonObject.getString("url");
                    pref.putString("img",url);

                    // TODO: 2017. 8. 13. 액티비티 새로고침 해서 새로 설정한 프로필이 보이도록 하기
                    dialog.setImageUrl(url)
                            .setEditing(false);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: 2017. 7. 26. 경고
                Toast.makeText(getActivity(), "이미지업로드에 실패 했습니다 볼리 에러", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onErrorResponse: "+error.getMessage());
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("id",pref.getString("id"));
                params.put("image",getStringImage(bitmap));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Content-Type","application/x-www-form-urlencoded"); //form ?
                return params;
            }
        };

        // Add the request to the RequestQueue.
        stringRequest.setTag(TAG);
        queue.add(stringRequest);
    }

    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    // for saving image into Internal storage
    private String saveToInternalStorage(Bitmap bitmapImage,String imageFileName){
        ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);

        // Create imageDir
        File mypath=new File(directory,imageFileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    // Load a Scaled Down Version of Bitmap into Memory
    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        Log.d("bitmap", "calculateInSampleSize: inSampleSize "+ inSampleSize);

        return inSampleSize;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    @SuppressLint("NewApi") // isDocumentUri
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
}
